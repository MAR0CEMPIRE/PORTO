package com.mar0empire.barbershop.main.fragments.barberia.setup

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mar0empire.barbershop.R
import com.mar0empire.barbershop.databinding.FragmentBarberiaDatosBasicosBinding
import com.mar0empire.barbershop.utils.CloudinaryHelper
import com.mar0empire.barbershop.viewmodel.SetUpBarberiaViewModel
import kotlinx.coroutines.launch

class DatosBasicosFragment : Fragment() {

    private var _binding: FragmentBarberiaDatosBasicosBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SetUpBarberiaViewModel by activityViewModels()

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val pedirPermiso = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) seleccionarImagen.launch("image/*")
        else Toast.makeText(requireContext(), "Permiso denegado", Toast.LENGTH_SHORT).show()
    }

    private val seleccionarImagen = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { subirFoto(it) } }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBarberiaDatosBasicosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //  Si viene del registro nuevo (hay email en VM) -> no cargar de Firestore
        //  Si es edición (hay sesión activa) -> cargar siempre de Firestore
        val uid = auth.currentUser?.uid
        if (uid != null && viewModel.emailRegistro.isEmpty()) {
            cargarDatosDeFirestore(uid)
        } else if (viewModel.nombre.isNotEmpty()) {
            // Restaurar del ViewModel si ya navegó hacia atrás
            rellenarCampos()
        }

        initListeners()
    }

    private fun cargarDatosDeFirestore(uid: String) {
        db.collection("barberia").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    viewModel.nombre = doc.getString("nombre") ?: ""
                    viewModel.descripcion = doc.getString("descripcion") ?: ""
                    viewModel.telefono = doc.getString("telefono") ?: ""
                    viewModel.fotoUrl = doc.getString("fotoPerfil") ?: ""
                    viewModel.direccion = doc.getString("direccion") ?: ""
                    viewModel.latitud = doc.getDouble("latitud") ?: 0.0
                    viewModel.longitud = doc.getDouble("longitud") ?: 0.0

                    // Cargar horarios
                    val horariosRaw = doc.get("horarios") as? List<Map<String, Any>>
                    if (!horariosRaw.isNullOrEmpty()) {
                        viewModel.horarios = horariosRaw.map { h ->
                            com.mar0empire.barbershop.models.HorarioDia(
                                dia = h["dia"] as? String ?: "",
                                abierto = h["abierto"] as? Boolean ?: false,
                                horaApertura = h["horaApertura"] as? String ?: "09:00",
                                horaCierre = h["horaCierre"] as? String ?: "20:00"
                            )
                        }
                    }

                    // Cargar servicios
                    val serviciosRaw = doc.get("servicios") as? List<Map<String, Any>>
                    if (!serviciosRaw.isNullOrEmpty()) {
                        viewModel.servicios.clear()
                        serviciosRaw.forEach { s ->
                            viewModel.servicios.add(
                                com.mar0empire.barbershop.models.Servicio(
                                    id = s["id"] as? String ?: "",
                                    nombre = s["nombre"] as? String ?: "",
                                    precio = s["precio"] as? Double ?: 0.0,
                                    duracionMinutos = (s["duracionMinutos"] as? Long)?.toInt() ?: 30
                                )
                            )
                        }
                    }

                    rellenarCampos()
                }
            }
    }

    private fun rellenarCampos() {
        binding.etNombre.setText(viewModel.nombre)
        binding.etDescripcion.setText(viewModel.descripcion)
        binding.etTelefono.setText(viewModel.telefono)
        if (viewModel.fotoUrl.isNotEmpty()) {
            Glide.with(this).load(viewModel.fotoUrl).circleCrop().into(binding.imgBarberia)
        }
    }

    private fun initListeners() {
        binding.btnCambiarFoto.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.READ_MEDIA_IMAGES
                    ) == PackageManager.PERMISSION_GRANTED
                ) seleccionarImagen.launch("image/*")
                else pedirPermiso.launch(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                seleccionarImagen.launch("image/*")
            }
        }

        binding.btnSiguiente.setOnClickListener {
            val nombre = binding.etNombre.text.toString().trim()
            val descripcion = binding.etDescripcion.text.toString().trim()
            val telefono = binding.etTelefono.text.toString().trim()

            if (nombre.isEmpty()) {
                binding.inputNombre.error = "El nombre es obligatorio"
                return@setOnClickListener
            }
            if (telefono.isEmpty()) {
                binding.inputTelefono.error = "El teléfono es obligatorio"
                return@setOnClickListener
            }

            viewModel.nombre = nombre
            viewModel.descripcion = descripcion
            viewModel.telefono = telefono

            findNavController().navigate(R.id.action_datosBasicos_to_ubicacion)
        }
    }

    private fun subirFoto(uri: Uri) {
        Glide.with(this).load(uri).circleCrop().into(binding.imgBarberia)
        binding.btnCambiarFoto.isEnabled = false
        binding.btnCambiarFoto.text = "Subiendo..."

        lifecycleScope.launch {
            CloudinaryHelper.subirImagen(requireContext(), uri)
                .onSuccess { url ->
                    viewModel.fotoUrl = url
                    binding.btnCambiarFoto.isEnabled = true
                    binding.btnCambiarFoto.text = "Cambiar foto"
                    Toast.makeText(requireContext(), "Foto subida ✓", Toast.LENGTH_SHORT).show()
                }
                .onFailure {
                    binding.btnCambiarFoto.isEnabled = true
                    binding.btnCambiarFoto.text = "Cambiar foto"
                    Toast.makeText(requireContext(), "Error al subir foto", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}