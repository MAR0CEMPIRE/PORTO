package com.mar0empire.barbershop.main.fragments.barberia

import android.app.AlertDialog
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
    private val db   = FirebaseFirestore.getInstance()
    private lateinit var adapter: ProximasCitasAdapter

    /** Timestamp del día actualmente seleccionado en el calendario */
    private var timestampDiaActual: Long = System.currentTimeMillis()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ProximasCitasAdapter(
            onConfirmar = { cita -> cambiarEstadoCita(cita.id, "confirmada") },
            onCancelar  = { cita -> cambiarEstadoCita(cita.id, "cancelada") },
            onCompletar = { cita -> cambiarEstadoCita(cita.id, "completada") },
            onEliminar  = { cita -> confirmarEliminarCita(cita) }
        )

        binding.rvCitasDia.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCitasDia.adapter = adapter

        cargarCitasDia(timestampDiaActual)

        binding.calendarView.setOnDateChangeListener { _, year, month, day ->
            val cal = Calendar.getInstance().apply { set(year, month, day, 0, 0, 0) }
            timestampDiaActual = cal.timeInMillis
            cargarCitasDia(timestampDiaActual)
        }
    }

    private fun cargarCitasDia(timestamp: Long) {
        val uid   = auth.currentUser?.uid ?: return
        val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))

        db.collection("citas")
            .whereEqualTo("barberiaId", uid)
            .whereEqualTo("fecha", fecha)
            .get()
            .addOnSuccessListener { docs ->
                if (_binding == null) return@addOnSuccessListener

                val lista = docs.documents.map { doc ->
                    CitaBarbero(
                        id       = doc.id,
                        cliente  = doc.getString("nombreCliente") ?: "",
                        hora     = doc.getString("hora")          ?: "",
                        servicio = doc.getString("servicio")      ?: "",
                        precio   = doc.getDouble("precio")        ?: 0.0,
                        estado   = doc.getString("estado")        ?: "pendiente"
                    )
                }.sortedBy { it.hora }

                adapter.submitList(lista)
            }
    }

    private fun cambiarEstadoCita(citaId: String, nuevoEstado: String) {
        db.collection("citas").document(citaId)
            .update("estado", nuevoEstado)
            .addOnSuccessListener {
                val msg = when (nuevoEstado) {
                    "confirmada" -> "Cita confirmada ✓"
                    "completada" -> "Cita marcada como completada ✓"
                    else         -> "Cita cancelada"
                }
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

                // Notificar al cliente si aplica
                if (nuevoEstado == "confirmada" || nuevoEstado == "cancelada") {
                    db.collection("citas").document(citaId).get()
                        .addOnSuccessListener { doc ->
                            val clienteId      = doc.getString("clienteId")      ?: return@addOnSuccessListener
                            val nombreBarberia = doc.getString("nombreBarberia") ?: ""
                            val fecha          = doc.getString("fecha")          ?: ""
                            val hora           = doc.getString("hora")           ?: ""

                            if (nuevoEstado == "confirmada") {
                                NotificacionHelper.citaConfirmada(
                                    uidCliente     = clienteId,
                                    nombreBarberia = nombreBarberia,
                                    fecha          = "$fecha $hora",
                                    citaId         = citaId
                                )
                            } else {
                                NotificacionHelper.citaCancelada(
                                    uid    = clienteId,
                                    nombre = nombreBarberia,
                                    fecha  = "$fecha $hora",
                                    citaId = citaId
                                )
                            }
                        }
                }

                cargarCitasDia(timestampDiaActual)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al actualizar la cita", Toast.LENGTH_SHORT).show()
            }
    }

    private fun confirmarEliminarCita(cita: CitaBarbero) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar cita")
            .setMessage("¿Seguro que quieres eliminar la cita de ${cita.cliente}? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ -> eliminarCita(cita.id) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarCita(citaId: String) {
        db.collection("citas").document(citaId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Cita eliminada", Toast.LENGTH_SHORT).show()
                cargarCitasDia(timestampDiaActual)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al eliminar la cita", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}