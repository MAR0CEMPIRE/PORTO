package com.mar0empire.barbershop.main.fragments.barberia.setup

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.switchmaterial.SwitchMaterial
import com.mar0empire.barbershop.R
import com.mar0empire.barbershop.databinding.FragmentBarberiaHorariosBinding
import com.mar0empire.barbershop.models.HorarioDia
import com.mar0empire.barbershop.viewmodel.SetUpBarberiaViewModel

class HorariosFragment : Fragment() {

    private var _binding: FragmentBarberiaHorariosBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SetUpBarberiaViewModel by activityViewModels()

    private val horariosActuales = mutableListOf<HorarioDia>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBarberiaHorariosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        horariosActuales.addAll(viewModel.horarios)
        generarFilasHorario()
        initListeners()
    }

    private fun generarFilasHorario() {
        binding.containerHorarios.removeAllViews()

        horariosActuales.forEachIndexed { index, horario ->
            val fila = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_horario_dia, binding.containerHorarios, false)

            val txtDia = fila.findViewById<TextView>(R.id.txtDia)
            val switchAbierto = fila.findViewById<SwitchMaterial>(R.id.switchAbierto)
            val layoutHoras = fila.findViewById<LinearLayout>(R.id.layoutHoras)
            val txtApertura = fila.findViewById<TextView>(R.id.txtHoraApertura)
            val txtCierre = fila.findViewById<TextView>(R.id.txtHoraCierre)

            txtDia.text = horario.dia
            switchAbierto.isChecked = horario.abierto
            txtApertura.text = horario.horaApertura
            txtCierre.text = horario.horaCierre
            layoutHoras.visibility = if (horario.abierto) View.VISIBLE else View.GONE

            switchAbierto.setOnCheckedChangeListener { _, isChecked ->
                horariosActuales[index] = horariosActuales[index].copy(abierto = isChecked)
                layoutHoras.visibility = if (isChecked) View.VISIBLE else View.GONE
            }

            txtApertura.setOnClickListener {
                mostrarTimePicker(horario.horaApertura) { hora ->
                    txtApertura.text = hora
                    horariosActuales[index] = horariosActuales[index].copy(horaApertura = hora)
                }
            }

            txtCierre.setOnClickListener {
                mostrarTimePicker(horario.horaCierre) { hora ->
                    txtCierre.text = hora
                    horariosActuales[index] = horariosActuales[index].copy(horaCierre = hora)
                }
            }

            binding.containerHorarios.addView(fila)
        }
    }

    private fun mostrarTimePicker(horaActual: String, callback: (String) -> Unit) {
        val partes = horaActual.split(":")
        val hora = partes[0].toInt()
        val minuto = partes[1].toInt()

        TimePickerDialog(requireContext(), { _, h, m ->
            callback(String.format("%02d:%02d", h, m))
        }, hora, minuto, true).show()
    }

    private fun initListeners() {
        binding.btnSiguiente.setOnClickListener {
            viewModel.horarios = horariosActuales.toList()
            findNavController().navigate(R.id.action_horarios_to_servicios)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}