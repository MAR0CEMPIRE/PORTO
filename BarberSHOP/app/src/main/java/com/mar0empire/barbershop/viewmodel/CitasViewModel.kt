package com.mar0empire.barbershop.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.mar0empire.barbershop.models.Cita
import com.mar0empire.barbershop.repository.CitasRepository

class CitasViewModel : ViewModel() {

    private val repo = CitasRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _proximas = MutableLiveData<List<Cita>>()
    val proximas: LiveData<List<Cita>> get() = _proximas

    private val _historial = MutableLiveData<List<Cita>>()
    val historial: LiveData<List<Cita>> get() = _historial

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    // Guardamos qué tab está activo para reintentar si Auth no estaba listo
    private var pendingLoad: (() -> Unit)? = null

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        if (firebaseAuth.currentUser != null) {
            // Auth ya disponible: ejecuta la carga pendiente si la hay
            pendingLoad?.invoke()
            pendingLoad = null
        }
    }

    init {
        auth.addAuthStateListener(authListener)
    }

    fun cargarProximas() {
        if (auth.currentUser == null) {
            Log.w("CitasViewModel", "Auth no listo, esperando...")
            pendingLoad = { cargarProximas() }
            return
        }
        repo.getCitasFuturas(
            onSuccess = { citas -> _proximas.value = citas },
            onError = {
                Log.e("CitasViewModel", "Error al cargar próximas: ${it.message}")
                _error.value = "Error al cargar citas"
            }
        )
    }

    fun cargarHistorial() {
        if (auth.currentUser == null) {
            Log.w("CitasViewModel", "Auth no listo, esperando...")
            pendingLoad = { cargarHistorial() }
            return
        }
        repo.getCitasPasadas(
            onSuccess = { citas -> _historial.value = citas },
            onError = {
                Log.e("CitasViewModel", "Error al cargar historial: ${it.message}")
                _error.value = "Error al cargar historial"
            }
        )
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authListener)
    }
}