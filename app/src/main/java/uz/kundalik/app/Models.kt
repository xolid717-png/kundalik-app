package uz.kundalik.app

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val login: String, val password: String)

@Serializable
data class LoginResponse(
    val status: String,
    val token: String? = null,
    val session_id: String? = null,
    val captcha_image: String? = null,
    val message: String? = null
)

@Serializable
data class CaptchaRequest(val session_id: String, val captcha_text: String)

@Serializable
data class DailyEntry(val day: String, val subject: String, val grade: String)

@Serializable
data class DailyMarksResponse(val entries: List<DailyEntry>)

@Serializable
data class ScheduleResponse(val schedule_text: String)

@Serializable
data class SubjectAverage(val subject: String, val average: String)

@Serializable
data class PeriodMarksResponse(val subjects: List<SubjectAverage>)
