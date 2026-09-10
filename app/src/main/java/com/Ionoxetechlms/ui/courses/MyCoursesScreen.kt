package com.Ionoxetechlms.ui.courses

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Ionoxetechlms.data.api.ApiClient
import com.Ionoxetechlms.data.api.CourseItem
import com.Ionoxetechlms.data.api.DashboardResponse
import com.Ionoxetechlms.ui.theme.IONOXELMSTheme
import com.Ionoxetechlms.ui.theme.LightGreenSoft
import com.Ionoxetechlms.ui.theme.ProfessionalGreen

val BrandOrange = Color(0xFFF97316)
val OrangeBg = Color(0xFFFFF7ED)
val OrangeBorder = Color(0x33F97316)
val NavyDark = Color(0xFF1A1F2E)
val PageBg = Color(0xFFF1F3F8)

/**
 * My Courses Screen for Ionoxetech LMS App
 * Embedded directly inside Dashboard scaffold without duplicate top bar
 */
@Composable
fun MyCoursesScreen(
    studentId: Int = 2,
    studentName: String = "Student",
    onCourseClick: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var dashboardData by remember { mutableStateOf<DashboardResponse?>(null) }

    LaunchedEffect(studentId) {
        try {
            val res = ApiClient.apiService.getMyCourses(studentId)
            if (res.isSuccessful && res.body() != null) {
                dashboardData = res.body()
            } else {
                val fallback = ApiClient.apiService.getDashboard(studentId)
                if (fallback.isSuccessful && fallback.body() != null) {
                    dashboardData = fallback.body()
                }
            }
        } catch (_: Exception) {
            // Demo Fallback Courses
            dashboardData = DashboardResponse(
                status = "success",
                studentName = studentName,
                studentId = "IO-ST251101",
                enrolledCount = 2,
                assignmentsPending = 1,
                courses = listOf(
                    CourseItem(
                        id = 1,
                        title = "Artificial Intelligence & Machine Learning",
                        thumbnail = "https://iili.io/fViYYl9.png",
                        description = "Start your journey into Artificial Intelligence and Machine Learning with simple concepts, no coding required."
                    ),
                    CourseItem(
                        id = 2,
                        title = "Full Stack Web Development (MERN)",
                        thumbnail = "",
                        description = "Master HTML, CSS, JavaScript, React, Node.js, Express, and MongoDB from scratch to build modern web applications."
                    )
                )
            )
        } finally {
            isLoading = false
        }
    }

    val allCourses = dashboardData?.courses ?: emptyList()
    val filteredCourses = if (searchQuery.isBlank()) {
        allCourses
    } else {
        allCourses.filter {
            it.title?.contains(searchQuery, ignoreCase = true) == true ||
            it.description?.contains(searchQuery, ignoreCase = true) == true
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = PageBg
    ) {
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
                // Page Title Section
                item {
                    Column {
                        Text(
                            text = "Course Library",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "You are enrolled in ",
                                fontSize = 13.sp,
                                color = Color(0xFF7A8BA5)
                            )
                            Text(
                                text = "${allCourses.size} courses.",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandOrange
                            )
                        }
                    }
                }

                // Search Input
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search courses…", fontSize = 13.sp, color = Color(0xFF7A8BA5)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF7A8BA5)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = BrandOrange,
                            unfocusedBorderColor = Color(0xFFE8ECF4),
                            cursorColor = BrandOrange
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Stats Strip Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Enrolled Courses Card
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE8ECF4))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(OrangeBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Book,
                                        contentDescription = null,
                                        tint = BrandOrange,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${allCourses.size}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NavyDark
                                    )
                                    Text(
                                        text = "Enrolled",
                                        fontSize = 11.sp,
                                        color = Color(0xFF7A8BA5)
                                    )
                                }
                            }
                        }

                        // Pending Tasks Card
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE8ECF4))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(LightGreenSoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = ProfessionalGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${dashboardData?.assignmentsPending ?: 0}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NavyDark
                                    )
                                    Text(
                                        text = "Pending Tasks",
                                        fontSize = 11.sp,
                                        color = Color(0xFF7A8BA5)
                                    )
                                }
                            }
                        }
                    }
                }

                // Courses List / Grid
                if (filteredCourses.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE8ECF4))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF8F9FC)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Book,
                                        contentDescription = null,
                                        tint = Color(0xFF7A8BA5),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (searchQuery.isNotBlank()) "No courses found" else "Your library is empty",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (searchQuery.isNotBlank()) "Try a different search query." else "You haven't been enrolled in any courses yet.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF7A8BA5)
                                )
                            }
                        }
                    }
                } else {
                    items(filteredCourses) { course ->
                        CourseLibraryCard(
                            course = course,
                            onClick = {
                                if (course.id > 0) {
                                    onCourseClick(course.id)
                                } else {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ionox.in/lms/student/mycourses.php"))
                                    context.startActivity(intent)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Course Library Card matching Web Design (mycourses.php)
 */
@Composable
fun CourseLibraryCard(
    course: CourseItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE8ECF4)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // Course Top Thumbnail Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(OrangeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = null,
                    tint = BrandOrange,
                    modifier = Modifier.size(42.dp)
                )
            }

            // Card Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Enrolled Badge
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = OrangeBg,
                    border = BorderStroke(1.dp, OrangeBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = BrandOrange,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ENROLLED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandOrange,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Title
                Text(
                    text = course.title ?: "LMS Course",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyDark,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Description
                Text(
                    text = course.description ?: "Access complete course materials and interactive video lessons.",
                    fontSize = 12.sp,
                    color = Color(0xFF7A8BA5),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Footer CTA Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = OrangeBg,
                        border = BorderStroke(1.dp, OrangeBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = BrandOrange,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Continue",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandOrange
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open",
                        tint = BrandOrange,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyCoursesScreenPreview() {
    IONOXELMSTheme {
        MyCoursesScreen()
    }
}
