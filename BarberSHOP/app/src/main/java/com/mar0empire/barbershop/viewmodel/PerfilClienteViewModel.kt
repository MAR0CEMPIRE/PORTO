package com.mar0empire.barbershop.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mar0empire.barbershop.models.User
import com.mar0empire.barbershop.repository.PerfilRepository

class PerfilClienteViewModel : ViewModel() {

    private val repo = PerfilRepository()
    private val _userData = MutableLiveData<User>()
    val userData: LiveData<User> get() = _userData

    fun cargarDatosUsuario(){
        repo.getUserData(
            onSuccess = {
                user -> _userData.value = user
            }, onError = {
                Log.e("PerfilClienteViewModel", "Error al cargar los datos del usuario")
            }
        )
    }
}