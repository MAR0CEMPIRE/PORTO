package com.mar0empire.barbershop.main.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.mar0empire.barbershop.R
import com.mar0empire.barbershop.databinding.ActivityMainClienteBinding
import com.mar0empire.barbershop.utils.NotificacionManager
import com.mar0empire.barbershop.utils.NotificacionService

class MainClienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainClienteBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavegacion()
        pedirPermisoNotificaciones()

        //  PRUEBA — ELIMINAR ANTES DE ENTREGAR EL TFG
        enviarNotificacionBienvenidaPrueba()
    }
    private fun setupNavegacion() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_cliente) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNavCliente.setupWithNavController(navController)
    }
    private fun pedirPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }
    }

    //  PRUEBA — ELIMINAR ANTES DE ENTREGAR EL TFG
    private fun enviarNotificacionBienvenidaPrueba() {
        Handler(Looper.getMainLooper()).postDelayed({
            NotificacionManager.mostrar(
                context = this,
                titulo = "¡Bienvenido a BarberSHOP! 💈",
                mensaje = "Encuentra tu barbería favorita y reserva tu cita.",
                tipo = "cita",
                id = 9999
            )
        }, 5 * 60 * 1000L) // 5 minutos
    }
    override fun onStart() {
        super.onStart()
        NotificacionService.iniciar(this)
    }

    override fun onStop() {
        super.onStop()
        NotificacionService.detener()
    }
}