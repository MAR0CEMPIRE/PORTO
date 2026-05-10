package com.mar0empire.barbershop.main.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.mar0empire.barbershop.R
import com.mar0empire.barbershop.databinding.ActivityCompletarBarberiaBinding

class CompletarBarberiaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompletarBarberiaBinding

    companion object {
        const val EXTRA_PASO = "extra_paso"
        const val PASO_DATOS = "datos"
        const val PASO_HORARIOS = "horarios"
        const val PASO_SERVICIOS = "servicios"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompletarBarberiaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        navegarAlPaso()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarCompletar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_setup_barberia) as NavHostFragment

        navHostFragment.navController
            .addOnDestinationChangedListener { _, destination, _ ->
                binding.toolbarCompletar.title = destination.label
            }
    }

    private fun navegarAlPaso() {
        val paso = intent.getStringExtra(EXTRA_PASO) ?: return

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_setup_barberia) as NavHostFragment

        val navController = navHostFragment.navController

        // Esperamos a que el NavController esté listo
        navController.addOnDestinationChangedListener(
            object : androidx.navigation.NavController.OnDestinationChangedListener {
                override fun onDestinationChanged(
                    controller: androidx.navigation.NavController,
                    destination: androidx.navigation.NavDestination,
                    arguments: android.os.Bundle?
                ) {
                    // Solo navegamos la primera vez
                    navController.removeOnDestinationChangedListener(this)

                    when (paso) {
                        PASO_HORARIOS -> navController.navigate(R.id.action_datosBasicos_to_ubicacion).also {
                            navController.navigate(R.id.action_ubicacion_to_horarios)
                        }
                        PASO_SERVICIOS -> navController.navigate(R.id.action_datosBasicos_to_ubicacion).also {
                            navController.navigate(R.id.action_ubicacion_to_horarios)
                            navController.navigate(R.id.action_horarios_to_servicios)
                        }
                        // PASO_DATOS → se queda en el primer paso por defecto
                    }
                }
            }
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_setup_barberia) as NavHostFragment
        return navHostFragment.navController.navigateUp() || super.onSupportNavigateUp()
    }
}