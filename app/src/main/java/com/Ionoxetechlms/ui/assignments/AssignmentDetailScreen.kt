package com.Ionoxetechlms.ui.assignments

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
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
import com.Ionoxetechlms.data.api.AssignmentDetailResponse
import com.Ionoxetechlms.data.api.QuestionDetail
import com.Ionoxetechlms.ui.theme.IONOXELMSTheme
import com.Ionoxetechlms.ui.theme.LightGreenSoft
import com.Ionoxetechlms.ui.theme.ProfessionalGreen

val HeroNavy = Color(0xFF1A1F2E)
val LightBorder = Color(0xFFE8ECF4)
val AmberWarn = Color(0xFFD97706)
val AmberBg = Color(0xFFFFFBEB)

/**
 * Assignment Detail & Results Review Screen in Jetpack Compose
 * Replicates web 'assignment_view.php'
 */
@Composable
fun AssignmentDetailScreen(
    studentId: Int = 2,
    assignmentId: Int = 1,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var detail by remember { mutableStateOf<AssignmentDetailResponse?>(null) }

    LaunchedEffect(assignmentId) {
        try {
            val res = ApiClient.apiService.getAssignmentDetail(studentId, assignmentId)
            if (res.isSuccessful && res.body() != null) {
                detail = res.body()
            }
        } catch (_: Exception) {
            // Mock Detail
            detail = AssignmentDetailResponse(
                status = "success",
                assignmentId = assignmentId,
                title = "Machine Learning Regression Model Assignment",
                courseName = "Artificial Intelligence & Machine Learning",
                totalMarks = 100,
                obtainedMarks = 90,
                percentage = 90,
                submittedAt = "19 Apr 2026, 06:30 PM",
                filePath = "https://ionox.in/uploads/assignments/mern_app.pdf",
                resolvedType = "mcq",
                submitted = true,
                correctCount = 9,
                wrongCount = 1,
                skippedCount = 0,
                questions = listOf(
                    QuestionDetail(
                        id = 101,
                        question = "What is the primary goal of Supervised Machine Learning?",
                        type = "mcq",
                        marks = 10,
                        options = mapOf(
                            "A" to "Clustering unlabeled data",
                            "B" to "Predicting target outputs using labeled data",
                            "C" to "Random number generation",
                            "D" to "Data compression"
                        ),
                        correctOption = "B",
                        studentAnswer = "B",
                        awardedMarks = 10f,
                        state = "correct"
                    )
                )
            )
        } finally {
            isLoading = false
        }
    }

    val data = detail ?: AssignmentDetailResponse("success")

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF1F3F8)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Topbar Navigation Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = HeroNavy
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (data.submitted) "ASSIGNMENT REVIEW" else "ASSIGNMENT PREVIEW",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7A8BA5),
                            letterSpacing = 0.7.sp
                        )
                        Text(
                            text = data.title ?: "Assignment Detail",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = HeroNavy,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
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
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Score Hero Banner
                    if (data.submitted) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = HeroNavy)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(
                                        text = "YOUR SCORE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.5f),
                                        letterSpacing = 0.8.sp
                                    )

                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = "${data.percentage ?: "--"}",
                                            fontSize = 50.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            lineHeight = 50.sp
                                        )
                                        Text(
                                            text = "%",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White.copy(alpha = 0.5f),
                                            modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                                        )
                                    }

                                    Text(
                                        text = when {
                                            (data.percentage ?: 0) >= 80 -> "🎉 Excellent work!"
                                            (data.percentage ?: 0) >= 50 -> "👍 Good effort!"
                                            else -> "📚 Keep practising!"
                                        },
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    LinearProgressIndicator(
                                        progress = { ((data.percentage ?: 0) / 100f).coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(100.dp)),
                                        color = ProfessionalGreen,
                                        trackColor = Color.White.copy(alpha = 0.15f)
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))
                                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("POINTS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.4f))
                                        Text("${data.obtainedMarks ?: 0} / ${data.totalMarks}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("COURSE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.4f))
                                        Text(data.courseName ?: "Course", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }

                        // Breakdown Pills (Correct / Wrong / Skipped)
                        if (data.resolvedType.lowercase() == "mcq") {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    BreakdownPill(modifier = Modifier.weight(1f), count = data.correctCount, label = "Correct", color = ProfessionalGreen, bgColor = LightGreenSoft)
                                    BreakdownPill(modifier = Modifier.weight(1f), count = data.wrongCount, label = "Wrong", color = Color(0xFFDC2626), bgColor = Color(0xFFFEF2F2))
                                    BreakdownPill(modifier = Modifier.weight(1f), count = data.skippedCount, label = "Skipped", color = AmberWarn, bgColor = AmberBg)
                                }
                            }
                        }

                        // Submitted File Card
                        if (!data.filePath.isNullOrBlank()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, LightBorder)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(OrangeBg),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.FilePresent,
                                                    contentDescription = null,
                                                    tint = BrandOrange,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text("Submitted Work File", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HeroNavy)
                                                Text("Download or open attachment", fontSize = 10.sp, color = Color(0xFF7A8BA5))
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(data.filePath))
                                                context.startActivity(intent)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = ProfessionalGreen),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.White, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Open", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Questions Section Label
                    item {
                        Text(
                            text = "Questions (${data.questions.size} Total)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = HeroNavy
                        )
                    }

                    // Question Review List
                    itemsIndexed(data.questions) { index, q ->
                        QuestionReviewCard(index = index + 1, q = q, isSubmitted = data.submitted)
                    }
                }
            }
        }
    }
}

@Composable
fun BreakdownPill(
    modifier: Modifier = Modifier,
    count: Int,
    label: String,
    color: Color,
    bgColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "$count", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun QuestionReviewCard(
    index: Int,
    q: QuestionDetail,
    isSubmitted: Boolean
) {
    val qState = q.state.lowercase()
    val isCorrect = qState == "correct"
    val isWrong = qState == "wrong"
    val isSkipped = qState == "skipped"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(
            1.dp,
            when {
                isCorrect -> ProfessionalGreen
                isWrong -> Color(0xFFDC2626)
                isSkipped -> AmberWarn
                else -> LightBorder
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Question Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Q$index",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7A8BA5)
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    if (isSubmitted) {
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = when {
                                isCorrect -> LightGreenSoft
                                isWrong -> Color(0xFFFEF2F2)
                                else -> AmberBg
                            },
                            border = BorderStroke(1.dp, when {
                                isCorrect -> Color(0x3316A34A)
                                isWrong -> Color(0x33DC2626)
                                else -> Color(0x33D97706)
                            })
                        ) {
                            Text(
                                text = when {
                                    isCorrect -> "CORRECT"
                                    isWrong -> "WRONG"
                                    else -> "SKIPPED"
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    isCorrect -> ProfessionalGreen
                                    isWrong -> Color(0xFFDC2626)
                                    else -> AmberWarn
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "${q.marks} pts",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = HeroNavy
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Question Text
            Text(
                text = q.question ?: "Question text",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = HeroNavy,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Options List
            q.options.forEach { (key, optionText) ->
                val isCorrectOption = key.equals(q.correctOption, ignoreCase = true)
                val isStudentSelected = key.equals(q.studentAnswer, ignoreCase = true)

                val optionBg = when {
                    isSubmitted && isStudentSelected && isCorrectOption -> LightGreenSoft
                    isSubmitted && isStudentSelected && !isCorrectOption -> Color(0xFFFEF2F2)
                    isSubmitted && isCorrectOption -> LightGreenSoft
                    else -> Color(0xFFF8F9FC)
                }

                val optionBorder = when {
                    isSubmitted && isStudentSelected && isCorrectOption -> ProfessionalGreen
                    isSubmitted && isStudentSelected && !isCorrectOption -> Color(0xFFDC2626)
                    isSubmitted && isCorrectOption -> ProfessionalGreen
                    else -> LightBorder
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = optionBg,
                    border = BorderStroke(1.dp, optionBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when {
                                isSubmitted && isCorrectOption -> ProfessionalGreen
                                isSubmitted && isStudentSelected -> Color(0xFFDC2626)
                                else -> Color.White
                            },
                            border = BorderStroke(1.dp, LightBorder)
                        ) {
                            Text(
                                text = key,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSubmitted && (isCorrectOption || isStudentSelected)) Color.White else Color(0xFF7A8BA5),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = optionText,
                            fontSize = 13.sp,
                            color = HeroNavy,
                            modifier = Modifier.weight(1f)
                        )

                        if (isSubmitted && isCorrectOption) {
                            Icon(Icons.Default.Check, contentDescription = "Correct", tint = ProfessionalGreen, modifier = Modifier.size(16.dp))
                        } else if (isSubmitted && isStudentSelected && !isCorrectOption) {
                            Icon(Icons.Default.Cancel, contentDescription = "Wrong", tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Correct Option Callout (if student answered wrong or skipped)
            if (isSubmitted && !q.correctOption.isNullOrBlank() && (!isCorrect)) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = LightGreenSoft,
                    border = BorderStroke(1.dp, Color(0x3316A34A))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = ProfessionalGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Correct Answer: Option ${q.correctOption}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ProfessionalGreen
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AssignmentDetailPreview() {
    IONOXELMSTheme {
        AssignmentDetailScreen()
    }
}
