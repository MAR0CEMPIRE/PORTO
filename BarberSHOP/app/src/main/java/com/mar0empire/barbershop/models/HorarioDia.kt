package com.mar0empire.barbershop.models

data class HorarioDia(
    val dia: String = "",
    val abierto: Boolean = false,
    val horaApertura: String = "09:00",
    val horaCierre: String = "20:00"
)