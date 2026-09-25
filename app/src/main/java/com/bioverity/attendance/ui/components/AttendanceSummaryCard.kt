
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bioverity.attendance.data.model.AttendanceRecord
import com.bioverity.attendance.ui.theme.CardWhite
import com.bioverity.attendance.ui.theme.Navy
import com.bioverity.attendance.ui.theme.TextPrimary
import com.bioverity.attendance.ui.theme.TextSecondary

@Composable
fun AttendanceSummaryCard(
    attendanceRecords: List<AttendanceRecord>
) {

    val presentCount =
        attendanceRecords.count { record ->
            record.status.equals(
                "Present",
                ignoreCase = true
            )
        }

    val lateCount =
        attendanceRecords.count { record ->
            record.status.equals(
                "Late",
                ignoreCase = true
            )
        }

    val absentCount =
        attendanceRecords.count { record ->
            record.status.equals(
                "Absent",
                ignoreCase = true
            )
        }

    val totalDays =
        presentCount + lateCount + absentCount

    val attendanceRate =
        if (totalDays > 0) {
            ((presentCount + lateCount) * 100) / totalDays
        } else {
            0
        }

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
                text = "Monthly Attendance",

                color = TextPrimary,

                style =
                    MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                SummaryItem(
                    value = presentCount.toString(),
                    label = "Present"
                )

                SummaryItem(
                    value = lateCount.toString(),
                    label = "Late"
                )

                SummaryItem(
                    value = absentCount.toString(),
                    label = "Absent"
                )

                SummaryItem(
                    value = "$attendanceRate%",
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

            style =
                MaterialTheme.typography.titleLarge
        )

        Text(
            text = label,

            color = TextSecondary,

            style =
                MaterialTheme.typography.labelSmall
        )
    }
}

