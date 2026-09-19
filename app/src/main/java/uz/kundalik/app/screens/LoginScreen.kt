package uz.kundalik.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import uz.kundalik.app.ApiClient
import uz.kundalik.app.ApiException

@Composable
fun LoginScreen(
    onLoggedIn: (token: String) -> Unit,
    onCaptchaRequired: (sessionId: String, captchaImageBase64: String) -> Unit
) {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Kundalikka kirish", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = login,
            onValueChange = { login = it },
            label = { Text("Login") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Parol") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(20.dp))

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(12.dp))
        }

        Button(
            onClick = {
                error = null
                loading = true
                scope.launch {
                    try {
                        val resp = ApiClient.login(login.trim(), password)
                        loading = false
                        when (resp.status) {
                            "logged_in" -> resp.token?.let(onLoggedIn)
                            "captcha_required" -> {
                                if (resp.session_id != null && resp.captcha_image != null) {
                                    onCaptchaRequired(resp.session_id, resp.captcha_image)
                                } else {
                                    error = "Captcha rasmi olinmadi, qayta urinib ko'ring."
                                }
                            }
                            else -> error = resp.message ?: "Login yoki parol noto'g'ri."
                        }
                    } catch (e: ApiException) {
                        loading = false
                        error = e.message
                    } catch (e: Exception) {
                        loading = false
                        error = "Tarmoq xatosi. Internetni tekshiring."
                    }
                }
            },
            enabled = !loading && login.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Kirilmoqda..." else "Kirish")
        }
    }
}
