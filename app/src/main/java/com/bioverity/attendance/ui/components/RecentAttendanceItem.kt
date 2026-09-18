package com.bioverity.attendance.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bioverity.attendance.data.model.AttendanceRecord
import com.bioverity.attendance.ui.theme.*

@Composable
fun RecentAttendanceItem(
    record: AttendanceRecord
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
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column {

                Text(
                    text = record.date,
                    style = MaterialTheme.typography.titleSmall
                )

                Spacer(
                    modifier = Modifier.height(5.dp)
                )

                Text(
                    text = "${record.checkIn ?: "--"}  →  ${record.checkOut ?: "--"}",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Column(
                horizontalAlignment = androidx.compose.ui.Alignment.End
            ) {

                Text(
                    text = record.status,
                    color = if (record.status == "Late") {
                        Warning
                    } else {
                        Success
                    },
                    style = MaterialTheme.typography.labelMedium
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = record.workingHours,
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}