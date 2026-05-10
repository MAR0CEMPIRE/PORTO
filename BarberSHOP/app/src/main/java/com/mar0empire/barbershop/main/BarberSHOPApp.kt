package com.mar0empire.barbershop.main

import android.app.Application
import com.mar0empire.barbershop.utils.NotificacionManager

class BarberSHOPApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificacionManager.crearCanales(this)
    }
}