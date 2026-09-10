package com.Ionoxetechlms.data.api

import com.google.gson.annotations.SerializedName

/**
 * Next Class Schedule Item
 */
data class NextClass(
    @SerializedName("course_title") val courseTitle: String? = null,
    @SerializedName("trainer_name") val trainerName: String? = null,
    @SerializedName("start_date") val startDate: String? = null,
    @SerializedName("start_time") val startTime: String? = null,
    @SerializedName("meeting_link") val meetingLink: String? = null
)

/**
 * Schedule / Timetable Session Item
 */
data class ScheduleItem(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("course_title") val courseTitle: String? = null,
    @SerializedName("trainer_name") val trainerName: String? = null,
    @SerializedName("start_date") val startDate: String? = null,
    @SerializedName("start_time") val startTime: String? = null,
    @SerializedName("meeting_link") val meetingLink: String? = null,
    @SerializedName("is_live") val isLive: Boolean = false,
    @SerializedName("is_today") val isToday: Boolean = false
)

/**
 * Enrolled Course Item
 */
data class CourseItem(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("title") val title: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("thumbnail") val thumbnail: String? = null
)

/**
 * Job Opening / Internship Item
 */
data class JobItem(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("title") val title: String? = null,
    @SerializedName("company") val company: String? = null,
    @SerializedName("location") val location: String? = "Remote",
    @SerializedName("description") val description: String? = null,
    @SerializedName("posted_date") val postedDate: String? = null
)

/**
 * Attendance Record Item
 */
data class AttendanceRecord(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("course_name") val courseName: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("day") val day: String? = null,
    @SerializedName("status") val status: String? = "present"
)

/**
 * Attendance Response Payload
 */
data class AttendanceResponse(
    @SerializedName("status") val status: String,
    @SerializedName("total_classes") val totalClasses: Int = 0,
    @SerializedName("attended_classes") val attendedClasses: Int = 0,
    @SerializedName("absent_classes") val absentClasses: Int = 0,
    @SerializedName("attendance_percentage") val attendancePercentage: Int = 100,
    @SerializedName("is_compliant") val isCompliant: Boolean = true,
    @SerializedName("records") val records: List<AttendanceRecord> = emptyList()
)

/**
 * Assignment Item Model
 */
data class AssignmentItem(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("title") val title: String? = null,
    @SerializedName("course_name") val courseName: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("due_date") val dueDate: String? = null,
    @SerializedName("total_marks") val totalMarks: Int = 100,
    @SerializedName("obtained_marks") val obtainedMarks: Int? = null,
    @SerializedName("submitted_at") val submittedAt: String? = null,
    @SerializedName("file_path") val filePath: String? = null,
    @SerializedName("state") val state: String = "open"
)

/**
 * Assignment List Response Payload
 */
data class AssignmentListResponse(
    @SerializedName("status") val status: String,
    @SerializedName("total") val total: Int = 0,
    @SerializedName("assignments") val assignments: List<AssignmentItem> = emptyList()
)

/**
 * Complete Dashboard Data Response Payload
 */
data class DashboardResponse(
    @SerializedName("status") val status: String,
    @SerializedName("student_name") val studentName: String? = "Student",
    @SerializedName("student_id") val studentId: String? = null,
    @SerializedName("enrolled_count") val enrolledCount: Int = 0,
    @SerializedName("quiz_count") val quizCount: Int = 0,
    @SerializedName("assignments_pending") val assignmentsPending: Int = 0,
    @SerializedName("notif_count") val notifCount: Int = 0,
    @SerializedName("next_class") val nextClass: NextClass? = null,
    @SerializedName("weekly_schedules") val weeklySchedules: List<ScheduleItem> = emptyList(),
    @SerializedName("courses") val courses: List<CourseItem> = emptyList(),
    @SerializedName("jobs") val jobs: List<JobItem> = emptyList()
)
