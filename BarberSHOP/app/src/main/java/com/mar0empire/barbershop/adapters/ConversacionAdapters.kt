package com.mar0empire.barbershop.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mar0empire.barbershop.R
import com.mar0empire.barbershop.databinding.ItemConversacionBinding
import com.mar0empire.barbershop.models.Conversacion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConversacionAdapters(
    private val onClick: (Conversacion) -> Unit
) : RecyclerView.Adapter<ConversacionAdapters.ViewHolder>() {

    private val conversaciones = mutableListOf<Conversacion>()

    inner class ViewHolder(val binding: ItemConversacionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemConversacionBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val conv = conversaciones[position]

        holder.binding.txtNombre.text = conv.nombreDestino
        holder.binding.txtUltimoMensaje.text = conv.ultimoMensaje

        // Hora
        if (conv.timestamp > 0) {
            holder.binding.txtHora.text = SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(Date(conv.timestamp))
        }

        // Foto
        if (conv.fotoDestino.isNotEmpty()) {
            Glide.with(holder.itemView)
                .load(conv.fotoDestino)
                .circleCrop()
                .placeholder(R.drawable.avatar_perfil)
                .into(holder.binding.imgAvatar)
        }

        // Badge no leídos
        if (conv.noLeidos > 0) {
            holder.binding.badgeNoLeidos.visibility = View.VISIBLE
            holder.binding.badgeNoLeidos.text = conv.noLeidos.toString()
        } else {
            holder.binding.badgeNoLeidos.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onClick(conv) }
    }

    override fun getItemCount() = conversaciones.size

    fun actualizar(nuevas: List<Conversacion>) {
        conversaciones.clear()
        conversaciones.addAll(nuevas)
        notifyDataSetChanged()
    }
}