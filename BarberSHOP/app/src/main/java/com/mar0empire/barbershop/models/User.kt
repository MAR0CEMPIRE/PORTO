package com.mar0empire.barbershop.models

data class User(
    val uid : String = "",
    val nombre: String = "",
    val email : String = "",
    val fotourl : String = "",
    val rol : String = "cliente"
)
