package com.mar0empire.barbershop.utils

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

object NotificacionService {

    private var listener: ListenerRegistration? = null

    fun iniciar(context: Context) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        listener = FirebaseFirestore.getInstance()
            .collection("notificaciones")
            .document(uid)
            .collection("items")
            .whereEqualTo("leida", false)
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener

                snapshots.documentChanges.forEach { cambio ->
                    // Solo procesar documentos nuevos, no modificaciones
                    if (cambio.type != DocumentChange.Type.ADDED) return@forEach

                    val doc = cambio.document

                    NotificacionManager.mostrar(
                        context = context,
                        titulo = doc.getString("titulo") ?: "",
                        mensaje = doc.getString("mensaje") ?: "",
                        tipo = doc.getString("tipo") ?: "cita",
                        id = doc.id.hashCode()
                    )

                    // Marcar como leída para que no vuelva a aparecer
                    FirebaseFirestore.getInstance()
                        .collection("notificaciones")
                        .document(uid)
                        .collection("items")
                        .document(doc.id)
                        .update("leida", true)
                }
            }
    }
    fun detener() {
        listener?.remove()
        listener = null
    }
}