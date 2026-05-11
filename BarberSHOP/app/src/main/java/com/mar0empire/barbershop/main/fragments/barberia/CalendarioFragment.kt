package com.mar0empire.barbershop.main.fragments.barberia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mar0empire.barbershop.adapters.ProximasCitasAdapter
import com.mar0empire.barbershop.databinding.FragmentCalendarioBinding
import com.mar0empire.barbershop.models.CitaBarbero
import com.mar0empire.barbershop.utils.NotificacionHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarioFragment : Fragment() {

    private var _binding: FragmentCalendarioBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: ProximasCitasAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ProximasCitasAdapter(
            onConfirmar = { cita -> cambiarEstadoCita(cita.id, "confirmada") },
            onCancelar = { cita -> cambiarEstadoCita(cita.id, "cancelada") }
        )

        binding.rvCitasDia.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCitasDia.adapter = adapter

        cargarCitasDia(System.currentTimeMillis())

        binding.calendarView.setOnDateChangeListener { _, year, month, day ->
            val cal = Calendar.getInstance().apply {
                set(year, month, day, 0, 0, 0)
            }
            cargarCitasDia(cal.timeInMillis)
        }
    }

    private fun cargarCitasDia(timestamp: Long) {
        val uid = auth.currentUser?.uid ?: return
        val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            .format(Date(timestamp))

        db.collection("citas")
            .whereEqualTo("barberiaId", uid)
            .whereEqualTo("fecha", fecha)
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

                adapter.submitList(lista)
            }
    }

    private fun cambiarEstadoCita(citaId: String, nuevoEstado: String) {
        db.collection("citas").document(citaId)
            .update("estado", nuevoEstado)
            .addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    if (nuevoEstado == "confirmada") "Cita confirmada ✓" else "Cita cancelada",
                    Toast.LENGTH_SHORT
                ).show()

                db.collection("citas").document(citaId).get()
                    .addOnSuccessListener { doc ->
                        val clienteId = doc.getString("clienteId") ?: return@addOnSuccessListener
                        val nombreBarberia = doc.getString("nombreBarberia") ?: ""
                        val fecha = doc.getString("fecha") ?: ""
                        val hora = doc.getString("hora") ?: ""

                        if (nuevoEstado == "confirmada") {
                            NotificacionHelper.citaConfirmada(
                                uidCliente = clienteId,
                                nombreBarberia = nombreBarberia,
                                fecha = "$fecha $hora",
                                citaId = citaId
                            )
                        } else {
                            NotificacionHelper.citaCancelada(
                                uid = clienteId,
                                nombre = nombreBarberia,
                                fecha = "$fecha $hora",
                                citaId = citaId
                            )
                        }

                        // Recargar el día actual
                        cargarCitasDia(System.currentTimeMillis())
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al actualizar", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}