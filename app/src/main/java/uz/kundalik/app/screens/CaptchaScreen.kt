package uz.kundalik.app.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import uz.kundalik.app.ApiClient
import uz.kundalik.app.ApiException

@Composable
fun CaptchaScreen(
    sessionId: String,
    captchaImageBase64: String,
    onLoggedIn: (token: String) -> Unit,
    onBack: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val bitmap = remember(captchaImageBase64) {
        try {
            val bytes = Base64.decode(captchaImageBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Captcha kodini kiriting", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        bitmap?.let {
            Image(bitmap = it.asImageBitmap(), contentDescription = "Captcha rasmi")
            Spacer(Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("Kod") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

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
                        val resp = ApiClient.verifyCaptcha(sessionId, code.trim())
                        loading = false
                        if (resp.status == "logged_in" && resp.token != null) {
                            onLoggedIn(resp.token)
                        } else {
                            error = resp.message ?: "Captcha noto'g'ri, qayta urinib ko'ring."
                        }
                    } catch (e: ApiException) {
                        loading = false
                        error = e.message
                    } catch (e: Exception) {
                        loading = false
                        error = "Tarmoq xatosi."
                    }
                }
            },
            enabled = !loading && code.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Tekshirilmoqda..." else "Tasdiqlash")
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onBack) { Text("Orqaga") }
    }
}
