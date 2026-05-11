package com.mar0empire.barbershop.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mar0empire.barbershop.R
import com.mar0empire.barbershop.databinding.ItemProximaCitaBinding
import com.mar0empire.barbershop.models.Cita

class CitasClienteAdapter(
    private val onClick: (Cita) -> Unit
) : ListAdapter<Cita, CitasClienteAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<Cita>() {
        override fun areItemsTheSame(old: Cita, new: Cita) = old.id == new.id
        override fun areContentsTheSame(old: Cita, new: Cita) = old == new
    }
) {
    inner class ViewHolder(val binding: ItemProximaCitaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemProximaCitaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cita = getItem(position)

        holder.binding.citaClienteNombreBarberia.text = cita.nombreBarberia
        holder.binding.citaClienteServicio.text = cita.servicio
        holder.binding.citaClienteFechaHora.text = "${cita.fecha} · ${cita.hora}"

        // Color del estado
        val color = when (cita.estado) {
            "confirmada" -> ContextCompat.getColor(holder.itemView.context, R.color.accent_green)
            "cancelada" -> ContextCompat.getColor(holder.itemView.context, R.color.accent_red)
            else -> ContextCompat.getColor(holder.itemView.context, R.color.estado_pendiente)
        }
        holder.binding.citaClienteEstado.text = cita.estado.replaceFirstChar { it.uppercase() }
        holder.binding.citaClienteEstado.setBackgroundColor(color)

        holder.itemView.setOnClickListener { onClick(cita) }
    }
}