package com.mar0empire.barbershop.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.LocationServices
import com.mar0empire.barbershop.models.Barberia
import com.mar0empire.barbershop.repository.BarberiaRepository

class HomeClienteViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = BarberiaRepository()
    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(application)

    private val _cercanas = MutableLiveData<List<Barberia>>()
    val cercanas: LiveData<List<Barberia>> get() = _cercanas

    private val _destacadas = MutableLiveData<List<Barberia>>()
    val destacadas: LiveData<List<Barberia>> get() = _destacadas

    private val _top = MutableLiveData<List<Barberia>>()
    val top: LiveData<List<Barberia>> get() = _top

    @SuppressLint("MissingPermission")
    fun cargarCercanas() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    repo.getBarberiasCercanas(
                        latitudUsuario = location.latitude,
                        longitudUsuario = location.longitude,
                        onSuccess = { _cercanas.value = it },
                        onError = { Log.e("HomeVM", "Error cercanas", it) }
                    )
                } else {
                    repo.getBarberiasDestacadas(
                        onSuccess = { _cercanas.value = it },
                        onError = { Log.e("HomeVM", "Error cercanas sin ubicación", it) }
                    )
                }
            }
            .addOnFailureListener {
                Log.e("HomeVM", "Error obteniendo ubicación", it)
            }
    }

    fun cargarDestacadas() {
        repo.getBarberiasDestacadas(
            onSuccess = { _destacadas.value = it },
            onError = { Log.e("HomeVM", "Error destacadas", it) }
        )
    }

    fun cargarTop() {
        repo.getBarberiasTop(
            onSuccess = { _top.value = it },
            onError = { Log.e("HomeVM", "Error top", it) }
        )
    }
}