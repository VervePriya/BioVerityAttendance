package com.bioverity.attendance.data.model

data class AttendanceResponse(
    val success: Boolean,
    val records: List<AttendanceDbRecord> = emptyList(),
    val reason: String? = null,
    val error: String? = null
)

data class AttendanceDbRecord(
    val id: String? = null,
    val person_id: String? = null,
    val person_name: String? = null,
    val attendance_type: String? = null,
    val attendance_time: String? = null
)