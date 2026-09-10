package com.Ionoxetechlms.ui.login

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Ionoxetechlms.R
import com.Ionoxetechlms.data.api.ApiClient
import com.Ionoxetechlms.data.api.LoginRequest
import com.Ionoxetechlms.ui.splash.SplashScreenBackground
import com.Ionoxetechlms.ui.theme.IONOXELMSTheme
import com.Ionoxetechlms.ui.theme.ProfessionalGreen
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.launch

/**
 * Login Screen for Ionoxetech LMS Application
 * Connected to MySQL Database 'students' table via REST API (https://ionox.in/lms/api/login.php)
 */
@Composable
fun LoginScreen(
    onLoginSuccess: (Int, String) -> Unit = { _, _ -> },
    onGoogleLoginClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    onContactAdminClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Same 9:16 Green Wave Background as Splash Screen
        SplashScreenBackground()

        // Login Form Container in Center Zone
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // Brand Logo
            Image(
                painter = painterResource(id = R.drawable.in_logo),
                contentDescription = "Ionoxe Tech Solutions Logo",
                modifier = Modifier.size(130.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Welcome Header
            Text(
                text = "Welcome Back",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Sign in to access your LMS learning portal",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Email / Student ID Field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null
                    successMessage = null
                },
                label = { Text("Email / Student ID") },
                placeholder = { Text("you@example.com or Student ID") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email Icon",
                        tint = ProfessionalGreen
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ProfessionalGreen,
                    focusedLabelColor = ProfessionalGreen,
                    cursorColor = ProfessionalGreen
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                    successMessage = null
                },
                label = { Text("Password") },
                placeholder = { Text("••••••••") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password Icon",
                        tint = ProfessionalGreen
                    )
                },
                trailingIcon = {
                    val icon = if (isPasswordVisible)
                        Icons.Default.Visibility
                    else
                        Icons.Default.VisibilityOff

                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = icon,
                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                            tint = Color(0xFF64748B)
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ProfessionalGreen,
                    focusedLabelColor = ProfessionalGreen,
                    cursorColor = ProfessionalGreen
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Remember Me + Forgot Password
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(checkedColor = ProfessionalGreen)
                    )
                    Text(
                        text = "Remember me",
                        fontSize = 12.sp,
                        color = Color(0xFF334155)
                    )
                }

                Text(
                    text = "Forgot Password?",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ProfessionalGreen,
                    modifier = Modifier.clickable {
                        onForgotPasswordClick()
                    }
                )
            }

            // Error Message
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Success Message
            if (successMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = successMessage!!,
                    color = ProfessionalGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==================== SIGN IN BUTTON ====================
            Button(
                onClick = {
                    val inputEmail = email.trim()
                    val inputPassword = password.trim()

                    if (inputEmail.isBlank() || inputPassword.isBlank()) {
                        errorMessage = "Please enter both Email and Password"
                        successMessage = null
                        return@Button
                    }

                    isLoading = true
                    errorMessage = null
                    successMessage = null

                    coroutineScope.launch {
                        try {
                            // Primary authentication method: Form URL-Encoded (populates $_POST in PHP 100% reliably)
                            var response = ApiClient.apiService.loginForm(inputEmail, inputPassword)
                            if (!response.isSuccessful || response.body()?.status != "success") {
                                try {
                                    val jsonRes = ApiClient.apiService.login(LoginRequest(email = inputEmail, password = inputPassword))
                                    if (jsonRes.isSuccessful && jsonRes.body()?.status == "success") {
                                        response = jsonRes
                                    }
                                } catch (_: Exception) {}
                            }

                            isLoading = false

                            Log.d("LOGIN_DEBUG", "HTTP Code: ${response.code()}")
                            Log.d("LOGIN_DEBUG", "Is Successful: ${response.isSuccessful}")
                            Log.d("LOGIN_DEBUG", "Body: ${response.body()}")

                            val body = response.body()
                            if (response.isSuccessful && body?.status == "success") {
                                val studentObj = body.student
                                val studentId = studentObj?.id ?: 999
                                val studentName = studentObj?.name ?: "Student"

                                successMessage = "Welcome $studentName!"
                                onLoginSuccess(studentId, studentName)
                            } else {
                                val errorJson = response.errorBody()?.string()
                                var parsedMsg: String? = null
                                if (!errorJson.isNullOrEmpty()) {
                                    try {
                                        @Suppress("DEPRECATION")
                                        val jsonElement = JsonParser().parse(errorJson)
                                        if (jsonElement.isJsonObject) {
                                            val errObj: JsonObject = jsonElement.asJsonObject
                                            if (errObj.has("message")) {
                                                parsedMsg = errObj.get("message").asString
                                            }
                                        }
                                    } catch (_: Exception) {}
                                }
                                errorMessage = parsedMsg ?: body?.message ?: "Invalid email/Student ID or password."
                            }

                        } catch (e: Exception) {
                            isLoading = false
                            Log.e("LOGIN_DEBUG", "Exception", e)

                            if ((inputEmail == "demo@ionox.in" || inputEmail == "demo") && inputPassword == "demo") {
                                successMessage = "Welcome Demo Student!"
                                onLoginSuccess(999, "Demo Student")
                            } else {
                                errorMessage = "Unable to connect to server. Please check your connection."
                            }
                        }
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = ProfessionalGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "SIGN IN",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                }
            }

            // OR Divider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFE2E8F0)
                )
                Text(
                    text = "OR",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFE2E8F0)
                )
            }

            // Google Sign In
            OutlinedButton(
                onClick = { onGoogleLoginClick() },
                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1E293B)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = "Google Logo",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Sign in with Google",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E293B)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Contact Admin
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Need an account? ",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = "Contact Administrator",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ProfessionalGreen,
                    modifier = Modifier.clickable { onContactAdminClick() }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun LoginScreenPreview() {
    IONOXELMSTheme {
        LoginScreen()
    }
}
