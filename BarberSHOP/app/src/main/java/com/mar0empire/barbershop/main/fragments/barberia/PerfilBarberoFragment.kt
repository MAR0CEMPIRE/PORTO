package com.mar0empire.barbershop.main.fragments.barberia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mar0empire.barbershop.R

class PerfilBarberoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Asegúrate de tener un layout llamado fragment_perfil_barberia
        return inflater.inflate(R.layout.activity_perfil_barberia, container, false)
    }
}
