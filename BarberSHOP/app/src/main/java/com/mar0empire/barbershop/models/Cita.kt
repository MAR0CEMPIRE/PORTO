package com.mar0empire.barbershop.models

data class Cita(
    val id: String = "",
    val clienteId: String = "",
    val barberiaId: String = "",
    val nombreBarberia: String = "",
    val nombreCliente: String = "",
    val ubicacion: String = "",
    val fechaTimestamp: Long = 0L,
    val fecha: String = "",
    val hora: String = "",
    val estado: String = "",
    val fotoBarberia: String = "",
    val servicio: String = ""
)