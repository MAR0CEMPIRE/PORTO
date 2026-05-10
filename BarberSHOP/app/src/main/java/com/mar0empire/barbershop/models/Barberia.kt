package com.mar0empire.barbershop.models

data class Barberia(
    val id: String = "",
    val nombre : String = "",
    val ubicacion : String = "",
    val fotoUrl: String = "",
    val rating: Double = 0.0,
    val distancia: Double = 0.0,
    val esDestacada: Boolean = false, // Corregido de esDestaca a esDestacada
    val esTop: Boolean = false,
    val esNueva: Boolean = false
)
