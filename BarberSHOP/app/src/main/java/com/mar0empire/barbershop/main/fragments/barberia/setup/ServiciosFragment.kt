package com.mar0empire.barbershop.main.fragments.barberia.setup

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.mar0empire.barbershop.R
import com.mar0empire.barbershop.databinding.FragmentBarberiaServiciosBinding
import com.mar0empire.barbershop.models.Servicio
import com.mar0empire.barbershop.viewmodel.SetupBarberiaViewModel
import java.util.UUID

class ServiciosFragment : Fragment() {

    private var _binding: FragmentBarberiaServiciosBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SetupBarberiaViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBarberiaServiciosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Restaurar servicios si ya los tenía
        viewModel.servicios.forEach { agregarFilaServicio(it) }

        initListeners()
    }

    private fun initListeners() {
        binding.btnAgregarServicio.setOnClickListener {
            mostrarDialogoAgregarServicio()
        }

        binding.btnSiguiente.setOnClickListener {
            if (viewModel.servicios.isEmpty()) {
                Toast.makeText(requireContext(), "Añade al menos un servicio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            findNavController().navigate(R.id.action_servicios_to_resumen)
        }
    }

    private fun mostrarDialogoAgregarServicio() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_agregar_servicio, null)

        val etNombre = dialogView.findViewById<TextInputEditText>(R.id.etNombreServicio)
        val etPrecio = dialogView.findViewById<TextInputEditText>(R.id.etPrecioServicio)
        val etDuracion = dialogView.findViewById<TextInputEditText>(R.id.etDuracionServicio)

        AlertDialog.Builder(requireContext())
            .setTitle("Nuevo servicio")
            .setView(dialogView)
            .setPositiveButton("Añadir") { _, _ ->
                val nombre = etNombre.text.toString().trim()
                val precio = etPrecio.text.toString().toDoubleOrNull() ?: 0.0
                val duracion = etDuracion.text.toString().toIntOrNull() ?: 30

                if (nombre.isEmpty()) {
                    Toast.makeText(requireContext(), "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val servicio = Servicio(
                    id = UUID.randomUUID().toString(),
                    nombre = nombre,
                    precio = precio,
                    duracionMinutos = duracion
                )

                viewModel.servicios.add(servicio)
                agregarFilaServicio(servicio)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun agregarFilaServicio(servicio: Servicio) {
        val fila = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_servicio_editable, binding.containerServicios, false)

        fila.findViewById<android.widget.TextView>(R.id.txtNombreServicio).text = servicio.nombre
        fila.findViewById<android.widget.TextView>(R.id.txtPrecioServicio).text = "${servicio.precio}€"
        fila.findViewById<android.widget.TextView>(R.id.txtDuracionServicio).text = "${servicio.duracionMinutos} min"

        fila.findViewById<android.widget.ImageView>(R.id.btnEliminarServicio).setOnClickListener {
            viewModel.servicios.remove(servicio)
            binding.containerServicios.removeView(fila)
        }

        binding.containerServicios.addView(fila)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}