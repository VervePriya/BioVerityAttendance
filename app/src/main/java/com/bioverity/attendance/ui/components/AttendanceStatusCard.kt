package com.bioverity.attendance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bioverity.attendance.data.model.AttendanceRecord
import com.bioverity.attendance.ui.theme.*

@Composable
fun AttendanceStatusCard(
    attendance: AttendanceRecord,
    onMarkAttendance: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Navy
        )
    ) {

        Column(
            modifier = Modifier.padding(22.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column {

                    Text(
                        text = "Today's Attendance",
                        color = Color.White.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = attendance.status,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            Teal.copy(alpha = 0.2f),
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {

                AttendanceTime(
                    title = "Check In",
                    value = attendance.checkIn ?: "--:--"
                )

                Spacer(
                    modifier = Modifier.width(35.dp)
                )

                AttendanceTime(
                    title = "Check Out",
                    value = attendance.checkOut ?: "--:--"
                )

                Spacer(
                    modifier = Modifier.width(35.dp)
                )

                AttendanceTime(
                    title = "Hours",
                    value = attendance.workingHours
                )
            }

            Spacer(
                modifier = Modifier.height(22.dp)
            )

            Button(
                onClick = onMarkAttendance,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Teal
                )
            ) {

                Icon(
                    Icons.Default.Fingerprint,
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text(
                    text = "Mark Attendance"
                )
            }
        }
    }
}

@Composable
private fun AttendanceTime(
    title: String,
    value: String
) {

    Column {

        Text(
            text = title,
            color = Color.White.copy(alpha = 0.65f),
            style = MaterialTheme.typography.labelSmall
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = value,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium
        )
    }
}