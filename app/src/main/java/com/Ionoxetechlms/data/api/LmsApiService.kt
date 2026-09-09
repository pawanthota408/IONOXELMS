package com.Ionoxetechlms.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface LmsApiService {

    // JSON Login
    @POST("api/login.php")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    // Form-urlencoded Login (fallback)
    @FormUrlEncoded
    @POST("api/login.php")
    suspend fun loginForm(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    // Password Reset
    @POST("api/login.php")
    suspend fun resetPassword(
        @Body request: PasswordResetRequest
    ): Response<PasswordResetResponse>

    // Student Dashboard Data API
    @GET("api/dashboard.php")
    suspend fun getDashboard(
        @Query("student_id") studentId: Int
    ): Response<DashboardResponse>
}
