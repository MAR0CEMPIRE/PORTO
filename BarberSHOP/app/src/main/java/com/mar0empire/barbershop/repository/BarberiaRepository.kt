package com.mar0empire.barbershop.repository

import android.location.Location
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mar0empire.barbershop.models.Barberia

class BarberiaRepository {

    private val db = FirebaseFirestore.getInstance()

    fun getBarberiasCercanas(
        latitudUsuario: Double,
        longitudUsuario: Double,
        onSuccess: (List<Barberia>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("barberia")
            .get()
            .addOnSuccessListener { result ->
                val barberias = result.documents.mapNotNull { doc ->
                    val lat = doc.getDouble("latitud") ?: return@mapNotNull null
                    val lon = doc.getDouble("longitud") ?: return@mapNotNull null
                    val nombre = doc.getString("nombre") ?: return@mapNotNull null

                    val distancia = calcularDistancia(latitudUsuario, longitudUsuario, lat, lon)
                    val serviciosRaw = doc.get("servicios") as? List<Map<String, Any>> ?: emptyList()
                    val nombresServicios = serviciosRaw.mapNotNull { it["nombre"] as? String }

                    Barberia(
                        id = doc.id,
                        nombre = nombre,
                        direccion = doc.getString("direccion") ?: "",
                        ubicacion = doc.getString("direccion") ?: "",
                        fotoPerfil = doc.getString("fotoPerfil") ?: "",
                        fotoUrl = doc.getString("fotoPerfil") ?: "",
                        rating = doc.getDouble("rating") ?: 0.0,
                        latitud = lat,
                        longitud = lon,
                        distancia = distancia,
                        servicios = nombresServicios
                    )
                }
                onSuccess(barberias.sortedBy { it.distancia }.take(10))
            }
            .addOnFailureListener { onError(it) }
    }

    fun getBarberiasDestacadas(
        onSuccess: (List<Barberia>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("barberia")
            .get()
            .addOnSuccessListener { result ->
                val barberias = result.documents.mapNotNull { doc ->
                    val serviciosRaw = doc.get("servicios") as? List<Map<String, Any>> ?: emptyList()
                    val nombresServicios = serviciosRaw.mapNotNull { it["nombre"] as? String }

                    Barberia(
                        id = doc.id,
                        nombre = doc.getString("nombre") ?: return@mapNotNull null,
                        direccion = doc.getString("direccion") ?: "",
                        ubicacion = doc.getString("direccion") ?: "",
                        fotoPerfil = doc.getString("fotoPerfil") ?: "",
                        fotoUrl = doc.getString("fotoPerfil") ?: "",
                        rating = doc.getDouble("rating") ?: 0.0,
                        latitud = doc.getDouble("latitud") ?: 0.0,
                        longitud = doc.getDouble("longitud") ?: 0.0,
                        servicios = nombresServicios
                    )
                }
                onSuccess(barberias)
            }
            .addOnFailureListener { onError(it) }
    }

    fun getBarberiasTop(
        onSuccess: (List<Barberia>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("barberia")
            .orderBy("rating", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { result ->
                val barberias = result.documents.mapNotNull { doc ->
                    val serviciosRaw = doc.get("servicios") as? List<Map<String, Any>> ?: emptyList()
                    val nombresServicios = serviciosRaw.mapNotNull { it["nombre"] as? String }

                    Barberia(
                        id = doc.id,
                        nombre = doc.getString("nombre") ?: return@mapNotNull null,
                        direccion = doc.getString("direccion") ?: "",
                        ubicacion = doc.getString("direccion") ?: "",
                        fotoPerfil = doc.getString("fotoPerfil") ?: "",
                        fotoUrl = doc.getString("fotoPerfil") ?: "",
                        rating = doc.getDouble("rating") ?: 0.0,
                        latitud = doc.getDouble("latitud") ?: 0.0,
                        longitud = doc.getDouble("longitud") ?: 0.0,
                        servicios = nombresServicios
                    )
                }
                onSuccess(barberias)
            }
            .addOnFailureListener { onError(it) }
    }

    private fun calcularDistancia(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val resultado = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, resultado)
        return (resultado[0] / 1000.0)
    }
}