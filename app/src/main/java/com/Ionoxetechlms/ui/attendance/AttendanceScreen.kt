package com.Ionoxetechlms.ui.attendance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Ionoxetechlms.R
import com.Ionoxetechlms.data.api.ApiClient
import com.Ionoxetechlms.data.api.AttendanceRecord
import com.Ionoxetechlms.data.api.AttendanceResponse
import com.Ionoxetechlms.ui.theme.IONOXELMSTheme
import com.Ionoxetechlms.ui.theme.LightGreenSoft
import com.Ionoxetechlms.ui.theme.ProfessionalGreen

val OrangeAccent = Color(0xFFF97316)
val RedAccent = Color(0xFFDC2626)
val CyanAccent = Color(0xFF0891B2)
val NavyTitle = Color(0xFF1A1F2E)
val LightMuted = Color(0xFF7A8BA5)

/**
 * Attendance Screen for Ionoxetech LMS Application
 * Synced with web 'attendance.php' layout and REST API
 */
@Composable
fun AttendanceScreen(
    studentId: Int = 2,
    studentName: String = "Student"
) {
    var isLoading by remember { mutableStateOf(true) }
    var attendanceData by remember { mutableStateOf<AttendanceResponse?>(null) }

    LaunchedEffect(studentId) {
        try {
            val res = ApiClient.apiService.getAttendance(studentId)
            if (res.isSuccessful && res.body() != null) {
                attendanceData = res.body()
            }
        } catch (_: Exception) {
            // Mock Fallback
            attendanceData = AttendanceResponse(
                status = "success",
                totalClasses = 12,
                attendedClasses = 11,
                absentClasses = 1,
                attendancePercentage = 92,
                isCompliant = true,
                records = listOf(
                    AttendanceRecord(1, "Artificial Intelligence & Machine Learning", "10 Sep 2026", "Thursday", "present"),
                    AttendanceRecord(2, "Artificial Intelligence & Machine Learning", "08 Sep 2026", "Tuesday", "present"),
                    AttendanceRecord(3, "Full Stack Web Development", "05 Sep 2026", "Saturday", "present"),
                    AttendanceRecord(4, "Full Stack Web Development", "03 Sep 2026", "Thursday", "absent")
                )
            )
        } finally {
            isLoading = false
        }
    }

    val data = attendanceData ?: AttendanceResponse("success")

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF1F3F8)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.in_logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Attendance",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyTitle
                    )
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ProfessionalGreen)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Summary Stats Row (4 Cards)
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AttendanceStatCard(
                                    modifier = Modifier.weight(1f),
                                    title = "Total Classes",
                                    value = "${data.totalClasses}",
                                    icon = Icons.Default.CalendarMonth,
                                    color = OrangeAccent,
                                    bgColor = Color(0xFFFFF7ED)
                                )
                                AttendanceStatCard(
                                    modifier = Modifier.weight(1f),
                                    title = "Present",
                                    value = "${data.attendedClasses}",
                                    icon = Icons.Default.CheckCircle,
                                    color = ProfessionalGreen,
                                    bgColor = LightGreenSoft
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AttendanceStatCard(
                                    modifier = Modifier.weight(1f),
                                    title = "Absent",
                                    value = "${data.absentClasses}",
                                    icon = Icons.Default.Cancel,
                                    color = RedAccent,
                                    bgColor = Color(0xFFFEF2F2)
                                )
                                AttendanceStatCard(
                                    modifier = Modifier.weight(1f),
                                    title = "Attendance Rate",
                                    value = "${data.attendancePercentage}%",
                                    icon = Icons.Default.PieChart,
                                    color = CyanAccent,
                                    bgColor = Color(0xFFF0F9FF)
                                )
                            }
                        }
                    }

                    // Eligibility Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE8ECF4))
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = "Eligibility Status",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyTitle
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Status Pill
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (data.isCompliant) LightGreenSoft else Color(0xFFFEF2F2),
                                    border = BorderStroke(1.dp, if (data.isCompliant) Color(0x3316A34A) else Color(0x33DC2626))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (data.isCompliant) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                            contentDescription = null,
                                            tint = if (data.isCompliant) ProfessionalGreen else RedAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (data.isCompliant) "Eligible for Examinations" else "Not Eligible (Low Attendance)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (data.isCompliant) ProfessionalGreen else RedAccent
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Progress Bar
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Your attendance", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = LightMuted)
                                        Text("${data.attendancePercentage}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyTitle)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { (data.attendancePercentage / 100f).coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(100.dp)),
                                        color = if (data.isCompliant) ProfessionalGreen else RedAccent,
                                        trackColor = Color(0xFFE8ECF4)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Minimum required: 75%",
                                        fontSize = 10.sp,
                                        color = LightMuted
                                    )
                                }
                            }
                        }
                    }

                    // Attendance History List
                    item {
                        Text(
                            text = "Attendance Records",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyTitle
                        )
                    }

                    if (data.records.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Text(
                                    text = "No attendance records found yet.",
                                    fontSize = 13.sp,
                                    color = LightMuted,
                                    modifier = Modifier.padding(20.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(data.records) { record ->
                            AttendanceRecordCard(record = record)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    bgColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE8ECF4))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyTitle
                )
                Text(
                    text = title,
                    fontSize = 10.sp,
                    color = LightMuted
                )
            }
        }
    }
}

@Composable
fun AttendanceRecordCard(record: AttendanceRecord) {
    val isPresent = (record.status?.lowercase() == "present")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE8ECF4))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.courseName ?: "Course Session",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyTitle
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${record.date ?: ""} • ${record.day ?: ""}",
                    fontSize = 12.sp,
                    color = LightMuted
                )
            }

            Surface(
                shape = RoundedCornerShape(100.dp),
                color = if (isPresent) LightGreenSoft else Color(0xFFFEF2F2),
                border = BorderStroke(1.dp, if (isPresent) Color(0x3316A34A) else Color(0x33DC2626))
            ) {
                Text(
                    text = if (isPresent) "PRESENT" else "ABSENT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPresent) ProfessionalGreen else RedAccent,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AttendanceScreenPreview() {
    IONOXELMSTheme {
        AttendanceScreen()
    }
}
