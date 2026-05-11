package com.mar0empire.barbershop.main.fragments.cliente

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.mar0empire.barbershop.adapters.BarberiaHomeAdapter
import com.mar0empire.barbershop.databinding.FragmentHomeClienteBinding
import com.mar0empire.barbershop.main.activities.PerfilBarberiaClienteActivity
import com.mar0empire.barbershop.models.Barberia
import com.mar0empire.barbershop.viewmodel.HomeClienteViewModel

class HomeClienteFragment : Fragment() {

    private var _binding: FragmentHomeClienteBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeClienteViewModel
    private lateinit var adapterCercanas: BarberiaHomeAdapter
    private lateinit var adapterDestacadas: BarberiaHomeAdapter
    private lateinit var adapterTop: BarberiaHomeAdapter

    // ✅ Guardamos todas las listas para poder filtrar
    private var todasCercanas = listOf<Barberia>()
    private var todasDestacadas = listOf<Barberia>()
    private var todasTop = listOf<Barberia>()

    private val pedirPermiso = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) viewModel.cargarCercanas()
        else viewModel.cargarDestacadas()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeClienteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        )[HomeClienteViewModel::class.java]

        configurarAdapters()
        observarDatos()
        pedirPermisoUbicacion()
        setupBuscador()

        viewModel.cargarDestacadas()
        viewModel.cargarTop()
    }

    private fun pedirPermisoUbicacion() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> viewModel.cargarCercanas()
            else -> pedirPermiso.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }


    private fun setupBuscador() {
        binding.etBusqueda.addTextChangedListener { texto ->
            val query = texto.toString().trim().lowercase()

            if (query.isEmpty()) {
                // Sin texto → mostrar todas
                adapterCercanas.submitList(todasCercanas)
                adapterDestacadas.submitList(todasDestacadas)
                adapterTop.submitList(todasTop)
            } else {
                // Con texto → filtrar por nombre o dirección
                val filtroCercanas = todasCercanas.filter {
                    it.nombre.lowercase().contains(query) ||
                            it.direccion.lowercase().contains(query)
                }
                val filtroDestacadas = todasDestacadas.filter {
                    it.nombre.lowercase().contains(query) ||
                            it.direccion.lowercase().contains(query)
                }
                val filtroTop = todasTop.filter {
                    it.nombre.lowercase().contains(query) ||
                            it.direccion.lowercase().contains(query)
                }
                adapterCercanas.submitList(filtroCercanas)
                adapterDestacadas.submitList(filtroDestacadas)
                adapterTop.submitList(filtroTop)
            }
        }
    }


    private fun abrirPerfilBarberia(barberiaId: String) {
        startActivity(
            Intent(requireContext(), PerfilBarberiaClienteActivity::class.java).apply {
                putExtra(PerfilBarberiaClienteActivity.EXTRA_BARBERIA_ID, barberiaId)
            }
        )
    }

    private fun configurarAdapters() {
        adapterCercanas = BarberiaHomeAdapter { barberia -> abrirPerfilBarberia(barberia.id) }
        adapterDestacadas = BarberiaHomeAdapter { barberia -> abrirPerfilBarberia(barberia.id) }
        adapterTop = BarberiaHomeAdapter { barberia -> abrirPerfilBarberia(barberia.id) }

        binding.rvCercanas.apply {
            layoutManager = LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false
            )
            adapter = adapterCercanas
        }

        binding.rvDestacadas.apply {
            layoutManager = LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false
            )
            adapter = adapterDestacadas
        }

        binding.rvTop.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapterTop
        }
    }


    private fun observarDatos() {
        viewModel.cercanas.observe(viewLifecycleOwner) { list ->
            todasCercanas = list
            adapterCercanas.submitList(list)
        }
        viewModel.destacadas.observe(viewLifecycleOwner) { list ->
            todasDestacadas = list
            adapterDestacadas.submitList(list)
        }
        viewModel.top.observe(viewLifecycleOwner) { list ->
            todasTop = list
            adapterTop.submitList(list)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}