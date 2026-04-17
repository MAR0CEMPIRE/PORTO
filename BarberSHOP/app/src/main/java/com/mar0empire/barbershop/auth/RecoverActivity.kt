package com.mar0empire.barbershop.auth

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.mar0empire.barbershop.databinding.ActivityRecoverBinding

class RecoverActivity : AppCompatActivity(){
    private lateinit var binding: ActivityRecoverBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecoverBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        initListeners()
    }

    private fun initListeners(){
        //Boton volver
        binding.volver.setOnClickListener {
            finish()
        }

        //Boton recuperar
        binding.btnRecover.setOnClickListener {
            val email = binding.txtEmail.text.toString().trim()

            if(email.isEmpty()){
                Toast.makeText(this, "Introduce tu emial", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            enviarCorreoRecuperacion(email)
        }
    }

    private fun enviarCorreoRecuperacion(email: String){
        binding.btnRecover.isEnabled = false

            auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                Toast.makeText(this, "Correo de recuperacion enviado", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al enviar el correo", Toast.LENGTH_SHORT).show()
                binding.btnRecover.isEnabled = true
            }
    }
}