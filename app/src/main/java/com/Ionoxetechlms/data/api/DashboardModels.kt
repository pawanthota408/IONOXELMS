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
