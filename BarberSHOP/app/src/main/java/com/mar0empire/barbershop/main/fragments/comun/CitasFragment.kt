package com.mar0empire.barbershop.main.fragments.comun

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mar0empire.barbershop.R
import com.mar0empire.barbershop.adapters.CitasClienteAdapter
import com.mar0empire.barbershop.viewmodel.CitasViewModel

class CitasFragment : Fragment() {

    private lateinit var viewModel: CitasViewModel
    private lateinit var tabProximas: TextView
    private lateinit var tabHistorial: TextView
    private lateinit var rvCitas: RecyclerView
    private lateinit var adapter: CitasClienteAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cita, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[CitasViewModel::class.java]

        tabProximas = view.findViewById(R.id.tab_proximas_citas)
        tabHistorial = view.findViewById(R.id.tab_historial_citas)
        rvCitas = view.findViewById(R.id.rv_citas)

        adapter = CitasClienteAdapter {}
        rvCitas.layoutManager = LinearLayoutManager(requireContext())
        rvCitas.adapter = adapter

        configurarTabs()
        observarDatos()
        observarErrores()
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
                resources.getDrawable(R.drawable.bg_section_selected, null).constantState
            ) adapter.submitList(lista)
        }

        viewModel.historial.observe(viewLifecycleOwner) { lista ->
            if (tabHistorial.background.constantState ==
                resources.getDrawable(R.drawable.bg_section_selected, null).constantState
            ) adapter.submitList(lista)
        }
    }

    private fun observarErrores() {
        viewModel.error.observe(viewLifecycleOwner) { mensaje ->
            if (!mensaje.isNullOrEmpty()) {
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
            }
        }
    }
}