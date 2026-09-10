package com.Ionoxetechlms.ui.profile

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Ionoxetechlms.data.api.ApiClient
import com.Ionoxetechlms.data.api.ProfileResponse
import com.Ionoxetechlms.ui.theme.IONOXELMSTheme
import com.Ionoxetechlms.ui.theme.LightGreenSoft
import com.Ionoxetechlms.ui.theme.ProfessionalGreen
import kotlinx.coroutines.launch

val BrandOrange = Color(0xFFF97316)
val OrangeBg = Color(0xFFFFF7ED)
val OrangeBorder = Color(0x33F97316)
val NavyDark = Color(0xFF0F172A)
val LightMuted = Color(0xFF94A3B8)

/**
 * Student Profile Screen in Jetpack Compose
 * Replicates web 'profile.php'
 */
@Composable
fun ProfileScreen(
    studentId: Int = 2,
    studentName: String = "Student",
    onLogoutClick: () -> Unit = {},
    onCertificatesClick: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var profileData by remember { mutableStateOf<ProfileResponse?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    // Edit form fields
    var editName by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editAddress by remember { mutableStateOf("") }

    LaunchedEffect(studentId) {
        try {
            val res = ApiClient.apiService.getProfile(studentId)
            if (res.isSuccessful && res.body() != null) {
                profileData = res.body()
            }
        } catch (_: Exception) {
            // Mock Fallback
            profileData = ProfileResponse(
                status = "success",
                id = studentId,
                studentId = "IO-ST251101",
                name = studentName,
                email = "mounika030721@gmail.com",
                phone = "+91 98765 43210",
                address = "Hyderabad, Telangana",
                batchName = "AI/ML Batch 2026",
                createdAt = "15 Nov 2025"
            )
        } finally {
            isLoading = false
        }
    }

    val p = profileData ?: ProfileResponse("success")

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF0F4F8)
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ProfessionalGreen)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Page Header Title
                item {
                    Text(
                        text = "My Profile",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )
                }

                // Hero Profile Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar Circle
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(BrandOrange),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val initial = (p.name ?: "S").take(1).uppercase()
                                    Text(
                                        text = initial,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = p.name ?: "Student",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NavyDark
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = p.email ?: "",
                                        fontSize = 12.sp,
                                        color = LightMuted
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Badges Row
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(100.dp),
                                    color = OrangeBg,
                                    border = BorderStroke(1.dp, OrangeBorder)
                                ) {
                                    Text(
                                        text = "ID: ${p.studentId ?: "IO-ST251101"}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandOrange,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(100.dp),
                                    color = LightGreenSoft,
                                    border = BorderStroke(1.dp, Color(0x3316A34A))
                                ) {
                                    Text(
                                        text = p.batchName ?: "General Batch",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ProfessionalGreen,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Edit Profile Button
                            Button(
                                onClick = {
                                    editName = p.name ?: ""
                                    editEmail = p.email ?: ""
                                    editPhone = p.phone ?: ""
                                    editAddress = p.address ?: ""
                                    showEditDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Edit Profile", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            }
                        }
                    }
                }

                // Personal Info Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = BrandOrange, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Personal Information", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(12.dp))

                            ProfileInfoRow("FULL NAME", p.name ?: "Not provided")
                            Spacer(modifier = Modifier.height(10.dp))
                            ProfileInfoRow("EMAIL ADDRESS", p.email ?: "Not provided")
                            Spacer(modifier = Modifier.height(10.dp))
                            ProfileInfoRow("PHONE NUMBER", if (!p.phone.isNullOrBlank()) p.phone else "Not provided")
                            Spacer(modifier = Modifier.height(10.dp))
                            ProfileInfoRow("ADDRESS", if (!p.address.isNullOrBlank()) p.address else "Not provided")
                        }
                    }
                }

                // Academic Details Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.School, contentDescription = null, tint = ProfessionalGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Academic Details", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(12.dp))

                            ProfileInfoRow("STUDENT ID", p.studentId ?: "IO-ST251101")
                            Spacer(modifier = Modifier.height(10.dp))
                            ProfileInfoRow("BATCH", p.batchName ?: "General Batch")
                            Spacer(modifier = Modifier.height(10.dp))
                            ProfileInfoRow("ACCOUNT CREATED", p.createdAt ?: "Joined recently")
                            Spacer(modifier = Modifier.height(10.dp))
                            ProfileInfoRow("ACCOUNT STATUS", "Active ✓")
                        }
                    }
                }

                // Certifications Quick Action Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCertificatesClick() },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(OrangeBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = BrandOrange, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Certifications & Credentials", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                                    Text("View and download verified certificates", fontSize = 11.sp, color = LightMuted)
                                }
                            }

                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Open", tint = BrandOrange, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Logout Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLogoutClick() },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        border = BorderStroke(1.dp, Color(0x33DC2626))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sign Out", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sign Out of Account", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                        }
                    }
                }
            }
        }

        // Edit Profile Modal Dialog
        if (showEditDialog) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold, color = NavyDark) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Full Name *") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editEmail,
                            onValueChange = { editEmail = it },
                            label = { Text("Email Address *") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editPhone,
                            onValueChange = { editPhone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editAddress,
                            onValueChange = { editAddress = it },
                            label = { Text("Address") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showEditDialog = false
                            isLoading = true
                            coroutineScope.launch {
                                try {
                                    val res = ApiClient.apiService.updateProfile(
                                        studentId = studentId,
                                        updateProfile = 1,
                                        name = editName,
                                        email = editEmail,
                                        phone = editPhone,
                                        address = editAddress
                                    )
                                    if (res.isSuccessful && res.body() != null) {
                                        profileData = res.body()
                                    }
                                } catch (_: Exception) {}
                                isLoading = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                    ) {
                        Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("Cancel", color = LightMuted)
                    }
                }
            )
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = LightMuted, letterSpacing = 0.6.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NavyDark)
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    IONOXELMSTheme {
        ProfileScreen()
    }
}
