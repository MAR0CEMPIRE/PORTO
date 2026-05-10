package com.mar0empire.barbershop.main.fragments.cliente

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mar0empire.barbershop.R
import com.mar0empire.barbershop.auth.LoginActivity
import com.mar0empire.barbershop.databinding.FragmentPerfilClienteBinding
import com.mar0empire.barbershop.main.activities.IdiomaActivity
import com.mar0empire.barbershop.main.activities.NotificacionesActivity
import com.mar0empire.barbershop.main.activities.PrivacidadActivity
import com.mar0empire.barbershop.main.activities.cliente.EditarPerfilActivity
import com.mar0empire.barbershop.utils.CloudinaryHelper
import kotlinx.coroutines.launch

class PerfilClienteFragment : Fragment() {

    private var _binding: FragmentPerfilClienteBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Abrir galería para cambiar foto directamente desde el perfil
    private val seleccionarImagen = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { subirFotoPerfil(it) }
    }

    // Launcher para EditarPerfilActivity — recarga perfil al volver
    private val editarPerfilLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            cargarDatosPerfil()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilClienteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cargarDatosPerfil()
        cargarProximasCitas()
        initListeners()
    }

    private fun cargarDatosPerfil() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    binding.txtNombreCliente.text = doc.getString("nombre") ?: ""

                    val foto = doc.getString("fotoPerfil")
                    if (!foto.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(foto)
                            .circleCrop()
                            .placeholder(R.drawable.avatar_perfil)
                            .into(binding.imgPerfil)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al cargar el perfil", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarProximasCitas() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("citas")
            .whereEqualTo("clienteId", uid)
            .whereEqualTo("estado", "confirmada")
            .get()
            .addOnSuccessListener { docs ->
                binding.containerCitas.removeAllViews()

                if (docs.isEmpty) {
                    val tv = android.widget.TextView(requireContext()).apply {
                        text = getString(R.string.citas_empty)
                        setTextColor(resources.getColor(R.color.gray_600, null))
                        textSize = 14f
                        setPadding(0, 16, 0, 16)
                    }
                    binding.containerCitas.addView(tv)
                    return@addOnSuccessListener
                }

                docs.forEach { doc ->
                    val itemView = LayoutInflater.from(requireContext())
                        .inflate(R.layout.item_proxima_cita, binding.containerCitas, false)

                    itemView.findViewById<android.widget.TextView>(R.id.nombre_barberia).text =
                        doc.getString("nombreBarberia") ?: ""

                    itemView.findViewById<android.widget.TextView>(R.id.fecha_cita).text =
                        "${doc.getString("fecha")} · ${doc.getString("hora")}"

                    binding.containerCitas.addView(itemView)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al cargar las citas", Toast.LENGTH_SHORT).show()
            }
    }

    private fun initListeners() {

        // Foto — click directo para cambiar
        val pedirPermiso = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { concedido ->
            if (concedido) {
                seleccionarImagen.launch("image/*")
            } else {
                Toast.makeText(requireContext(), "Permiso denegado", Toast.LENGTH_SHORT).show()
            }
        }

        // Editar perfil
        binding.btnEditar.setOnClickListener {
            editarPerfilLauncher.launch(
                Intent(requireContext(), EditarPerfilActivity::class.java)
            )
        }

        // Notificaciones
        binding.btnNotificaciones.setOnClickListener {
            startActivity(Intent(requireContext(), NotificacionesActivity::class.java))
        }

        // Idioma
        binding.btnIdioma.setOnClickListener {
            startActivity(Intent(requireContext(), IdiomaActivity::class.java))
        }

        // Privacidad
        binding.btnPrivacidad.setOnClickListener {
            startActivity(Intent(requireContext(), PrivacidadActivity::class.java))
        }

        // Cerrar sesión
        binding.btnLogout.setOnClickListener {
            mostrarDialogoCerrarSesion()
        }
    }
    private fun subirFotoPerfil(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return

        // Mostrar imagen inmediatamente
        Glide.with(this)
            .load(uri)
            .circleCrop()
            .into(binding.imgPerfil)

        Toast.makeText(requireContext(), "Subiendo foto...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            CloudinaryHelper.subirImagen(requireContext(), uri)
                .onSuccess { url ->
                    db.collection("users").document(uid)
                        .update("fotoPerfil", url)
                        .addOnSuccessListener {
                            Toast.makeText(
                                requireContext(),
                                "Foto actualizada ✓",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
                .onFailure {
                    Toast.makeText(
                        requireContext(),
                        "Error al subir la foto",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun mostrarDialogoCerrarSesion() {
        AlertDialog.Builder(requireContext())
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro de que quieres cerrar sesión?")
            .setPositiveButton("Cerrar sesión") { _, _ ->
                auth.signOut()
                startActivity(
                    Intent(requireContext(), LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}