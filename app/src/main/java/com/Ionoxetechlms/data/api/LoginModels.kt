package com.Ionoxetechlms.data.api

import com.google.gson.annotations.SerializedName

/**
 * Login Request JSON Payload
 */
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

/**
 * Student Data Model
 */
data class Student(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String? = "student"
)

/**
 * Login Response JSON Payload
 */
data class LoginResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("student") val student: Student? = null
)

/**
 * Password Reset Request JSON Payload
 */
data class PasswordResetRequest(
    @SerializedName("reset_email") val resetEmail: String
)

/**
 * Password Reset Response JSON Payload
 */
data class PasswordResetResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)
