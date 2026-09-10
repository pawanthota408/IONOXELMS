package com.Ionoxetechlms.ui.assignments

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Ionoxetechlms.R
import com.Ionoxetechlms.data.api.ApiClient
import com.Ionoxetechlms.data.api.AssignmentItem
import com.Ionoxetechlms.data.api.AssignmentListResponse
import com.Ionoxetechlms.ui.theme.IONOXELMSTheme
import com.Ionoxetechlms.ui.theme.LightGreenSoft
import com.Ionoxetechlms.ui.theme.ProfessionalGreen

val BrandOrange = Color(0xFFF97316)
val OrangeBg = Color(0xFFFFF7ED)
val OrangeBorder = Color(0x33F97316)
val NavyDark = Color(0xFF1A1F2E)
val LightMuted = Color(0xFF7A8BA5)
val PurpleAccent = Color(0xFF7C3AED)
val PurpleBg = Color(0xFFF5F3FF)

/**
 * Assignments & Tasks Screen for Ionoxetech LMS Application
 * Enforces business rules:
 * 1. Already submitted -> View result only
 * 2. Not submitted & due date active -> Attempt assignment
 * 3. Not submitted & due date passed -> View correct answers (0 Marks), attempt blocked
 */
@Composable
fun AssignmentsScreen(
    studentId: Int = 2,
    studentName: String = "Student",
    onAssignmentClick: (Int, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var assignmentsData by remember { mutableStateOf<AssignmentListResponse?>(null) }

    LaunchedEffect(studentId) {
        try {
            val res = ApiClient.apiService.getAssignments(studentId)
            if (res.isSuccessful && res.body() != null) {
                assignmentsData = res.body()
            }
        } catch (_: Exception) {
            // Demo Fallback
            assignmentsData = AssignmentListResponse(
                status = "success",
                total = 2,
                assignments = listOf(
                    AssignmentItem(
                        id = 1,
                        title = "Machine Learning Regression Model Assignment",
                        courseName = "Artificial Intelligence & Machine Learning",
                        description = "Implement Linear and Polynomial Regression algorithms using Python and NumPy.",
                        dueDate = "10 May 2026",
                        totalMarks = 100,
                        resolvedType = "mcq",
                        state = "open"
                    ),
                    AssignmentItem(
                        id = 2,
                        title = "Full Stack MERN CRUD Application",
                        courseName = "Full Stack Web Development",
                        description = "Build a responsive REST API backend with Express and Node.js connected to React frontend.",
                        dueDate = "20 Apr 2026",
                        totalMarks = 100,
                        obtainedMarks = 92,
                        submittedAt = "19 Apr 2026",
                        filePath = "https://ionox.in/uploads/assignments/mern_app.pdf",
                        resolvedType = "project",
                        state = "submitted"
                    )
                )
            )
        } finally {
            isLoading = false
        }
    }

    val list = assignmentsData?.assignments ?: emptyList()
    val mcqList = list.filter { it.resolvedType.lowercase() == "mcq" }
    val projList = list.filter { it.resolvedType.lowercase() == "project" || it.resolvedType.lowercase() == "descriptive" }

    val activeList = if (selectedTab == 0) mcqList else projList

    val submittedCount = list.count { it.state.lowercase() == "submitted" }
    val openCount = list.count { it.state.lowercase() == "open" }
    val overdueCount = list.count { it.state.lowercase() == "overdue" }

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
                        text = "Assignments & Projects",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
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
                    // Title Banner
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE8ECF4))
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = "Assignments & Projects",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Sorted newest first • Active deadline allows attempts • Overdue shows answers (0 Marks)",
                                    fontSize = 12.sp,
                                    color = LightMuted
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(100.dp),
                                        color = OrangeBg,
                                        border = BorderStroke(1.dp, OrangeBorder)
                                    ) {
                                        Text(
                                            text = "${mcqList.size} MCQ(s)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandOrange,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(100.dp),
                                        color = PurpleBg,
                                        border = BorderStroke(1.dp, Color(0x337C3AED))
                                    ) {
                                        Text(
                                            text = "${projList.size} Descriptive(s)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PurpleAccent,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Stats Row (4 summary cards)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TaskStatCard(modifier = Modifier.weight(1f), title = "Total", value = "${list.size}", color = BrandOrange)
                            TaskStatCard(modifier = Modifier.weight(1f), title = "Submitted", value = "$submittedCount", color = ProfessionalGreen)
                            TaskStatCard(modifier = Modifier.weight(1f), title = "Live Now", value = "$openCount", color = Color(0xFF0891B2))
                            TaskStatCard(modifier = Modifier.weight(1f), title = "Expired", value = "$overdueCount", color = Color(0xFFDC2626))
                        }
                    }

                    // Tab Row (MCQ vs Descriptive)
                    item {
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.White,
                            contentColor = BrandOrange,
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = BrandOrange
                                )
                            }
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = { Text("MCQ (${mcqList.size})", fontWeight = FontWeight.Bold) }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = { Text("Descriptive (${projList.size})", fontWeight = FontWeight.Bold) }
                            )
                        }
                    }

                    // Assignment List
                    if (activeList.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Text(
                                    text = if (selectedTab == 0) "No MCQ Assignments Yet" else "No Descriptive Projects Yet",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyDark,
                                    modifier = Modifier.padding(24.dp)
                                )
                            }
                        }
                    } else {
                        items(activeList) { assignment ->
                            AssignmentCard(
                                item = assignment,
                                onClick = {
                                    if (assignment.id > 0) {
                                        onAssignmentClick(assignment.id, assignment.state.lowercase())
                                    } else {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ionox.in/lms/student/assignments.php"))
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
}

@Composable
fun TaskStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE8ECF4))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                fontSize = 10.sp,
                color = LightMuted
            )
        }
    }
}

@Composable
fun AssignmentCard(
    item: AssignmentItem,
    onClick: () -> Unit
) {
    val state = item.state.lowercase()
    val isSubmitted = state == "submitted"
    val isExpired = state == "overdue"
    val isLocked = state == "locked"
    val isProject = item.resolvedType.lowercase() == "project" || item.resolvedType.lowercase() == "descriptive"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE8ECF4)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Top Row: Course Tag + State Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = if (isProject) PurpleBg else OrangeBg,
                    border = BorderStroke(1.dp, if (isProject) Color(0x337C3AED) else OrangeBorder)
                ) {
                    Text(
                        text = item.courseName ?: "Course",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isProject) PurpleAccent else BrandOrange,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = when (state) {
                        "submitted" -> LightGreenSoft
                        "overdue" -> Color(0xFFFEF2F2)
                        "locked" -> Color(0xFFF8F9FC)
                        else -> OrangeBg
                    },
                    border = BorderStroke(1.dp, when (state) {
                        "submitted" -> Color(0x3316A34A)
                        "overdue" -> Color(0x33DC2626)
                        "locked" -> Color(0xFFE8ECF4)
                        else -> OrangeBorder
                    })
                ) {
                    Text(
                        text = when (state) {
                            "submitted" -> "SUBMITTED"
                            "overdue" -> "EXPIRED (0 MARKS)"
                            "locked" -> "LOCKED"
                            else -> "LIVE NOW"
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (state) {
                            "submitted" -> ProfessionalGreen
                            "overdue" -> Color(0xFFDC2626)
                            "locked" -> LightMuted
                            else -> BrandOrange
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = item.title ?: "Assignment Task",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = NavyDark
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Type Indicator
            Text(
                text = if (isProject) "Descriptive • Write answer or upload file" else "MCQ • Multiple Choice Questions",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isProject) PurpleAccent else BrandOrange
            )

            // Description
            if (!item.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    fontSize = 12.sp,
                    color = LightMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Due Date & Total Marks
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Due: ${item.dueDate ?: "No due date"}",
                    fontSize = 11.sp,
                    color = if (isExpired) Color(0xFFDC2626) else LightMuted,
                    fontWeight = if (isExpired) FontWeight.Bold else FontWeight.Normal
                )

                Text(
                    text = if (isSubmitted) "Marks: ${item.obtainedMarks ?: 0} / ${item.totalMarks}" else if (isExpired) "Score: 0 / ${item.totalMarks}" else "Total: ${item.totalMarks} Marks",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isExpired && !isSubmitted) Color(0xFFDC2626) else NavyDark
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Button Rule Enforcer
            Button(
                onClick = { onClick() },
                enabled = !isLocked,
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        isSubmitted -> ProfessionalGreen
                        isExpired -> Color(0xFFDC2626)
                        isLocked -> Color(0xFF94A3B8)
                        isProject -> PurpleAccent
                        else -> BrandOrange
                    }
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = when {
                        isSubmitted -> "View My Result"
                        isExpired -> "View Answers (0 Marks - Attempt Closed)"
                        isLocked -> "Locked"
                        isProject -> "Submit Project"
                        else -> "Attempt Assignment"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AssignmentsScreenPreview() {
    IONOXELMSTheme {
        AssignmentsScreen()
    }
}
