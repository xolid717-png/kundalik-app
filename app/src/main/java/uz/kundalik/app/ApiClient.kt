package uz.kundalik.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Backend serveringiz manzili (ngrok tunnel orqali).
 * DIQQAT: ngrok bepul tarifida bu manzil server qayta ishga
 * tushirilganda o'zgarishi mumkin — o'zgarsa shu yerni yangilab,
 * qaytadan GitHub'ga yuklash kerak bo'ladi.
 */
object ApiConfig {
    const val BASE_URL = "https://snooper-tameness-trailing.ngrok-free.dev"
}

class ApiException(message: String) : Exception(message)

object ApiClient {

    private val json = Json { ignoreUnknownKeys = true }

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .build()

    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    private suspend fun post(path: String, bodyJson: String): String =
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(ApiConfig.BASE_URL + path)
                .addHeader("ngrok-skip-browser-warning", "true")
                .post(bodyJson.toRequestBody(jsonMedia))
                .build()
            client.newCall(request).execute().use { resp ->
                val text = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) {
                    throw ApiException("Server xatosi (${resp.code})")
                }
                text
            }
        }

    private suspend fun get(path: String, token: String): String =
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(ApiConfig.BASE_URL + path)
                .addHeader("Authorization", "Bearer $token")
                .addHeader("ngrok-skip-browser-warning", "true")
                .get()
                .build()
            client.newCall(request).execute().use { resp ->
                val text = resp.body?.string().orEmpty()
                if (resp.code == 401) throw ApiException("SESSION_EXPIRED")
                if (!resp.isSuccessful) throw ApiException("Server xatosi (${resp.code})")
                text
            }
        }

    suspend fun login(login: String, password: String): LoginResponse {
        val body = json.encodeToString(LoginRequest.serializer(), LoginRequest(login, password))
        return json.decodeFromString(LoginResponse.serializer(), post("/api/auth/login", body))
    }

    suspend fun verifyCaptcha(sessionId: String, captchaText: String): LoginResponse {
        val body = json.encodeToString(
            CaptchaRequest.serializer(),
            CaptchaRequest(sessionId, captchaText)
        )
        return json.decodeFromString(LoginResponse.serializer(), post("/api/auth/captcha", body))
    }

    suspend fun dailyMarks(token: String): DailyMarksResponse =
        json.decodeFromString(DailyMarksResponse.serializer(), get("/api/marks/daily", token))

    suspend fun schedule(token: String): ScheduleResponse =
        json.decodeFromString(ScheduleResponse.serializer(), get("/api/schedule", token))

    suspend fun periodMarks(token: String): PeriodMarksResponse =
        json.decodeFromString(PeriodMarksResponse.serializer(), get("/api/marks/period", token))
}
