package com.Ionoxetechlms.data.api

import com.google.gson.annotations.SerializedName

 /**
 * Login Request Payload
 */
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

/**
 * Student Data Model matching 'students' MySQL Table
 */
data class Student(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("student_id") val studentId: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("batch_id") val batchId: String? = null,
    @SerializedName("ca_id") val caId: String? = null,
    @SerializedName("role") val role: String? = "student"
)

/**
 * Login Response Payload
 */
data class LoginResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("student") val student: Student? = null
)

/**
 * Password Reset Request Payload
 */
data class PasswordResetRequest(
    @SerializedName("reset_email") val resetEmail: String
)

/**
 * Password Reset Response Payload
 */
data class PasswordResetResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)
