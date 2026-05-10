package com.mar0empire.barbershop.main.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.mar0empire.barbershop.adapters.MensajeAdapter
import com.mar0empire.barbershop.databinding.ActivityChatBinding
import com.mar0empire.barbershop.models.Mensaje
import com.mar0empire.barbershop.utils.NotificacionHelper

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: MensajeAdapter

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val rtdb = FirebaseDatabase
        .getInstance("https://barbershoptfg-default-rtdb.firebaseio.com")
        .reference

    private lateinit var chatId: String
    private lateinit var uidDestino: String
    private lateinit var nombreDestino: String
    private var chatListener: ChildEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recibir datos del Intent
        uidDestino = intent.getStringExtra("uid_destino") ?: return
        nombreDestino = intent.getStringExtra("nombre_destino") ?: ""

        // Generar chatId único y consistente
        val uidActual = auth.currentUser?.uid ?: return
        chatId = generarChatId(uidActual, uidDestino)

        setupUI()
        setupRecyclerView()
        escucharMensajes()
        initListeners()
    }


    private fun setupUI() {
        binding.txtNombreChat.text = nombreDestino

        // Cargar foto del destino
        db.collection("users").document(uidDestino).get()
            .addOnSuccessListener { doc ->
                val foto = doc.getString("fotoPerfil")
                if (!foto.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(foto)
                        .circleCrop()
                        .into(binding.imgAvatarChat)
                }
            }
    }


    private fun setupRecyclerView() {
        val uidActual = auth.currentUser?.uid ?: return
        adapter = MensajeAdapter(mutableListOf(), uidActual)

        val layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true // Mensajes desde abajo
        }

        binding.rvMensajes.layoutManager = layoutManager
        binding.rvMensajes.adapter = adapter
    }


    private fun escucharMensajes() {
        chatListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val mensaje = snapshot.getValue(Mensaje::class.java) ?: return
                val mensajeConId = mensaje.copy(id = snapshot.key ?: "")
                adapter.agregarMensaje(mensajeConId)
                binding.rvMensajes.scrollToPosition(adapter.itemCount - 1)

                // Marcar como leído si es para mí
                val uidActual = auth.currentUser?.uid ?: return
                if (mensaje.senderId != uidActual && !mensaje.leido) {
                    rtdb.child("chats/$chatId/mensajes/${snapshot.key}/leido")
                        .setValue(true)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ChatActivity, "Error al cargar mensajes", Toast.LENGTH_SHORT).show()
            }
        }

        rtdb.child("chats/$chatId/mensajes")
            .addChildEventListener(chatListener!!)
    }


    private fun initListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnEnviar.setOnClickListener {
            val texto = binding.txtMensaje.text.toString().trim()
            if (texto.isNotEmpty()) {
                enviarMensaje(texto)
            }
        }
    }


    private fun enviarMensaje(texto: String) {
        val uidActual = auth.currentUser?.uid ?: return

        val mensaje = Mensaje(
            texto = texto,
            senderId = uidActual,
            timestamp = System.currentTimeMillis(),
            leido = false
        )

        // Guardar en Realtime Database
        rtdb.child("chats/$chatId/mensajes")
            .push()
            .setValue(mensaje)
            .addOnSuccessListener {
                binding.txtMensaje.text?.clear()

                // Enviar notificación al destinatario
                obtenerNombreActual { nombreActual ->
                    NotificacionHelper.nuevoMensaje(
                        uidDestino = uidDestino,
                        nombreRemitente = nombreActual,
                        preview = texto.take(50),
                        chatId = chatId
                    )
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al enviar el mensaje", Toast.LENGTH_SHORT).show()
            }
    }


    private fun obtenerNombreActual(callback: (String) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                callback(doc.getString("nombre") ?: "Alguien")
            }
    }


    private fun generarChatId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_${uid2}"
        else "${uid2}_${uid1}"
    }


    override fun onDestroy() {
        super.onDestroy()
        chatListener?.let {
            rtdb.child("chats/$chatId/mensajes").removeEventListener(it)
        }
    }
}