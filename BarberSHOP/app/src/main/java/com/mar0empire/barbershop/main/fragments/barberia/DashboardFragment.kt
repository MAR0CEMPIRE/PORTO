package com.mar0empire.barbershop.main.fragments.barberia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mar0empire.barbershop.R
import com.mar0empire.barbershop.adapters.ProximasCitasAdapter
import com.mar0empire.barbershop.databinding.FragmentDashboardBinding
import com.mar0empire.barbershop.models.CitaBarbero
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var adapterCitas: ProximasCitasAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapterCitas = ProximasCitasAdapter(
            onConfirmar = { cita -> cambiarEstadoCita(cita.id, "confirmada") },
            onCancelar = { cita -> cambiarEstadoCita(cita.id, "cancelada") }
        )

        binding.recyclerCitas.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCitas.adapter = adapterCitas

        cargarDatosBarberia()
        cargarServicios()
        cargarCitasHoy()
    }

    private fun cargarDatosBarberia() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("barberia").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) return@addOnSuccessListener
                binding.txtNombreBarberia.text = doc.getString("nombre") ?: ""

                val img = doc.getString("fotoPerfil")
                if (!img.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(img)
                        .placeholder(R.drawable.avatar_perfil)
                        .into(binding.imgBarberia)
                }
            }
    }

    private fun cargarServicios() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("barberia").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val servicios = doc.get("servicios") as? List<*> ?: emptyList<Any>()
                binding.txtServiciosActivos.text = servicios.size.toString()
            }
    }

    private fun cargarCitasHoy() {
        val uid = auth.currentUser?.uid ?: return
        val hoy = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        db.collection("citas")
            .whereEqualTo("barberiaId", uid)
            .whereEqualTo("fecha", hoy)
            .get()
            .addOnSuccessListener { docs ->
                if (_binding == null) return@addOnSuccessListener

                val lista = docs.documents.map { doc ->
                    CitaBarbero(
                        id = doc.id,
                        cliente = doc.getString("nombreCliente") ?: "",
                        hora = doc.getString("hora") ?: "",
                        servicio = doc.getString("servicio") ?: "",
                        precio = doc.getDouble("precio") ?: 0.0,
                        estado = doc.getString("estado") ?: "pendiente"
                    )
                }.sortedBy { it.hora }

                adapterCitas.submitList(lista)
                binding.txtCitasHoy.text = lista.size.toString()
                val ingresos = lista
                    .filter { it.estado == "confirmada" }
                    .sumOf { it.precio }
                binding.txtIngresosHoy.text = "${ingresos}€"
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al cargar citas", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cambiarEstadoCita(citaId: String, nuevoEstado: String) {
        val uid = auth.currentUser?.uid ?: return

        db.collection("citas").document(citaId)
            .update("estado", nuevoEstado)
            .addOnSuccessListener {
                val mensaje = if (nuevoEstado == "confirmada") "Cita confirmada ✓"
                else "Cita cancelada"
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()

                // Notificar al cliente
                db.collection("citas").document(citaId).get()
                    .addOnSuccessListener { doc ->
                        val clienteId = doc.getString("clienteId") ?: return@addOnSuccessListener
                        val nombreBarberia = doc.getString("nombreBarberia") ?: ""
                        val fecha = doc.getString("fecha") ?: ""
                        val hora = doc.getString("hora") ?: ""

                        if (nuevoEstado == "confirmada") {
                            com.mar0empire.barbershop.utils.NotificacionHelper.citaConfirmada(
                                uidCliente = clienteId,
                                nombreBarberia = nombreBarberia,
                                fecha = "$fecha $hora",
                                citaId = citaId
                            )
                        } else {
                            com.mar0empire.barbershop.utils.NotificacionHelper.citaCancelada(
                                uid = clienteId,
                                nombre = nombreBarberia,
                                fecha = "$fecha $hora",
                                citaId = citaId
                            )
                        }
                    }

                // Recargar lista
                cargarCitasHoy()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al actualizar la cita", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}