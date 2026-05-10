package com.mar0empire.barbershop.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mar0empire.barbershop.R
import com.mar0empire.barbershop.models.CitaBarbero

class ProximasCitasAdapter :
    androidx.recyclerview.widget.ListAdapter<CitaBarbero, ProximasCitasAdapter.ViewHolder>(
        object : androidx.recyclerview.widget.DiffUtil.ItemCallback<CitaBarbero>() {
            override fun areItemsTheSame(old: CitaBarbero, new: CitaBarbero) = old.hora == new.hora
            override fun areContentsTheSame(old: CitaBarbero, new: CitaBarbero) = old == new
        }
    ) {

    inner class ViewHolder(val view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val txtCliente = view.findViewById<TextView>(R.id.txtCliente)
        val txtHora = view.findViewById<TextView>(R.id.txtHora)
        val txtServicio = view.findViewById<TextView>(R.id.txtServicio)
        val txtPrecio = view.findViewById<TextView>(R.id.txtPrecio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cita_barbero, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cita = getItem(position)
        holder.txtCliente.text = cita.cliente
        holder.txtHora.text = cita.hora
        holder.txtServicio.text = cita.servicio
        holder.txtPrecio.text = "${cita.precio}€"
    }
}
