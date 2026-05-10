package com.mar0empire.barbershop.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mar0empire.barbershop.models.Cita

class CitasRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getCitasFuturas(
        onSuccess: (List<Cita>) -> Unit,
        onError: (Exception) -> Unit
    ){
        val uid = auth.currentUser?.uid ?: return
        db.collection("citas")
            .whereEqualTo("idCliente", uid)
            .whereGreaterThan("fecha", System.currentTimeMillis())
            .orderBy("fecha", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { result ->
                val citas = result.toObjects(Cita::class.java)
                onSuccess(citas)
            }
            .addOnFailureListener { e -> onError(e) }
    }
    fun getCitasPasadas(
        onSuccess: (List<Cita>) -> Unit,
        onError: (Exception) -> Unit
    ){
        val uid = auth.currentUser?.uid ?: return
        db.collection("citas")
            .whereEqualTo("idCliente", uid)
            .whereLessThan("fecha", System.currentTimeMillis())
            .orderBy("fecha", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { result ->
                val citas = result.toObjects(Cita::class.java)
                onSuccess(citas)
            }
            .addOnFailureListener { e -> onError(e) }
    }
}