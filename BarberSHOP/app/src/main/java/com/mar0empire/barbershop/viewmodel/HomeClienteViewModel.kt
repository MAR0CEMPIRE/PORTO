package com.mar0empire.barbershop.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mar0empire.barbershop.models.Barberia
import com.mar0empire.barbershop.repository.BarberiaRepository

class HomeClienteViewModel : ViewModel() {

    private val repo = BarberiaRepository()

    private val _cercanas = MutableLiveData<List<Barberia>>()
    val cercanas: LiveData<List<Barberia>> get() = _cercanas

    private val _destacadas = MutableLiveData<List<Barberia>>()
    val destacadas: LiveData<List<Barberia>> get() = _destacadas

    private val _top = MutableLiveData<List<Barberia>>()
    val top: LiveData<List<Barberia>> get() = _top

    fun cargarCercanas() {
        repo.getBarberiasCercanas(
            onSuccess = { _cercanas.value = it },
            onError = { Log.e("HomeVM", "Error cercanas", it) }
        )
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
