package com.mar0empire.barbershop.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.mar0empire.barbershop.R
import com.mar0empire.barbershop.databinding.ActivityLoginBinding
import com.mar0empire.barbershop.main.activities.CompletarBarberiaActivity
import com.mar0empire.barbershop.main.activities.MainBarberiaActivity
import com.mar0empire.barbershop.main.activities.MainClienteActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val googleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(this, gso)
    }

    private val googleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Toast.makeText(this, "Error con Google: ${e.message}", Toast.LENGTH_SHORT).show()
            binding.loginGoogle.isEnabled = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()

        initListeners()
    }

    private fun initListeners() {

        // Iniciar sesión con email
        binding.btnLogin.setOnClickListener {
            val email    = binding.txtEmail.text.toString().trim()
            val password = binding.etPassword.editText?.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            loginUser(email, password)
        }

        // Iniciar sesión con Google
        binding.loginGoogle.setOnClickListener {
            binding.loginGoogle.isEnabled = false
            // signOut previo para que siempre muestre el picker de cuentas
            googleSignInClient.signOut().addOnCompleteListener {
                googleLauncher.launch(googleSignInClient.signInIntent)
            }
        }

        // Registrarse
        binding.tvregistrarse.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Recuperar contraseña
        binding.tvRecover.setOnClickListener {
            startActivity(Intent(this, RecoverActivity::class.java))
        }
    }


    private fun loginUser(email: String, password: String) {
        binding.btnLogin.isEnabled = false

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                leerRolYRedirigir(uid, onError = { binding.btnLogin.isEnabled = true })
            }
            .addOnFailureListener {
                Toast.makeText(this, "Email o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                binding.btnLogin.isEnabled = true
            }
    }


    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val uid   = result.user?.uid ?: return@addOnSuccessListener
                val isNew = result.additionalUserInfo?.isNewUser ?: false

                if (isNew) {
                    // Primera vez con Google → crear usuario como cliente
                    db.collection("users").document(uid).set(
                        hashMapOf(
                            "nombre" to (result.user?.displayName ?: ""),
                            "email"  to (result.user?.email       ?: ""),
                            "rol"    to "cliente"
                        )
                    ).addOnSuccessListener {
                        irA(MainClienteActivity::class.java)
                    }.addOnFailureListener {
                        Toast.makeText(this, "Error al guardar usuario", Toast.LENGTH_SHORT).show()
                        binding.loginGoogle.isEnabled = true
                    }
                } else {
                    // Ya tiene cuenta → leer rol y redirigir igual que email
                    leerRolYRedirigir(uid, onError = { binding.loginGoogle.isEnabled = true })
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show()
                binding.loginGoogle.isEnabled = true
            }
    }

    // ── Lógica compartida de redirección ────────────────────────────────────

    private fun leerRolYRedirigir(uid: String, onError: () -> Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                    onError()
                    return@addOnSuccessListener
                }

                when (doc.getString("rol")) {
                    "cliente"  -> irA(MainClienteActivity::class.java)
                    "barberia" -> verificarBarberia(uid, onError)
                    else       -> { Toast.makeText(this, "Rol no válido", Toast.LENGTH_SHORT).show(); onError() }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al obtener datos", Toast.LENGTH_SHORT).show()
                onError()
            }
    }

    private fun verificarBarberia(uid: String, onError: () -> Unit) {
        db.collection("barberia").document(uid).get()
            .addOnSuccessListener { doc ->
                when {
                    !doc.exists()                          -> irA(CompletarBarberiaActivity::class.java)
                    doc.getBoolean("configurado") != true  -> irA(CompletarBarberiaActivity::class.java)
                    else                                   -> irA(MainBarberiaActivity::class.java)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al verificar barbería", Toast.LENGTH_SHORT).show()
                onError()
            }
    }

    private fun irA(destino: Class<*>) {
        startActivity(
            Intent(this, destino).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }
}