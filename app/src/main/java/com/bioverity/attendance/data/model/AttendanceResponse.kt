package com.bioverity.attendance.data.model

data class AttendanceResponse(
    val success: Boolean = false,
    val records: List<AttendanceDbRecord> = emptyList(),
    val reason: String? = null,
    val error: String? = null
)

data class AttendanceDbRecord(
    val id: String? = null,
    val person_id: String? = null,
    val person_name: String? = null,
    val attendance_type: String? = null,
    val attendance_time: String? = null,
    val note: String? = null
)

data class AttendanceSaveRequest(
    val person_id: String,
    val person_name: String,
    val attendance_type: String,
    val note: String? = null
)

data class AttendanceSaveResponse(
    val success: Boolean = false,
    val message: String? = null,
    val attendance_type: String? = null,
    val attendance_time: String? = null,
    val attendance_datetime: String? = null,
    val status: String? = null,
    val attendance_completed: Boolean? = null,
    val checkout_required: Boolean? = null,
    val working_hours: String? = null,
    val working_minutes: Int? = null,
    val reason: String? = null,
    val error: String? = null,
    val record: AttendanceDbRecord? = null
)