package com.mar0empire.barbershop.models

data class Barberia(
    val id: String = "",
    val nombre: String = "",
    val ubicacion: String = "",
    val direccion: String = "",
    val fotoUrl: String = "",
    val fotoPerfil: String = "",
    val rating: Double = 0.0,
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val esDestacada: Boolean = false,
    val esTop: Boolean = false,
    val distancia: Double = 0.0 
)