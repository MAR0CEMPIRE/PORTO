package com.mar0empire.barbershop.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mar0empire.barbershop.models.Barberia

class BarberiaRepository {

    private val db = FirebaseFirestore.getInstance()

    fun getBarberiasCercanas(
        onSuccess: (List<Barberia>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("barberias")
            .orderBy("distancia", Query.Direction.ASCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { result ->
                onSuccess(result.toObjects(Barberia::class.java))
            }
            .addOnFailureListener { onError(it) }
    }

    fun getBarberiasDestacadas(
        onSuccess: (List<Barberia>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("barberias")
            .whereEqualTo("esDestacada", true)
            .get()
            .addOnSuccessListener { result ->
                onSuccess(result.toObjects(Barberia::class.java))
            }
            .addOnFailureListener { onError(it) }
    }

    fun getBarberiasTop(
        onSuccess: (List<Barberia>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("barberias")
            .orderBy("rating", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { result ->
                onSuccess(result.toObjects(Barberia::class.java))
            }
            .addOnFailureListener { onError(it) }
    }
}
