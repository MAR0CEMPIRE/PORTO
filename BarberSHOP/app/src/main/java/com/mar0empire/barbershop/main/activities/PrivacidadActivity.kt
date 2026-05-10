package com.mar0empire.barbershop.main.activities

import com.mar0empire.barbershop.databinding.ActivityPriysegBinding
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mar0empire.barbershop.auth.LoginActivity

class PrivacidadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPriysegBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPriysegBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListeners()
    }

    private fun initListeners() {

        binding.ivAtras.setOnClickListener {
            finish()
        }

        binding.btnCambiarPassword.setOnClickListener {
            enviarEmailCambioPassword()
        }

        binding.btnEliminarCuenta.setOnClickListener {
            confirmarEliminarCuenta()
        }
    }

    private fun enviarEmailCambioPassword() {
        val email = auth.currentUser?.email ?: return

        binding.btnCambiarPassword.isEnabled = false

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Email enviado a $email",
                    Toast.LENGTH_LONG
                ).show()
                binding.btnCambiarPassword.isEnabled = true
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al enviar el email", Toast.LENGTH_SHORT).show()
                binding.btnCambiarPassword.isEnabled = true
            }
    }

    private fun confirmarEliminarCuenta() {
        AlertDialog.Builder(this)
            .setTitle("¿Eliminar cuenta?")
            .setMessage("Esta acción es irreversible. Se eliminarán todos tus datos.")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarCuenta()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarCuenta() {
        val uid = auth.currentUser?.uid ?: return

        binding.btnEliminarCuenta.isEnabled = false

        db.collection("users").document(uid).delete()
            .addOnSuccessListener {
                auth.currentUser?.delete()
                    ?.addOnSuccessListener {
                        Toast.makeText(this, "Cuenta eliminada", Toast.LENGTH_SHORT).show()
                        irAlLogin()
                    }
                    ?.addOnFailureListener {
                        Toast.makeText(this, "Error al eliminar la cuenta", Toast.LENGTH_SHORT).show()
                        binding.btnEliminarCuenta.isEnabled = true
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al eliminar los datos", Toast.LENGTH_SHORT).show()
                binding.btnEliminarCuenta.isEnabled = true
            }
    }

    private fun irAlLogin() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }
}