package com.Ionoxetechlms.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface LmsApiService {

    // JSON Body Login
    @POST("api/login.php")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    // Form URL-Encoded Login
    @FormUrlEncoded
    @POST("api/login.php")
    suspend fun loginForm(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    // Password Reset Request
    @POST("api/login.php")
    suspend fun resetPassword(
        @Body request: PasswordResetRequest
    ): Response<PasswordResetResponse>
}
