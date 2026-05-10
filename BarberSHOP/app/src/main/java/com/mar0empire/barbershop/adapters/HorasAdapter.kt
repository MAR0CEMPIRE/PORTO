package com.mar0empire.barbershop.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mar0empire.barbershop.R
import com.mar0empire.barbershop.databinding.ItemHoraBinding
import com.mar0empire.barbershop.models.HoraItem

class HorasAdapter(
    private val horas: MutableList<HoraItem>, // ← MutableList
    private val onHoraClick: (HoraItem) -> Unit
) : RecyclerView.Adapter<HorasAdapter.HoraViewHolder>() {

    private var horaSeleccionada: Int = -1

    inner class HoraViewHolder(val binding: ItemHoraBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HoraViewHolder {
        val binding = ItemHoraBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HoraViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HoraViewHolder, position: Int) {
        val item = horas[position]
        val context = holder.itemView.context

        holder.binding.txtHora.text = item.hora

        if (!item.disponible) {
            holder.binding.txtDisponible.text = context.getString(R.string.hora_no_disponible)
            holder.binding.txtDisponible.setTextColor(
                ContextCompat.getColor(context, R.color.accent_red)
            )
            holder.binding.cardHora.isEnabled = false
            holder.binding.cardHora.alpha = 0.5f
        } else {
            holder.binding.txtDisponible.text = context.getString(R.string.hora_disponible)
            holder.binding.txtDisponible.setTextColor(
                ContextCompat.getColor(context, R.color.accent_green)
            )
            holder.binding.cardHora.alpha = 1f
            holder.binding.cardHora.isEnabled = true
        }

        if (horaSeleccionada == position) {
            holder.binding.cardHora.setCardBackgroundColor(
                ContextCompat.getColor(context, R.color.black)
            )
            holder.binding.txtHora.setTextColor(
                ContextCompat.getColor(context, R.color.white)
            )
            holder.binding.txtDisponible.setTextColor(
                ContextCompat.getColor(context, R.color.gray_600)
            )
        } else {
            holder.binding.cardHora.setCardBackgroundColor(
                ContextCompat.getColor(context, R.color.white)
            )
            holder.binding.txtHora.setTextColor(
                ContextCompat.getColor(context, R.color.black)
            )
        }

        holder.binding.cardHora.setOnClickListener {
            if (item.disponible) {
                val anterior = horaSeleccionada
                horaSeleccionada = position
                notifyItemChanged(anterior)
                notifyItemChanged(position)
                onHoraClick(item)
            }
        }
    }

    override fun getItemCount() = horas.size

    // ─── Actualizar horas desde fuera ────────────────────────────────────────
    fun actualizarHoras(nuevasHoras: List<HoraItem>) {
        horas.clear()
        horas.addAll(nuevasHoras)
        horaSeleccionada = -1
        notifyDataSetChanged()
    }
}