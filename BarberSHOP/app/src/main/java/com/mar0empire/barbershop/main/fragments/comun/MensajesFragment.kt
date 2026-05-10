package com.mar0empire.barbershop.main.fragments.comun

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mar0empire.barbershop.R

class MensajesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Usamos el layout de cliente ya que está en el grafo del cliente
        return inflater.inflate(R.layout.fragment_mensajes, container, false)
    }
}
