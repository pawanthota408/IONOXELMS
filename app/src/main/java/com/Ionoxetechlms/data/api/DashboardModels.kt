package com.Ionoxetechlms.data.api

import com.google.gson.annotations.SerializedName

/**
 * Next Class Schedule Item
 */
data class NextClass(
    @SerializedName("course_title") val courseTitle: String? = null,
    @SerializedName("trainer_name") val trainerName: String? = null,
    @SerializedName("start_date")   val startDate: String? = null,
    @SerializedName("start_time")   val startTime: String? = null,
    @SerializedName("meeting_link") val meetingLink: String? = null
)

/**
 * Schedule / Timetable Session Item
 */
data class ScheduleItem(
    @SerializedName("id")           val id: Int = 0,
    @SerializedName("course_title") val courseTitle: String? = null,
    @SerializedName("trainer_name") val trainerName: String? = null,
    @SerializedName("start_date")   val startDate: String? = null,
    @SerializedName("start_time")   val startTime: String? = null,
    @SerializedName("meeting_link") val meetingLink: String? = null,
    @SerializedName("is_live")      val isLive: Boolean = false,
    @SerializedName("is_today")     val isToday: Boolean = false
)

/**
 * Enrolled Course Item
 */
data class CourseItem(
    @SerializedName("id")          val id: Int = 0,
    @SerializedName("title")       val title: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("thumbnail")   val thumbnail: String? = null
)

/**
 * Job Opening / Internship Item
 */
data class JobItem(
    @SerializedName("id")          val id: Int = 0,
    @SerializedName("title")       val title: String? = null,
    @SerializedName("company")     val company: String? = null,
    @SerializedName("location")    val location: String? = "Remote",
    @SerializedName("description") val description: String? = null,
    @SerializedName("posted_date") val postedDate: String? = null
)

/**
 * Attendance Record Item
 */
data class AttendanceRecord(
    @SerializedName("id")          val id: Int = 0,
    @SerializedName("course_name") val courseName: String? = null,
    @SerializedName("date")        val date: String? = null,
    @SerializedName("day")         val day: String? = null,
    @SerializedName("status")      val status: String? = "present"
)

/**
 * Attendance Response Payload
 */
data class AttendanceResponse(
    @SerializedName("status")                val status: String = "success",
    @SerializedName("total_classes")         val totalClasses: Int = 0,
    @SerializedName("attended_classes")      val attendedClasses: Int = 0,
    @SerializedName("absent_classes")        val absentClasses: Int = 0,
    @SerializedName("attendance_percentage") val attendancePercentage: Int = 100,
    @SerializedName("is_compliant")          val isCompliant: Boolean = true,
    @SerializedName("records")               val records: List<AttendanceRecord> = emptyList()
)

/**
 * Assignment Item Model
 */
data class AssignmentItem(
    @SerializedName("id")             val id: Int = 0,
    @SerializedName("title")          val title: String? = null,
    @SerializedName("course_name")    val courseName: String? = null,
    @SerializedName("description")    val description: String? = null,
    @SerializedName("due_date")       val dueDate: String? = null,
    @SerializedName("scheduled_at")   val scheduledAt: String? = null,
    @SerializedName("total_marks")    val totalMarks: Int = 100,
    @SerializedName("obtained_marks") val obtainedMarks: Int? = null,
    @SerializedName("submitted_at")   val submittedAt: String? = null,
    @SerializedName("file_path")      val filePath: String? = null,
    @SerializedName("resolved_type")  val resolvedType: String = "mcq",
    @SerializedName("state")          val state: String = "open"
)

/**
 * Assignment List Response Payload
 */
data class AssignmentListResponse(
    @SerializedName("status")      val status: String = "success",
    @SerializedName("total")       val total: Int = 0,
    @SerializedName("assignments") val assignments: List<AssignmentItem> = emptyList()
)

/**
 * Individual Question Detail Item
 */
data class QuestionDetail(
    @SerializedName("id")             val id: Int = 0,
    @SerializedName("question")       val question: String? = null,
    @SerializedName("type")           val type: String = "mcq",
    @SerializedName("marks")          val marks: Int = 10,
    @SerializedName("options")        val options: Map<String, String> = emptyMap(),
    @SerializedName("correct_option") val correctOption: String? = null,
    @SerializedName("student_answer") val studentAnswer: String? = null,
    @SerializedName("awarded_marks")  val awardedMarks: Float? = null,
    @SerializedName("state")          val state: String = "none"
)

/**
 * Assignment Detail Review Response Payload
 */
data class AssignmentDetailResponse(
    @SerializedName("status")          val status: String = "success",
    @SerializedName("assignment_id")   val assignmentId: Int = 0,
    @SerializedName("title")           val title: String? = null,
    @SerializedName("course_name")     val courseName: String? = null,
    @SerializedName("total_marks")     val totalMarks: Int = 100,
    @SerializedName("obtained_marks")  val obtainedMarks: Int? = null,
    @SerializedName("percentage")      val percentage: Int? = null,
    @SerializedName("submitted_at")    val submittedAt: String? = null,
    @SerializedName("file_path")       val filePath: String? = null,
    @SerializedName("resolved_type")   val resolvedType: String = "mcq",
    @SerializedName("submitted")       val submitted: Boolean = false,
    @SerializedName("correct_count")   val correctCount: Int = 0,
    @SerializedName("wrong_count")     val wrongCount: Int = 0,
    @SerializedName("skipped_count")   val skippedCount: Int = 0,
    @SerializedName("questions")       val questions: List<QuestionDetail> = emptyList()
)

/**
 * Assignment Submit Request Payload
 */
data class AssignmentSubmitRequest(
    @SerializedName("student_id")    val studentId: Int,
    @SerializedName("assignment_id") val assignmentId: Int,
    @SerializedName("answers")       val answers: Map<String, String> = emptyMap()
)

/**
 * Assignment Submit Response Payload
 */
data class AssignmentSubmitResponse(
    @SerializedName("status")         val status: String = "success",
    @SerializedName("message")        val message: String = "",
    @SerializedName("obtained_marks") val obtainedMarks: Int = 0,
    @SerializedName("total_marks")    val totalMarks: Int = 100
)

/**
 * Profile Response Payload
 */
data class ProfileResponse(
    @SerializedName("status")     val status: String = "success",
    @SerializedName("id")         val id: Int = 0,
    @SerializedName("student_id") val studentId: String? = null,
    @SerializedName("name")       val name: String? = null,
    @SerializedName("email")      val email: String? = null,
    @SerializedName("phone")      val phone: String? = null,
    @SerializedName("address")    val address: String? = null,
    @SerializedName("batch_name") val batchName: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

/**
 * Certificate Item Model
 */
data class CertificateItem(
    @SerializedName("certificate_id")   val certificateId: String? = null,
    @SerializedName("certificate_name") val certificateName: String? = null,
    @SerializedName("course_title")     val courseTitle: String? = null,
    @SerializedName("image_url")        val imageUrl: String? = null,
    @SerializedName("issued_at")        val issuedAt: String? = null
)

/**
 * Certificate List Response Payload
 */
data class CertificateResponse(
    @SerializedName("status")       val status: String = "success",
    @SerializedName("total")        val total: Int = 0,
    @SerializedName("certificates") val certificates: List<CertificateItem> = emptyList()
)

/**
 * Lesson Item Model
 *
 * Matches the current PHP payload from course_lessons.php:
 *   {
 *     "id": 1,
 *     "title": "...",
 *     "video_url": "...",
 *     "description": "..."
 *   }
 *
 * NOTE: The PHP no longer returns course_id, lesson_order, or batch_id
 * in the lesson objects. Old fields are removed so nothing silently
 * assumes they're populated.
 */
data class LessonItem(
    @SerializedName("id")          val id: Int = 0,
    @SerializedName("title")       val title: String? = null,
    @SerializedName("video_url")   val videoUrl: String? = null,
    @SerializedName("description") val description: String? = null
)

/**
 * Course Lessons Response Payload
 *
 * Matches the current PHP payload from course_lessons.php:
 *   {
 *     "status": "success",
 *     "course_id": 1,
 *     "title": "...",
 *     "description": "...",
 *     "thumbnail": "...",
 *     "total_lessons": 3,
 *     "lessons": [ ... ]
 *   }
 */
data class CourseLessonsResponse(
    @SerializedName("status")        val status: String = "success",
    @SerializedName("course_id")     val courseId: Int = 0,
    @SerializedName("title")         val title: String? = null,
    @SerializedName("description")   val description: String? = null,
    @SerializedName("thumbnail")     val thumbnail: String? = null,
    @SerializedName("total_lessons") val totalLessons: Int = 0,
    @SerializedName("lessons")       val lessons: List<LessonItem> = emptyList()
)

/**
 * Complete Dashboard Data Response Payload
 */
data class DashboardResponse(
    @SerializedName("status")              val status: String = "success",
    @SerializedName("student_name")        val studentName: String? = "Student",
    @SerializedName("student_id")          val studentId: String? = null,
    @SerializedName("enrolled_count")      val enrolledCount: Int = 0,
    @SerializedName("quiz_count")          val quizCount: Int = 0,
    @SerializedName("assignments_pending") val assignmentsPending: Int = 0,
    @SerializedName("notif_count")         val notifCount: Int = 0,
    @SerializedName("next_class")          val nextClass: NextClass? = null,
    @SerializedName("weekly_schedules")    val weeklySchedules: List<ScheduleItem> = emptyList(),
    @SerializedName("courses")             val courses: List<CourseItem> = emptyList(),
    @SerializedName("jobs")                val jobs: List<JobItem> = emptyList()
)