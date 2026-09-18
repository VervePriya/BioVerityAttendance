package com.bioverity.attendance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bioverity.attendance.ui.components.*
import com.bioverity.attendance.ui.theme.*
import com.bioverity.attendance.viewmodel.AttendanceViewModel

@Composable
fun HomeScreen(
    viewModel: AttendanceViewModel,
    onNavigate: (Int) -> Unit
) {

    val employee by viewModel.employee.collectAsState()
    val attendance by viewModel.todayAttendance.collectAsState()
    val recentAttendance by viewModel.recentAttendance.collectAsState()

    Scaffold(
        containerColor = Background,
        bottomBar = {
            BottomNavigationBar(
                selectedIndex = 0,
                onItemSelected = onNavigate
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(horizontal = 20.dp)
        ) {

            Spacer(
                modifier = Modifier.height(22.dp)
            )

            // HEADER

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(TealLight),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = employee.name
                                .take(1)
                                .uppercase(),
                            color = Teal,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    Spacer(
                        modifier = Modifier.width(12.dp)
                    )

                    Column {

                        Text(
                            text = "Good Morning",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            text = employee.name,
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                IconButton(
                    onClick = {}
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.NotificationsNone,
                        contentDescription = "Notifications",
                        tint = Navy
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "16 September 2026",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(
                modifier = Modifier.height(22.dp)
            )

            // ATTENDANCE CARD

            AttendanceStatusCard(
                attendance = attendance,
                onMarkAttendance = {
                    viewModel.markAttendance()
                }
            )

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            // LOCATION STATUS

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardWhite
                )
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                SuccessLight,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = "✓",
                            color = Success,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(
                        modifier = Modifier.width(12.dp)
                    )

                    Column {

                        Text(
                            text = "Attendance Location",
                            style = MaterialTheme.typography.titleSmall
                        )

                        Text(
                            text = "Location verification ready",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(22.dp)
            )

            Text(
                text = "This Month",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            AttendanceSummaryCard()

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Recent Attendance",
                    style = MaterialTheme.typography.titleMedium
                )

                TextButton(
                    onClick = {
                        onNavigate(1)
                    }
                ) {

                    Text(
                        text = "View All",
                        color = Teal
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            recentAttendance.forEach { record ->

                RecentAttendanceItem(
                    record = record
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )
        }
    }
}