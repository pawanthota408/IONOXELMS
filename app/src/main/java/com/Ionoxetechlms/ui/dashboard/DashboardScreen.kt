package com.Ionoxetechlms.ui.dashboard

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Surface
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
import com.Ionoxetechlms.ui.courses.MyCoursesScreen
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
        studentId = studentId,
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
    studentId: Int = 999,
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
                when (selectedTab) {
                    1 -> {
                        MyCoursesScreen(
                            studentId = studentId,
                            studentName = dashboardData.studentName ?: "Student"
                        )
                    }
                    else -> {
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

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFF7ED),
                border = BorderStroke(1.dp, Color(0xFFFED7AA))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📋",
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (pendingTasks > 0) "You have $pendingTasks pending task(s) to complete." else "You're all caught up with your tasks!",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFC2410C)
                    )
                }
            }
        }
    }
}

/**
 * Stats Summary Row
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
            accentColor = Color(0xFFF97316),
            bgColor = Color(0xFFFFF7ED)
        )

        StatCard(
            modifier = Modifier.weight(1f),
            title = "Quizzes",
            value = quizzes.toString(),
            icon = Icons.Default.Help,
            accentColor = ProfessionalGreen,
            bgColor = Color(0xFFECFDF5)
        )

        StatCard(
            modifier = Modifier.weight(1f),
            title = "Pending",
            value = pending.toString(),
            icon = Icons.Default.Assignment,
            accentColor = Color(0xFF0891B2),
            bgColor = Color(0xFFECFEFF)
        )
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    bgColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Text(
                text = title,
                fontSize = 11.sp,
                color = Color(0xFF64748B)
            )
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
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = Color(0xFF22C55E).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "LIVE NOW",
                        color = Color(0xFF4ADE80),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "${nextClass.startDate ?: "Today"} ${nextClass.startTime ?: ""}",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = nextClass.courseTitle ?: "Upcoming Class",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            if (!nextClass.trainerName.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Trainer: ${nextClass.trainerName}",
                    fontSize = 13.sp,
                    color = Color(0xFFCBD5E1)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onJoinClick(nextClass.meetingLink) },
                colors = ButtonDefaults.buttonColors(containerColor = ProfessionalGreen),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = "Video",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "JOIN CLASS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
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
        Text(
            text = "Class Schedule",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (schedules.isEmpty()) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "No classes scheduled for today.",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                schedules.forEach { item ->
                    ScheduleCardItem(item = item, onJoinClick = onJoinClick)
                }
            }
        }
    }
}

@Composable
fun ScheduleCardItem(
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
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.courseTitle ?: "Session",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${item.startDate ?: ""} • ${item.startTime ?: ""}",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }

            if (!item.meetingLink.isNullOrBlank()) {
                Button(
                    onClick = { onJoinClick(item.meetingLink) },
                    colors = ButtonDefaults.buttonColors(containerColor = ProfessionalGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("JOIN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

/**
 * My Courses Grid Section
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
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Text(
                text = "${courses.size} Enrolled",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = ProfessionalGreen
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (courses.isEmpty()) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "No enrolled courses yet.",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                courses.forEach { course ->
                    CourseCardItem(course = course)
                }
            }
        }
    }
}

@Composable
fun CourseCardItem(course: CourseItem) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFFF7ED)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = Color(0xFFF97316),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.title ?: "Course",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = course.description ?: "Active course",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Open",
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Latest Jobs Section
 */
@Composable
fun JobsSection(jobs: List<JobItem>) {
    Column {
        Text(
            text = "Career Opportunities",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            jobs.forEach { job ->
                JobCardItem(job = job)
            }
        }
    }
}

@Composable
fun JobCardItem(job: JobItem) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = job.title ?: "Opportunity",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = Color(0xFFEFF6FF)
                ) {
                    Text(
                        text = job.location ?: "Remote",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = job.company ?: "Ionoxe Partner",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = ProfessionalGreen
            )

            if (!job.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = job.description,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Bottom Navigation Bar
 */
@Composable
fun DashboardBottomNavigation(
    selectedTab: Int = 0,
    pendingTasks: Int = 0,
    onTabSelected: (Int) -> Unit = {}
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
fun createMockDashboardData(studentName: String = "Student"): DashboardResponse {
    return DashboardResponse(
        status = "success",
        studentName = studentName,
        studentId = "IO-ST251101",
        enrolledCount = 2,
        quizCount = 0,
        assignmentsPending = 1,
        notifCount = 1,
        nextClass = NextClass(
            courseTitle = "Artificial Intelligence & Machine Learning",
            trainerName = "Lead AI Instructor",
            startDate = "Today",
            startTime = "06:00 PM",
            meetingLink = "https://meet.google.com"
        ),
        weeklySchedules = listOf(
            ScheduleItem(
                id = 101,
                courseTitle = "AI & Machine Learning Concepts",
                trainerName = "Lead Instructor",
                startDate = "Today",
                startTime = "06:00 PM",
                meetingLink = "https://meet.google.com",
                isLive = true,
                isToday = true
            )
        ),
        courses = listOf(
            CourseItem(
                id = 1,
                title = "Artificial Intelligence & Machine Learning",
                description = "Understand how machines learn, think, and make predictions.",
                thumbnail = "https://iili.io/fViYYl9.png"
            ),
            CourseItem(
                id = 2,
                title = "Full Stack Web Development (MERN)",
                description = "HTML, CSS, JS, React, Node.js, Express, MongoDB.",
                thumbnail = ""
            )
        ),
        jobs = listOf(
            JobItem(
                id = 2,
                title = "AI/ML Engineer",
                company = "Levino Softlabs",
                location = "Work From Office",
                description = "Build and train machine learning models.",
                postedDate = "2026-04-18"
            )
        )
    )
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    IONOXELMSTheme {
        DashboardContent(
            dashboardData = createMockDashboardData("Kota Mounika")
        )
    }
}
