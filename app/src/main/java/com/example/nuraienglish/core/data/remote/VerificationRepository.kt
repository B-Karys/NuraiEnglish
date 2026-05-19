package com.example.nuraienglish.core.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VerificationRepository @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val json = "application/json; charset=utf-8".toMediaType()

    // Change to your machine's IP when testing on a physical device.
    // 10.0.2.2 is the Android emulator's alias for localhost.
    private val baseUrl = "https://nuraienglish.onrender.com"

    suspend fun sendCode(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val body = JSONObject().put("email", email).toString().toRequestBody(json)
            val request = Request.Builder().url("$baseUrl/api/send-code").post(body).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val msg = runCatching {
                        JSONObject(response.body?.string() ?: "").optString("error")
                    }.getOrDefault("Server error ${response.code}")
                    error(msg)
                }
            }
        }
    }

    suspend fun verifyCode(email: String, code: String): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            val body = JSONObject().put("email", email).put("code", code).toString().toRequestBody(json)
            val request = Request.Builder().url("$baseUrl/api/verify-code").post(body).build()
            client.newCall(request).execute().use { response ->
                val text = response.body?.string() ?: "{}"
                val obj = JSONObject(text)
                obj.optBoolean("valid", false)
            }
        }
    }
}
