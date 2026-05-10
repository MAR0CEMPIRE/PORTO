package com.mar0empire.barbershop.main.fragments.cliente

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.mar0empire.barbershop.adapters.BarberiaHomeAdapter
import com.mar0empire.barbershop.databinding.FragmentHomeClienteBinding
import com.mar0empire.barbershop.viewmodel.HomeClienteViewModel

class HomeClienteFragment : Fragment() {

    private var _binding: FragmentHomeClienteBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeClienteViewModel
    private lateinit var adapterCercanas: BarberiaHomeAdapter
    private lateinit var adapterDestacadas: BarberiaHomeAdapter
    private lateinit var adapterTop: BarberiaHomeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeClienteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(HomeClienteViewModel::class.java)

        configurarAdapters()
        observarDatos()

        viewModel.cargarCercanas()
        viewModel.cargarDestacadas()
        viewModel.cargarTop()
    }

    private fun configurarAdapters() {
        adapterCercanas = BarberiaHomeAdapter()
        adapterDestacadas = BarberiaHomeAdapter()
        adapterTop = BarberiaHomeAdapter()


        binding.rvCercanas.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = adapterCercanas
        }

        binding.rvDestacadas.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = adapterDestacadas
        }

        binding.rvTop.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = adapterTop
        }
    }

    private fun observarDatos() {
        viewModel.cercanas.observe(viewLifecycleOwner) { list ->
            adapterCercanas.submitList(list)
        }

        viewModel.destacadas.observe(viewLifecycleOwner) { list ->
            adapterDestacadas.submitList(list)
        }

        viewModel.top.observe(viewLifecycleOwner) { list ->
            adapterTop.submitList(list)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}