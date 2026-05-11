package com.mar0empire.barbershop.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.firebase.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream

object CloudinaryHelper {

    private val client = OkHttpClient()

    private val cloudName = "dbxsj3lak"
    private val uploadPreset = "barbershop_preset"

    private val url = "https://api.cloudinary.com/v1_1/$cloudName/image/upload"

    suspend fun subirImagen(context: Context, uri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Comprimir imagen antes de subir
                val bytes = comprimirImagen(context, uri)

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "file",
                        "imagen.jpg",
                        bytes.toRequestBody("image/jpeg".toMediaType())
                    )
                    .addFormDataPart("upload_preset", uploadPreset)
                    .build()

                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()

                if (response.isSuccessful && body != null) {
                    val json = JSONObject(body)
                    val imageUrl = json.getString("secure_url")
                    Result.success(imageUrl)
                } else {
                    Result.failure(Exception("Error al subir imagen: ${response.code}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun comprimirImagen(context: Context, uri: Uri): ByteArray {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)

        // Redimensionar a máximo 800x800
        val scaled = Bitmap.createScaledBitmap(
            bitmap,
            minOf(bitmap.width, 800),
            minOf(bitmap.height, 800),
            true
        )

        val outputStream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return outputStream.toByteArray()
    }
}