package com.mar0empire.barbershop.main.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.mar0empire.barbershop.auth.AuthRepository
import com.mar0empire.barbershop.auth.LoginActivity
import com.mar0empire.barbershop.databinding.ActivitySplashBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val authRepository = AuthRepository()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        iniciarApp()
    }

    private fun iniciarApp() {
        lifecycleScope.launch {
            delay(1500)

            try {
                if (!authRepository.haySesion) {
                    irA(LoginActivity::class.java)
                    return@launch
                }

                val uid = authRepository.usuarioActual?.uid ?: run {
                    irA(LoginActivity::class.java)
                    return@launch
                }

                val rol = withContext(Dispatchers.IO) {
                    authRepository.obtenerRol(uid)
                }

                when (rol) {
                    "cliente" -> irA(MainClienteActivity::class.java)

                    "barberia" -> {
                        // Comprobar si ya completó la configuración
                        val configurado = withContext(Dispatchers.IO) {
                            try {
                                val doc = db.collection("barberia")
                                    .document(uid)
                                    .get()
                                    .await()
                                doc.getBoolean("configurado") == true
                            } catch (e: Exception) {
                                false
                            }
                        }

                        if (configurado) {
                            irA(MainBarberiaActivity::class.java)
                        } else {
                            irA(CompletarBarberiaActivity::class.java)
                        }
                    }

                    else -> {
                        authRepository.cerrarSesion()
                        irA(LoginActivity::class.java)
                    }
                }

            } catch (e: Exception) {
                Log.e("SplashError", "Error al iniciar: ${e.message}")
                Toast.makeText(
                    this@SplashActivity,
                    "Error de conexión",
                    Toast.LENGTH_SHORT
                ).show()
                irA(LoginActivity::class.java)
            }
        }
    }

    private fun irA(destino: Class<*>) {
        startActivity(
            Intent(this, destino).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }
}