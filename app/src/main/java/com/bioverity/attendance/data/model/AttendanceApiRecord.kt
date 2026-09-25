
package com.bioverity.attendance.data.model

data class AttendanceApiRecord(
    val id: String? = null,
    val personId: String,
    val personName: String,
    val attendanceType: String,
    val attendanceTime: String
)

