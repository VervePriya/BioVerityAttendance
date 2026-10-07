package com.bioverity.attendance.viewmodel

import android.content.Context
import android.util.Log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.bioverity.attendance.data.api.ApiClient
import com.bioverity.attendance.data.model.AttendanceDbRecord
import com.bioverity.attendance.data.model.AttendanceRecord
import com.bioverity.attendance.data.model.Employee
import com.bioverity.attendance.data.session.AppSession

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

class AttendanceViewModel : ViewModel() {

    companion object {

        private const val TAG =
            "AttendanceViewModel"

        private const val TIME_ZONE =
            "Asia/Colombo"

        private const val CHECK_IN =
            "CHECK_IN"

        private const val CHECK_OUT =
            "CHECK_OUT"

        private const val PRESENT =
            "Present"

        private const val LATE =
            "Late"

        private const val ABSENT =
            "Absent"

        private const val NOT_MARKED =
            "Not Marked"

        private const val DEFAULT_WORKING_HOURS =
            "00h 00m"

        private const val MAX_RECENT_RECORDS =
            7
    }

    // =============================================================
    // TIMEZONE
    // =============================================================

    private val sriLankaZone: ZoneId =
        ZoneId.of(TIME_ZONE)

    // =============================================================
    // FORMATTERS
    // =============================================================

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
    // ATTENDANCE RULE
    //
    // 09:00 AM = Present
    // After 09:00 AM = Late
    // =============================================================

    private val lateTime =
        LocalTime.of(9, 0)

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
        _employee.asStateFlow()

    // =============================================================
    // TODAY ATTENDANCE
    // =============================================================

    private val _todayAttendance =
        MutableStateFlow(
            createNotMarkedRecord()
        )

    val todayAttendance: StateFlow<AttendanceRecord> =
        _todayAttendance.asStateFlow()

    // =============================================================
    // RECENT ATTENDANCE
    // =============================================================

    private val _recentAttendance =
        MutableStateFlow(
            emptyList<AttendanceRecord>()
        )

    val recentAttendance: StateFlow<List<AttendanceRecord>> =
        _recentAttendance.asStateFlow()

    // =============================================================
    // MONTHLY ATTENDANCE
    // =============================================================

    private val _monthlyAttendance =
        MutableStateFlow(
            emptyList<AttendanceRecord>()
        )

    val monthlyAttendance: StateFlow<List<AttendanceRecord>> =
        _monthlyAttendance.asStateFlow()

    // =============================================================
    // LOADING
    // =============================================================

    private val _isLoading =
        MutableStateFlow(false)

    val isLoading: StateFlow<Boolean> =
        _isLoading.asStateFlow()

    // =============================================================
    // ERROR
    // =============================================================

    private val _errorMessage =
        MutableStateFlow<String?>(null)

    val errorMessage: StateFlow<String?> =
        _errorMessage.asStateFlow()

    // =============================================================
// TODAY'S WORK PLAN / NOTE
// =============================================================

    private val _todayWorkPlan =
        MutableStateFlow("")

    val todayWorkPlan: StateFlow<String> =
        _todayWorkPlan.asStateFlow()

    fun setTodayWorkPlan(
        workPlan: String
    ) {
        _todayWorkPlan.value = workPlan
    }
    private val _todayCheckoutNote = MutableStateFlow("")
    val todayCheckoutNote: StateFlow<String> =
        _todayCheckoutNote.asStateFlow()

    fun setTodayCheckoutNote(note: String) {
        _todayCheckoutNote.value = note
    }

    fun clearTodayWorkPlan() {
        _todayWorkPlan.value = ""
    }

    // =============================================================
    // LOAD EMPLOYEE
    // =============================================================

    fun loadEmployee(
        context: Context
    ) {

        val personId =
            AppSession
                .getPersonId(context)
                .orEmpty()

        val name =
            AppSession
                .getName(context)
                .orEmpty()

        val employeeId =
            AppSession
                .getEmployeeId(context)

        val email =
            AppSession
                .getEmail(context)

        val imageUrl =
            AppSession
                .getImageUrl(context)

        Log.d(
            TAG,
            "Loading employee: personId=$personId, name=$name"
        )

        _employee.value =
            Employee(
                id = personId,
                name = name,
                employeeId = employeeId,
                email = email,
                phone = null,
                position = null,
                department = null,
                imageUrl = imageUrl
            )

        if (personId.isBlank()) {

            setError(
                "Employee ID is not available"
            )

            return
        }

        loadAttendance(
            personId
        )
    }

    // =============================================================
    // LOAD ATTENDANCE
    // =============================================================

    fun loadAttendance(
        personId: String
    ) {

        if (personId.isBlank()) {

            setError(
                "Employee ID is missing"
            )

            return
        }

        viewModelScope.launch {

            _isLoading.value = true
            clearError()

            try {

                Log.d(
                    TAG,
                    "Fetching attendance for personId=$personId"
                )

                val response =
                    ApiClient.api.getAttendance(
                        personId
                    )

                Log.d(
                    TAG,
                    "API success=${response.success}"
                )

                Log.d(
                    TAG,
                    "API records count=${response.records.size}"
                )

                if (!response.success) {

                    setError(
                        response.error
                            ?: response.reason
                            ?: "Unable to load attendance"
                    )

                    return@launch
                }

                // =================================================
                // DATABASE RECORD LOGGING
                // =================================================

                response.records.forEach { record ->

                    Log.d(
                        TAG,
                        "DB RECORD: " +
                                "type=${record.attendance_type}, " +
                                "time=${record.attendance_time}, " +
                                "name=${record.person_name}"
                    )
                }

                // =================================================
                // BUILD MONTHLY ATTENDANCE
                // =================================================

                val monthly =
                    buildMonthlyAttendance(
                        response.records
                    )

                _monthlyAttendance.value =
                    monthly

                // =================================================
                // UPDATE TODAY
                // =================================================

                val today =
                    todayDateText()

                val todayRecord =
                    monthly.firstOrNull {
                        it.date == today
                    }

                _todayAttendance.value =
                    todayRecord
                        ?: createNotMarkedRecord()

                // =================================================
                // UPDATE RECENT
                // =================================================

                _recentAttendance.value =
                    monthly
                        .sortedByDescending {
                            parseDisplayDate(
                                it.date
                            )
                        }
                        .take(
                            MAX_RECENT_RECORDS
                        )

                Log.d(
                    TAG,
                    "Attendance loaded successfully"
                )

            } catch (exception: Exception) {

                Log.e(
                    TAG,
                    "Unable to load attendance",
                    exception
                )

                setError(
                    "Unable to load attendance: " +
                            (
                                    exception.message
                                        ?: "Unknown error"
                                    )
                )

            } finally {

                _isLoading.value = false
            }
        }
    }

    // =============================================================
    // REFRESH ATTENDANCE
    // =============================================================

    fun refreshAttendance(
        context: Context
    ) {

        val personId =
            AppSession
                .getPersonId(context)
                .orEmpty()

        if (personId.isBlank()) {

            setError(
                "Employee ID is not available"
            )

            return
        }

        loadAttendance(
            personId
        )
    }

    // =============================================================
    // MARK ATTENDANCE
    //
    // Updates the local UI state immediately.
    // Backend persistence is handled separately through:
    //
    // POST /attendance
    // =============================================================

    fun markAttendance() {

        val now =
            LocalDateTime.now(
                sriLankaZone
            )

        val currentDate =
            now.toLocalDate()

        val currentTime =
            now.toLocalTime()

        val currentDateText =
            formatDate(
                currentDate
            )

        val currentTimeText =
            formatTime(
                currentTime
            )

        val currentAttendance =
            _todayAttendance.value

        // =========================================================
        // CHECK IN
        // =========================================================

        if (
            currentAttendance.checkIn == null
        ) {

            val status =
                calculateStatus(
                    currentTime
                )

            val newRecord =
                AttendanceRecord(
                    date =
                        currentDateText,

                    checkIn =
                        currentTimeText,

                    checkOut =
                        null,

                    status =
                        status,

                    workingHours =
                        DEFAULT_WORKING_HOURS
                )

            updateAttendanceState(
                newRecord
            )

            Log.d(
                TAG,
                "Local CHECK_IN state updated: $newRecord"
            )

            return
        }

        // =========================================================
        // CHECK OUT
        // =========================================================

        if (
            currentAttendance.checkOut == null
        ) {

            val checkInTime =
                parseTime(
                    currentAttendance.checkIn
                )

            val workingHours =
                if (checkInTime != null) {

                    val checkInDateTime =
                        LocalDateTime.of(
                            currentDate,
                            checkInTime
                        )

                    calculateWorkingHours(
                        checkIn = checkInDateTime,
                        checkOut = now
                    )

                } else {

                    DEFAULT_WORKING_HOURS
                }

            val updatedRecord =
                currentAttendance.copy(
                    date =
                        currentDateText,

                    checkOut =
                        currentTimeText,

                    workingHours =
                        workingHours
                )

            updateAttendanceState(
                updatedRecord
            )

            Log.d(
                TAG,
                "Local CHECK_OUT state updated: $updatedRecord"
            )
        }
    }

    // =============================================================
    // UPDATE ALL ATTENDANCE STATE
    // =============================================================

    private fun updateAttendanceState(
        record: AttendanceRecord
    ) {

        // ---------------------------------------------------------
        // TODAY
        // ---------------------------------------------------------

        _todayAttendance.value =
            record

        // ---------------------------------------------------------
        // RECENT
        // ---------------------------------------------------------

        updateRecentAttendance(
            record
        )

        // ---------------------------------------------------------
        // MONTHLY
        // ---------------------------------------------------------

        updateMonthlyAttendance(
            record
        )
    }

    // =============================================================
    // UPDATE RECENT ATTENDANCE
    // =============================================================

    private fun updateRecentAttendance(
        record: AttendanceRecord
    ) {

        val updatedList =
            _recentAttendance.value
                .filterNot {
                    it.date == record.date
                }
                .toMutableList()

        updatedList.add(
            record
        )

        _recentAttendance.value =
            updatedList
                .sortedByDescending {
                    parseDisplayDate(
                        it.date
                    )
                }
                .take(
                    MAX_RECENT_RECORDS
                )
    }

    // =============================================================
    // UPDATE MONTHLY ATTENDANCE
    // =============================================================

    private fun updateMonthlyAttendance(
        record: AttendanceRecord
    ) {

        val updatedList =
            _monthlyAttendance.value
                .filterNot {
                    it.date == record.date
                }
                .toMutableList()

        updatedList.add(
            record
        )

        _monthlyAttendance.value =
            updatedList
                .sortedByDescending {
                    parseDisplayDate(
                        it.date
                    )
                }
    }

    // =============================================================
    // BUILD MONTHLY ATTENDANCE
    // =============================================================

    private fun buildMonthlyAttendance(
        databaseRecords: List<AttendanceDbRecord>
    ): List<AttendanceRecord> {

        val today =
            LocalDate.now(
                sriLankaZone
            )

        val firstDayOfMonth =
            today.withDayOfMonth(
                1
            )

        val grouped =
            mutableMapOf<
                    LocalDate,
                    MutableList<ParsedAttendance>
                    >()

        for (record in databaseRecords) {

            val timestamp =
                parseDatabaseDateTime(
                    record.attendance_time
                )

            if (timestamp == null) {

                Log.w(
                    TAG,
                    "Skipping invalid timestamp: " +
                            record.attendance_time
                )

                continue
            }

            val localDate =
                timestamp.toLocalDate()

            // =====================================================
            // CURRENT MONTH ONLY
            // =====================================================

            if (
                localDate.isBefore(
                    firstDayOfMonth
                )
            ) {
                continue
            }

            // =====================================================
            // NEVER SHOW FUTURE DATES
            // =====================================================

            if (
                localDate.isAfter(
                    today
                )
            ) {
                continue
            }

            val type =
                record.attendance_type
                    ?.trim()
                    ?.uppercase(
                        Locale.ENGLISH
                    )
                    .orEmpty()

            if (
                type != CHECK_IN &&
                type != CHECK_OUT
            ) {

                Log.w(
                    TAG,
                    "Ignoring unknown attendance type=$type"
                )

                continue
            }

            grouped
                .getOrPut(localDate) {
                    mutableListOf()
                }
                .add(
                    ParsedAttendance(
                        type =
                            type,

                        dateTime =
                            timestamp
                    )
                )
        }

        return createMonthlyRecords(
            firstDayOfMonth =
                firstDayOfMonth,

            today =
                today,

            grouped =
                grouped
        )
    }

    // =============================================================
    // CREATE MONTHLY RECORDS
    // =============================================================

    private fun createMonthlyRecords(
        firstDayOfMonth: LocalDate,
        today: LocalDate,
        grouped: Map<
                LocalDate,
                List<ParsedAttendance>
                >
    ): List<AttendanceRecord> {

        val result =
            mutableListOf<AttendanceRecord>()

        var currentDate =
            firstDayOfMonth

        while (
            !currentDate.isAfter(today)
        ) {

            // =====================================================
            // WEEKENDS
            // =====================================================

            if (
                !isWorkingDay(
                    currentDate
                )
            ) {

                currentDate =
                    currentDate.plusDays(
                        1
                    )

                continue
            }

            val dayRecords =
                grouped[currentDate]
                    ?.sortedBy {
                        it.dateTime
                    }
                    ?: emptyList()

            // =====================================================
            // FIRST CHECK-IN
            // =====================================================

            val checkInRecord =
                dayRecords
                    .filter {
                        it.type == CHECK_IN
                    }
                    .minByOrNull {
                        it.dateTime
                    }

            // =====================================================
            // LAST CHECK-OUT
            // =====================================================

            val checkOutRecord =
                dayRecords
                    .filter {
                        it.type == CHECK_OUT
                    }
                    .maxByOrNull {
                        it.dateTime
                    }

            // =====================================================
            // NO CHECK-IN
            // =====================================================

            if (
                checkInRecord == null
            ) {

                result.add(
                    AttendanceRecord(
                        date =
                            formatDate(
                                currentDate
                            ),

                        checkIn =
                            null,

                        checkOut =
                            null,

                        status =
                            if (
                                currentDate == today
                            ) {
                                NOT_MARKED
                            } else {
                                ABSENT
                            },

                        workingHours =
                            DEFAULT_WORKING_HOURS
                    )
                )

                currentDate =
                    currentDate.plusDays(
                        1
                    )

                continue
            }

            // =====================================================
            // CHECK-IN STATUS
            // =====================================================

            val checkInTime =
                checkInRecord
                    .dateTime
                    .toLocalTime()

            val status =
                calculateStatus(
                    checkInTime
                )

            // =====================================================
            // CHECK-IN DISPLAY
            // =====================================================

            val displayCheckIn =
                formatTime(
                    checkInTime
                )

            // =====================================================
            // CHECK-OUT DISPLAY
            // =====================================================

            val displayCheckOut =
                checkOutRecord
                    ?.dateTime
                    ?.toLocalTime()
                    ?.let {
                        formatTime(
                            it
                        )
                    }

            // =====================================================
            // WORKING HOURS
            // =====================================================

            val workingHours =
                calculateWorkingHours(
                    checkIn =
                        checkInRecord.dateTime,

                    checkOut =
                        checkOutRecord?.dateTime
                )

            result.add(
                AttendanceRecord(
                    date =
                        formatDate(
                            currentDate
                        ),

                    checkIn =
                        displayCheckIn,

                    checkOut =
                        displayCheckOut,

                    status =
                        status,

                    workingHours =
                        workingHours
                )
            )

            currentDate =
                currentDate.plusDays(
                    1
                )
        }

        return result
            .sortedByDescending {
                parseDisplayDate(
                    it.date
                )
            }
    }

    // =============================================================
    // DATABASE TIMESTAMP PARSER
    // =============================================================

    private fun parseDatabaseDateTime(
        value: String?
    ): LocalDateTime? {

        if (
            value.isNullOrBlank()
        ) {
            return null
        }

        val cleanValue =
            value.trim()

        // =========================================================
        // CASE 1
        //
        // Backend returns local Sri Lanka time.
        //
        // Example:
        // 2026-09-29 13:45:25
        // =========================================================

        try {

            val normalized =
                cleanValue.replaceFirst(
                    " ",
                    "T"
                )

            if (
                !normalized.endsWith("Z") &&
                !normalized.contains("+") &&
                !containsTimezoneOffset(
                    normalized
                )
            ) {

                return LocalDateTime.parse(
                    normalized
                )
            }

        } catch (exception: Exception) {

            Log.d(
                TAG,
                "Local timestamp parsing failed: $cleanValue"
            )
        }

        // =========================================================
        // CASE 2
        //
        // Timestamp with offset.
        //
        // Example:
        // 2026-09-29 08:15:25+00
        // =========================================================

        try {

            var normalized =
                cleanValue.replaceFirst(
                    " ",
                    "T"
                )

            if (
                normalized.endsWith(
                    "+00"
                )
            ) {

                normalized =
                    normalized.dropLast(
                        3
                    ) + "+00:00"
            }

            val offsetDateTime =
                OffsetDateTime.parse(
                    normalized
                )

            return offsetDateTime
                .atZoneSameInstant(
                    sriLankaZone
                )
                .toLocalDateTime()

        } catch (exception: Exception) {

            Log.d(
                TAG,
                "Offset timestamp parsing failed: $cleanValue"
            )
        }

        // =========================================================
        // CASE 3
        //
        // UTC timestamp.
        //
        // Example:
        // 2026-09-29T08:15:25Z
        // =========================================================

        try {

            return Instant
                .parse(
                    cleanValue
                )
                .atZone(
                    sriLankaZone
                )
                .toLocalDateTime()

        } catch (exception: Exception) {

            Log.e(
                TAG,
                "Unable to parse timestamp: $cleanValue",
                exception
            )
        }

        return null
    }

    // =============================================================
    // CHECK WHETHER TIMESTAMP CONTAINS OFFSET
    // =============================================================

    private fun containsTimezoneOffset(
        value: String
    ): Boolean {

        val timePart =
            value.substringAfter(
                "T",
                ""
            )

        return timePart
            .drop(1)
            .contains("-")
    }

    // =============================================================
    // CALCULATE STATUS
    // =============================================================

    private fun calculateStatus(
        checkInTime: LocalTime
    ): String {

        return if (
            checkInTime.isAfter(
                lateTime
            )
        ) {
            LATE
        } else {
            PRESENT
        }
    }

    // =============================================================
    // CALCULATE WORKING HOURS
    // =============================================================

    private fun calculateWorkingHours(
        checkIn: LocalDateTime?,
        checkOut: LocalDateTime?
    ): String {

        if (
            checkIn == null ||
            checkOut == null
        ) {
            return DEFAULT_WORKING_HOURS
        }

        if (
            checkOut.isBefore(
                checkIn
            )
        ) {
            return DEFAULT_WORKING_HOURS
        }

        val duration =
            Duration.between(
                checkIn,
                checkOut
            )

        val totalMinutes =
            duration
                .toMinutes()
                .coerceAtLeast(
                    0
                )

        val hours =
            totalMinutes / 60

        val minutes =
            totalMinutes % 60

        return String.format(
            Locale.ENGLISH,
            "%02dh %02dm",
            hours,
            minutes
        )
    }

    // =============================================================
    // PARSE DISPLAY TIME
    // =============================================================

    private fun parseTime(
        value: String?
    ): LocalTime? {

        if (
            value.isNullOrBlank()
        ) {
            return null
        }

        return try {

            LocalTime.parse(
                value
                    .trim()
                    .uppercase(
                        Locale.ENGLISH
                    ),
                timeFormatter
            )

        } catch (exception: Exception) {

            Log.w(
                TAG,
                "Unable to parse display time: $value"
            )

            null
        }
    }

    // =============================================================
    // FORMAT TIME
    // =============================================================

    private fun formatTime(
        time: LocalTime
    ): String {

        return time.format(
            timeFormatter
        )
    }

    // =============================================================
    // FORMAT DATE
    // =============================================================

    private fun formatDate(
        date: LocalDate
    ): String {

        return date.format(
            dateFormatter
        )
    }

    // =============================================================
    // TODAY DATE TEXT
    // =============================================================

    private fun todayDateText(): String {

        return formatDate(
            LocalDate.now(
                sriLankaZone
            )
        )
    }

    // =============================================================
    // WORKING DAY
    // =============================================================

    private fun isWorkingDay(
        date: LocalDate
    ): Boolean {

        return date.dayOfWeek.value in 1..5
    }

    // =============================================================
    // DEFAULT RECORD
    // =============================================================

    private fun createNotMarkedRecord():
            AttendanceRecord {

        return AttendanceRecord(
            date =
                todayDateText(),

            checkIn =
                null,

            checkOut =
                null,

            status =
                NOT_MARKED,

            workingHours =
                DEFAULT_WORKING_HOURS
        )
    }

    // =============================================================
    // PARSE DISPLAY DATE
    // =============================================================

    private fun parseDisplayDate(
        value: String
    ): LocalDate {

        return try {

            LocalDate.parse(
                value,
                dateFormatter
            )

        } catch (
            exception: DateTimeParseException
        ) {

            LocalDate.MIN
        }
    }

    // =============================================================
    // ERROR MANAGEMENT
    // =============================================================

    private fun setError(
        message: String
    ) {

        _errorMessage.value =
            message
    }

    fun clearError() {

        _errorMessage.value =
            null
    }

    fun clearTodayCheckoutNote() {
        _todayCheckoutNote.value = ""
    }

    // =============================================================
    // INTERNAL DATA MODEL
    // =============================================================

    private data class ParsedAttendance(
        val type: String,
        val dateTime: LocalDateTime
    )
}