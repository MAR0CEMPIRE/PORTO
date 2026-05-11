package com.mar0empire.barbershop.models

data class CitaBarbero(
    val id: String = "",
    val cliente: String = "",
    val hora: String = "",
    val servicio: String = "",
    val precio: Double = 0.0,
    val estado: String = "pendiente"
)