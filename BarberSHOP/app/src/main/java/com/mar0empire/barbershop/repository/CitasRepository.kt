package com.mar0empire.barbershop.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mar0empire.barbershop.models.Cita

class CitasRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getCitasFuturas(
        onSuccess: (List<Cita>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.w("CitasRepository", "getCitasFuturas: usuario no autenticado aún")
            onError(Exception("Usuario no autenticado"))
            return
        }

        val ahora = System.currentTimeMillis()
        Log.d("CITAS_DEBUG", "Buscando citas futuras para UID: $uid")

        db.collection("citas")
            .whereEqualTo("clienteId", uid)
            .get()
            .addOnSuccessListener { result ->
                Log.d("CITAS_DEBUG", "Docs encontrados: ${result.size()}")
                result.documents.forEach { doc -> Log.d("CITAS_DEBUG", "Doc: ${doc.data}") }

                val citas = result.documents
                    .filter { doc -> (doc.getLong("fechaTimestamp") ?: 0L) >= ahora }
                    .map { doc ->
                        Cita(
                            id = doc.id,
                            clienteId = doc.getString("clienteId") ?: "",
                            barberiaId = doc.getString("barberiaId") ?: "",
                            nombreBarberia = doc.getString("nombreBarberia") ?: "",
                            nombreCliente = doc.getString("nombreCliente") ?: "",
                            ubicacion = doc.getString("ubicacion") ?: "",
                            fechaTimestamp = doc.getLong("fechaTimestamp") ?: 0L,
                            fecha = doc.getString("fecha") ?: "",
                            hora = doc.getString("hora") ?: "",
                            estado = doc.getString("estado") ?: "",
                            fotoBarberia = doc.getString("fotoBarberia") ?: "",
                            servicio = doc.getString("servicio") ?: ""
                        )
                    }
                Log.d("CITAS_DEBUG", "Citas futuras: ${citas.size}")
                onSuccess(citas)
            }
            .addOnFailureListener { e ->
                Log.e("CITAS_DEBUG", "Error: ${e.message}")
                onError(e)
            }
    }

    fun getCitasPasadas(
        onSuccess: (List<Cita>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.w("CitasRepository", "getCitasPasadas: usuario no autenticado aún")
            onError(Exception("Usuario no autenticado"))
            return
        }

        val ahora = System.currentTimeMillis()

        db.collection("citas")
            .whereEqualTo("clienteId", uid)
            .get()
            .addOnSuccessListener { result ->
                val citas = result.documents
                    .filter { doc -> (doc.getLong("fechaTimestamp") ?: 0L) < ahora }
                    .map { doc ->
                        Cita(
                            id = doc.id,
                            clienteId = doc.getString("clienteId") ?: "",
                            barberiaId = doc.getString("barberiaId") ?: "",
                            nombreBarberia = doc.getString("nombreBarberia") ?: "",
                            nombreCliente = doc.getString("nombreCliente") ?: "",
                            ubicacion = doc.getString("ubicacion") ?: "",
                            fechaTimestamp = doc.getLong("fechaTimestamp") ?: 0L,
                            fecha = doc.getString("fecha") ?: "",
                            hora = doc.getString("hora") ?: "",
                            estado = doc.getString("estado") ?: "",
                            fotoBarberia = doc.getString("fotoBarberia") ?: "",
                            servicio = doc.getString("servicio") ?: ""
                        )
                    }
                Log.d("CITAS_DEBUG", "Citas pasadas: ${citas.size}")
                onSuccess(citas)
            }
            .addOnFailureListener { e ->
                Log.e("CITAS_DEBUG", "Error: ${e.message}")
                onError(e)
            }
    }
}