package com.bioverity.attendance.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bioverity.attendance.ui.components.BottomNavigationBar
import com.bioverity.attendance.ui.theme.Background
import com.bioverity.attendance.ui.theme.CardWhite
import com.bioverity.attendance.ui.theme.Navy
import com.bioverity.attendance.ui.theme.Success
import com.bioverity.attendance.ui.theme.SuccessLight
import com.bioverity.attendance.ui.theme.Teal
import com.bioverity.attendance.ui.theme.TealLight
import com.bioverity.attendance.ui.theme.TextPrimary
import com.bioverity.attendance.ui.theme.TextSecondary
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.bioverity.attendance.viewmodel.AttendanceViewModel
import com.bioverity.attendance.data.model.AttendanceRecord


@Composable
fun AttendanceScreen(
    viewModel: AttendanceViewModel,
    onNavigate: (Int) -> Unit,
    onFaceRecognition: () -> Unit
) {
    val attendance by viewModel.todayAttendance.collectAsState()

    Scaffold(
        containerColor = Background,

        bottomBar = {
            BottomNavigationBar(
                selectedIndex = 1,
                onItemSelected = onNavigate
            )
        }

    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            // =========================================================
            // HEADER
            // =========================================================

            Text(
                text = "Attendance",
                color = TextPrimary,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = "Mark your attendance securely",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            // =========================================================
            // TODAY CARD
            // =========================================================

            TodayAttendanceCard(
                attendance = attendance
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            // =========================================================
            // MARK ATTENDANCE CARD
            // =========================================================

            Card(
                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(22.dp),

                colors = CardDefaults.cardColors(
                    containerColor = CardWhite
                )
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                ) {

                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(TealLight),

                            contentAlignment =
                                Alignment.Center
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.CameraAlt,

                                contentDescription =
                                    "Face recognition",

                                tint = Teal,

                                modifier =
                                    Modifier.size(26.dp)
                            )
                        }

                        Spacer(
                            modifier = Modifier.width(14.dp)
                        )

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            Text(
                                text = "Mark Attendance",

                                color = TextPrimary,

                                style =
                                    MaterialTheme.typography.titleMedium
                            )

                            Spacer(
                                modifier = Modifier.height(3.dp)
                            )

                            Text(
                                text =
                                    "Verify your identity using face recognition",

                                color = TextSecondary,

                                style =
                                    MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )

                    Button(
                        onClick = {
                            onFaceRecognition()
                        },

                        modifier =
                            Modifier.fillMaxWidth(),

                        shape =
                            RoundedCornerShape(14.dp)
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.CameraAlt,

                            contentDescription = null
                        )

                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )

                        Text(
                            text = "Start Face Verification"
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            // =========================================================
            // VERIFICATION PROCESS
            // =========================================================

            Text(
                text = "Attendance Verification",
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            VerificationStep(
                number = "1",
                title = "Face Verification",
                description =
                    "Confirm your identity using the front camera",
                icon = Icons.Default.CameraAlt,
                active = true
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            VerificationStep(
                number = "2",
                title = "Location Verification",
                description =
                    "Confirm that you are within the office location",
                icon = Icons.Default.LocationOn,
                active = true
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            VerificationStep(
                number = "3",
                title = "Attendance Recorded",
                description =
                    "Your check-in or check-out is securely saved",
                icon = Icons.Default.CheckCircle,
                active = true
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            // =========================================================
            // HISTORY
            // =========================================================

            Row(
                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text = "Attendance History",

                    color = TextPrimary,

                    style =
                        MaterialTheme.typography.titleMedium
                )

                TextButton(
                    onClick = {}
                ) {

                    Text(
                        text = "View All",
                        color = Teal
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            AttendanceHistoryPreview()

            Spacer(
                modifier = Modifier.height(24.dp)
            )
        }
    }
}


// =============================================================
// TODAY ATTENDANCE
// =============================================================

@Composable
private fun TodayAttendanceCard(attendance: AttendanceRecord) {

    Card(
        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(20.dp),

        colors = CardDefaults.cardColors(
            containerColor = Teal
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column {

                    Text(
                        text = "Today",

                        color = Color.White.copy(
                            alpha = 0.75f
                        ),

                        style =
                            MaterialTheme.typography.bodySmall
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = "Attendance Status",

                        color = Color.White,

                        style =
                            MaterialTheme.typography.titleMedium
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = Color.White.copy(
                                alpha = 0.15f
                            ),
                            shape = CircleShape
                        ),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.CalendarMonth,

                        contentDescription = null,

                        tint = Color.White
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                TimeItem(
                    title = "Check In",
                    value = attendance.checkIn ?: "--:--"
                )

                TimeItem(
                    title = "Check Out",
                    value = attendance.checkOut ?: "--:--"
                )

                TimeItem(
                    title = "Hours",
                    value = attendance.workingHours
                )
            }
        }
    }
}


// =============================================================
// TIME ITEM
// =============================================================

@Composable
private fun TimeItem(
    title: String,
    value: String
) {

    Column {

        Text(
            text = title,

            color = Color.White.copy(
                alpha = 0.70f
            ),

            style =
                MaterialTheme.typography.bodySmall
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = value,

            color = Color.White,

            style =
                MaterialTheme.typography.titleMedium
        )
    }
}


// =============================================================
// VERIFICATION STEP
// =============================================================

@Composable
private fun VerificationStep(
    number: String,
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    active: Boolean
) {

    Card(
        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(16.dp),

        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color =
                            if (active) {
                                TealLight
                            } else {
                                MaterialTheme
                                    .colorScheme
                                    .surfaceVariant
                            },

                        shape = CircleShape
                    ),

                contentAlignment =
                    Alignment.Center
            ) {

                Icon(
                    imageVector = icon,

                    contentDescription = null,

                    tint =
                        if (active) {
                            Teal
                        } else {
                            TextSecondary
                        },

                    modifier =
                        Modifier.size(21.dp)
                )
            }

            Spacer(
                modifier = Modifier.width(14.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = title,

                    color = TextPrimary,

                    style =
                        MaterialTheme.typography.titleSmall
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text = description,

                    color = TextSecondary,

                    style =
                        MaterialTheme.typography.bodySmall
                )
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        color = TealLight,
                        shape = CircleShape
                    ),

                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text = number,

                    color = Teal,

                    style =
                        MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}


// =============================================================
// HISTORY PREVIEW
// =============================================================

@Composable
private fun AttendanceHistoryPreview() {

    Card(
        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(16.dp),

        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = SuccessLight,
                        shape = CircleShape
                    ),

                contentAlignment =
                    Alignment.Center
            ) {

                Icon(
                    imageVector =
                        Icons.Default.CheckCircle,

                    contentDescription = null,

                    tint = Success,

                    modifier =
                        Modifier.size(21.dp)
                )
            }

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "Attendance records",

                    color = TextPrimary,

                    style =
                        MaterialTheme.typography.titleSmall
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text =
                        "View your previous check-in and check-out records",

                    color = TextSecondary,

                    style =
                        MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}