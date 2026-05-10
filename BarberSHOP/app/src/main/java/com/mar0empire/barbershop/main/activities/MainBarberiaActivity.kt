package com.mar0empire.barbershop.main.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.mar0empire.barbershop.R
import com.mar0empire.barbershop.databinding.ActivityMainBarberiaBinding
import com.mar0empire.barbershop.utils.NotificacionService

class MainBarberiaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBarberiaBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBarberiaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavegacion()
        pedirPermisoNotificaciones()
    }
    private fun setupNavegacion() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_barberia) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNavBarberia.setupWithNavController(navController)

        // Evitar recargar el fragmento si ya estamos en él
        binding.bottomNavBarberia.setOnItemSelectedListener { item ->
            if (item.itemId != navController.currentDestination?.id) {
                navController.navigate(item.itemId)
            }
            true
        }
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

    override fun onStart() {
        super.onStart()
        NotificacionService.iniciar(this)
    }

    override fun onStop() {
        super.onStop()
        NotificacionService.detener()
    }
}