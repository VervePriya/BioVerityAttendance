package com.bioverity.attendance.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bioverity.attendance.ui.theme.*

@Composable
fun AttendanceSummaryCard() {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Text(
                text = "September Attendance",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                SummaryItem(
                    value = "14",
                    label = "Present"
                )

                SummaryItem(
                    value = "1",
                    label = "Late"
                )

                SummaryItem(
                    value = "2",
                    label = "Absent"
                )

                SummaryItem(
                    value = "92%",
                    label = "Rate"
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    value: String,
    label: String
) {

    Column {

        Text(
            text = value,
            color = Navy,
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = label,
            color = TextSecondary,
            style = MaterialTheme.typography.labelSmall
        )
    }
}