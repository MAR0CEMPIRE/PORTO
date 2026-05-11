package com.mar0empire.barbershop.main.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mar0empire.barbershop.adapters.HorasAdapter
import com.mar0empire.barbershop.databinding.ActivityReservarCitaBinding
import com.mar0empire.barbershop.models.HoraItem
import com.mar0empire.barbershop.utils.NotificacionHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ReservarCitaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReservarCitaBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var barberiaId: String
    private lateinit var nombreBarberia: String
    private lateinit var fotoBarberia: String
    private lateinit var ubicacionBarberia: String

    private var servicioSeleccionado: String = ""
    private var precioSeleccionado: Double = 0.0
    private var fechaSeleccionada: Long = System.currentTimeMillis()
    private var horaSeleccionada: String = ""
    private lateinit var horasAdapter: HorasAdapter

    companion object {
        const val EXTRA_BARBERIA_ID = "barberia_id"
        const val EXTRA_BARBERIA_NOMBRE = "barberia_nombre"
        const val EXTRA_BARBERIA_FOTO = "barberia_foto"
        const val EXTRA_BARBERIA_UBICACION = "barberia_ubicacion"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReservarCitaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        barberiaId = intent.getStringExtra(EXTRA_BARBERIA_ID) ?: run { finish(); return }
        nombreBarberia = intent.getStringExtra(EXTRA_BARBERIA_NOMBRE) ?: ""
        fotoBarberia = intent.getStringExtra(EXTRA_BARBERIA_FOTO) ?: ""
        ubicacionBarberia = intent.getStringExtra(EXTRA_BARBERIA_UBICACION) ?: ""

        setupHorasRecycler()
        cargarServicios()
        initListeners()
    }

    private fun setupHorasRecycler() {
        horasAdapter = HorasAdapter(mutableListOf()) { horaItem ->
            horaSeleccionada = horaItem.hora
        }
        binding.rvHoras.layoutManager = GridLayoutManager(
            this, 2, GridLayoutManager.HORIZONTAL, false
        )
        binding.rvHoras.adapter = horasAdapter
        cargarHorasDisponibles(fechaSeleccionada)
    }

    private fun cargarServicios() {
        db.collection("barberia").document(barberiaId)
            .get()
            .addOnSuccessListener { doc ->
                val servicios = doc.get("servicios") as? List<Map<String, Any>>
                    ?: return@addOnSuccessListener

                binding.chipGroupServicios.removeAllViews()

                servicios.forEach { servicio ->
                    val nombre = servicio["nombre"] as? String ?: return@forEach
                    val precio = servicio["precio"] as? Double ?: 0.0
                    val duracion = (servicio["duracionMinutos"] as? Long)?.toInt() ?: 30

                    val chip = Chip(this).apply {
                        text = "$nombre · ${precio}€ · ${duracion}min"
                        isCheckable = true
                        setChipBackgroundColorResource(android.R.color.transparent)
                        setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked) {
                                servicioSeleccionado = nombre
                                precioSeleccionado = precio // ✅
                            }
                        }
                    }
                    binding.chipGroupServicios.addView(chip)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar servicios", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarHorasDisponibles(fechaTimestamp: Long) {
        db.collection("barberia").document(barberiaId)
            .get()
            .addOnSuccessListener { doc ->
                val horarios = doc.get("horarios") as? List<Map<String, Any>>
                    ?: return@addOnSuccessListener

                val cal = Calendar.getInstance().apply { timeInMillis = fechaTimestamp }
                val diasSemana = listOf(
                    "Domingo", "Lunes", "Martes", "Miércoles",
                    "Jueves", "Viernes", "Sábado"
                )
                val diaSeleccionado = diasSemana[cal.get(Calendar.DAY_OF_WEEK) - 1]
                val horarioDia = horarios.firstOrNull { it["dia"] == diaSeleccionado }

                if (horarioDia == null || horarioDia["abierto"] == false) {
                    horasAdapter.actualizarHoras(emptyList())
                    Toast.makeText(this, "La barbería está cerrada ese día", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val apertura = horarioDia["horaApertura"] as? String ?: "09:00"
                val cierre = horarioDia["horaCierre"] as? String ?: "20:00"
                horasAdapter.actualizarHoras(generarSlots(apertura, cierre, fechaTimestamp))
            }
    }

    private fun generarSlots(apertura: String, cierre: String, fechaTimestamp: Long): List<HoraItem> {
        val slots = mutableListOf<HoraItem>()
        val ahora = System.currentTimeMillis()

        val calApertura = Calendar.getInstance().apply {
            timeInMillis = fechaTimestamp
            val (h, m) = apertura.split(":").map { it.toInt() }
            set(Calendar.HOUR_OF_DAY, h)
            set(Calendar.MINUTE, m)
            set(Calendar.SECOND, 0)
        }

        val calCierre = Calendar.getInstance().apply {
            timeInMillis = fechaTimestamp
            val (h, m) = cierre.split(":").map { it.toInt() }
            set(Calendar.HOUR_OF_DAY, h)
            set(Calendar.MINUTE, m)
        }

        while (calApertura.before(calCierre)) {
            val horaStr = String.format(
                "%02d:%02d",
                calApertura.get(Calendar.HOUR_OF_DAY),
                calApertura.get(Calendar.MINUTE)
            )
            slots.add(HoraItem(horaStr, calApertura.timeInMillis > ahora))
            calApertura.add(Calendar.MINUTE, 30)
        }
        return slots
    }

    private fun initListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.calendarView.setOnDateChangeListener { _, year, month, day ->
            val cal = Calendar.getInstance().apply { set(year, month, day, 0, 0, 0) }
            fechaSeleccionada = cal.timeInMillis
            cargarHorasDisponibles(fechaSeleccionada)
        }

        binding.btnConfirmarReserva.setOnClickListener { confirmarReserva() }
    }

    private fun confirmarReserva() {
        val uid = auth.currentUser?.uid ?: return

        if (servicioSeleccionado.isEmpty()) {
            Toast.makeText(this, "Selecciona un servicio", Toast.LENGTH_SHORT).show()
            return
        }
        if (horaSeleccionada.isEmpty()) {
            Toast.makeText(this, "Selecciona una hora", Toast.LENGTH_SHORT).show()
            return
        }

        val nota = binding.etNota.editText?.text.toString().trim()
        val fechaFormato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            .format(Date(fechaSeleccionada))

        binding.btnConfirmarReserva.isEnabled = false
        binding.btnConfirmarReserva.text = "Reservando..."

        db.collection("users").document(uid).get()
            .addOnSuccessListener { userDoc ->
                val nombreCliente = userDoc.getString("nombre") ?: "Cliente"

                val cita = hashMapOf(
                    "clienteId" to uid,
                    "nombreCliente" to nombreCliente,
                    "barberiaId" to barberiaId,
                    "nombreBarberia" to nombreBarberia,
                    "fotoBarberia" to fotoBarberia,
                    "ubicacion" to ubicacionBarberia,
                    "servicio" to servicioSeleccionado,
                    "precio" to precioSeleccionado,
                    "fecha" to fechaFormato,
                    "hora" to horaSeleccionada,
                    "fechaTimestamp" to fechaSeleccionada,
                    "nota" to nota,
                    "estado" to "pendiente",
                    "creadoEn" to System.currentTimeMillis()
                )

                db.collection("citas")
                    .add(cita)
                    .addOnSuccessListener { docRef ->
                        NotificacionHelper.nuevaCitaSolicitada(
                            uidBarbero = barberiaId,
                            nombreCliente = nombreCliente,
                            fecha = "$fechaFormato $horaSeleccionada",
                            citaId = docRef.id
                        )
                        Toast.makeText(
                            this,
                            "¡Cita solicitada! El barbero la confirmará pronto.",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                    .addOnFailureListener {
                        binding.btnConfirmarReserva.isEnabled = true
                        binding.btnConfirmarReserva.text = "Reservar"
                        Toast.makeText(this, "Error al reservar", Toast.LENGTH_SHORT).show()
                    }
            }
    }
}