package com.mar0empire.barbershop.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.mar0empire.barbershop.R
import com.mar0empire.barbershop.databinding.ActivityMainClienteBinding


class MainClienteActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainClienteBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupNavegation()
    }

    private fun setupNavegation(){
        //Obtener el navhost
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_cliente) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavCliente.setupWithNavController(navController)
    }
}