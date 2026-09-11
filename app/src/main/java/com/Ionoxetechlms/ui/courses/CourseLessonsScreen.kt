package com.Ionoxetechlms.ui.courses

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.Ionoxetechlms.data.api.ApiClient
import com.Ionoxetechlms.data.api.CourseLessonsResponse
import com.Ionoxetechlms.ui.dashboard.DashboardBottomNavigation
import com.Ionoxetechlms.ui.theme.IONOXELMSTheme
import com.Ionoxetechlms.ui.theme.ProfessionalGreen

private const val TAG = "CourseLessons"

val PlayerNavy = Color(0xFF0F172A)
val PrimaryIndigo = Color(0xFF4F46E5)
val IndigoSoft = Color(0xFFEEF2FF)
val CardBorder = Color(0xFFE2E8F0)
val MutedText = Color(0xFF64748B)

/**
 * Course Lessons & Embedded In-App Video Player Screen in Jetpack Compose
 * Features:
 * 1. Shows Course Thumbnail before video plays, and hides thumbnail when student taps Play.
 * 2. Plays Youtube, Google Drive preview, Vimeo, and direct MP4 videos seamlessly in-app.
 * 3. Expandable Description ("Read More" / "Show Less").
 */
@Composable
fun CourseLessonsScreen(
    studentId: Int = 2,
    courseId: Int = 1,
    onBackClick: () -> Unit = {}
) {
    var isLoading by remember { mutableStateOf(true) }
    var courseData by remember { mutableStateOf<CourseLessonsResponse?>(null) }
    var activeLessonIndex by remember { mutableIntStateOf(0) }

    // State controlling thumbnail vs active video playback
    var isVideoStarted by remember { mutableStateOf(false) }

    // State controlling Expandable Description
    var isDescExpanded by remember { mutableStateOf(false) }

    BackHandler { onBackClick() }

    val completedLessons = remember { mutableStateMapOf<Int, Boolean>() }

    // ── Load live lessons from API ─────────────────────────────────────────
    LaunchedEffect(studentId, courseId) {
        isLoading = true
        try {
            Log.d(TAG, "Fetching lessons: courseId=$courseId studentId=$studentId")
            val res = ApiClient.apiService.getCourseLessons(courseId, studentId)

            if (res.isSuccessful && res.body() != null) {
                val body = res.body()!!
                Log.d(TAG, "Fetched ${body.lessons.size} lessons for course: ${body.title}")
                courseData = body
                if (body.lessons.isNotEmpty()) {
                    activeLessonIndex = 0
                }
            } else {
                Log.w(TAG, "API failed: HTTP ${res.code()} ${res.message()}")
                courseData = emptyCourse(courseId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during getCourseLessons: ${e.message}", e)
            courseData = emptyCourse(courseId)
        } finally {
            isLoading = false
        }
    }

    val lessons = courseData?.lessons ?: emptyList()
    val totalLessons = lessons.size
    val completedCount = completedLessons.size

    Scaffold(
        bottomBar = {
            DashboardBottomNavigation(
                selectedTab = 1,
                pendingTasks = 0,
                onTabSelected = { onBackClick() }
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = Color(0xFFF8FAFF)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Header Bar ────────────────────────────────────────────
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
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = PlayerNavy
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(
                                    text = courseData?.title ?: "Course Content",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PlayerNavy,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (totalLessons == 0) "No lessons available"
                                    else "$totalLessons Lessons available",
                                    fontSize = 11.sp,
                                    color = MutedText
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "$completedCount / ${totalLessons.coerceAtLeast(1)} Done",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryIndigo
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            LinearProgressIndicator(
                                progress = {
                                    if (totalLessons == 0) 0f
                                    else (completedCount / totalLessons.toFloat()).coerceIn(0f, 1f)
                                },
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
                } else if (lessons.isEmpty()) {
                    // ── Empty state ───────────────────────────────────────
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = MutedText,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "No lessons yet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PlayerNavy
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Content will be added by your trainer soon.",
                                fontSize = 12.sp,
                                color = MutedText
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // ── Video Player Card ─────────────────────────────
                        item {
                            val safeIndex = activeLessonIndex.coerceIn(0, lessons.lastIndex)
                            val activeLesson = lessons.getOrNull(safeIndex)
                            val rawVideoUrl = activeLesson?.videoUrl?.trim().orEmpty()

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = PlayerNavy),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(210.dp)
                                            .background(Color.Black),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isVideoStarted && rawVideoUrl.isNotBlank()) {
                                            InAppVideoWebView(videoUrl = rawVideoUrl)
                                        } else {
                                            // Thumbnail Overlay before Video Play
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color(0xFF1E293B))
                                                    .clickable { isVideoStarted = true },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    modifier = Modifier.padding(16.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(60.dp)
                                                            .clip(CircleShape)
                                                            .background(PrimaryIndigo),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            Icons.Default.PlayArrow,
                                                            contentDescription = "Play Lesson",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(36.dp)
                                                        )
                                                    }

                                                    Spacer(modifier = Modifier.height(12.dp))

                                                    Text(
                                                        text = activeLesson?.title ?: "Start Lesson Video",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )

                                                    Spacer(modifier = Modifier.height(2.dp))

                                                    Text(
                                                        text = "Tap to play video in-app",
                                                        fontSize = 11.sp,
                                                        color = Color.White.copy(alpha = 0.7f)
                                                    )
                                                }
                                            }
                                        }
                                    }

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
                                                    if (isDone) completedLessons.remove(activeId)
                                                    else completedLessons[activeId] = true
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (isDone) ProfessionalGreen
                                                    else Color.White.copy(alpha = 0.3f)
                                                )
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
                                            text = activeLesson?.title ?: "Select a lesson",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Expandable Description ("Read More" / "Show Less")
                                        val fullDesc = activeLesson?.description?.takeIf { it.isNotBlank() }
                                            ?: "No additional lesson notes provided."

                                        Column(modifier = Modifier.animateContentSize()) {
                                            Text(
                                                text = fullDesc,
                                                fontSize = 12.sp,
                                                color = Color(0xFF94A3B8),
                                                lineHeight = 18.sp,
                                                maxLines = if (isDescExpanded) Int.MAX_VALUE else 3,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            if (fullDesc.length > 100) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(
                                                    modifier = Modifier
                                                        .clickable { isDescExpanded = !isDescExpanded }
                                                        .padding(vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = if (isDescExpanded) "Show Less" else "Read More",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFA5B4FC)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Icon(
                                                        imageVector = if (isDescExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                        contentDescription = null,
                                                        tint = Color(0xFFA5B4FC),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // ── Playlist Header ───────────────────────────────
                        item {
                            Text(
                                text = "Course Content ($totalLessons ${if (totalLessons == 1) "Lesson" else "Lessons"})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PlayerNavy
                            )
                        }

                        // ── Playlist ──────────────────────────────────────
                        itemsIndexed(lessons) { index, lesson ->
                            val isPlaying = (index == activeLessonIndex)
                            val isDone = (completedLessons[lesson.id] == true)

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        activeLessonIndex = index
                                        isVideoStarted = false // Show thumbnail on new lesson selection
                                        isDescExpanded = false
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isPlaying) IndigoSoft else Color.White
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isPlaying) PrimaryIndigo else CardBorder
                                )
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
                                            .background(
                                                if (isPlaying) PrimaryIndigo
                                                else Color(0xFFF1F5F9)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isPlaying) {
                                            Icon(
                                                Icons.Default.Videocam,
                                                contentDescription = "Playing",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
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
                                            text = lesson.title ?: "Lesson ${index + 1}",
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
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Done",
                                            tint = ProfessionalGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Empty course response
 */
private fun emptyCourse(courseId: Int) = CourseLessonsResponse(
    status = "empty",
    courseId = courseId,
    title = null,
    description = null,
    thumbnail = null,
    totalLessons = 0,
    lessons = emptyList()
)

/**
 * Embedded In-App WebView Video Player
 * Configured with Mobile User-Agent to render Google Drive previews & MP4 video streams smoothly
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InAppVideoWebView(videoUrl: String) {
    val embedUrl = remember(videoUrl) { formatEmbedVideoUrl(videoUrl) }

    Log.d(TAG, "InAppVideoWebView: raw='$videoUrl' → embed='$embedUrl'")

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.userAgentString = "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        Log.d(TAG, "WebView onPageStarted: $url")
                    }
                    override fun onPageFinished(view: WebView?, url: String?) {
                        Log.d(TAG, "WebView onPageFinished: $url")
                    }
                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        Log.e(
                            TAG,
                            "WebView onReceivedError: url=${request?.url} " +
                                    "code=${error?.errorCode} desc=${error?.description}"
                        )
                    }
                }
                webChromeClient = WebChromeClient()
                loadUrl(embedUrl)
            }
        },
        update = { webView ->
            if (webView.url != embedUrl) {
                Log.d(TAG, "WebView update() → loadUrl($embedUrl)")
                webView.loadUrl(embedUrl)
            }
        }
    )
}

fun formatEmbedVideoUrl(url: String): String {
    if (url.isBlank()) return "about:blank"
    val trimmed = url.trim()
    return when {
        trimmed.contains("drive.google.com/file/d/") -> {
            if (trimmed.endsWith("/preview")) trimmed
            else {
                val fileId = trimmed.substringAfter("/file/d/").substringBefore("/")
                "https://drive.google.com/file/d/$fileId/preview"
            }
        }
        trimmed.contains("youtube.com/watch?v=") -> {
            val videoId = trimmed.substringAfter("v=").substringBefore("&")
            "https://www.youtube-nocookie.com/embed/$videoId?autoplay=1&rel=0"
        }
        trimmed.contains("youtu.be/") -> {
            val videoId = trimmed.substringAfter("youtu.be/").substringBefore("?")
            "https://www.youtube-nocookie.com/embed/$videoId?autoplay=1&rel=0"
        }
        trimmed.contains("vimeo.com/") -> {
            val videoId = trimmed.substringAfter("vimeo.com/").substringBefore("?")
            "https://player.vimeo.com/video/$videoId?autoplay=1"
        }
        else -> trimmed
    }
}

@Preview(showBackground = true)
@Composable
fun CourseLessonsPreview() {
    IONOXELMSTheme {
        CourseLessonsScreen()
    }
}
