package com.example.applibros.utils

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

fun uriToBase64(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()
        inputStream?.close()

        if (bytes == null || bytes.isEmpty()) {
            Log.e("ImgBB", "Bytes de imagen vacíos o nulos")
            null
        } else {
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            Log.d("ImgBB", "Imagen convertida correctamente (${base64.length} caracteres)")
            base64
        }
    } catch (e: Exception) {
        Log.e("ImgBB", "Error al convertir imagen: ${e.message}")
        null
    }
}




fun uploadToImgBB(
    context: Context,
    imageUri: Uri,
    apiKey: String,
    onSuccess: (String) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val base64Image = uriToBase64(context, imageUri) ?: run {
        Handler(Looper.getMainLooper()).post {
            onFailure(Exception("Error al convertir la imagen a Base64"))
        }
        return
    }

    val url = URL("https://api.imgbb.com/1/upload?key=$apiKey")
    val postData = "image=" + URLEncoder.encode(base64Image, "UTF-8")
    Log.d("ImgBB", "POST a: $url")
    Log.d("ImgBB", "base64 (primeros 100): ${base64Image.take(100)}")
    Log.d("ImgBB", "POST data: image=${base64Image.take(50)}...")

    Thread {
        try {
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

            val out = OutputStreamWriter(conn.outputStream)
            out.write(postData)
            out.flush()
            out.close()

            val response = try {
                conn.inputStream.bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                conn.errorStream?.bufferedReader()?.use { it.readText() } ?: throw e
            }
            Log.e("ImgBB", "Respuesta de ImgBB:\n$response")

            val imageUrl = Regex("\"url\":\"(.*?)\"")
                .find(response)
                ?.groupValues?.get(1)
                ?.replace("\\/", "/")

            if (imageUrl != null) {
                Handler(Looper.getMainLooper()).post {
                    onSuccess(imageUrl)
                }
            } else {
                Handler(Looper.getMainLooper()).post {
                    onFailure(Exception("No se pudo extraer la URL de la respuesta"))
                }
            }

        } catch (e: Exception) {
            Handler(Looper.getMainLooper()).post {
                onFailure(e)
            }
        }
    }.start()
}
