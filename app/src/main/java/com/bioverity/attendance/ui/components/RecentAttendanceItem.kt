
package com.bioverity.attendance.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bioverity.attendance.data.model.AttendanceRecord
import com.bioverity.attendance.ui.theme.CardWhite
import com.bioverity.attendance.ui.theme.Success
import com.bioverity.attendance.ui.theme.TextPrimary
import com.bioverity.attendance.ui.theme.TextSecondary
import com.bioverity.attendance.ui.theme.Warning

@Composable
fun RecentAttendanceItem(
    record: AttendanceRecord
) {
    val checkInTime = record.checkIn
        ?.takeIf { it.isNotBlank() }
        ?: "Not marked"

    val checkOutTime = record.checkOut
        ?.takeIf { it.isNotBlank() }
        ?: "Not yet checked out"

    val statusColor = when (record.status) {
        "Late" -> Warning

        "Absent", "Not Marked" ->
            MaterialTheme.colorScheme.error

        else -> Success
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Date and attendance status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.date,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.padding(horizontal = 4.dp))

                Text(
                    text = record.status,
                    color = statusColor,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Check-in
            AttendanceTimeRow(
                label = "Check-in",
                time = checkInTime
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Check-out
            AttendanceTimeRow(
                label = "Check-out",
                time = checkOutTime,
                pending = record.checkOut.isNullOrBlank()
            )

            // Working hours
            if (record.workingHours.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Working hours: ${record.workingHours}",
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun AttendanceTimeRow(
    label: String,
    time: String,
    pending: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = time,
            color = if (pending) {
                TextSecondary
            } else {
                TextPrimary
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
