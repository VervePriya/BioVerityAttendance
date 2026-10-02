
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


// ================================================================
// COLORS
// ================================================================

private val PoyaColor = Color(0xFF1976D2)
private val LateColor = Color(0xFFFFA726)
private val AbsentColor = Color(0xFFE57373)
private val HolidayBackground = Color(0xFFE3F2FD)


// ================================================================
// SRI LANKAN POYA HOLIDAYS - 2026
// ================================================================
//
// Official Sri Lankan 2026 Poya dates.
//
// These dates should be updated each year or, for a production
// system, preferably loaded from a backend holiday table.
// ================================================================

private val poyaHolidays2026: Map<LocalDate, String> = mapOf(

    LocalDate.of(2026, 1, 3) to
            "Duruthu Full Moon Poya Day",

    LocalDate.of(2026, 2, 1) to
            "Navam Full Moon Poya Day",

    LocalDate.of(2026, 3, 2) to
            "Medin Full Moon Poya Day",

    LocalDate.of(2026, 4, 1) to
            "Bak Full Moon Poya Day",

    LocalDate.of(2026, 5, 1) to
            "Vesak Full Moon Poya Day",

    LocalDate.of(2026, 5, 30) to
            "Adhi Poson Full Moon Poya Day",

    LocalDate.of(2026, 6, 29) to
            "Poson Full Moon Poya Day",

    LocalDate.of(2026, 7, 29) to
            "Esala Full Moon Poya Day",

    LocalDate.of(2026, 8, 27) to
            "Nikini Full Moon Poya Day",

    LocalDate.of(2026, 9, 26) to
            "Binara Full Moon Poya Day",

    LocalDate.of(2026, 10, 25) to
            "Vap Full Moon Poya Day",

    LocalDate.of(2026, 11, 24) to
            "Il Full Moon Poya Day",

    LocalDate.of(2026, 12, 23) to
            "Unduvap Full Moon Poya Day"
)


// ================================================================
// FIND POYA HOLIDAY
// ================================================================

private fun getPoyaHoliday(
    date: LocalDate
): String? {

    return if (date.year == 2026) {
        poyaHolidays2026[date]
    } else {
        null
    }
}


// ================================================================
// CALENDAR SCREEN
// ================================================================

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
    // SELECTED POYA HOLIDAY
    // =========================================================

    val selectedPoyaHoliday =
        getPoyaHoliday(selectedDate)


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


                            // ---------------------------------
                            // ATTENDANCE
                            // ---------------------------------

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
                            // POYA HOLIDAY
                            // ---------------------------------

                            val poyaHoliday =
                                getPoyaHoliday(date)


                            // ---------------------------------
                            // CALENDAR DAY
                            // ---------------------------------

                            CalendarDay(
                                modifier =
                                    Modifier.weight(1f),

                                date =
                                    date,

                                attendance =
                                    attendance,

                                poyaHoliday =
                                    poyaHoliday,

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
                    horizontal = 12.dp
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
                    Modifier.size(12.dp)
            )

            CalendarLegend(
                color =
                    LateColor,
                text = "Late"
            )

            Spacer(
                modifier =
                    Modifier.size(12.dp)
            )

            CalendarLegend(
                color =
                    AbsentColor,
                text = "Absent"
            )

            Spacer(
                modifier =
                    Modifier.size(12.dp)
            )

            CalendarLegend(
                color =
                    PoyaColor,
                text = "Poya Holiday"
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


                // =================================================
                // POYA HOLIDAY HAS PRIORITY
                // =================================================

                if (selectedPoyaHoliday != null) {

                    AttendanceDetailRow(
                        label = "Status",

                        value =
                            "Poya Holiday"
                    )

                    AttendanceDetailRow(
                        label = "Holiday",

                        value =
                            selectedPoyaHoliday
                    )

                } else if (selectedAttendance == null) {

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
    poyaHoliday: String?,
    isToday: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {

    // =============================================================
    // BACKGROUND
    // =============================================================

    val backgroundColor =
        when {

            isSelected ->
                PoyaColor

            poyaHoliday != null ->
                HolidayBackground

            isToday ->
                Color(0xFFE3F2FD)

            else ->
                Color.Transparent
        }


    // =============================================================
    // TEXT COLOR
    // =============================================================

    val textColor =
        when {

            isSelected ->
                Color.White

            poyaHoliday != null ->
                PoyaColor

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
                    isSelected ||
                    poyaHoliday != null
                ) {
                    FontWeight.Bold
                } else {
                    FontWeight.Normal
                },

            color =
                textColor
        )


        Spacer(
            modifier =
                Modifier.height(4.dp)
        )


        // =========================================================
        // STATUS INDICATOR
        // =========================================================

        when {

            // -----------------------------------------------------
            // POYA HOLIDAY
            // -----------------------------------------------------

            poyaHoliday != null -> {

                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(
                            color =
                                if (isSelected) {
                                    Color.White
                                } else {
                                    PoyaColor
                                },

                            shape =
                                CircleShape
                        )
                )
            }


            // -----------------------------------------------------
            // ATTENDANCE
            // -----------------------------------------------------

            attendance != null -> {

                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(
                            color =
                                if (isSelected) {
                                    Color.White
                                } else {
                                    getAttendanceColor(
                                        attendance.status
                                    )
                                },

                            shape =
                                CircleShape
                        )
                )
            }
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

            fontSize = 10.sp,

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
            LateColor

        status.equals(
            "Absent",
            ignoreCase = true
        ) ->
            AbsentColor

        else ->
            Color(0xFFBDBDBD)
    }
}

