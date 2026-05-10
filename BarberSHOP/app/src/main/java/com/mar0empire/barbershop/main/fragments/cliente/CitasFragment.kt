package com.mar0empire.barbershop.main.fragments.cliente

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mar0empire.barbershop.R
import com.mar0empire.barbershop.models.Cita
import com.mar0empire.barbershop.utils.EstadoCita
import com.mar0empire.barbershop.viewmodel.CitasViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CitasFragment : Fragment() {

    private lateinit var viewModel: CitasViewModel
    private lateinit var tabProximas: TextView
    private lateinit var tabHistorial: TextView
    private lateinit var containerCitas: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cita, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(CitasViewModel::class.java)

        tabProximas = view.findViewById(R.id.tab_proximas_citas)
        tabHistorial = view.findViewById(R.id.tab_historial_citas)
        containerCitas = view.findViewById(R.id.container_citas)

        configurarTabs()
        observarDatos()

        // Cargar por defecto
        seleccionarTabProximas()
    }

    private fun configurarTabs() {
        tabProximas.setOnClickListener { seleccionarTabProximas() }
        tabHistorial.setOnClickListener { seleccionarTabHistorial() }
    }

    private fun seleccionarTabProximas() {
        activarTab(tabProximas)
        desactivarTab(tabHistorial)

        viewModel.cargarProximas()
    }

    private fun seleccionarTabHistorial() {
        activarTab(tabHistorial)
        desactivarTab(tabProximas)

        viewModel.cargarHistorial()
    }

    private fun activarTab(tab: TextView) {
        tab.setBackgroundResource(R.drawable.bg_section_selected)
        tab.setTextColor(requireContext().getColor(R.color.white))
    }

    private fun desactivarTab(tab: TextView) {
        tab.setBackgroundResource(R.drawable.bg_section_noselected)
        tab.setTextColor(requireContext().getColor(R.color.gray_300))
    }

    private fun observarDatos() {
        viewModel.proximas.observe(viewLifecycleOwner) { lista ->
            if (tabProximas.background.constantState ==
                resources.getDrawable(R.drawable.bg_section_selected).constantState
            ) {
                mostrarCitas(lista)
            }
        }

        viewModel.historial.observe(viewLifecycleOwner) { lista ->
            if (tabHistorial.background.constantState ==
                resources.getDrawable(R.drawable.bg_section_selected).constantState
            ) {
                mostrarCitas(lista)
            }
        }
    }

    private fun mostrarCitas(lista: List<Cita>) {
        containerCitas.removeAllViews()

        lista.forEach { cita ->
            val item = layoutInflater.inflate(R.layout.item_proxima_cita, containerCitas, false)

            val nombre = item.findViewById<TextView>(R.id.nombre_barberia)
            val ubicacion = item.findViewById<TextView>(R.id.ubicacion_cita)
            val fecha = item.findViewById<TextView>(R.id.fecha_cita)
            val estadoView = item.findViewById<TextView>(R.id.estado_cita)

            nombre.text = cita.nombreBarberia
            ubicacion.text = cita.ubicacion

            val fechaFormateada =
                SimpleDateFormat("dd MMM • HH:mm", Locale.getDefault()).format(Date(cita.fecha))
            fecha.text = fechaFormateada

            EstadoCita.aplicarTintEstado(
                estadoView,
                cita.estado,
                requireContext()
            )
            containerCitas.addView(item)
        }
    }

}