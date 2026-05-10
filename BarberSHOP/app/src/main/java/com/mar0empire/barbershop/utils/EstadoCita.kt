package com.mar0empire.barbershop.utils

import android.content.Context
import android.widget.TextView
import com.mar0empire.barbershop.R

object EstadoCita {

    fun aplicarTintEstado(textView: TextView, estado: String, context: Context) {

        val color = when (estado.lowercase()) {
            "confirmada" -> context.getColor(R.color.estado_confirmada)
            "pendiente" -> context.getColor(R.color.estado_pendiente)
            "cancelada" -> context.getColor(R.color.estado_cancelada)
            "completada" -> context.getColor(R.color.estado_completada)
            else -> context.getColor(R.color.gray_600)
        }

        textView.background.setTint(color)
        textView.text = estado.replaceFirstChar { it.uppercase() }
    }
}
