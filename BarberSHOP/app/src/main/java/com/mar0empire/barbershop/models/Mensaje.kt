package com.mar0empire.barbershop.models

data class Mensaje(
    val id: String = "",
    val texto: String = "",
    val senderId: String = "",
    val timestamp: Long = 0L,
    val leido: Boolean = false
)