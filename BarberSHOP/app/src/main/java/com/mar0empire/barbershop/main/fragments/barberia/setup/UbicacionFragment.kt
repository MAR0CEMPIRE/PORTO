package com.mar0empire.barbershop.main.fragments.barberia.setup

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mar0empire.barbershop.R
import com.mar0empire.barbershop.databinding.FragmentBarberiaUbicacionBinding
import com.mar0empire.barbershop.viewmodel.SetUpBarberiaViewModel
import java.util.Locale

class UbicacionFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentBarberiaUbicacionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SetUpBarberiaViewModel by activityViewModels()

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var ubicacionSeleccionada: LatLng? = null

    private val pedirPermiso = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) obtenerUbicacionActual()
        else Toast.makeText(requireContext(), "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBarberiaUbicacionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // ⚠️ SOLO PARA DEPURAR — ELIMINAR DESPUÉS
        val apiKey = requireContext().packageManager
            .getApplicationInfo(requireContext().packageName, PackageManager.GET_META_DATA)
            .metaData?.getString("com.google.android.geo.API_KEY")
        Log.d("MAPS_KEY", "API Key: $apiKey")

        // Inicializar mapa — ahora el fragment ya existe en el XML
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapContainer) as SupportMapFragment
        mapFragment.getMapAsync(this)
        initListeners()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Si ya tenía ubicación guardada
        if (viewModel.latitud != 0.0) {
            val pos = LatLng(viewModel.latitud, viewModel.longitud)
            moverMapa(pos)
        }

        // Click en el mapa para seleccionar ubicación
        mMap.setOnMapClickListener { latLng ->
            seleccionarUbicacion(latLng)
        }
    }

    private fun initListeners() {
        binding.btnUbicacionActual.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                obtenerUbicacionActual()
            } else {
                pedirPermiso.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        binding.btnBuscarDireccion.setOnClickListener {
            mostrarDialogoBuscarDireccion()
        }

        binding.btnSiguiente.setOnClickListener {
            val ubicacion = ubicacionSeleccionada

            if (ubicacion == null) {
                Toast.makeText(requireContext(), "Selecciona una ubicación", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.latitud = ubicacion.latitude
            viewModel.longitud = ubicacion.longitude

            findNavController().navigate(R.id.action_ubicacion_to_horarios)
        }
    }

    private fun obtenerUbicacionActual() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    seleccionarUbicacion(latLng)
                } else {
                    Toast.makeText(requireContext(), "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: SecurityException) {
            Toast.makeText(requireContext(), "Error de permisos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarDialogoBuscarDireccion() {
        val input = android.widget.EditText(requireContext()).apply {
            hint = "Ej: Calle Mayor 1, Madrid"
            setPadding(48, 32, 48, 16)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Buscar dirección")
            .setView(input)
            .setPositiveButton("Buscar") { _, _ ->
                val direccion = input.text.toString().trim()
                if (direccion.isNotEmpty()) buscarDireccion(direccion)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun buscarDireccion(direccion: String) {
        try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val resultados = geocoder.getFromLocationName(direccion, 1)

            if (!resultados.isNullOrEmpty()) {
                val resultado = resultados[0]
                val latLng = LatLng(resultado.latitude, resultado.longitude)
                viewModel.direccion = resultado.getAddressLine(0) ?: direccion
                seleccionarUbicacion(latLng)
            } else {
                Toast.makeText(requireContext(), "Dirección no encontrada", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al buscar la dirección", Toast.LENGTH_SHORT).show()
        }
    }

    private fun seleccionarUbicacion(latLng: LatLng) {
        ubicacionSeleccionada = latLng
        moverMapa(latLng)
    }

    private fun moverMapa(latLng: LatLng) {
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(latLng).title("Tu barbería"))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}