package com.mar0empire.barbershop.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Usuario actual
    val usuarioActual: FirebaseUser?
        get() = auth.currentUser

    // ¿Hay sesión activa?
    val haySesion: Boolean
        get() = auth.currentUser != null

    // Login
    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val resultado = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(resultado.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Registro
    suspend fun registrar(email: String, password: String, nombre: String, rol: String): Result<FirebaseUser> {
        return try {
            val resultado = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = resultado.user!!.uid

            // Guardar en Firestore
            db.collection("users").document(uid).set(
                hashMapOf(
                    "nombre" to nombre,
                    "email" to email,
                    "rol" to rol
                )
            ).await()

            Result.success(resultado.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener rol del usuario
    suspend fun obtenerRol(uid: String): String? {
        return try {
            val doc = db.collection("users").document(uid).get().await()
            doc.getString("rol")
        } catch (e: Exception) {
            null
        }
    }

    // Cerrar sesión
    fun cerrarSesion() {
        auth.signOut()
    }

    // Recuperar contraseña
    suspend fun recuperarPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}