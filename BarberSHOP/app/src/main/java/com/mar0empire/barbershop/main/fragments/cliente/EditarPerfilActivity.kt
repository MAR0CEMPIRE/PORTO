package com.mar0empire.barbershop.main.fragments.cliente

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mar0empire.barbershop.R
import com.mar0empire.barbershop.databinding.ActivityEditarPerfilBinding
import com.mar0empire.barbershop.utils.CloudinaryHelper
import kotlinx.coroutines.launch

class EditarPerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditarPerfilBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private var uriImagenSeleccionada: Uri? = null
    private var fotoUrlActual: String? = null

    private val seleccionarImagen = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            uriImagenSeleccionada = it
            Glide.with(this)
                .load(it)
                .circleCrop()
                .into(binding.imgPerfilEditar)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cargarDatosActuales()
        initListeners()
    }

    private fun cargarDatosActuales() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    // Nombre
                    binding.etNombre.setText(doc.getString("nombre") ?: "")

                    // Foto
                    fotoUrlActual = doc.getString("fotoPerfil")
                    if (!fotoUrlActual.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(fotoUrlActual)
                            .circleCrop()
                            .placeholder(R.drawable.avatar_perfil)
                            .into(binding.imgPerfilEditar)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar los datos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun initListeners() {

        // Volver atrás
        binding.ivAtras.setOnClickListener{
            finish()
        }

        // Cambiar foto
        binding.btnCambiarFoto.setOnClickListener {
            seleccionarImagen.launch("image/*")
        }

        // Guardar cambios
        binding.btnGuardar.setOnClickListener {
            val nuevoNombre = binding.etNombre.text.toString().trim()

            if (nuevoNombre.isEmpty()) {
                binding.inputNombre.error = "El nombre no puede estar vacío"
                return@setOnClickListener
            }

            binding.btnGuardar.isEnabled = false
            binding.btnGuardar.text = "Guardando..."

            // Si hay imagen nueva → subirla primero
            val uri = uriImagenSeleccionada
            if (uri != null) {
                lifecycleScope.launch {
                    CloudinaryHelper.subirImagen(this@EditarPerfilActivity, uri)
                        .onSuccess { url ->
                            guardarEnFirestore(nuevoNombre, url)
                        }
                        .onFailure {
                            runOnUiThread {
                                binding.btnGuardar.isEnabled = true
                                binding.btnGuardar.text = "Guardar"
                                Toast.makeText(
                                    this@EditarPerfilActivity,
                                    "Error al subir la foto",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            } else {
                // Sin imagen nueva → solo guardar nombre
                guardarEnFirestore(nuevoNombre, fotoUrlActual)
            }
        }
    }

    private fun guardarEnFirestore(nombre: String, fotoUrl: String?) {
        val uid = auth.currentUser?.uid ?: return

        val datos = mutableMapOf<String, Any>("nombre" to nombre)
        if (fotoUrl != null) datos["fotoPerfil"] = fotoUrl

        db.collection("users").document(uid)
            .update(datos)
            .addOnSuccessListener {
                Toast.makeText(this, "Perfil actualizado ✓", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
            .addOnFailureListener {
                binding.btnGuardar.isEnabled = true
                binding.btnGuardar.text = "Guardar"
                Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
            }
    }
}