package com.mar0empire.barbershop.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.mar0empire.barbershop.R
import com.mar0empire.barbershop.main.activities.SplashActivity

object NotificacionManager {

    private const val CHANNEL_CITAS = "canal_citas"
    private const val CHANNEL_MENSAJES = "canal_mensajes"
    private const val CHANNEL_RECORDATORIOS = "canal_recordatorios"

    fun crearCanales(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_CITAS,
                    "Citas",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notificaciones sobre citas"
                }
            )

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_MENSAJES,
                    "Mensajes",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Nuevos mensajes de chat"
                }
            )

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_RECORDATORIOS,
                    "Recordatorios",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Recordatorios de citas próximas"
                }
            )
        }
    }

    fun mostrar(
        context: Context,
        titulo: String,
        mensaje: String,
        tipo: String,
        id: Int = System.currentTimeMillis().toInt()
    ) {
        val canal = when (tipo) {
            "mensaje" -> CHANNEL_MENSAJES
            "recordatorio" -> CHANNEL_RECORDATORIOS
            else -> CHANNEL_CITAS
        }

        val intent = Intent(context, SplashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificacion = NotificationCompat.Builder(context, canal)
            .setSmallIcon(R.drawable.campana_blanca)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(id, notificacion)
    }
}