package com.mar0empire.barbershop.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mar0empire.barbershop.models.Cita
import com.mar0empire.barbershop.repository.CitasRepository

class CitasViewModel : ViewModel() {
    private val repo = CitasRepository()
    private val _proximas = MutableLiveData<List<Cita>>()
    val proximas : LiveData<List<Cita>> get() = _proximas
    private val _historial = MutableLiveData<List<Cita>>()
    val historial : LiveData<List<Cita>> get() = _historial

    fun cargarProximas(){
        repo.getCitasFuturas(
            onSuccess = {
                citas -> _proximas.value = citas
            }, onError = {
                Log.e("CitasViewModel", "Error al cargar las citas")
            }
        )
    }
    fun cargarHistorial(){
        repo.getCitasPasadas(
        onSuccess = {
                citas -> _proximas.value = citas
        }, onError = {
            Log.e("CitasViewModel", "Error al cargar las citas")
        }
    )
    }
}