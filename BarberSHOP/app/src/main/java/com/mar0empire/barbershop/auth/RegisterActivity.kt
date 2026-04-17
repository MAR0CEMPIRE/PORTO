package com.mar0empire.barbershop.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mar0empire.barbershop.main.MainBarberiaActivity
import com.mar0empire.barbershop.main.MainClienteActivity
import com.mar0empire.barbershop.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity(){
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore

    private var rolSeleccioado = "cliente" // Valor por defecto

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        initToggle()
        initListeners()
    }

    private fun initToggle(){
        binding.toggleTipoUsuario.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if(isChecked){
                when(checkedId){
                    binding.btnCliente.id -> {
                        rolSeleccioado = "cliente"
                        binding.layoutBarberia.visibility = View.GONE
                    }
                    binding.btnBarberia.id -> {
                        rolSeleccioado = "barberia"
                        binding.layoutBarberia.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
    private fun initListeners(){

        //Boton volver
        binding.volver.setOnClickListener {
            finish()
        }

        //Boton registrar
        binding.btnRegistrar.setOnClickListener {
            registrarUsuario()
        }
    }

    private fun registrarUsuario(){
        val nombre = binding.txtNombre.text.toString().trim()
        val email = binding.txtEmail.text.toString().trim()
        val password = binding.etPassword.editText?.text.toString().trim()
        val confirmPassword = binding.etConfirmpassword.editText?.text.toString().trim()

        if(nombre.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()){
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }
        if(password != confirmPassword){
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        //Si es barberia, validar campos adicionales
        if(rolSeleccioado == "barberia"){
            val nombreBarberia = binding.txtNombreBarberia.text.toString().trim()
            val direccion = binding.txtDireccion.text.toString().trim()
            val telefono = binding.txtTelefono.text.toString().trim()

            if (nombreBarberia.isEmpty() || direccion.isEmpty() || telefono.isEmpty()){
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return
            }
        }

        binding.btnRegistrar.isEnabled = false

        //Crear usuario en Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener

                //Dayos
                val userData = hashMapOf(
                    "nombre" to nombre,
                    "email" to email,
                    "rol" to rolSeleccioado
                )

                //Guardar datos en coleccion users
                db.collection("users").document(uid).set(userData)
                    .addOnSuccessListener {
                        if(rolSeleccioado == "cliente"){
                            //Redirigir a interfaz del cliente
                            startActivity(Intent(this, MainClienteActivity::class.java))
                            finish()
                        }else{
                           registrarBarberia(uid)
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al guardar los datos", Toast.LENGTH_SHORT).show()
                        binding.btnRegistrar.isEnabled = true
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al crear el usuario", Toast.LENGTH_SHORT).show()
                binding.btnRegistrar.isEnabled = true
            }
    }
    private fun registrarBarberia(uid: String){
        val nombreBarberia = binding.txtNombreBarberia.text.toString().trim()
        val direccion = binding.txtDireccion.text.toString().trim()
        val telefono = binding.txtTelefono.text.toString().trim()

        val barberiaData = hashMapOf(
            "barberiaID" to uid,
            "nombre" to nombreBarberia,
            "direccion" to direccion,
            "telefono" to telefono,
            "servicios" to emptyList<String>(),
            "horario" to emptyList<String>()
        )

        db.collection("barberia").document(uid).set(barberiaData)
            .addOnSuccessListener {
                startActivity(Intent(this, MainBarberiaActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al registrar la barbería", Toast.LENGTH_SHORT).show()
                binding.btnRegistrar.isEnabled = true
            }
    }
}