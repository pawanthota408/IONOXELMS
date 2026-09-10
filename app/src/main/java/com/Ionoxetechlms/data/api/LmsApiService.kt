package com.Ionoxetechlms.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface LmsApiService {

    // Clean URL endpoints without .php to avoid Hostinger hPanel 301 redirects
    @FormUrlEncoded
    @POST("api/login")
    suspend fun loginForm(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    @POST("api/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @FormUrlEncoded
    @POST("api/login")
    suspend fun resetPasswordForm(
        @Field("reset_email") resetEmail: String
    ): Response<PasswordResetResponse>

    @POST("api/login")
    suspend fun resetPassword(
        @Body request: PasswordResetRequest
    ): Response<PasswordResetResponse>

    @GET("api/dashboard")
    suspend fun getDashboard(
        @Query("student_id") studentId: Int
    ): Response<DashboardResponse>

    @GET("api/mycourses")
    suspend fun getMyCourses(
        @Query("student_id") studentId: Int
    ): Response<DashboardResponse>

    @GET("api/attendance")
    suspend fun getAttendance(
        @Query("student_id") studentId: Int
    ): Response<AttendanceResponse>

    @GET("api/assignments")
    suspend fun getAssignments(
        @Query("student_id") studentId: Int
    ): Response<AssignmentListResponse>

    @GET("api/assignment_detail")
    suspend fun getAssignmentDetail(
        @Query("student_id") studentId: Int,
        @Query("assignment_id") assignmentId: Int
    ): Response<AssignmentDetailResponse>
}
