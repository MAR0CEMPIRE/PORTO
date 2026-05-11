package com.mar0empire.barbershop.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mar0empire.barbershop.models.HorarioDia
import com.mar0empire.barbershop.models.Servicio

class SetUpBarberiaViewModel : ViewModel() {

    var emailRegistro: String = ""
    var passwordRegistro: String = ""
    var nombreUsuario: String = ""

    var nombre: String = ""
    var descripcion: String = ""
    var telefono: String = ""
    var fotoUrl: String = ""

    var latitud: Double = 0.0
    var longitud: Double = 0.0
    var direccion: String = ""

    var horarios: List<HorarioDia> = listOf(
        HorarioDia("Lunes", false),
        HorarioDia("Martes", false),
        HorarioDia("Miércoles", false),
        HorarioDia("Jueves", false),
        HorarioDia("Viernes", false),
        HorarioDia("Sábado", false),
        HorarioDia("Domingo", false)
    )

    val servicios: MutableList<Servicio> = mutableListOf()

    // Evita recargar Firestore si ya se hizo en esta instancia del ViewModel
    var datosFirestoreCargados: Boolean = false

    /**
     * Carga desde Firestore TODOS los datos de la barbería (modo edición).
     * Solo ejecuta la query una vez; las siguientes llamadas invocan onListo() directamente.
     */
    fun cargarDatosFirestoreCompletos(onListo: () -> Unit) {
        if (datosFirestoreCargados) {
            onListo()
            return
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            onListo()
            return
        }

        FirebaseFirestore.getInstance()
            .collection("barberia")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    nombre      = doc.getString("nombre")      ?: ""
                    descripcion = doc.getString("descripcion") ?: ""
                    telefono    = doc.getString("telefono")    ?: ""
                    fotoUrl     = doc.getString("fotoPerfil")  ?: ""
                    direccion   = doc.getString("direccion")   ?: ""
                    latitud     = doc.getDouble("latitud")     ?: 0.0
                    longitud    = doc.getDouble("longitud")    ?: 0.0

                    // Horarios
                    val horariosRaw = doc.get("horarios") as? List<Map<String, Any>>
                    if (!horariosRaw.isNullOrEmpty()) {
                        horarios = horariosRaw.map { h ->
                            HorarioDia(
                                dia          = h["dia"]          as? String  ?: "",
                                abierto      = h["abierto"]      as? Boolean ?: false,
                                horaApertura = h["horaApertura"] as? String  ?: "09:00",
                                horaCierre   = h["horaCierre"]   as? String  ?: "20:00"
                            )
                        }
                    }

                    // Servicios
                    val serviciosRaw = doc.get("servicios") as? List<Map<String, Any>>
                    if (!serviciosRaw.isNullOrEmpty()) {
                        servicios.clear()
                        serviciosRaw.forEach { s ->
                            servicios.add(
                                Servicio(
                                    id              = s["id"]              as? String ?: "",
                                    nombre          = s["nombre"]          as? String ?: "",
                                    precio          = (s["precio"]         as? Number)?.toDouble() ?: 0.0,
                                    duracionMinutos = (s["duracionMinutos"] as? Number)?.toInt()   ?: 30
                                )
                            )
                        }
                    }
                }
                datosFirestoreCargados = true
                onListo()
            }
            .addOnFailureListener {
                datosFirestoreCargados = true
                onListo()
            }
    }
}
