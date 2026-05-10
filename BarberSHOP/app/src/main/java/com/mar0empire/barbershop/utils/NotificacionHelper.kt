package com.mar0empire.barbershop.utils

import com.google.firebase.firestore.FirebaseFirestore

object NotificacionHelper {

    private val db = FirebaseFirestore.getInstance()

    fun nuevaCitaSolicitada(uidBarbero: String, nombreCliente: String, fecha: String, citaId: String) {
        enviar(
            uid = uidBarbero,
            titulo = "Nueva cita solicitada",
            mensaje = "$nombreCliente quiere una cita el $fecha",
            tipo = "nueva_cita",
            datos = mapOf("citaId" to citaId)
        )
    }

    fun citaConfirmada(uidCliente: String, nombreBarberia: String, fecha: String, citaId: String) {
        enviar(
            uid = uidCliente,
            titulo = "¡Cita confirmada!",
            mensaje = "$nombreBarberia ha confirmado tu cita el $fecha",
            tipo = "confirmada",
            datos = mapOf("citaId" to citaId)
        )
    }

    fun citaCancelada(uid: String, nombre: String, fecha: String, citaId: String) {
        enviar(
            uid = uid,
            titulo = "Cita cancelada",
            mensaje = "La cita del $fecha con $nombre ha sido cancelada",
            tipo = "cancelada",
            datos = mapOf("citaId" to citaId)
        )
    }

    fun nuevoMensaje(uidDestino: String, nombreRemitente: String, preview: String, chatId: String) {
        enviar(
            uid = uidDestino,
            titulo = nombreRemitente,
            mensaje = preview,
            tipo = "mensaje",
            datos = mapOf("chatId" to chatId)
        )
    }

    fun recordatorioCita(uidCliente: String, nombreBarberia: String, fecha: String, citaId: String) {
        enviar(
            uid = uidCliente,
            titulo = "Recordatorio de cita",
            mensaje = "Mañana tienes cita en $nombreBarberia a las $fecha",
            tipo = "recordatorio",
            datos = mapOf("citaId" to citaId)
        )
    }

    private fun enviar(
        uid: String,
        titulo: String,
        mensaje: String,
        tipo: String,
        datos: Map<String, String> = emptyMap()
    ) {
        val notif = hashMapOf(
            "titulo" to titulo,
            "mensaje" to mensaje,
            "tipo" to tipo,
            "leida" to false,
            "fecha" to System.currentTimeMillis(),
            "datos" to datos
        )

        db.collection("notificaciones")
            .document(uid)
            .collection("items")
            .add(notif)
    }
}