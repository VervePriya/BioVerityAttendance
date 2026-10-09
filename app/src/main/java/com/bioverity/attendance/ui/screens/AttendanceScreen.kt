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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bioverity.attendance.data.model.AttendanceRecord
import com.bioverity.attendance.ui.components.BottomNavigationBar
import com.bioverity.attendance.ui.theme.Background
import com.bioverity.attendance.ui.theme.CardWhite
import com.bioverity.attendance.ui.theme.Success
import com.bioverity.attendance.ui.theme.SuccessLight
import com.bioverity.attendance.ui.theme.Teal
import com.bioverity.attendance.ui.theme.TealLight
import com.bioverity.attendance.ui.theme.TextPrimary
import com.bioverity.attendance.ui.theme.TextSecondary
import com.bioverity.attendance.viewmodel.AttendanceOverview
import com.bioverity.attendance.viewmodel.AttendanceViewModel

@Composable
fun AttendanceScreen(
    viewModel: AttendanceViewModel,
    onNavigate: (Int) -> Unit,
    onFaceRecognition: () -> Unit,
    onCalendarClick: () -> Unit
) {

    val todayAttendance by
    viewModel.todayAttendance.collectAsState()

    val monthlyAttendance by
    viewModel.monthlyAttendance.collectAsState()

    val attendanceOverview by
    viewModel.attendanceOverview.collectAsState()

    // ============================================================
    // TODAY'S WORK PLAN
    // ============================================================

    val todayWorkPlan by
    viewModel.todayWorkPlan.collectAsState()

    // ============================================================
    // RECENT RECORDS
    // ============================================================

    val recentRecords =
        monthlyAttendance.take(5)

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
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(
                    horizontal = 20.dp
                )
        ) {

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            // =====================================================
            // HEADER
            // =====================================================

            Text(
                text = "Attendance",
                color = TextPrimary,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = "Manage your daily attendance",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            // =====================================================
            // TODAY
            // =====================================================

            TodayAttendanceCard(
                attendance = todayAttendance,
                onCalendarClick = onCalendarClick
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            // =====================================================
            // TODAY'S WORK PLAN
            // =====================================================

            if (todayAttendance.checkIn == null) {

                TodayWorkPlanCard(
                    workPlan = todayWorkPlan,
                    onWorkPlanChange = {
                        viewModel.setTodayWorkPlan(it)
                    }
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )
            }

            // =====================================================
            // FACE RECOGNITION
            // =====================================================

            FaceAttendanceCard(
                onFaceRecognition = {
                    onFaceRecognition()
                }
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            // =====================================================
            // MONTHLY SUMMARY
            // =====================================================

            SectionTitle(
                title = "This Month"
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            AttendanceSummaryCard(
                overview = attendanceOverview
            )

            Spacer(
                modifier = Modifier.height(22.dp)
            )

            // =====================================================
            // RECENT HISTORY
            // =====================================================

            SectionTitle(
                title = "Recent Attendance"
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            if (recentRecords.isEmpty()) {

                EmptyAttendanceHistory()

            } else {

                recentRecords.forEachIndexed {
                        index,
                        record ->

                    AttendanceHistoryItem(
                        record = record
                    )

                    if (
                        index !=
                        recentRecords.lastIndex
                    ) {

                        Spacer(
                            modifier =
                                Modifier.height(10.dp)
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )
        }
    }
}


// =============================================================
// SECTION TITLE
// =============================================================

@Composable
private fun SectionTitle(
    title: String
) {

    Text(
        text = title,
        color = TextPrimary,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
}


// =============================================================
// TODAY ATTENDANCE
// =============================================================

@Composable
private fun TodayAttendanceCard(
    attendance: AttendanceRecord,
    onCalendarClick: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
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
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "Today's Attendance",
                        color =
                            Color.White.copy(
                                alpha = 0.78f
                            ),
                        style =
                            MaterialTheme.typography.bodySmall
                    )

                    Spacer(
                        modifier =
                            Modifier.height(5.dp)
                    )

                    Text(
                        text = attendance.status,
                        color = Color.White,
                        style =
                            MaterialTheme.typography.titleLarge,
                        fontWeight =
                            FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onCalendarClick,
                    modifier = Modifier.size(46.dp)
                ) {

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                color =
                                    Color.White.copy(
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
                            contentDescription =
                                "Open attendance calendar",
                            tint = Color.White,
                            modifier =
                                Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                TodayTimeItem(
                    title = "Check In",
                    value =
                        attendance.checkIn
                            ?: "--:--"
                )

                TodayTimeItem(
                    title = "Check Out",
                    value =
                        attendance.checkOut
                            ?: "--:--"
                )

                TodayTimeItem(
                    title = "Working Hours",
                    value =
                        attendance.workingHours
                )
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            // =====================================================
            // SHORT BY
            // =====================================================

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text = "Short By",
                    color =
                        Color.White.copy(
                            alpha = 0.70f
                        ),
                    style =
                        MaterialTheme.typography.labelSmall
                )

                Text(
                    text = attendance.shortBy,
                    color = Color.White,
                    style =
                        MaterialTheme.typography.titleSmall,
                    fontWeight =
                        FontWeight.SemiBold
                )
            }
        }
    }
}


// =============================================================
// TODAY TIME ITEM
// =============================================================

@Composable
private fun TodayTimeItem(
    title: String,
    value: String
) {

    Column {

        Text(
            text = title,
            color =
                Color.White.copy(
                    alpha = 0.70f
                ),
            style =
                MaterialTheme.typography.labelSmall
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = value,
            color = Color.White,
            style =
                MaterialTheme.typography.titleSmall,
            fontWeight =
                FontWeight.SemiBold
        )
    }
}


// =============================================================
// TODAY'S WORK PLAN CARD
// =============================================================

@Composable
private fun TodayWorkPlanCard(
    workPlan: String,
    onWorkPlanChange: (String) -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 1.dp
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {

            Text(
                text = "Today's Work Plan",
                color = TextPrimary,
                style =
                    MaterialTheme.typography.titleMedium,
                fontWeight =
                    FontWeight.SemiBold
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text =
                    "Write what you plan to work on today",
                color = TextSecondary,
                style =
                    MaterialTheme.typography.bodySmall
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            OutlinedTextField(
                value = workPlan,
                onValueChange = onWorkPlanChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                placeholder = {

                    Text(
                        text =
                            "What are you starting/finishing? Anything your manager should know about?"
                    )
                },
                shape =
                    RoundedCornerShape(14.dp),
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Text
                    )
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = "Optional",
                color = TextSecondary,
                style =
                    MaterialTheme.typography.labelSmall
            )
        }
    }
}


// =============================================================
// FACE ATTENDANCE CARD
// =============================================================

@Composable
private fun FaceAttendanceCard(
    onFaceRecognition: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 1.dp
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            TealLight
                        ),
                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = Teal,
                        modifier =
                            Modifier.size(27.dp)
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
                            MaterialTheme.typography.titleMedium,
                        fontWeight =
                            FontWeight.SemiBold
                    )

                    Spacer(
                        modifier = Modifier.height(3.dp)
                    )

                    Text(
                        text =
                            "Use face recognition to check in or check out",
                        color = TextSecondary,
                        style =
                            MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            Button(
                onClick = onFaceRecognition,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
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
                    text = "Verify with Face"
                )
            }
        }
    }
}


// =============================================================
// MONTHLY SUMMARY
// =============================================================

@Composable
private fun AttendanceSummaryCard(
    overview: AttendanceOverview
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 1.dp
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
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
                        text = "Attendance Overview",
                        color = TextPrimary,
                        style =
                            MaterialTheme.typography.titleMedium,
                        fontWeight =
                            FontWeight.SemiBold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(3.dp)
                    )

                    Text(
                        text = "Current month",
                        color = TextSecondary,
                        style =
                            MaterialTheme.typography.bodySmall
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = TealLight,
                            shape = CircleShape
                        ),
                    contentAlignment =
                        Alignment.Center
                ) {

                    Text(
                        text = "${overview.attendanceRate}%",
                        color = Teal,
                        style =
                            MaterialTheme.typography.titleSmall,
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            // =====================================================
            // FIRST ROW
            // =====================================================

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(6.dp)
            ) {

                SummaryItem(
                    modifier =
                        Modifier.weight(1f),
                    value = overview.present,
                    label = "Present",
                    icon =
                        Icons.Default.CheckCircle,
                    iconBackground =
                        SuccessLight,
                    iconTint =
                        Success
                )

                SummaryItem(
                    modifier =
                        Modifier.weight(1f),
                    value = overview.late,
                    label = "Late",
                    icon =
                        Icons.Default.Schedule,
                    iconBackground =
                        TealLight,
                    iconTint =
                        Teal
                )

                SummaryItem(
                    modifier =
                        Modifier.weight(1f),
                    value = overview.shortHours,
                    label = "Short Hours",
                    icon =
                        Icons.Default.AccessTime,
                    iconBackground =
                        Color(0xFFFFF8E1),
                    iconTint =
                        Color(0xFFC28A00)
                )

                SummaryItem(
                    modifier =
                        Modifier.weight(1f),
                    value = overview.absent,
                    label = "Absent",
                    icon =
                        Icons.Default.Warning,
                    iconBackground =
                        Color(0xFFFFF1F0),
                    iconTint =
                        Color(0xFFD9534F)
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            // =====================================================
            // SECOND ROW
            // =====================================================

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(6.dp)
            ) {

                SummaryItem(
                    modifier =
                        Modifier.weight(1f),
                    value = overview.leave,
                    label = "Leave",
                    icon =
                        Icons.Default.CalendarMonth,
                    iconBackground =
                        TealLight,
                    iconTint =
                        Teal
                )

                SummaryItem(
                    modifier =
                        Modifier.weight(1f),
                    value = overview.checkOutRequired,
                    label = "Not Check-out",
                    icon =
                        Icons.Default.Schedule,
                    iconBackground =
                        Color(0xFFFFF8E1),
                    iconTint =
                        Color(0xFFC28A00)
                )

                SummaryItem(
                    modifier =
                        Modifier.weight(1f),
                    value = overview.notMarked,
                    label = "Not Marked",
                    icon =
                        Icons.Default.AccessTime,
                    iconBackground =
                        Color(0xFFF1F3F5),
                    iconTint =
                        TextSecondary
                )
            }
        }
    }
}


// =============================================================
// SUMMARY ITEM
// =============================================================

@Composable
private fun SummaryItem(
    modifier: Modifier,
    value: Int,
    label: String,
    icon: ImageVector,
    iconBackground: Color,
    iconTint: Color
) {

    Column(
        modifier = modifier,
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .size(34.dp)
                .background(
                    color = iconBackground,
                    shape = CircleShape
                ),
            contentAlignment =
                Alignment.Center
        ) {

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(
            text = value.toString(),
            color = TextPrimary,
            style =
                MaterialTheme.typography.titleMedium,
            fontWeight =
                FontWeight.Bold
        )

        Text(
            text = label,
            color = TextSecondary,
            style =
                MaterialTheme.typography.labelSmall
        )
    }
}


// =============================================================
// ATTENDANCE HISTORY ITEM
// =============================================================

@Composable
private fun AttendanceHistoryItem(
    record: AttendanceRecord
) {

    val normalizedStatus =
        record.status
            .trim()
            .lowercase()

    val statusColor =
        when (normalizedStatus) {

            "present" ->
                Success

            "late" ->
                Teal

            "short hours" ->
                Color(0xFFC28A00)

            "absent" ->
                Color(0xFFD9534F)

            "leave" ->
                Teal

            "check-out required" ->
                Color(0xFFC28A00)

            "not marked" ->
                TextSecondary

            else ->
                TextSecondary
        }

    val statusBackground =
        when (normalizedStatus) {

            "present" ->
                SuccessLight

            "late" ->
                TealLight

            "short hours" ->
                Color(0xFFFFF8E1)

            "absent" ->
                Color(0xFFFFF1F0)

            "leave" ->
                TealLight

            "check-out required" ->
                Color(0xFFFFF8E1)

            "not marked" ->
                Color(0xFFF1F3F5)

            else ->
                Color(0xFFF1F3F5)
        }

    val statusIcon =
        when (normalizedStatus) {

            "present" ->
                Icons.Default.CheckCircle

            "late" ->
                Icons.Default.Schedule

            "short hours" ->
                Icons.Default.AccessTime

            "absent" ->
                Icons.Default.Warning

            "leave" ->
                Icons.Default.CalendarMonth

            "check-out required" ->
                Icons.Default.Schedule

            else ->
                Icons.Default.AccessTime
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            color = statusBackground,
                            shape = CircleShape
                        ),
                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(21.dp)
                    )
                }

                Spacer(
                    modifier = Modifier.width(12.dp)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = record.date,
                        color = TextPrimary,
                        style =
                            MaterialTheme.typography.titleSmall,
                        fontWeight =
                            FontWeight.SemiBold
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Row(
                        horizontalArrangement =
                            Arrangement.spacedBy(10.dp)
                    ) {

                        Text(
                            text =
                                "IN ${record.checkIn ?: "--:--"}",
                            color = TextSecondary,
                            style =
                                MaterialTheme.typography.bodySmall
                        )

                        Text(
                            text =
                                "OUT ${record.checkOut ?: "--:--"}",
                            color = TextSecondary,
                            style =
                                MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Column(
                    horizontalAlignment =
                        Alignment.End
                ) {

                    Text(
                        text = record.status,
                        color = statusColor,
                        style =
                            MaterialTheme.typography.labelMedium,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(3.dp)
                    )

                    Text(
                        text = record.workingHours,
                        color = TextSecondary,
                        style =
                            MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            // =====================================================
            // SHORT BY
            // =====================================================

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Text(
                    text = "Short By",
                    color = TextSecondary,
                    style =
                        MaterialTheme.typography.labelSmall
                )

                Text(
                    text = record.shortBy,
                    color = statusColor,
                    style =
                        MaterialTheme.typography.labelSmall,
                    fontWeight =
                        FontWeight.SemiBold
                )
            }
        }
    }
}


// =============================================================
// EMPTY HISTORY
// =============================================================

@Composable
private fun EmptyAttendanceHistory() {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 28.dp,
                    horizontal = 20.dp
                ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        color = TealLight,
                        shape = CircleShape
                    ),
                contentAlignment =
                    Alignment.Center
            ) {

                Icon(
                    imageVector =
                        Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = Teal,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = "No attendance yet",
                color = TextPrimary,
                style =
                    MaterialTheme.typography.titleSmall,
                fontWeight =
                    FontWeight.SemiBold
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text =
                    "Your attendance records will appear here.",
                color = TextSecondary,
                style =
                    MaterialTheme.typography.bodySmall
            )
        }
    }
}