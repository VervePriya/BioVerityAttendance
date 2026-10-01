
package com.bioverity.attendance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.bioverity.attendance.data.model.AttendanceRecord
import com.bioverity.attendance.ui.theme.Background
import com.bioverity.attendance.ui.theme.CardWhite
import com.bioverity.attendance.ui.theme.Success
import com.bioverity.attendance.ui.theme.TextPrimary
import com.bioverity.attendance.ui.theme.TextSecondary
import com.bioverity.attendance.viewmodel.AttendanceViewModel

import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


@Composable
fun CalendarScreen(
    viewModel: AttendanceViewModel,
    onBack: () -> Unit
) {

    // =========================================================
    // SRI LANKA TIMEZONE
    // =========================================================

    val sriLankaZone =
        remember {
            ZoneId.of("Asia/Colombo")
        }


    // =========================================================
    // MONTHLY ATTENDANCE FROM VIEWMODEL
    // =========================================================

    val monthlyAttendance by
    viewModel.monthlyAttendance.collectAsState()


    // =========================================================
    // TODAY
    // =========================================================

    val today =
        remember {
            LocalDate.now(sriLankaZone)
        }


    // =========================================================
    // DISPLAYED MONTH
    // =========================================================

    var displayedMonth by remember {

        mutableStateOf(
            YearMonth.from(today)
        )
    }


    // =========================================================
    // SELECTED DATE
    // =========================================================

    var selectedDate by remember {

        mutableStateOf(today)
    }


    // =========================================================
    // DATABASE DATE FORMAT
    //
    // Your ViewModel uses:
    // "dd MMM yyyy"
    // Example:
    // "01 Oct 2026"
    // =========================================================

    val attendanceDateFormatter =
        remember {

            DateTimeFormatter.ofPattern(
                "dd MMM yyyy",
                Locale.ENGLISH
            )
        }


    // =========================================================
    // SELECTED ATTENDANCE
    // =========================================================

    val selectedAttendance =
        monthlyAttendance.firstOrNull {

            try {

                LocalDate.parse(
                    it.date,
                    attendanceDateFormatter
                ) == selectedDate

            } catch (_: Exception) {

                false
            }
        }


    // =========================================================
    // CALENDAR INFORMATION
    // =========================================================

    val firstDay =
        displayedMonth.atDay(1)

    val daysInMonth =
        displayedMonth.lengthOfMonth()

    // Monday = 0
    // Tuesday = 1
    // ...
    // Sunday = 6

    val firstDayOffset =
        firstDay.dayOfWeek.value - 1


    // =========================================================
    // MAIN SCREEN
    // =========================================================

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {


        // =====================================================
        // TOP BAR
        // =====================================================

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 14.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onBack
            ) {

                Icon(
                    imageVector =
                        Icons.Default.ArrowBack,

                    contentDescription =
                        "Back",

                    tint =
                        TextPrimary
                )
            }


            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            ) {

                Text(
                    text = "Attendance Calendar",

                    fontSize = 20.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        TextPrimary
                )

                Text(
                    text =
                        "View your monthly attendance",

                    fontSize = 13.sp,

                    color =
                        TextSecondary
                )
            }


            Icon(
                imageVector =
                    Icons.Default.Event,

                contentDescription =
                    null,

                tint =
                    TextPrimary,

                modifier =
                    Modifier.size(26.dp)
            )
        }


        // =====================================================
        // MONTH SELECTOR
        // =====================================================

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp
                ),

            shape =
                RoundedCornerShape(16.dp),

            colors =
                CardDefaults.cardColors(
                    containerColor =
                        CardWhite
                )
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 8.dp,
                        vertical = 8.dp
                    ),

                verticalAlignment =
                    Alignment.CenterVertically,

                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                IconButton(
                    onClick = {

                        displayedMonth =
                            displayedMonth.minusMonths(1)

                        selectedDate =
                            displayedMonth.atDay(1)
                    }
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.KeyboardArrowLeft,

                        contentDescription =
                            "Previous month",

                        tint =
                            TextPrimary
                    )
                }


                Text(
                    text =
                        displayedMonth.format(
                            DateTimeFormatter.ofPattern(
                                "MMMM yyyy",
                                Locale.ENGLISH
                            )
                        ),

                    fontSize = 18.sp,

                    fontWeight =
                        FontWeight.SemiBold,

                    color =
                        TextPrimary
                )


                IconButton(
                    onClick = {

                        val nextMonth =
                            displayedMonth.plusMonths(1)

                        if (
                            !nextMonth.isAfter(
                                YearMonth.from(today)
                            )
                        ) {

                            displayedMonth =
                                nextMonth

                            selectedDate =
                                nextMonth.atDay(1)
                        }
                    }
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.KeyboardArrowRight,

                        contentDescription =
                            "Next month",

                        tint =
                            TextPrimary
                    )
                }
            }
        }


        Spacer(
            modifier =
                Modifier.height(16.dp)
        )


        // =====================================================
        // WEEK DAYS
        // =====================================================

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp
                )
        ) {

            listOf(
                "MON",
                "TUE",
                "WED",
                "THU",
                "FRI",
                "SAT",
                "SUN"
            ).forEach { day ->

                Text(
                    text = day,

                    modifier =
                        Modifier.weight(1f),

                    textAlign =
                        TextAlign.Center,

                    fontSize = 11.sp,

                    fontWeight =
                        FontWeight.SemiBold,

                    color =
                        TextSecondary
                )
            }
        }


        Spacer(
            modifier =
                Modifier.height(8.dp)
        )


        // =====================================================
        // CALENDAR GRID
        // =====================================================

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp
                )
        ) {

            val totalCells =
                firstDayOffset + daysInMonth

            val rows =
                (totalCells + 6) / 7


            for (row in 0 until rows) {

                Row(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    for (column in 0..6) {

                        val cellIndex =
                            row * 7 + column

                        val dayNumber =
                            cellIndex -
                                    firstDayOffset +
                                    1


                        // -------------------------------------
                        // EMPTY CELL
                        // -------------------------------------

                        if (
                            dayNumber < 1 ||
                            dayNumber > daysInMonth
                        ) {

                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                            )

                        } else {

                            val date =
                                displayedMonth.atDay(
                                    dayNumber
                                )


                            val attendance =
                                monthlyAttendance.firstOrNull {

                                    try {

                                        LocalDate.parse(
                                            it.date,
                                            attendanceDateFormatter
                                        ) == date

                                    } catch (_: Exception) {

                                        false
                                    }
                                }


                            // ---------------------------------
                            // IMPORTANT:
                            //
                            // weight is given HERE,
                            // inside the Row.
                            // ---------------------------------

                            CalendarDay(
                                modifier =
                                    Modifier.weight(1f),

                                date =
                                    date,

                                attendance =
                                    attendance,

                                isToday =
                                    date == today,

                                isSelected =
                                    date == selectedDate,

                                onClick = {

                                    selectedDate =
                                        date
                                }
                            )
                        }
                    }
                }
            }
        }


        Spacer(
            modifier =
                Modifier.height(16.dp)
        )


        // =====================================================
        // LEGEND
        // =====================================================

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp
                ),

            horizontalArrangement =
                Arrangement.Center
        ) {

            CalendarLegend(
                color = Success,
                text = "Present"
            )

            Spacer(
                modifier =
                    Modifier.size(20.dp)
            )

            CalendarLegend(
                color =
                    Color(0xFFFFA726),

                text = "Late"
            )

            Spacer(
                modifier =
                    Modifier.size(20.dp)
            )

            CalendarLegend(
                color =
                    Color(0xFFE57373),

                text = "Absent"
            )
        }


        Spacer(
            modifier =
                Modifier.height(16.dp)
        )


        // =====================================================
        // SELECTED DATE DETAILS
        // =====================================================

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp
                ),

            shape =
                RoundedCornerShape(18.dp),

            colors =
                CardDefaults.cardColors(
                    containerColor =
                        CardWhite
                )
        ) {

            Column(
                modifier =
                    Modifier.padding(18.dp)
            ) {

                Text(
                    text =
                        selectedDate.format(
                            DateTimeFormatter.ofPattern(
                                "EEEE, dd MMMM yyyy",
                                Locale.ENGLISH
                            )
                        ),

                    fontSize = 17.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        TextPrimary
                )


                Spacer(
                    modifier =
                        Modifier.height(14.dp)
                )


                if (selectedAttendance == null) {

                    Text(
                        text =
                            "No attendance record",

                        fontSize = 14.sp,

                        color =
                            TextSecondary
                    )

                } else {

                    AttendanceDetailRow(
                        label = "Status",

                        value =
                            selectedAttendance.status
                    )

                    AttendanceDetailRow(
                        label = "Check-in",

                        value =
                            selectedAttendance.checkIn
                                ?: "--"
                    )

                    AttendanceDetailRow(
                        label = "Check-out",

                        value =
                            selectedAttendance.checkOut
                                ?: "--"
                    )

                    AttendanceDetailRow(
                        label = "Working hours",

                        value =
                            selectedAttendance.workingHours
                    )
                }
            }
        }
    }
}


// =================================================================
// CALENDAR DAY
// =================================================================

@Composable
private fun CalendarDay(
    modifier: Modifier,
    date: LocalDate,
    attendance: AttendanceRecord?,
    isToday: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {

    val backgroundColor =
        when {

            isSelected ->
                Color(0xFF1976D2)

            isToday ->
                Color(0xFFE3F2FD)

            else ->
                Color.Transparent
        }


    val textColor =
        when {

            isSelected ->
                Color.White

            else ->
                TextPrimary
        }


    Column(
        modifier = modifier
            .height(56.dp)
            .padding(2.dp)
            .clip(
                RoundedCornerShape(10.dp)
            )
            .background(
                backgroundColor
            )
            .clickable(
                onClick = onClick
            ),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center
    ) {

        Text(
            text =
                date.dayOfMonth.toString(),

            fontSize = 14.sp,

            fontWeight =
                if (
                    isToday ||
                    isSelected
                ) {
                    FontWeight.Bold
                } else {
                    FontWeight.Normal
                },

            color =
                textColor
        )


        if (attendance != null) {

            Spacer(
                modifier =
                    Modifier.height(4.dp)
            )

            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(
                        color =
                            getAttendanceColor(
                                attendance.status
                            ),

                        shape =
                            CircleShape
                    )
            )
        }
    }
}


// =================================================================
// LEGEND
// =================================================================

@Composable
private fun CalendarLegend(
    color: Color,
    text: String
) {

    Row(
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = color,
                    shape = CircleShape
                )
        )

        Spacer(
            modifier =
                Modifier.size(5.dp)
        )

        Text(
            text = text,

            fontSize = 11.sp,

            color =
                TextSecondary
        )
    }
}


// =================================================================
// ATTENDANCE DETAIL ROW
// =================================================================

@Composable
private fun AttendanceDetailRow(
    label: String,
    value: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 5.dp
            ),

        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Text(
            text = label,

            fontSize = 13.sp,

            color =
                TextSecondary
        )

        Text(
            text = value,

            fontSize = 13.sp,

            fontWeight =
                FontWeight.SemiBold,

            color =
                TextPrimary
        )
    }
}


// =================================================================
// ATTENDANCE COLOR
// =================================================================

private fun getAttendanceColor(
    status: String
): Color {

    return when {

        status.equals(
            "Present",
            ignoreCase = true
        ) ->
            Success

        status.equals(
            "Late",
            ignoreCase = true
        ) ->
            Color(0xFFFFA726)

        status.equals(
            "Absent",
            ignoreCase = true
        ) ->
            Color(0xFFE57373)

        else ->
            Color(0xFFBDBDBD)
    }
}
