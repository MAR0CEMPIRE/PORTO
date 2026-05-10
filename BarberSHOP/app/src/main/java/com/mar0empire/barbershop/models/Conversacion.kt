package com.mar0empire.barbershop.models

data class Conversacion(
    val chatId: String = "",
    val uidDestino: String = "",
    val nombreDestino: String = "",
    val fotoDestino: String = "",
    val ultimoMensaje: String = "",
    val timestamp: Long = 0L,
    val noLeidos: Int = 0
)