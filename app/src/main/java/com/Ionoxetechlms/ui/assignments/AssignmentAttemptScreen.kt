package com.Ionoxetechlms.ui.assignments

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.Ionoxetechlms.data.api.ApiClient
import com.Ionoxetechlms.data.api.AssignmentDetailResponse
import com.Ionoxetechlms.data.api.AssignmentSubmitRequest
import com.Ionoxetechlms.data.api.QuestionDetail
import com.Ionoxetechlms.ui.theme.IONOXELMSTheme
import com.Ionoxetechlms.ui.theme.ProfessionalGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Assignment Attempt / Proctored Exam Screen in Jetpack Compose
 * Replicates web 'assignment_attempt.php' with BackHandler
 */
@Composable
fun AssignmentAttemptScreen(
    studentId: Int = 2,
    assignmentId: Int = 1,
    onSubmitted: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var detail by remember { mutableStateOf<AssignmentDetailResponse?>(null) }

    var currentQIndex by remember { mutableIntStateOf(0) }
    val studentAnswers = remember { mutableStateMapOf<String, String>() }
    var showSubmitDialog by remember { mutableStateOf(false) }

    // System Back Handler: 1-step back navigation
    BackHandler {
        onBackClick()
    }

    // Countdown timer (in seconds)
    var remainingSeconds by remember { mutableIntStateOf(3600) }

    LaunchedEffect(assignmentId) {
        try {
            val res = ApiClient.apiService.getAssignmentDetail(studentId, assignmentId)
            if (res.isSuccessful && res.body() != null) {
                detail = res.body()
            }
        } catch (_: Exception) {
            // Mock Exam Data
            detail = AssignmentDetailResponse(
                status = "success",
                assignmentId = assignmentId,
                title = "Machine Learning Regression Model Assignment",
                courseName = "Artificial Intelligence & Machine Learning",
                totalMarks = 100,
                resolvedType = "mcq",
                submitted = false,
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
                        )
                    ),
                    QuestionDetail(
                        id = 102,
                        question = "Explain the difference between Overfitting and Underfitting in Machine Learning models.",
                        type = "descriptive",
                        marks = 20,
                        options = emptyMap()
                    )
                )
            )
        } finally {
            isLoading = false
        }
    }

    // Timer countdown effect
    LaunchedEffect(remainingSeconds) {
        if (remainingSeconds > 0 && !isSubmitting && !isLoading) {
            delay(1000L)
            remainingSeconds -= 1
        } else if (remainingSeconds == 0 && !isSubmitting) {
            // Auto submit when time expires
            showSubmitDialog = true
        }
    }

    val questions = detail?.questions ?: emptyList()
    val totalQuestions = questions.size.coerceAtLeast(1)
    val answeredCount = studentAnswers.size

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF1F3F8)
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ProfessionalGreen)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {

                // Exam Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NavyDark)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = detail?.title ?: "Exam Mode",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${detail?.courseName ?: "Course"} • $totalQuestions Questions",
                                fontSize = 10.sp,
                                color = LightMuted
                            )
                        }

                        // Timer Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = OrangeBg,
                            border = BorderStroke(1.dp, OrangeBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = "Timer", tint = BrandOrange, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                val minutes = remainingSeconds / 60
                                val seconds = remainingSeconds % 60
                                Text(
                                    text = String.format(Locale.US, "%02d:%02d", minutes, seconds),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandOrange
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Finish Button
                        Button(
                            onClick = { showSubmitDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ProfessionalGreen),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("FINISH", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                // Top Progress Bar
                LinearProgressIndicator(
                    progress = { ((currentQIndex + 1) / totalQuestions.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = BrandOrange,
                    trackColor = LightBorder
                )

                // Questions Dots Palette Row
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        itemsIndexed(questions) { index, q ->
                            val isCurrent = (index == currentQIndex)
                            val isAnswered = studentAnswers.containsKey(q.id.toString())

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            isCurrent -> BrandOrange
                                            isAnswered -> ProfessionalGreen
                                            else -> Color(0xFFF8F9FC)
                                        }
                                    )
                                    .clickable { currentQIndex = index },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrent || isAnswered) Color.White else LightMuted
                                )
                            }
                        }
                    }
                }

                // Active Question Card Body
                if (questions.isNotEmpty()) {
                    val activeQ = questions[currentQIndex.coerceIn(0, questions.size - 1)]
                    val qKey = activeQ.id.toString()
                    val activeAnswer = studentAnswers[qKey] ?: ""
                    val isDescriptive = activeQ.type.lowercase() == "descriptive"

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, LightBorder)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {

                                // Question Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(100.dp),
                                        color = if (isDescriptive) PurpleBg else OrangeBg,
                                        border = BorderStroke(1.dp, if (isDescriptive) Color(0x337C3AED) else OrangeBorder)
                                    ) {
                                        Text(
                                            text = if (isDescriptive) "DESCRIPTIVE" else "MCQ",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDescriptive) PurpleAccent else BrandOrange,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }

                                    Text(
                                        text = "Question ${currentQIndex + 1} of $totalQuestions • ${activeQ.marks} pts",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LightMuted
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Question Text
                                Text(
                                    text = activeQ.question ?: "Question text",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyDark,
                                    lineHeight = 22.sp
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // MCQ Options or Descriptive Text Field
                                if (!isDescriptive && activeQ.options.isNotEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        activeQ.options.forEach { (key, optionText) ->
                                            val isSelected = activeAnswer.equals(key, ignoreCase = true)

                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        studentAnswers[qKey] = key
                                                    },
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (isSelected) OrangeBg else Color(0xFFF8F9FC),
                                                border = BorderStroke(1.dp, if (isSelected) BrandOrange else LightBorder)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(28.dp)
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(if (isSelected) BrandOrange else Color.White),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = key,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSelected) Color.White else LightMuted
                                                        )
                                                    }

                                                    Spacer(modifier = Modifier.width(12.dp))

                                                    Text(
                                                        text = optionText,
                                                        fontSize = 13.sp,
                                                        color = NavyDark,
                                                        modifier = Modifier.weight(1f)
                                                    )

                                                    if (isSelected) {
                                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BrandOrange, modifier = Modifier.size(18.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // Descriptive Multiline Text Area
                                    OutlinedTextField(
                                        value = activeAnswer,
                                        onValueChange = { studentAnswers[qKey] = it },
                                        placeholder = { Text("Write your answer here in detail...", fontSize = 13.sp, color = LightMuted) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(160.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color(0xFFF8F9FC),
                                            focusedBorderColor = BrandOrange,
                                            unfocusedBorderColor = LightBorder
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Footer Navigation Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$answeredCount / $totalQuestions answered",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightMuted
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (currentQIndex > 0) {
                                OutlinedButton(
                                    onClick = { currentQIndex -= 1 },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, LightBorder)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Back", fontSize = 12.sp)
                                }
                            }

                            if (currentQIndex < totalQuestions - 1) {
                                Button(
                                    onClick = { currentQIndex += 1 },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Next", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next", modifier = Modifier.size(14.dp))
                                }
                            } else {
                                Button(
                                    onClick = { showSubmitDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = ProfessionalGreen),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Submit", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("SUBMIT", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Submit Confirmation Dialog
        if (showSubmitDialog) {
            AlertDialog(
                onDismissRequest = { showSubmitDialog = false },
                title = { Text("Submit Exam?", fontWeight = FontWeight.Bold, color = NavyDark) },
                text = {
                    Column {
                        Text("Are you sure you want to submit your answers? You cannot change them afterwards.", fontSize = 13.sp, color = LightMuted)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Answered: $answeredCount of $totalQuestions", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ProfessionalGreen)
                        Text("Skipped: ${totalQuestions - answeredCount}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandOrange)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showSubmitDialog = false
                            isSubmitting = true
                            coroutineScope.launch {
                                try {
                                    ApiClient.apiService.submitAssignment(
                                        AssignmentSubmitRequest(
                                            studentId = studentId,
                                            assignmentId = assignmentId,
                                            answers = studentAnswers.toMap()
                                        )
                                    )
                                } catch (_: Exception) {}
                                isSubmitting = false
                                onSubmitted()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ProfessionalGreen)
                    ) {
                        Text("Yes, Submit Now", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSubmitDialog = false }) {
                        Text("Review Questions", color = LightMuted)
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AssignmentAttemptPreview() {
    IONOXELMSTheme {
        AssignmentAttemptScreen()
    }
}
