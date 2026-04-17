package com.mar0empire.barbershop.main.fragments.cliente

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.mar0empire.barbershop.R
import com.mar0empire.barbershop.adapters.BarberiaAdapter
import com.mar0empire.barbershop.databinding.FragmentHomeClienteBinding
import com.mar0empire.barbershop.models.Barberia

class HomeClienteFragment : Fragment() {

    private var _binding: FragmentHomeClienteBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeClienteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Datos dummy (luego Firestore)
        val cercanas = listOf(
            Barberia("Urban Fade", "Póvoa de Varzim", 4.8, R.drawable.placeholder_barberia),
            Barberia("Classic Barber", "Vila do Conde", 4.7, R.drawable.placeholder_barberia)
        )

        val destacadas = listOf(
            Barberia("Elegance Barber", "Porto", 4.9, R.drawable.placeholder_barberia),
            Barberia("Royal Cuts", "Braga", 4.8, R.drawable.placeholder_barberia)
        )

        val top = listOf(
            Barberia("Master Barber", "Porto", 5.0, R.drawable.placeholder_barberia),
            Barberia("Old School Barber", "Guimarães", 4.9, R.drawable.placeholder_barberia)
        )

        // Configurar RecyclerViews
        binding.rvCercanas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCercanas.adapter = BarberiaAdapter(cercanas)

        binding.rvDestacadas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDestacadas.adapter = BarberiaAdapter(destacadas)

        binding.rvTop.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTop.adapter = BarberiaAdapter(top)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
