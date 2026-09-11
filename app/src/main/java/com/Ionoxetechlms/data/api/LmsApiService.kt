package com.Ionoxetechlms.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface LmsApiService {

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

    @POST("api/submit_assignment")
    suspend fun submitAssignment(
        @Body request: AssignmentSubmitRequest
    ): Response<AssignmentSubmitResponse>

    @GET("api/profile")
    suspend fun getProfile(
        @Query("student_id") studentId: Int
    ): Response<ProfileResponse>

    @FormUrlEncoded
    @POST("api/profile")
    suspend fun updateProfile(
        @Field("student_id") studentId: Int,
        @Field("update_profile") updateProfile: Int = 1,
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("phone") phone: String,
        @Field("address") address: String
    ): Response<ProfileResponse>

    @GET("api/certificates")
    suspend fun getCertificates(
        @Query("student_id") studentId: Int
    ): Response<CertificateResponse>

    @GET("api/course_lesson")
    suspend fun getCourseLessons(
        @Query("student_id") studentId: Int,
        @Query("course_id") courseId: Int
    ): Response<CourseLessonsResponse>

    @GET("course_lesson.php")
    suspend fun getCourseLessonsDirect(
        @Query("student_id") studentId: Int,
        @Query("course_id") courseId: Int
    ): Response<CourseLessonsResponse>
}
