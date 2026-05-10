package com.mar0empire.barbershop.main.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.mar0empire.barbershop.databinding.ActivityIdiomaBinding

class IdiomaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIdiomaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIdiomaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListeners()
    }

    private fun initListeners() {

        binding.ivAtras.setOnClickListener {
            finish()
        }

        binding.btnEspanol.setOnClickListener {
            cambiarIdioma("es")
        }

        binding.btnIngles.setOnClickListener {
            cambiarIdioma("en")
        }
    }

    private fun cambiarIdioma(codigo: String) {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(codigo)
        )

    }
}