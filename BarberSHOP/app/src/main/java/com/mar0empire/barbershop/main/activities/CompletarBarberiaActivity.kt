package com.mar0empire.barbershop.main.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.mar0empire.barbershop.R
import com.mar0empire.barbershop.databinding.ActivityCompletarBarberiaBinding
import com.mar0empire.barbershop.viewmodel.SetUpBarberiaViewModel

class CompletarBarberiaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompletarBarberiaBinding
    private lateinit var viewModel: SetUpBarberiaViewModel

    companion object {
        const val EXTRA_PASO = "extra_paso"
        const val PASO_DATOS = "datos"
        const val PASO_HORARIOS = "horarios"
        const val PASO_SERVICIOS = "servicios"

        const val EXTRA_NOMBRE   = "nombre"
        const val EXTRA_EMAIL    = "email"
        const val EXTRA_PASSWORD = "password"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompletarBarberiaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[SetUpBarberiaViewModel::class.java]

        // Guardar datos del registro nuevo en el ViewModel
        intent.getStringExtra(EXTRA_NOMBRE)?.let   { viewModel.nombreUsuario    = it }
        intent.getStringExtra(EXTRA_EMAIL)?.let    { viewModel.emailRegistro    = it }
        intent.getStringExtra(EXTRA_PASSWORD)?.let { viewModel.passwordRegistro = it }

        setupToolbar()

        val paso = intent.getStringExtra(EXTRA_PASO)

        // Si es modo edición (no registro nuevo) y hay un paso destino, precargar Firestore antes de navegar
        if (paso != null && viewModel.emailRegistro.isEmpty()) {
            viewModel.cargarDatosFirestoreCompletos {
                navegarAlPaso(paso)
            }
        } else {
            // Registro nuevo -> navegar directamente (sin carga previa)
            if (paso != null) navegarAlPaso(paso)
        }
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

    private fun navegarAlPaso(paso: String) {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_setup_barberia) as NavHostFragment

        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener(
            object : androidx.navigation.NavController.OnDestinationChangedListener {
                override fun onDestinationChanged(
                    controller: androidx.navigation.NavController,
                    destination: androidx.navigation.NavDestination,
                    arguments: android.os.Bundle?
                ) {
                    navController.removeOnDestinationChangedListener(this)
                    when (paso) {
                        PASO_HORARIOS -> {
                            navController.navigate(R.id.action_datosBasicos_to_ubicacion)
                            navController.navigate(R.id.action_ubicacion_to_horarios)
                        }
                        PASO_SERVICIOS -> {
                            navController.navigate(R.id.action_datosBasicos_to_ubicacion)
                            navController.navigate(R.id.action_ubicacion_to_horarios)
                            navController.navigate(R.id.action_horarios_to_servicios)
                        }
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
