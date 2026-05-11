package com.mar0empire.barbershop.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.mar0empire.barbershop.R
import com.mar0empire.barbershop.databinding.ItemBarberiaHomeBinding
import com.mar0empire.barbershop.models.Barberia

class BarberiaHomeAdapter(
    private val onClick: ((Barberia) -> Unit)? = null
) : ListAdapter<Barberia, BarberiaHomeAdapter.BarberiaViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<Barberia>() {
        override fun areItemsTheSame(oldItem: Barberia, newItem: Barberia) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Barberia, newItem: Barberia) =
            oldItem == newItem
    }

    inner class BarberiaViewHolder(private val binding: ItemBarberiaHomeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(barberia: Barberia) {
            binding.txtNombre.text = barberia.nombre
            binding.txtUbicacion.text = barberia.direccion.ifEmpty { barberia.ubicacion }

            // Rating
            binding.txtRating.text = if (barberia.rating > 0)
                String.format("%.1f", barberia.rating)
            else "Nuevo"

            // Foto
            val foto = barberia.fotoPerfil.ifEmpty { barberia.fotoUrl }
            if (foto.isNotEmpty()) {
                Glide.with(binding.root)
                    .load(foto)
                    .placeholder(R.drawable.placeholder_barberia)
                    .centerCrop()
                    .into(binding.imgBarberia)
            }

            // Chips de servicios
            binding.chipGroupEstilos.removeAllViews()
            barberia.servicios.take(3).forEach { nombreServicio ->
                val chip = Chip(binding.root.context).apply {
                    text = nombreServicio
                    isCheckable = false
                    setChipBackgroundColorResource(android.R.color.transparent)
                }
                binding.chipGroupEstilos.addView(chip)
            }

            binding.root.setOnClickListener { onClick?.invoke(barberia) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = BarberiaViewHolder(
        ItemBarberiaHomeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: BarberiaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}