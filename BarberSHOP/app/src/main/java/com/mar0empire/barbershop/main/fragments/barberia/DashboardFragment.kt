package com.mar0empire.barbershop.main.fragments.barberia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mar0empire.barbershop.R
import com.mar0empire.barbershop.adapters.ProximasCitasAdapter
import com.mar0empire.barbershop.databinding.FragmentDashboardBinding
import com.mar0empire.barbershop.models.CitaBarbero
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    // 1. Corregido el tipo de binding
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val adapterCitas = ProximasCitasAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 2. Corregido el inflado del binding
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 3. Añadida la llamada a super
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerCitas.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCitas.adapter = adapterCitas

        cargarDatosBarberia()
        cargarServicios()
        cargarCitasHoy()
    }

    private fun cargarDatosBarberia() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("barberias").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) return@addOnSuccessListener

                binding.txtNombreBarberia.text = doc.getString("nombre") ?: ""

                val img = doc.getString("imagenPrincipal")
                if (!img.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(img)
                        .placeholder(R.drawable.avatar_perfil)
                        .into(binding.imgBarberia)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al cargar barbería", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarServicios() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("barberias").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val servicios = doc.get("servicios") as? List<*> ?: emptyList<Any>()
                binding.txtServiciosActivos.text = servicios.size.toString()
            }
    }

    private fun cargarCitasHoy() {
        val uid = auth.currentUser?.uid ?: return

        val hoy = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        db.collection("citas")
            .whereEqualTo("barberiaId", uid)
            .whereEqualTo("fecha", hoy)
            .get()
            .addOnSuccessListener { docs ->

                val lista = docs.documents.map { doc ->
                    CitaBarbero(
                        cliente = doc.getString("clienteNombre") ?: "",
                        hora = doc.getString("hora") ?: "",
                        servicio = doc.getString("servicio") ?: "",
                        precio = doc.getDouble("precio") ?: 0.0
                    )
                }

                adapterCitas.submitList(lista)

                binding.txtCitasHoy.text = lista.size.toString()

                val ingresos = lista.sumOf { it.precio }
                binding.txtIngresosHoy.text = "${ingresos}€"
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al cargar citas", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
