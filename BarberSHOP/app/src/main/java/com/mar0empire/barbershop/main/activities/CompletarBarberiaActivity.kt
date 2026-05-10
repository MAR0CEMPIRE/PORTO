package com.mar0empire.barbershop.main.activities.barberia

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.mar0empire.barbershop.R
import com.mar0empire.barbershop.databinding.ActivityCompletarBarberiaBinding

class CompletarBarberiaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompletarBarberiaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompletarBarberiaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarCompletar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Cambiar el título según el fragment actual
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_setup_barberia) as NavHostFragment

        navHostFragment.navController
            .addOnDestinationChangedListener { _, destination, _ ->
                binding.toolbarCompletar.title = destination.label
            }
    }

    // Botón atrás del toolbar
    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_setup_barberia) as NavHostFragment

        return navHostFragment.navController.navigateUp() || super.onSupportNavigateUp()
    }
}