package com.mar0empire.barbershop.main.activities


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mar0empire.barbershop.R
import com.mar0empire.barbershop.databinding.ActivityPerfilBarberiaBinding

class PerfilBarberiaClienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilBarberiaBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var barberiaId: String
    private var nombreBarberia: String = ""
    private var fotoBarberia: String = ""
    private var ubicacion: String = ""

    companion object {
        const val EXTRA_BARBERIA_ID = "barberia_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBarberiaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        barberiaId = intent.getStringExtra(EXTRA_BARBERIA_ID) ?: run {
            finish()
            return
        }

        cargarDatosBarberia()
        initListeners()
    }

    private fun cargarDatosBarberia() {
        db.collection("barberia").document(barberiaId)
            .get()
            .addOnSuccessListener { doc ->
                nombreBarberia = doc.getString("nombre") ?: ""
                fotoBarberia = doc.getString("fotoPerfil") ?: ""
                ubicacion = doc.getString("direccion") ?: ""

                binding.txtNombreBarberia.text = nombreBarberia
                binding.txtUbicacion.text = ubicacion

                if (fotoBarberia.isNotEmpty()) {
                    Glide.with(this)
                        .load(fotoBarberia)
                        .centerCrop()
                        .into(binding.imgBarberia)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar la barbería", Toast.LENGTH_SHORT).show()
            }
    }

    private fun initListeners() {
        // Volver atrás
        binding.icBack.setOnClickListener { finish() }

        // Reservar cita
        binding.llReservar.setOnClickListener {
            startActivity(
                Intent(this, ReservarCitaActivity::class.java).apply {
                    putExtra(ReservarCitaActivity.EXTRA_BARBERIA_ID, barberiaId)
                    putExtra(ReservarCitaActivity.EXTRA_BARBERIA_NOMBRE, nombreBarberia)
                    putExtra(ReservarCitaActivity.EXTRA_BARBERIA_FOTO, fotoBarberia)
                    putExtra(ReservarCitaActivity.EXTRA_BARBERIA_UBICACION, ubicacion)
                }
            )
        }

        // Chat con la barbería
        binding.llChat.setOnClickListener {
            startActivity(
                Intent(this, ChatActivity::class.java).apply {
                    putExtra("uid_destino", barberiaId)
                    putExtra("nombre_destino", nombreBarberia)
                }
            )
        }

        // Compartir
        binding.icShare.setOnClickListener {
            val texto = "¡Mira esta barbería! $nombreBarberia - $ubicacion"
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, texto)
            }
            startActivity(Intent.createChooser(intent, "Compartir barbería"))
        }
    }
}