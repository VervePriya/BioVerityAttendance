package com.bioverity.attendance.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.bioverity.attendance.data.model.AttendanceRecord
import com.bioverity.attendance.data.model.Employee

class AttendanceViewModel : ViewModel() {

    private val _employee = MutableStateFlow(
        Employee(
            id = "EMP001",
            name = "Abinaya",
            employeeId = "BV-001",
            department = "AI & Analytics"
        )
    )

    val employee: StateFlow<Employee> = _employee

    private val _todayAttendance = MutableStateFlow(
        AttendanceRecord(
            date = "16 Sep 2026",
            checkIn = null,
            checkOut = null,
            status = "Not Marked",
            workingHours = "00h 00m"
        )
    )

    val todayAttendance: StateFlow<AttendanceRecord> =
        _todayAttendance

    private val _recentAttendance = MutableStateFlow(
        listOf(
            AttendanceRecord(
                "15 Sep 2026",
                "08:42 AM",
                "06:03 PM",
                "Present",
                "09h 21m"
            ),
            AttendanceRecord(
                "14 Sep 2026",
                "08:37 AM",
                "05:58 PM",
                "Present",
                "09h 21m"
            ),
            AttendanceRecord(
                "13 Sep 2026",
                "09:12 AM",
                "06:01 PM",
                "Late",
                "08h 49m"
            )
        )
    )

    val recentAttendance: StateFlow<List<AttendanceRecord>> =
        _recentAttendance

    fun markAttendance() {
        _todayAttendance.value =
            _todayAttendance.value.copy(
                checkIn = "09:56 AM",
                status = "Checked In"
            )
    }
}