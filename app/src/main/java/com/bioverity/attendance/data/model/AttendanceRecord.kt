package com.bioverity.attendance.data.model

data class AttendanceRecord(
    val date: String,
    val checkIn: String?,
    val checkOut: String?,
    val status: String,
    val workingHours: String
)