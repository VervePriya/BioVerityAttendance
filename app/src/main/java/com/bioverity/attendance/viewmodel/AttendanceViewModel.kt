
package com.bioverity.attendance.viewmodel

import android.content.Context

import androidx.lifecycle.ViewModel

import com.bioverity.attendance.data.model.AttendanceRecord
import com.bioverity.attendance.data.model.Employee
import com.bioverity.attendance.data.session.AppSession

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class AttendanceViewModel : ViewModel() {

    private val _monthlyAttendance =
        MutableStateFlow<List<AttendanceRecord>>(emptyList())

    val monthlyAttendance:
            StateFlow<List<AttendanceRecord>> =
        _monthlyAttendance

    private val sriLankaZone =
        ZoneId.of("Asia/Colombo")

    private val timeFormatter =
        DateTimeFormatter.ofPattern(
            "hh:mm a",
            Locale.ENGLISH
        )

    private val dateFormatter =
        DateTimeFormatter.ofPattern(
            "dd MMM yyyy",
            Locale.ENGLISH
        )


    // =============================================================
    // EMPLOYEE
    // =============================================================

    private val _employee =
        MutableStateFlow(
            Employee(
                id = "",
                name = "",
                employeeId = null,
                email = null,
                phone = null,
                position = null,
                department = null,
                imageUrl = null
            )
        )

    val employee: StateFlow<Employee> =
        _employee


    // =============================================================
    // LOAD LOGGED-IN EMPLOYEE
    // =============================================================

    fun loadEmployee(context: Context) {

        _employee.value = Employee(

            id = AppSession
                .getPersonId(context)
                .orEmpty(),

            name = AppSession
                .getName(context)
                .orEmpty(),

            employeeId = AppSession
                .getEmployeeId(context),

            email = AppSession
                .getEmail(context),

            phone = null,

            position = null,

            department = null,

            imageUrl = AppSession
                .getImageUrl(context)
        )
    }


    // =============================================================
    // TODAY'S ATTENDANCE
    // =============================================================

    private val _todayAttendance =
        MutableStateFlow(

            AttendanceRecord(

                date =
                    LocalDate
                        .now(sriLankaZone)
                        .format(dateFormatter),

                checkIn = null,

                checkOut = null,

                status = "Not Marked",

                workingHours = "00h 00m"
            )
        )

    val todayAttendance:
            StateFlow<AttendanceRecord> =
        _todayAttendance


    // =============================================================
    // RECENT ATTENDANCE
    // =============================================================

    private val _recentAttendance =
        MutableStateFlow(
            emptyList<AttendanceRecord>()
        )

    val recentAttendance:
            StateFlow<List<AttendanceRecord>> =
        _recentAttendance


    // =============================================================
    // MARK ATTENDANCE
    // =============================================================

    /**
     * First successful face verification:
     * CHECK IN
     *
     * Second successful face verification:
     * CHECK OUT
     *
     * After CHECK OUT:
     * working hours are calculated automatically.
     */
    fun markAttendance() {

        val now =
            LocalDateTime.now(
                sriLankaZone
            )

        val currentTime =
            now.format(timeFormatter)

        val currentDate =
            now.format(dateFormatter)

        val currentAttendance =
            _todayAttendance.value


        // =========================================================
        // CHECK IN
        // =========================================================

        if (currentAttendance.checkIn == null) {

            _todayAttendance.value =
                currentAttendance.copy(

                    date = currentDate,

                    checkIn = currentTime,

                    checkOut = null,

                    status = "Checked In",

                    workingHours = "00h 00m"
                )

            return
        }


        // =========================================================
        // CHECK OUT
        // =========================================================

        if (currentAttendance.checkOut == null) {

            val checkInTime =
                parseTime(
                    currentAttendance.checkIn
                )

            if (checkInTime != null) {

                val checkInDateTime =
                    LocalDateTime.of(
                        LocalDate.now(
                            sriLankaZone
                        ),
                        checkInTime
                    )

                val duration =
                    Duration.between(
                        checkInDateTime,
                        now
                    )

                val totalMinutes =
                    duration
                        .toMinutes()
                        .coerceAtLeast(0)

                val hours =
                    totalMinutes / 60

                val minutes =
                    totalMinutes % 60


                _todayAttendance.value =
                    currentAttendance.copy(

                        date = currentDate,

                        checkOut = currentTime,

                        status = "Present",

                        workingHours =
                            String.format(
                                Locale.ENGLISH,
                                "%02dh %02dm",
                                hours,
                                minutes
                            )
                    )

            } else {

                _todayAttendance.value =
                    currentAttendance.copy(

                        date = currentDate,

                        checkOut = currentTime,

                        status = "Present"
                    )
            }

            return
        }


        // =========================================================
        // ALREADY CHECKED IN AND CHECKED OUT
        // =========================================================

        // Do nothing.
    }


    // =============================================================
    // PARSE TIME
    // =============================================================

    private fun parseTime(
        time: String
    ): LocalTime? {

        return try {

            LocalTime.parse(
                time.uppercase(
                    Locale.ENGLISH
                ),
                timeFormatter
            )

        } catch (_: Exception) {

            null
        }
    }
}

