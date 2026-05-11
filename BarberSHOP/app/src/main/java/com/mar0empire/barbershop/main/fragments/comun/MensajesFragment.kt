package com.mar0empire.barbershop.main.fragments.comun

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.mar0empire.barbershop.adapters.ConversacionAdapters
import com.mar0empire.barbershop.databinding.FragmentMensajesBinding
import com.mar0empire.barbershop.main.activities.ChatActivity
import com.mar0empire.barbershop.models.Conversacion

class MensajesFragment : Fragment() {

    private var _binding: FragmentMensajesBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val rtdb = FirebaseDatabase
        .getInstance("https://barbershoptfg-default-rtdb.firebaseio.com")
        .reference

    private lateinit var adapter: ConversacionAdapters
    private var todasLasConversaciones = listOf<Conversacion>()
    private var conversacionesListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMensajesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        cargarConversaciones()
        setupBuscador()
    }

    private fun setupRecyclerView() {
        adapter = ConversacionAdapters { conversacion ->
            startActivity(
                Intent(requireContext(), ChatActivity::class.java).apply {
                    putExtra("uid_destino", conversacion.uidDestino)
                    putExtra("nombre_destino", conversacion.nombreDestino)
                }
            )
        }
        binding.rvChats.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChats.adapter = adapter
    }

    private fun setupBuscador() {
        binding.etBuscar.editText?.addTextChangedListener { texto ->
            val query = texto.toString().trim().lowercase()
            val filtradas = if (query.isEmpty()) todasLasConversaciones
            else todasLasConversaciones.filter {
                it.nombreDestino.lowercase().contains(query)
            }
            adapter.actualizar(filtradas)
            mostrarEstadoVacio(filtradas.isEmpty())
        }
    }

    private fun cargarConversaciones() {
        val uid = auth.currentUser?.uid ?: return

        conversacionesListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding == null) return

                val chatsDelUsuario = snapshot.children
                    .filter { it.key?.contains(uid) == true }
                    .toList()

                if (chatsDelUsuario.isEmpty()) {
                    mostrarEstadoVacio(true)
                    return
                }

                val conversaciones = mutableListOf<Conversacion>()
                var pendientes = chatsDelUsuario.size
                var completados = 0

                chatsDelUsuario.forEach { chatSnap ->
                    val chatId = chatSnap.key ?: run {
                        completados++
                        if (completados == pendientes) finalizarCarga(conversaciones)
                        return@forEach
                    }

                    val uidDestino = chatId.split("__")
                        .firstOrNull { it != uid } ?: run {
                        completados++
                        if (completados == pendientes) finalizarCarga(conversaciones)
                        return@forEach
                    }

                    val mensajes = chatSnap.child("mensajes")
                    val ultimoMensaje = mensajes.children.lastOrNull()
                    val textoUltimo = ultimoMensaje?.child("texto")
                        ?.getValue(String::class.java) ?: ""
                    val timestamp = ultimoMensaje?.child("timestamp")
                        ?.getValue(Long::class.java) ?: 0L
                    val noLeidos = mensajes.children.count { msg ->
                        val leido = msg.child("leido").getValue(Boolean::class.java) ?: false
                        val sender = msg.child("senderId").getValue(String::class.java) ?: ""
                        !leido && sender != uid
                    }

                    //  Busca en users Y en barberia
                    buscarInfoUsuario(uidDestino) { nombre, foto ->
                        if (_binding == null) return@buscarInfoUsuario

                        conversaciones.add(
                            Conversacion(
                                chatId = chatId,
                                uidDestino = uidDestino,
                                nombreDestino = nombre,
                                fotoDestino = foto,
                                ultimoMensaje = textoUltimo,
                                timestamp = timestamp,
                                noLeidos = noLeidos
                            )
                        )

                        completados++
                        if (completados == pendientes) finalizarCarga(conversaciones)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (_binding == null) return
                mostrarEstadoVacio(true)
            }
        }

        rtdb.child("chats").addValueEventListener(conversacionesListener!!)
    }

    private fun buscarInfoUsuario(
        uid: String,
        callback: (nombre: String, foto: String) -> Unit
    ) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val rol = doc.getString("rol") ?: "cliente"

                    if (rol == "barberia") {
                        //  Es barbero -> usar nombre de la barbería
                        db.collection("barberia").document(uid).get()
                            .addOnSuccessListener { docBarberia ->
                                callback(
                                    docBarberia.getString("nombre") ?: "Barbería",
                                    docBarberia.getString("fotoPerfil") ?: ""
                                )
                            }
                            .addOnFailureListener {
                                callback(doc.getString("nombre") ?: "Usuario", "")
                            }
                    } else {
                        // Es cliente -> usar nombre del usuario
                        callback(
                            doc.getString("nombre") ?: "Usuario",
                            doc.getString("fotoPerfil") ?: ""
                        )
                    }
                } else {
                    callback("Usuario", "")
                }
            }
            .addOnFailureListener {
                callback("Usuario", "")
            }
    }

    private fun finalizarCarga(conversaciones: MutableList<Conversacion>) {
        if (_binding == null) return
        val ordenadas = conversaciones.sortedByDescending { it.timestamp }
        todasLasConversaciones = ordenadas

        val queryActual = binding.etBuscar.editText?.text.toString().trim().lowercase()
        val filtradas = if (queryActual.isEmpty()) ordenadas
        else ordenadas.filter { it.nombreDestino.lowercase().contains(queryActual) }

        adapter.actualizar(filtradas)
        mostrarEstadoVacio(filtradas.isEmpty())
    }

    private fun mostrarEstadoVacio(vacio: Boolean) {
        binding.layoutEmpty.visibility = if (vacio) View.VISIBLE else View.GONE
        binding.rvChats.visibility = if (vacio) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        conversacionesListener?.let {
            rtdb.child("chats").removeEventListener(it)
        }
        conversacionesListener = null
        _binding = null
    }
}