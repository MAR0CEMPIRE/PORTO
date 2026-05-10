package com.mar0empire.barbershop.main.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mar0empire.barbershop.databinding.ActivityAjustesNotificacionesBinding

class NotificacionesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAjustesNotificacionesBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAjustesNotificacionesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cargarPreferencias()
        initListeners()
    }
    private fun cargarPreferencias() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    binding.switchCitas.isChecked = doc.getBoolean("notif_citas") ?: true
                    binding.switchMensajes.isChecked = doc.getBoolean("notif_mensajes") ?: true
                    binding.switchRecordatorios.isChecked = doc.getBoolean("notif_recordatorios") ?: true
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar preferencias", Toast.LENGTH_SHORT).show()
            }
    }

    private fun initListeners() {

        binding.ivAtras.setOnClickListener {
            finish()
        }

        binding.btnGuardarNotificaciones.setOnClickListener {
            guardarPreferencias()
        }
    }
    private fun guardarPreferencias() {
        val uid = auth.currentUser?.uid ?: return

        binding.btnGuardarNotificaciones.isEnabled = false
        binding.btnGuardarNotificaciones.text = "Guardando..."

        db.collection("users").document(uid)
            .update(
                mapOf(
                    "notif_citas" to binding.switchCitas.isChecked,
                    "notif_mensajes" to binding.switchMensajes.isChecked,
                    "notif_recordatorios" to binding.switchRecordatorios.isChecked
                )
            )
            .addOnSuccessListener {
                Toast.makeText(this, "Preferencias guardadas ✓", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                binding.btnGuardarNotificaciones.isEnabled = true
                binding.btnGuardarNotificaciones.text = "Guardar"
                Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
            }
    }
}