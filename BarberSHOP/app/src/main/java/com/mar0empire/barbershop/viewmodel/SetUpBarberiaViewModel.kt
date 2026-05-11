package com.mar0empire.barbershop.viewmodel

import androidx.lifecycle.ViewModel
import com.mar0empire.barbershop.models.HorarioDia
import com.mar0empire.barbershop.models.Servicio

class SetUpBarberiaViewModel : ViewModel() {

    var emailRegistro: String = ""
    var passwordRegistro: String = ""
    var nombreUsuario: String = ""

    var nombre: String = ""
    var descripcion: String = ""
    var telefono: String = ""
    var fotoUrl: String = ""

    var latitud: Double = 0.0
    var longitud: Double = 0.0
    var direccion: String = ""

    var horarios: List<HorarioDia> = listOf(
        HorarioDia("Lunes", false),
        HorarioDia("Martes", false),
        HorarioDia("Miércoles", false),
        HorarioDia("Jueves", false),
        HorarioDia("Viernes", false),
        HorarioDia("Sábado", false),
        HorarioDia("Domingo", false)
    )

    val servicios: MutableList<Servicio> = mutableListOf()
}