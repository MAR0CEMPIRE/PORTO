package com.mar0empire.barbershop.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // Importación añadida
import com.mar0empire.barbershop.R
import com.mar0empire.barbershop.databinding.ItemBarberiaHomeBinding
import com.mar0empire.barbershop.models.Barberia

class BarberiaAdapter(private val lista: List<Barberia>) :
    RecyclerView.Adapter<BarberiaAdapter.BarberiaViewHolder>() {

    inner class BarberiaViewHolder(val binding: ItemBarberiaHomeBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarberiaViewHolder {
        val binding = ItemBarberiaHomeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BarberiaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BarberiaViewHolder, position: Int) {
        val item = lista[position]
        holder.binding.apply {
            txtNombre.text = item.nombre
            txtUbicacion.text = item.ubicacion
            txtRating.text = item.rating.toString()

            Glide.with(holder.itemView.context)
                .load(item.fotoUrl)
                .placeholder(R.drawable.placeholder_barberia) // Imagen mientras carga
                .error(R.drawable.placeholder_barberia)       // Imagen si falla
                .into(imgBarberia)
        }
    }

    override fun getItemCount() = lista.size
}