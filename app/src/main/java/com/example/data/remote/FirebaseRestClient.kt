package com.example.data.remote

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class FirebaseRestClient {
    private val client = OkHttpClient()
    private val baseDbUrl = "https://duty-tracker-pro-329c4-default-rtdb.firebaseio.com"

    suspend fun syncUserData(uid: String, profileJson: String, settingsJson: String, dutyJson: String, advanceJson: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("name", JSONObject(profileJson).optString("name", "User"))
                put("phone", JSONObject(profileJson).optString("phone", ""))
                put("dialCode", JSONObject(profileJson).optString("dialCode", ""))
                put("country", JSONObject(profileJson).optString("country", ""))
                put("countryCode", JSONObject(profileJson).optString("countryCode", ""))
                put("flag", JSONObject(profileJson).optString("flag", ""))
                put("pin", JSONObject(profileJson).optString("pin", ""))
                put("avatar", JSONObject(profileJson).optString("avatar", ""))
                put("createdAt", JSONObject(profileJson).optString("createdAt", ""))
                put("lang", JSONObject(profileJson).optString("lang", "en"))
                put("theme", JSONObject(profileJson).optString("theme", "dark"))
                if (settingsJson.isNotEmpty()) {
                    put("settings", JSONObject(settingsJson))
                }
                if (dutyJson.isNotEmpty()) {
                    put("attendance", JSONObject(dutyJson))
                }
                if (advanceJson.isNotEmpty()) {
                    put("advances", JSONObject(advanceJson))
                }
            }

            val body = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$baseDbUrl/users/$uid.json")
                .put(body)
                .build()

            client.newCall(request).execute().use { response ->
                Log.d("FirebaseRestClient", "Sync status: ${response.code}")
                return@withContext response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e("FirebaseRestClient", "Sync failed: ${e.message}", e)
            return@withContext false
        }
    }

    suspend fun fetchUserData(uid: String): JSONObject? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseDbUrl/users/$uid.json")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val str = response.body?.string()
                    if (str != null && str != "null") {
                        return@withContext JSONObject(str)
                    }
                }
            }
            return@withContext null
        } catch (e: Exception) {
            Log.e("FirebaseRestClient", "Fetch failed: ${e.message}", e)
            return@withContext null
        }
    }
}
