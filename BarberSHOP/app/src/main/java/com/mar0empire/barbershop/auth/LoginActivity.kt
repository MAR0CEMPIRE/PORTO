package com.mar0empire.barbershop.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mar0empire.barbershop.databinding.ActivityLoginBinding
import com.mar0empire.barbershop.main.activities.MainBarberiaActivity
import com.mar0empire.barbershop.main.activities.MainClienteActivity
import com.mar0empire.barbershop.main.barberia.setup.CompletarBarberiaActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        initListeners()
    }

    private fun initListeners() {

        // Iniciar sesión
        binding.btnLogin.setOnClickListener {
            val email = binding.txtEmail.text.toString().trim()
            val password = binding.etPassword.editText?.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(email, password)
        }

        // Registrarse
        binding.tvregistrarse.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Recuperar contraseña
        binding.tvRecover.setOnClickListener {
            startActivity(Intent(this, RecoverActivity::class.java))
        }
    }

    private fun loginUser(email: String, password: String) {
        binding.btnLogin.isEnabled = false

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener

                // Leer rol del usuario
                db.collection("users").document(uid).get()
                    .addOnSuccessListener { doc ->
                        if (!doc.exists()) {
                            Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                            binding.btnLogin.isEnabled = true
                            return@addOnSuccessListener
                        }

                        val rol = doc.getString("rol")

                        when (rol) {

                            "cliente" -> {
                                startActivity(Intent(this, MainClienteActivity::class.java))
                                finish()
                            }

                            "barberia" -> {
                                verificarBarberia(uid)
                            }

                            else -> {
                                Toast.makeText(this, "Rol no válido", Toast.LENGTH_SHORT).show()
                                binding.btnLogin.isEnabled = true
                            }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al obtener datos", Toast.LENGTH_SHORT).show()
                        binding.btnLogin.isEnabled = true
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Email o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                binding.btnLogin.isEnabled = true
            }
    }

    private fun verificarBarberia(uid: String) {

        db.collection("barberias").document(uid).get()
            .addOnSuccessListener { doc ->

                // Si NO existe → debe crearla
                if (!doc.exists()) {
                    startActivity(Intent(this, CompletarBarberiaActivity::class.java))
                    finish()
                    return@addOnSuccessListener
                }

                // Si existe pero NO está completada → debe terminarla
                val completado = doc.getBoolean("completado") ?: false

                if (!completado) {
                    startActivity(Intent(this, CompletarBarberiaActivity::class.java))
                    finish()
                    return@addOnSuccessListener
                }

                // Si está completada → acceso al dashboard
                startActivity(Intent(this, MainBarberiaActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al verificar barbería", Toast.LENGTH_SHORT).show()
                binding.btnLogin.isEnabled = true
            }
    }
}
