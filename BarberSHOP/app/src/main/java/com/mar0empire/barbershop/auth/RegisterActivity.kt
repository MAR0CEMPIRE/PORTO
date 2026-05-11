package com.mar0empire.barbershop.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mar0empire.barbershop.databinding.ActivityRegisterBinding
import com.mar0empire.barbershop.main.activities.CompletarBarberiaActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var rolSeleccionado = "cliente"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        initToggle()
        initListeners()
    }

    private fun initToggle() {
        binding.toggleTipoUsuario.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                rolSeleccionado = when (checkedId) {
                    binding.btnCliente.id -> "cliente"
                    else -> "barberia"
                }
            }
        }
    }



    private fun initListeners() {
        binding.volver.setOnClickListener { finish() }
        binding.btnRegistrar.setOnClickListener { validarYContinuar() }
    }

    private fun validarYContinuar() {
        val nombre = binding.txtNombre.text.toString().trim()
        val email = binding.txtEmail.text.toString().trim()
        val password = binding.txtPassword.text.toString().trim()
        val confirmPassword = binding.txtConfirmpassword.text.toString().trim()

        if (nombre.isEmpty() || email.isEmpty() ||
            password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }
        if (password != confirmPassword) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 8) {
            Toast.makeText(this, "La contraseña debe tener al menos 8 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnRegistrar.isEnabled = false

        if (rolSeleccionado == "cliente") {
            registrarCliente(nombre, email, password)
        } else {
            // Barbería -> ir al wizard
            startActivity(
                Intent(this, CompletarBarberiaActivity::class.java).apply {
                    putExtra("nombre", nombre)
                    putExtra("email", email)
                    putExtra("password", password)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            )
        }
    }

    private fun registrarCliente(nombre: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener

                db.collection("users").document(uid).set(
                    hashMapOf(
                        "nombre" to nombre,
                        "email" to email,
                        "rol" to "cliente"
                    )
                ).addOnSuccessListener {
                    Toast.makeText(this, "¡Cuenta creada! Inicia sesión.", Toast.LENGTH_SHORT).show()

                    // REDIRECCIÓN DIRECTA AL LOGIN
                    startActivity(
                        Intent(this, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                    )
                }.addOnFailureListener {
                    Toast.makeText(this, "Error al guardar los datos", Toast.LENGTH_SHORT).show()
                    binding.btnRegistrar.isEnabled = true
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al crear el usuario", Toast.LENGTH_SHORT).show()
                binding.btnRegistrar.isEnabled = true
            }
    }
}
