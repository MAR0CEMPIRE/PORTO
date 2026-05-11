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
    private val onConfirmar:  (CitaBarbero) -> Unit,
    private val onCancelar:   (CitaBarbero) -> Unit,
    private val onCompletar:  (CitaBarbero) -> Unit = {},
    private val onEliminar:   (CitaBarbero) -> Unit = {}
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
        val cita    = getItem(position)
        val context = holder.itemView.context
        val b       = holder.binding

        b.citaBarberoHora.text          = cita.hora
        b.citaBarberoNombreCliente.text = cita.cliente
        b.citaBarberoServicio.text      = cita.servicio
        b.citaBarberoPrecio.text        = "${cita.precio}€"
        b.citaBarberoEstado.text        = cita.estado.replaceFirstChar { it.uppercase() }

        // Color del chip de estado
        val colorEstado = when (cita.estado) {
            "confirmada"  -> ContextCompat.getColor(context, R.color.estado_confirmada)
            "cancelada"   -> ContextCompat.getColor(context, R.color.estado_cancelada)
            "completada"  -> ContextCompat.getColor(context, R.color.accent_green)
            else          -> ContextCompat.getColor(context, R.color.estado_pendiente)
        }
        b.citaBarberoEstado.setBackgroundColor(colorEstado)

        // Grupo principal: Confirmar / Cancelar  ->  solo si PENDIENTE
        b.citaBarberoBotones.visibility =
            if (cita.estado == "pendiente") View.VISIBLE else View.GONE

        // Botón COMPLETADA  ->  solo si CONFIRMADA
        b.btnCompletarCita.visibility =
            if (cita.estado == "confirmada") View.VISIBLE else View.GONE

        // Botón ELIMINAR  ->  solo si CANCELADA
        b.btnEliminarCita.visibility =
            if (cita.estado == "cancelada") View.VISIBLE else View.GONE

        // --- Listeners ---
        b.btnConfirmarCita.setOnClickListener { onConfirmar(cita) }
        b.btnCancelarCita.setOnClickListener  { onCancelar(cita)  }
        b.btnCompletarCita.setOnClickListener { onCompletar(cita) }
        b.btnEliminarCita.setOnClickListener  { onEliminar(cita)  }
    }
}
