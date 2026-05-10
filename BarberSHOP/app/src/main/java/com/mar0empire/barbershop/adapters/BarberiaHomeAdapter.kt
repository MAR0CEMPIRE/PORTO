package com.mar0empire.barbershop.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mar0empire.barbershop.R
import com.mar0empire.barbershop.databinding.ItemBarberiaHomeBinding
import com.mar0empire.barbershop.models.Barberia

class BarberiaHomeAdapter(
    private val onClick: ((Barberia) -> Unit)? = null
) : ListAdapter<Barberia, BarberiaHomeAdapter.BarberiaViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<Barberia>() {
        override fun areItemsTheSame(oldItem: Barberia, newItem: Barberia): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Barberia, newItem: Barberia): Boolean =
            oldItem == newItem
    }

    inner class BarberiaViewHolder(private val binding: ItemBarberiaHomeBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(barberia: Barberia) {
            binding.apply {
                txtNombre.text = barberia.nombre
                txtUbicacion.text = barberia.ubicacion
                txtRating.text = barberia.rating.toString()

                Glide.with(itemView.context)
                    .load(barberia.fotoUrl)
                    .placeholder(R.drawable.placeholder_barberia)
                    .error(R.drawable.placeholder_barberia)
                    .into(imgBarberia)

                root.setOnClickListener {
                    onClick?.invoke(barberia)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarberiaViewHolder {
        val binding = ItemBarberiaHomeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BarberiaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BarberiaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
