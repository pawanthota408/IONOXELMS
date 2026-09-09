package com.Ionoxetechlms.ui.dashboard

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Ionoxetechlms.R
import com.Ionoxetechlms.data.api.ApiClient
import com.Ionoxetechlms.data.api.CourseItem
import com.Ionoxetechlms.data.api.DashboardResponse
import com.Ionoxetechlms.data.api.JobItem
import com.Ionoxetechlms.data.api.NextClass
import com.Ionoxetechlms.data.api.ScheduleItem
import com.Ionoxetechlms.ui.theme.IONOXELMSTheme
import com.Ionoxetechlms.ui.theme.ProfessionalGreen
import kotlinx.coroutines.launch

/**
 * Student Dashboard Screen for Ionoxetech LMS Application
 * Synced with dashboard.php web layout
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    studentId: Int = 999,
    studentName: String = "Student",
    onLogoutClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var dashboardData by remember { mutableStateOf<DashboardResponse?>(null) }

    // Fetch live dashboard data from API
    LaunchedEffect(studentId) {
        coroutineScope.launch {
            try {
                val res = ApiClient.apiService.getDashboard(studentId)
                isLoading = false
                if (res.isSuccessful && res.body() != null) {
                    dashboardData = res.body()
                } else {
                    dashboardData = createMockDashboardData(studentName)
                }
            } catch (_: Exception) {
                isLoading = false
                dashboardData = createMockDashboardData(studentName)
            }
        }
    }

    DashboardContent(
        isLoading = isLoading,
        dashboardData = dashboardData ?: createMockDashboardData(studentName),
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        onLogoutClick = onLogoutClick,
        onJoinMeetingClick = { link ->
            if (!link.isNullOrBlank()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                context.startActivity(intent)
            }
        }
    )
}

/**
 * Content Layout for Dashboard
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    isLoading: Boolean = false,
    dashboardData: DashboardResponse,
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onJoinMeetingClick: (String?) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.in_logo),
                            contentDescription = "Ionoxe Logo",
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "IONOXE LMS",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }
                },
                actions = {
                    val notifCount = dashboardData.notifCount
                    IconButton(onClick = { }) {
                        BadgedBox(
                            badge = {
                                if (notifCount > 0) {
                                    Badge(containerColor = Color(0xFFF97316)) {
                                        Text(text = notifCount.toString(), color = Color.White)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color(0xFF475569)
                            )
                        }
                    }

                    // Student Avatar Circle
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF97316))
                            .clickable { onLogoutClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        val initial = (dashboardData.studentName ?: "S").take(1).uppercase()
                        Text(
                            text = initial,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            DashboardBottomNavigation(
                selectedTab = selectedTab,
                pendingTasks = dashboardData.assignmentsPending,
                onTabSelected = onTabSelected
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF1F3F8))
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ProfessionalGreen)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // 1. WELCOME STRIP BANNER
                    WelcomeStripBanner(
                        studentName = dashboardData.studentName ?: "Student",
                        pendingTasks = dashboardData.assignmentsPending
                    )

                    // 2. STATS ROW (3 Cards: Enrolled, Quizzes, Pending Tasks)
                    StatsSummaryRow(
                        enrolled = dashboardData.enrolledCount,
                        quizzes = dashboardData.quizCount,
                        pending = dashboardData.assignmentsPending
                    )

                    // 3. NEXT CLASS BANNER (if available)
                    if (dashboardData.nextClass != null) {
                        NextClassBanner(
                            nextClass = dashboardData.nextClass,
                            onJoinClick = onJoinMeetingClick
                        )
                    }

                    // 4. CLASS SCHEDULE TABLE
                    ClassScheduleSection(
                        schedules = dashboardData.weeklySchedules,
                        onJoinClick = onJoinMeetingClick
                    )

                    // 5. MY COURSES GRID
                    MyCoursesSection(courses = dashboardData.courses)

                    // 6. LATEST OPPORTUNITIES / JOBS
                    if (dashboardData.jobs.isNotEmpty()) {
                        JobsSection(jobs = dashboardData.jobs)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

/**
 * Welcome Strip Banner
 */
@Composable
fun WelcomeStripBanner(
    studentName: String,
    pendingTasks: Int
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            val firstName = studentName.split(" ").firstOrNull() ?: studentName
            Text(
                text = "Hi, $firstName! 👋",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Welcome back to your learning portal.",
                fontSize = 13.sp,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (pendingTasks > 0) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFFFFF7ED))
                        .border(1.dp, Color(0xFFFED7AA), CircleShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "⚠️ $pendingTasks pending task${if (pendingTasks > 1) "s" else ""}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEA580C)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFFF0FDF4))
                        .border(1.dp, Color(0xFFBBF7D0), CircleShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "All caught up!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF16A34A)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 3 Stats Summary Row Cards
 */
@Composable
fun StatsSummaryRow(
    enrolled: Int,
    quizzes: Int,
    pending: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Enrolled",
            value = enrolled.toString(),
            icon = Icons.Default.Book,
            iconBg = Color(0xFFFFF7ED),
            iconTint = Color(0xFFF97316)
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Quizzes",
            value = quizzes.toString(),
            icon = Icons.Default.Help,
            iconBg = Color(0xFFF0FDF4),
            iconTint = Color(0xFF16A34A)
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Pending",
            value = pending.toString(),
            icon = Icons.Default.Assignment,
            iconBg = Color(0xFFF0F9FF),
            iconTint = Color(0xFF0891B2)
        )
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = title,
                    fontSize = 10.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

/**
 * Next Class Banner
 */
@Composable
fun NextClassBanner(
    nextClass: NextClass,
    onJoinClick: (String?) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(Color(0xFFFED7AA))),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "NEXT CLASS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEA580C),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = nextClass.courseTitle ?: "Scheduled Class",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "${nextClass.startDate ?: ""} · ${nextClass.startTime ?: ""} · ${nextClass.trainerName ?: "Instructor"}",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }

            Button(
                onClick = { onJoinClick(nextClass.meetingLink) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "JOIN", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Class Schedule Section
 */
@Composable
fun ClassScheduleSection(
    schedules: List<ScheduleItem>,
    onJoinClick: (String?) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Class Schedule",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Upcoming sessions this week",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }
            Text(
                text = "History →",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ProfessionalGreen,
                modifier = Modifier.clickable { }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (schedules.isEmpty()) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No sessions scheduled for this week.",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                schedules.forEach { item ->
                    ScheduleCard(item = item, onJoinClick = onJoinClick)
                }
            }
        }
    }
}

@Composable
fun ScheduleCard(
    item: ScheduleItem,
    onJoinClick: (String?) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Date Chip
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (item.isToday) Color(0xFFF97316) else Color(0xFFF8F9FC))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val dateParts = (item.startDate ?: "").split("-")
                    val dayStr = dateParts.lastOrNull() ?: "01"
                    Text(
                        text = dayStr,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (item.isToday) Color.White else Color(0xFF0F172A)
                    )
                }

                Column {
                    Text(
                        text = item.courseTitle ?: "Course Session",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.startTime ?: ""} · Instructor: ${item.trainerName ?: "Instructor"}",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                    if (item.isLive) {
                        Text(
                            text = "● LIVE NOW",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF16A34A)
                        )
                    }
                }
            }

            if (!item.meetingLink.isNullOrBlank()) {
                Button(
                    onClick = { onJoinClick(item.meetingLink) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (item.isLive) Color(0xFFF97316) else ProfessionalGreen
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(text = "JOIN", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * My Courses Section
 */
@Composable
fun MyCoursesSection(courses: List<CourseItem>) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Courses",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = "View All →",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ProfessionalGreen,
                modifier = Modifier.clickable { }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (courses.isEmpty()) {
            Text(
                text = "You are not enrolled in any courses yet.",
                fontSize = 13.sp,
                color = Color(0xFF94A3B8)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                courses.forEach { course ->
                    CourseCard(course = course)
                }
            }
        }
    }
}

@Composable
fun CourseCard(course: CourseItem) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = ProfessionalGreen,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.title ?: "Enrolled Course",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = course.description ?: "Course lessons & assignments",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Resume",
                tint = Color(0xFFF97316),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Jobs Section
 */
@Composable
fun JobsSection(jobs: List<JobItem>) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Latest Opportunities",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Hand-picked openings for you",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }
            Text(
                text = "View All →",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ProfessionalGreen,
                modifier = Modifier.clickable { }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            jobs.forEach { job ->
                JobCard(job = job)
            }
        }
    }
}

@Composable
fun JobCard(job: JobItem) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFF7ED)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (job.company ?: "C").take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF97316)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = job.title ?: "Opening",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "${job.company ?: ""} · ${job.location ?: "Remote"}",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFFF0FDF4))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "NEW",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A)
                    )
                }
            }
        }
    }
}

/**
 * Mobile 5-Tab Bottom Navigation Bar
 */
@Composable
fun DashboardBottomNavigation(
    selectedTab: Int,
    pendingTasks: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFFF97316), selectedTextColor = Color(0xFFF97316))
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = { Icon(Icons.Default.MenuBook, contentDescription = "Courses") },
            label = { Text("Courses", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFFF97316), selectedTextColor = Color(0xFFF97316))
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            icon = {
                BadgedBox(
                    badge = {
                        if (pendingTasks > 0) {
                            Badge(containerColor = Color(0xFFF97316)) {
                                Text(pendingTasks.toString(), color = Color.White)
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.Assignment, contentDescription = "Tasks")
                }
            },
            label = { Text("Tasks", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFFF97316), selectedTextColor = Color(0xFFF97316))
        )
        NavigationBarItem(
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) },
            icon = { Icon(Icons.Default.Work, contentDescription = "Jobs") },
            label = { Text("Jobs", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFFF97316), selectedTextColor = Color(0xFFF97316))
        )
        NavigationBarItem(
            selected = selectedTab == 4,
            onClick = { onTabSelected(4) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFFF97316), selectedTextColor = Color(0xFFF97316))
        )
    }
}

/**
 * Mock Data Generator
 */
private fun createMockDashboardData(studentName: String): DashboardResponse {
    return DashboardResponse(
        status = "success",
        studentName = studentName,
        studentId = "STU999",
        enrolledCount = 3,
        quizCount = 5,
        assignmentsPending = 2,
        notifCount = 1,
        nextClass = NextClass(
            courseTitle = "Full Stack Web Development",
            trainerName = "Alex Johnson",
            startDate = "2025-02-24",
            startTime = "10:00 AM",
            meetingLink = "https://meet.google.com"
        ),
        weeklySchedules = listOf(
            ScheduleItem(
                id = 1,
                courseTitle = "Full Stack Web Development",
                trainerName = "Alex Johnson",
                startDate = "2025-02-24",
                startTime = "10:00 AM",
                meetingLink = "https://meet.google.com",
                isLive = true,
                isToday = true
            ),
            ScheduleItem(
                id = 2,
                courseTitle = "Python & Data Science",
                trainerName = "Sarah Connor",
                startDate = "2025-02-26",
                startTime = "02:00 PM",
                meetingLink = "https://meet.google.com",
                isLive = false,
                isToday = false
            )
        ),
        courses = listOf(
            CourseItem(
                id = 101,
                title = "Full Stack Web Development",
                description = "Master HTML, CSS, JS, PHP, MySQL and React to build modern web apps."
            ),
            CourseItem(
                id = 102,
                title = "Python & Data Analytics",
                description = "Learn Python programming, pandas, data visualization and machine learning basics."
            )
        ),
        jobs = listOf(
            JobItem(
                id = 1,
                title = "Junior Web Developer",
                company = "Ionoxe Tech Solutions",
                location = "Hyderabad / Remote"
            )
        )
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun DashboardScreenPreview() {
    IONOXELMSTheme {
        DashboardContent(
            isLoading = false,
            dashboardData = createMockDashboardData("Balaji Thota")
        )
    }
}
