package com.mar0empire.barbershop.models

data class Cita (
    val uid : String ="",
    val idCliente : String ="",
    val idBarberia : String ="",
    val nombreBarberia : String ="",
    val ubicacion : String ="",
    val fecha : Long = 0L,
    val hora : String ="",
    val estado : String ="",
    val fotoBarberia : String =""
)
