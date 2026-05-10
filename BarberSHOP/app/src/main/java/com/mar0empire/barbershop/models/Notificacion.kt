package com.mar0empire.barbershop.models


data class Notificacion(
    val id: String = "",
    val titulo: String = "",
    val mensaje: String = "",
    val tipo: String = "", // nueva_cita, confirmada, cancelada, recordatorio, mensaje
    val leida: Boolean = false,
    val fecha: Long = 0L,
    val datos: Map<String, String> = emptyMap()
)