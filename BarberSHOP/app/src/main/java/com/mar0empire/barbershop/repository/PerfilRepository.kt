package com.mar0empire.barbershop.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mar0empire.barbershop.models.User

class PerfilRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun getUserData(
        onSuccess: (User) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = getCurrentUserId() ?: return

        db.collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val user = doc.toObject(User::class.java)
                if (user != null) onSuccess(user)
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }
}
