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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
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
import com.Ionoxetechlms.data.api.CourseLessonsResponse
import com.Ionoxetechlms.data.api.LessonItem
import com.Ionoxetechlms.ui.theme.IONOXELMSTheme
import com.Ionoxetechlms.ui.theme.LightGreenSoft
import com.Ionoxetechlms.ui.theme.ProfessionalGreen

val PlayerNavy = Color(0xFF0F172A)
val PrimaryIndigo = Color(0xFF4F46E5)
val IndigoSoft = Color(0xFFEEF2FF)
val CardBorder = Color(0xFFE2E8F0)
val MutedText = Color(0xFF64748B)

/**
 * Course Lessons & Video Player Screen in Jetpack Compose
 * Replicates web 'course_lessons.php'
 */
@Composable
fun CourseLessonsScreen(
    studentId: Int = 2,
    courseId: Int = 1,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var courseData by remember { mutableStateOf<CourseLessonsResponse?>(null) }
    var activeLessonIndex by remember { mutableIntStateOf(0) }

    // Map tracking completed lesson IDs
    val completedLessons = remember { mutableStateMapOf<Int, Boolean>() }

    LaunchedEffect(courseId) {
        try {
            val res = ApiClient.apiService.getCourseLessons(studentId, courseId)
            if (res.isSuccessful && res.body() != null) {
                courseData = res.body()
            }
        } catch (_: Exception) {
            // Mock Fallback
            courseData = CourseLessonsResponse(
                status = "success",
                courseId = courseId,
                title = "Artificial Intelligence & Machine Learning",
                description = "Master core Machine Learning and Artificial Intelligence algorithms.",
                totalLessons = 3,
                lessons = listOf(
                    LessonItem(
                        id = 101,
                        title = "Lesson 1: Introduction & Environment Setup",
                        videoUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                        description = "Welcome to the course! In this lesson we will set up python and Jupyter Notebooks."
                    ),
                    LessonItem(
                        id = 102,
                        title = "Lesson 2: Core ML Concepts & Architecture",
                        videoUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                        description = "Learn fundamental concepts of Supervised vs Unsupervised machine learning models."
                    ),
                    LessonItem(
                        id = 103,
                        title = "Lesson 3: Hands-On Regression Project",
                        videoUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                        description = "Build a real-world linear regression model using Python and NumPy."
                    )
                )
            )
        } finally {
            isLoading = false
        }
    }

    val lessons = courseData?.lessons ?: emptyList()
    val totalLessons = lessons.size.coerceAtLeast(1)
    val completedCount = completedLessons.size

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF8FAFF)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header Navigation Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PlayerNavy)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(text = "My Courses > Lessons", fontSize = 10.sp, color = MutedText, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = courseData?.title ?: "Course Content",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PlayerNavy,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Progress Pill
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$completedCount / $totalLessons Done",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryIndigo
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        LinearProgressIndicator(
                            progress = { (completedCount / totalLessons.toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .width(80.dp)
                                .height(5.dp)
                                .clip(RoundedCornerShape(100.dp)),
                            color = PrimaryIndigo,
                            trackColor = CardBorder
                        )
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryIndigo)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Video Player Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = PlayerNavy),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            val activeLesson = lessons.getOrNull(activeLessonIndex.coerceIn(0, lessons.size - 1))

                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .background(PlayerNavy)
                                        .clickable {
                                            if (!activeLesson?.videoUrl.isNullOrBlank()) {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(activeLesson?.videoUrl))
                                                context.startActivity(intent)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(PrimaryIndigo),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(32.dp))
                                    }
                                }

                                // Active Lesson Title & Description
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(100.dp),
                                            color = PrimaryIndigo.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "NOW PLAYING",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFA5B4FC),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }

                                        val activeId = activeLesson?.id ?: 0
                                        val isDone = completedLessons[activeId] == true

                                        OutlinedButton(
                                            onClick = {
                                                if (isDone) completedLessons.remove(activeId) else completedLessons[activeId] = true
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, if (isDone) ProfessionalGreen else Color.White.copy(alpha = 0.3f))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = if (isDone) ProfessionalGreen else Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isDone) "Completed" else "Mark Done",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDone) ProfessionalGreen else Color.White
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = activeLesson?.title ?: "Select a lesson to start",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = activeLesson?.description ?: "No additional lesson notes provided.",
                                        fontSize = 12.sp,
                                        color = Color(0xFF94A3B8),
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }

                    // Lessons Module Playlist Header
                    item {
                        Text(
                            text = "Course Content ($totalLessons Lessons)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PlayerNavy
                        )
                    }

                    // Playlist Items List
                    itemsIndexed(lessons) { index, lesson ->
                        val isPlaying = (index == activeLessonIndex)
                        val isDone = (completedLessons[lesson.id] == true)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { activeLessonIndex = index },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isPlaying) IndigoSoft else Color.White
                            ),
                            border = BorderStroke(1.dp, if (isPlaying) PrimaryIndigo else CardBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isPlaying) PrimaryIndigo else Color(0xFFF1F5F9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isPlaying) {
                                        Icon(Icons.Default.Videocam, contentDescription = "Playing", tint = Color.White, modifier = Modifier.size(18.dp))
                                    } else {
                                        Text(
                                            text = "${index + 1}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MutedText
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = lesson.title ?: "Lesson",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPlaying) PrimaryIndigo else PlayerNavy,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Video Lesson",
                                        fontSize = 10.sp,
                                        color = MutedText
                                    )
                                }

                                if (isDone) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Done", tint = ProfessionalGreen, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CourseLessonsPreview() {
    IONOXELMSTheme {
        CourseLessonsScreen()
    }
}
