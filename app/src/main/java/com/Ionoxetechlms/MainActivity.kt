package com.Ionoxetechlms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.Ionoxetechlms.data.pref.UserPreferences
import com.Ionoxetechlms.ui.dashboard.DashboardScreen
import com.Ionoxetechlms.ui.login.LoginScreen
import com.Ionoxetechlms.ui.splash.SplashScreen
import com.Ionoxetechlms.ui.theme.IONOXELMSTheme

enum class AppScreen {
    SPLASH,
    LOGIN,
    MAIN
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            IONOXELMSTheme {
                val context = LocalContext.current
                var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }
                var loggedInStudentId by remember { mutableIntStateOf(UserPreferences.getStudentId(context)) }
                var loggedInStudentName by remember { mutableStateOf(UserPreferences.getStudentName(context)) }

                Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                    when (screen) {
                        AppScreen.SPLASH -> {
                            SplashScreen(
                                onSplashFinished = {
                                    if (UserPreferences.isLoggedIn(context)) {
                                        loggedInStudentId = UserPreferences.getStudentId(context)
                                        loggedInStudentName = UserPreferences.getStudentName(context)
                                        currentScreen = AppScreen.MAIN
                                    } else {
                                        currentScreen = AppScreen.LOGIN
                                    }
                                }
                            )
                        }
                        AppScreen.LOGIN -> {
                            LoginScreen(
                                onLoginSuccess = { studentId, studentName ->
                                    UserPreferences.saveUserSession(context, studentId, studentName)
                                    loggedInStudentId = studentId
                                    loggedInStudentName = studentName
                                    currentScreen = AppScreen.MAIN
                                }
                            )
                        }
                        AppScreen.MAIN -> {
                            DashboardScreen(
                                studentId = loggedInStudentId,
                                studentName = loggedInStudentName,
                                onLogoutClick = {
                                    UserPreferences.clearSession(context)
                                    currentScreen = AppScreen.LOGIN
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
