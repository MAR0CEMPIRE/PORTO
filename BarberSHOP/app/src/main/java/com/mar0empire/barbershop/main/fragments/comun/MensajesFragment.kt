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
            val filtradas = if (query.isEmpty()) {
                todasLasConversaciones
            } else {
                todasLasConversaciones.filter {
                    it.nombreDestino.lowercase().contains(query)
                }
            }
            adapter.actualizar(filtradas)
            mostrarEstadoVacio(filtradas.isEmpty())
        }
    }

    private fun cargarConversaciones() {
        val uid = auth.currentUser?.uid ?: return

        conversacionesListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Verificar que el binding sigue activo
                if (_binding == null) return

                val chatsDelUsuario = snapshot.children.filter { chatSnap ->
                    chatSnap.key?.contains(uid) == true
                }.toList()

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
                        return@forEach
                    }

                    val uidDestino = chatId
                        .split("_")
                        .firstOrNull { it != uid } ?: run {
                        completados++
                        return@forEach
                    }

                    val mensajes = chatSnap.child("mensajes")
                    val ultimoMensaje = mensajes.children.lastOrNull()
                    val textoUltimo = ultimoMensaje
                        ?.child("texto")
                        ?.getValue(String::class.java) ?: ""
                    val timestamp = ultimoMensaje
                        ?.child("timestamp")
                        ?.getValue(Long::class.java) ?: 0L
                    val noLeidos = mensajes.children.count { msg ->
                        val leido = msg.child("leido")
                            .getValue(Boolean::class.java) ?: false
                        val sender = msg.child("senderId")
                            .getValue(String::class.java) ?: ""
                        !leido && sender != uid
                    }

                    db.collection("users").document(uidDestino).get()
                        .addOnSuccessListener { doc ->
                            if (_binding == null) return@addOnSuccessListener

                            val nombre = doc.getString("nombre") ?: "Usuario"
                            val foto = doc.getString("fotoPerfil") ?: ""

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
                            if (completados == pendientes) {
                                if (_binding == null) return@addOnSuccessListener
                                val ordenadas = conversaciones
                                    .sortedByDescending { it.timestamp }
                                todasLasConversaciones = ordenadas
                                adapter.actualizar(ordenadas)
                                mostrarEstadoVacio(ordenadas.isEmpty())
                            }
                        }
                        .addOnFailureListener {
                            completados++
                            if (completados == pendientes && _binding != null) {
                                mostrarEstadoVacio(conversaciones.isEmpty())
                            }
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Verificar binding antes de actualizar UI
                if (_binding == null) return
                mostrarEstadoVacio(true)
            }
        }

        rtdb.child("chats").addValueEventListener(conversacionesListener!!)
    }

    private fun mostrarEstadoVacio(vacio: Boolean) {
        binding.layoutEmpty.visibility = if (vacio) View.VISIBLE else View.GONE
        binding.rvChats.visibility = if (vacio) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Eliminar el listener cuando se destruye la vista
        conversacionesListener?.let {
            rtdb.child("chats").removeEventListener(it)
        }
        conversacionesListener = null
        _binding = null
    }
}