package com.mar0empire.barbershop.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mar0empire.barbershop.databinding.ItemBarberiaTarjetaBinding
import com.mar0empire.barbershop.models.Barberia

class BarberiaAdapter(private val lista: List<Barberia>) :
    RecyclerView.Adapter<BarberiaAdapter.BarberiaViewHolder>() {

    inner class BarberiaViewHolder(val binding: ItemBarberiaTarjetaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarberiaViewHolder {
        val binding = ItemBarberiaTarjetaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BarberiaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BarberiaViewHolder, position: Int) {
        val item = lista[position]
        holder.binding.txtNombre.text = item.nombre
        holder.binding.txtUbicacion.text = item.ubicacion
        holder.binding.txtRating.text = "${item.rating}"
        holder.binding.imgBarberia.setImageResource(item.imagen)
    }

    override fun getItemCount() = lista.size
}
