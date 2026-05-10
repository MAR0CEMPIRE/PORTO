package com.mar0empire.barbershop.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mar0empire.barbershop.databinding.ItemMensajeEnviadoBinding
import com.mar0empire.barbershop.databinding.ItemMensajeRecibidoBinding
import com.mar0empire.barbershop.models.Mensaje
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MensajeAdapter(
    private val mensajes: MutableList<Mensaje>,
    private val uidActual: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TIPO_ENVIADO = 1
        const val TIPO_RECIBIDO = 2
    }


    inner class EnviadoViewHolder(val binding: ItemMensajeEnviadoBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class RecibidoViewHolder(val binding: ItemMensajeRecibidoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return if (mensajes[position].senderId == uidActual) TIPO_ENVIADO
        else TIPO_RECIBIDO
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TIPO_ENVIADO) {
            EnviadoViewHolder(
                ItemMensajeEnviadoBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        } else {
            RecibidoViewHolder(
                ItemMensajeRecibidoBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val mensaje = mensajes[position]
        val hora = SimpleDateFormat("HH:mm", Locale.getDefault())
            .format(Date(mensaje.timestamp))

        when (holder) {
            is EnviadoViewHolder -> {
                holder.binding.txtMensaje.text = mensaje.texto
                holder.binding.txtHora.text = hora
            }
            is RecibidoViewHolder -> {
                holder.binding.txtMensaje.text = mensaje.texto
                holder.binding.txtHora.text = hora
            }
        }
    }

    override fun getItemCount() = mensajes.size

    fun agregarMensaje(mensaje: Mensaje) {
        mensajes.add(mensaje)
        notifyItemInserted(mensajes.size - 1)
    }
}