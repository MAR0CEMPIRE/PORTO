package com.mar0empire.barbershop.main.fragments.barberia

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mar0empire.barbershop.R
import com.mar0empire.barbershop.auth.LoginActivity
import com.mar0empire.barbershop.databinding.FragmentPerfilBarberoBinding
import com.mar0empire.barbershop.main.activities.CompletarBarberiaActivity

class PerfilBarberoFragment : Fragment() {

    private var _binding: FragmentPerfilBarberoBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBarberoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cargarDatosBarberia()
        initListeners()
    }


    private fun cargarDatosBarberia() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("barberia").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    binding.txtNombreBarbero.text = doc.getString("nombre") ?: ""
                    binding.txtDireccionBarbero.text = doc.getString("direccion") ?: ""
                    binding.txtDescripcionBarbero.text = doc.getString("descripcion") ?: ""

                    val foto = doc.getString("fotoPerfil")
                    if (!foto.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(foto)
                            .circleCrop()
                            .placeholder(R.drawable.avatar_perfil)
                            .into(binding.imgPerfilBarbero)
                    }

                    val servicios = doc.get("servicios") as? List<*>
                    binding.txtServiciosCount.text = servicios?.size?.toString() ?: "0"
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al cargar datos", Toast.LENGTH_SHORT).show()
            }

        // Contar citas totales
        db.collection("citas")
            .whereEqualTo("barberiaId", uid)
            .get()
            .addOnSuccessListener { docs ->
                binding.txtTotalCitas.text = docs.size().toString()
            }
    }



    private fun initListeners() {

        // Botón editar del header
        binding.btnEditarBarberia.setOnClickListener {
            startActivity(
                Intent(requireContext(), CompletarBarberiaActivity::class.java).apply {
                    putExtra(CompletarBarberiaActivity.EXTRA_PASO, CompletarBarberiaActivity.PASO_DATOS)
                }
            )
        }

        // Editar datos básicos
        binding.btnEditarPerfilBarbero.setOnClickListener {
            startActivity(
                Intent(requireContext(), CompletarBarberiaActivity::class.java).apply {
                    putExtra(CompletarBarberiaActivity.EXTRA_PASO, CompletarBarberiaActivity.PASO_DATOS)
                }
            )
        }

        // Gestionar horarios
        binding.btnHorarios.setOnClickListener {
            startActivity(
                Intent(requireContext(), CompletarBarberiaActivity::class.java).apply {
                    putExtra(CompletarBarberiaActivity.EXTRA_PASO, CompletarBarberiaActivity.PASO_HORARIOS)
                }
            )
        }

        // Gestionar servicios
        binding.btnServiciosPerfil.setOnClickListener {
            startActivity(
                Intent(requireContext(), CompletarBarberiaActivity::class.java).apply {
                    putExtra(CompletarBarberiaActivity.EXTRA_PASO, CompletarBarberiaActivity.PASO_SERVICIOS)
                }
            )
        }

        // Cambiar contraseña
        binding.btnCambiarPasswordBarbero.setOnClickListener {
            val email = auth.currentUser?.email ?: return@setOnClickListener
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(
                        requireContext(),
                        "Email enviado a $email",
                        Toast.LENGTH_LONG
                    ).show()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        requireContext(),
                        "Error al enviar el email",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        // Cerrar sesión
        binding.btnLogoutBarbero.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro de que quieres cerrar sesión?")
                .setPositiveButton("Cerrar sesión") { _, _ ->
                    auth.signOut()
                    startActivity(
                        Intent(requireContext(), LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                    )
                }
                .setNegativeButton("Cancelar", null)
                .show()


        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}