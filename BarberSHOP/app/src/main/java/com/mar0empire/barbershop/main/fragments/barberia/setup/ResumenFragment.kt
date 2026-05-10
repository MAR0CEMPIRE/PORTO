package com.mar0empire.barbershop.main.fragments.barberia.setup

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mar0empire.barbershop.databinding.FragmentBarberiaResumenBinding
import com.mar0empire.barbershop.main.activities.MainBarberiaActivity
import com.mar0empire.barbershop.viewmodel.SetupBarberiaViewModel

class ResumenFragment : Fragment() {

    private var _binding: FragmentBarberiaResumenBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SetupBarberiaViewModel by activityViewModels()

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBarberiaResumenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mostrarResumen()
        initListeners()
    }

    private fun mostrarResumen() {
        val container = binding.containerResumen

        agregarFila(container, "Nombre", viewModel.nombre)
        agregarFila(container, "Teléfono", viewModel.telefono)
        agregarFila(container, "Descripción", viewModel.descripcion)
        agregarFila(container, "Ubicación", viewModel.direccion.ifEmpty { "${viewModel.latitud}, ${viewModel.longitud}" })

        val diasAbiertos = viewModel.horarios
            .filter { it.abierto }
            .joinToString("\n") { "${it.dia}: ${it.horaApertura} - ${it.horaCierre}" }
        agregarFila(container, "Horarios", diasAbiertos.ifEmpty { "Sin horarios" })

        val serviciosTexto = viewModel.servicios
            .joinToString("\n") { "${it.nombre} — ${it.precio}€ — ${it.duracionMinutos}min" }
        agregarFila(container, "Servicios", serviciosTexto.ifEmpty { "Sin servicios" })
    }

    private fun agregarFila(container: android.widget.LinearLayout, label: String, valor: String) {
        val tv = TextView(requireContext()).apply {
            text = "$label: $valor"
            textSize = 15f
            setPadding(0, 8, 0, 8)
        }
        container.addView(tv)
    }

    private fun initListeners() {
        binding.btnFinalizar.setOnClickListener {
            guardarEnFirestore()
        }
    }

    private fun guardarEnFirestore() {
        val uid = auth.currentUser?.uid ?: return

        binding.btnFinalizar.isEnabled = false
        binding.btnFinalizar.text = "Guardando..."

        val horariosMapa = viewModel.horarios.map { horario ->
            mapOf(
                "dia" to horario.dia,
                "abierto" to horario.abierto,
                "horaApertura" to horario.horaApertura,
                "horaCierre" to horario.horaCierre
            )
        }

        val serviciosMapa = viewModel.servicios.map { servicio ->
            mapOf(
                "id" to servicio.id,
                "nombre" to servicio.nombre,
                "precio" to servicio.precio,
                "duracionMinutos" to servicio.duracionMinutos
            )
        }

        val datos = hashMapOf(
            "nombre" to viewModel.nombre,
            "descripcion" to viewModel.descripcion,
            "telefono" to viewModel.telefono,
            "fotoPerfil" to viewModel.fotoUrl,
            "latitud" to viewModel.latitud,
            "longitud" to viewModel.longitud,
            "direccion" to viewModel.direccion,
            "horarios" to horariosMapa,
            "servicios" to serviciosMapa,
            "configurado" to true
        )

        db.collection("barberia").document(uid)
            .set(datos)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "¡Barbería configurada! ✓", Toast.LENGTH_SHORT).show()
                startActivity(
                    Intent(requireContext(), MainBarberiaActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                )
            }
            .addOnFailureListener {
                binding.btnFinalizar.isEnabled = true
                binding.btnFinalizar.text = "Finalizar"
                Toast.makeText(requireContext(), "Error al guardar", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}