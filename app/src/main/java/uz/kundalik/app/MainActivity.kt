package uz.kundalik.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import uz.kundalik.app.screens.CaptchaScreen
import uz.kundalik.app.screens.DashboardScreen
import uz.kundalik.app.screens.LoginScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier) {
                    KundalikApp()
                }
            }
        }
    }
}

@Composable
fun KundalikApp() {
    val navController: NavHostController = rememberNavController()
    val context = LocalContext.current
    val tokenStore = remember { TokenStore(context) }

    var sessionId by remember { mutableStateOf("") }
    var captchaImageBase64 by remember { mutableStateOf("") }

    val startDestination = if (tokenStore.getToken() != null) "dashboard" else "login"

    NavHost(navController = navController, startDestination = startDestination) {

        composable("login") {
            LoginScreen(
                onLoggedIn = { token ->
                    tokenStore.saveToken(token)
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onCaptchaRequired = { sid, imgBase64 ->
                    sessionId = sid
                    captchaImageBase64 = imgBase64
                    navController.navigate("captcha")
                }
            )
        }

        composable("captcha") {
            CaptchaScreen(
                sessionId = sessionId,
                captchaImageBase64 = captchaImageBase64,
                onLoggedIn = { token ->
                    tokenStore.saveToken(token)
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("dashboard") {
            DashboardScreen(
                token = tokenStore.getToken().orEmpty(),
                onLogout = {
                    tokenStore.clear()
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
        }
    }
}
