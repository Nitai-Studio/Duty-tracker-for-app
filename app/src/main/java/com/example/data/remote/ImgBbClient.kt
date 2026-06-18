package com.example.data.remote

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.ByteArrayOutputStream

object ImgBbClient {
    private val client = OkHttpClient()
    private const val API_KEY = "c762b5315ee9263c36ed04156b0ff758"
    private const val UPLOAD_URL = "https://api.imgbb.com/1/upload"

    suspend fun uploadBitmap(bitmap: Bitmap): String? = withContext(Dispatchers.IO) {
        try {
            // Compress image to JPEG
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos)
            val bytes = baos.toByteArray()
            val base64Image = Base64.encodeToString(bytes, Base64.DEFAULT)

            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("key", API_KEY)
                .addFormDataPart("image", base64Image)
                .build()

            val request = Request.Builder()
                .url(UPLOAD_URL)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val str = response.body?.string() ?: return@use null
                    val json = JSONObject(str)
                    val data = json.getJSONObject("data")
                    return@withContext data.getString("url")
                } else {
                    Log.e("ImgBbClient", "Upload error: ${response.code} ${response.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("ImgBbClient", "Upload exception: ${e.message}", e)
        }
        return@withContext null
    }

    suspend fun uploadBytes(bytes: ByteArray): String? = withContext(Dispatchers.IO) {
        try {
            // Decode and rescale/compress to keep sizing optimal
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            var scale = 1
            while (options.outWidth / scale / 2 >= 512 && options.outHeight / scale / 2 >= 512) {
                scale *= 2
            }
            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = scale }
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions) ?: return@withContext null
            val url = uploadBitmap(bitmap)
            bitmap.recycle()
            return@withContext url
        } catch (e: Exception) {
            Log.e("ImgBbClient", "Upload bytes failed: ${e.message}", e)
        }
        return@withContext null
    }
}
