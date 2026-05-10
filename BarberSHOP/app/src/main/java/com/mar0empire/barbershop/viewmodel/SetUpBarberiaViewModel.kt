package com.mar0empire.barbershop.viewmodel


import androidx.lifecycle.ViewModel
import com.mar0empire.barbershop.models.HorarioDia
import com.mar0empire.barbershop.models.Servicio

class SetUpBarberiaViewModel : ViewModel() {

    // Datos básicos
    var nombre: String = ""
    var descripcion: String = ""
    var telefono: String = ""
    var fotoUrl: String = ""

    // Ubicación
    var latitud: Double = 0.0
    var longitud: Double = 0.0
    var direccion: String = ""

    // Horarios
    var horarios: List<HorarioDia> = listOf(
        HorarioDia("Lunes", false),
        HorarioDia("Martes", false),
        HorarioDia("Miércoles", false),
        HorarioDia("Jueves", false),
        HorarioDia("Viernes", false),
        HorarioDia("Sábado", false),
        HorarioDia("Domingo", false)
    )

    // Servicios
    val servicios: MutableList<Servicio> = mutableListOf()
}