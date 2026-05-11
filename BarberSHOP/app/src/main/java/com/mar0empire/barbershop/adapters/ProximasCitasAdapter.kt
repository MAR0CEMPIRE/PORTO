package com.mar0empire.barbershop.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mar0empire.barbershop.R
import com.mar0empire.barbershop.databinding.ItemCitaBarberoBinding
import com.mar0empire.barbershop.models.CitaBarbero

class ProximasCitasAdapter(
    private val onConfirmar: (CitaBarbero) -> Unit,
    private val onCancelar: (CitaBarbero) -> Unit
) : ListAdapter<CitaBarbero, ProximasCitasAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<CitaBarbero>() {
        override fun areItemsTheSame(old: CitaBarbero, new: CitaBarbero) = old.id == new.id
        override fun areContentsTheSame(old: CitaBarbero, new: CitaBarbero) = old == new
    }
) {
    inner class ViewHolder(val binding: ItemCitaBarberoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemCitaBarberoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cita = getItem(position)
        val context = holder.itemView.context

        holder.binding.citaBarberoHora.text = cita.hora
        holder.binding.citaBarberoNombreCliente.text = cita.cliente
        holder.binding.citaBarberoServicio.text = cita.servicio
        holder.binding.citaBarberoPrecio.text = "${cita.precio}€"
        holder.binding.citaBarberoEstado.text = cita.estado.replaceFirstChar { it.uppercase() }

        // Color del estado
        when (cita.estado) {
            "confirmada" -> holder.binding.citaBarberoEstado.setBackgroundColor(
                ContextCompat.getColor(context, R.color.estado_confirmada))
            "cancelada" -> holder.binding.citaBarberoEstado.setBackgroundColor(
                ContextCompat.getColor(context, R.color.estado_cancelada))
            else -> holder.binding.citaBarberoEstado.setBackgroundColor(
                ContextCompat.getColor(context, R.color.estado_pendiente))
        }

        // Mostrar botones solo si está pendiente
        if (cita.estado == "pendiente") {
            holder.binding.citaBarberoBotones.visibility = View.VISIBLE
            holder.binding.btnConfirmarCita.setOnClickListener { onConfirmar(cita) }
            holder.binding.btnCancelarCita.setOnClickListener { onCancelar(cita) }
        } else {
            holder.binding.citaBarberoBotones.visibility = View.GONE
        }
    }
}