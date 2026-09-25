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
import kotlinx.coroutines.launch

import java.time.Duration
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
        private const val TAG = "AttendanceViewModel"
    }

    // =============================================================
    // TIMEZONE
    // =============================================================

    private val sriLankaZone =
        ZoneId.of("Asia/Colombo")


    // =============================================================
    // DISPLAY FORMATTERS
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
    // DATABASE FORMATTERS
    // =============================================================

    /*
     * Current Supabase column:
     *
     * timestamp without time zone
     *
     * Example:
     *
     * 2026-09-24 09:15:00
     */

    private val databaseSpaceFormatter =
        DateTimeFormatter.ofPattern(
            "yyyy-MM-dd HH:mm:ss",
            Locale.ENGLISH
        )

    private val databaseSpaceMillisFormatter =
        DateTimeFormatter.ofPattern(
            "yyyy-MM-dd HH:mm:ss.SSS",
            Locale.ENGLISH
        )


    /*
     * API currently returns:
     *
     * 2026-09-24T09:15:00
     */

    private val databaseIsoFormatter =
        DateTimeFormatter.ofPattern(
            "yyyy-MM-dd'T'HH:mm:ss",
            Locale.ENGLISH
        )

    private val databaseIsoMillisFormatter =
        DateTimeFormatter.ofPattern(
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            Locale.ENGLISH
        )


    // =============================================================
    // ATTENDANCE RULE
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
        _employee


    // =============================================================
    // TODAY
    // =============================================================

    private val _todayAttendance =
        MutableStateFlow(
            createNotMarkedRecord()
        )

    val todayAttendance: StateFlow<AttendanceRecord> =
        _todayAttendance


    // =============================================================
    // RECENT ATTENDANCE
    // =============================================================

    private val _recentAttendance =
        MutableStateFlow(
            emptyList<AttendanceRecord>()
        )

    val recentAttendance: StateFlow<List<AttendanceRecord>> =
        _recentAttendance


    // =============================================================
    // MONTHLY ATTENDANCE
    // =============================================================

    private val _monthlyAttendance =
        MutableStateFlow(
            emptyList<AttendanceRecord>()
        )

    val monthlyAttendance: StateFlow<List<AttendanceRecord>> =
        _monthlyAttendance


    // =============================================================
    // LOADING
    // =============================================================

    private val _isLoading =
        MutableStateFlow(false)

    val isLoading: StateFlow<Boolean> =
        _isLoading


    // =============================================================
    // ERROR
    // =============================================================

    private val _errorMessage =
        MutableStateFlow<String?>(null)

    val errorMessage: StateFlow<String?> =
        _errorMessage


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

            _errorMessage.value =
                "Employee ID is not available"

            return
        }


        loadAttendance(personId)
    }


    // =============================================================
    // LOAD ATTENDANCE
    // =============================================================

    fun loadAttendance(
        personId: String
    ) {

        if (personId.isBlank()) {

            _errorMessage.value =
                "Employee ID is missing"

            return
        }


        viewModelScope.launch {

            _isLoading.value = true
            _errorMessage.value = null


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

                    _errorMessage.value =
                        response.error
                            ?: response.reason
                                    ?: "Unable to load attendance"

                    return@launch
                }


                // =================================================
                // DEBUG EACH DATABASE RECORD
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
                // BUILD MONTH
                // =================================================

                val monthly =
                    buildMonthlyAttendance(
                        response.records
                    )


                Log.d(
                    TAG,
                    "MONTHLY RESULT COUNT=${monthly.size}"
                )


                monthly.forEach { record ->

                    Log.d(
                        TAG,
                        "MONTHLY: " +
                                "${record.date} | " +
                                "IN=${record.checkIn} | " +
                                "OUT=${record.checkOut} | " +
                                "STATUS=${record.status} | " +
                                "HOURS=${record.workingHours}"
                    )
                }


                _monthlyAttendance.value =
                    monthly


                // =================================================
                // TODAY
                // =================================================

                val todayText =
                    todayDateText()


                val todayRecord =
                    monthly.firstOrNull {

                        it.date == todayText
                    }


                _todayAttendance.value =
                    todayRecord
                        ?: createNotMarkedRecord()


                Log.d(
                    TAG,
                    "TODAY=$todayText, " +
                            "todayRecord=$todayRecord"
                )


                // =================================================
                // RECENT
                // =================================================

                _recentAttendance.value =
                    monthly
                        .sortedByDescending {
                            parseDisplayDate(
                                it.date
                            )
                        }
                        .take(7)


                Log.d(
                    TAG,
                    "RECENT RESULT COUNT=" +
                            _recentAttendance.value.size
                )


            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "Unable to load attendance",
                    e
                )

                _errorMessage.value =
                    "Unable to load attendance: ${
                        e.message
                            ?: "Unknown error"
                    }"

            } finally {

                _isLoading.value = false
            }
        }
    }


    // =============================================================
    // REFRESH
    // =============================================================

    fun refreshAttendance(
        context: Context
    ) {

        val personId =
            AppSession
                .getPersonId(context)
                .orEmpty()


        if (personId.isBlank()) {

            _errorMessage.value =
                "Employee ID is not available"

            return
        }


        loadAttendance(personId)
    }


    // =============================================================
    // MARK ATTENDANCE
    //
    // Kept for compatibility.
    //
    // Actual attendance should be saved by:
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
            formatDate(currentDate)

        val currentTimeText =
            currentTime.format(
                timeFormatter
            )

        val currentAttendance =
            _todayAttendance.value


        // =========================================================
        // CHECK IN
        // =========================================================

        if (currentAttendance.checkIn == null) {

            val status =
                if (
                    currentTime.isAfter(lateTime)
                ) {
                    "Late"
                } else {
                    "Present"
                }


            _todayAttendance.value =
                AttendanceRecord(
                    date = currentDateText,
                    checkIn = currentTimeText,
                    checkOut = null,
                    status = status,
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
                        currentDate,
                        checkInTime
                    )

                val workingHours =
                    calculateWorkingHours(
                        checkInDateTime,
                        now
                    )


                _todayAttendance.value =
                    currentAttendance.copy(
                        date = currentDateText,
                        checkOut = currentTimeText,
                        workingHours = workingHours
                    )

            } else {

                _todayAttendance.value =
                    currentAttendance.copy(
                        date = currentDateText,
                        checkOut = currentTimeText,
                        workingHours = "00h 00m"
                    )
            }
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
            today.withDayOfMonth(1)


        Log.d(
            TAG,
            "Building monthly attendance"
        )

        Log.d(
            TAG,
            "Today=$today"
        )

        Log.d(
            TAG,
            "First day=$firstDayOfMonth"
        )


        // =========================================================
        // GROUP RECORDS BY DATE
        // =========================================================

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
                    "Could not parse attendance_time=" +
                            record.attendance_time
                )

                continue
            }


            val localDate =
                timestamp.toLocalDate()


            Log.d(
                TAG,
                "Parsed: ${record.attendance_type} " +
                        "${record.attendance_time} -> " +
                        "$timestamp -> $localDate"
            )


            // =====================================================
            // CURRENT MONTH ONLY
            // =====================================================

            if (
                localDate.isBefore(
                    firstDayOfMonth
                )
            ) {

                Log.d(
                    TAG,
                    "Skipping previous month record: $localDate"
                )

                continue
            }


            // =====================================================
            // NO FUTURE DATES
            // =====================================================

            if (
                localDate.isAfter(today)
            ) {

                Log.d(
                    TAG,
                    "Skipping future record: $localDate"
                )

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
                type != "CHECK_IN" &&
                type != "CHECK_OUT"
            ) {

                Log.w(
                    TAG,
                    "Unknown attendance type: $type"
                )

                continue
            }


            grouped
                .getOrPut(localDate) {
                    mutableListOf()
                }
                .add(
                    ParsedAttendance(
                        type = type,
                        dateTime = timestamp
                    )
                )
        }


        Log.d(
            TAG,
            "Grouped attendance dates=${grouped.keys}"
        )


        // =========================================================
        // CREATE MONTHLY RESULT
        // =========================================================

        val result =
            mutableListOf<AttendanceRecord>()


        var currentDate =
            firstDayOfMonth


        while (
            !currentDate.isAfter(today)
        ) {

            // =====================================================
            // WEEKENDS ARE IGNORED
            // =====================================================

            val isWorkingDay =
                currentDate.dayOfWeek.value in 1..5


            if (!isWorkingDay) {

                currentDate =
                    currentDate.plusDays(1)

                continue
            }


            val dayRecords =
                grouped[currentDate]
                    ?.sortedBy {
                        it.dateTime
                    }
                    ?: emptyList()


            // =====================================================
            // CHECK IN
            // =====================================================

            val checkInRecord =
                dayRecords
                    .filter {
                        it.type == "CHECK_IN"
                    }
                    .minByOrNull {
                        it.dateTime
                    }


            // =====================================================
            // CHECK OUT
            // =====================================================

            val checkOutRecord =
                dayRecords
                    .filter {
                        it.type == "CHECK_OUT"
                    }
                    .maxByOrNull {
                        it.dateTime
                    }


            // =====================================================
            // NO CHECK-IN
            // =====================================================

            if (checkInRecord == null) {

                val status =
                    if (
                        currentDate == today
                    ) {
                        "Not Marked"
                    } else {
                        "Absent"
                    }


                result.add(
                    AttendanceRecord(
                        date =
                            formatDate(
                                currentDate
                            ),

                        checkIn = null,

                        checkOut = null,

                        status = status,

                        workingHours = "00h 00m"
                    )
                )


                currentDate =
                    currentDate.plusDays(1)

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
                if (
                    checkInTime.isAfter(
                        lateTime
                    )
                ) {
                    "Late"
                } else {
                    "Present"
                }


            // =====================================================
            // DISPLAY TIMES
            // =====================================================

            val displayCheckIn =
                checkInTime.format(
                    timeFormatter
                )


            val displayCheckOut =
                checkOutRecord
                    ?.dateTime
                    ?.toLocalTime()
                    ?.format(
                        timeFormatter
                    )


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
                currentDate.plusDays(1)
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

        if (value.isNullOrBlank()) {
            return null
        }


        val cleanValue =
            value.trim()


        Log.d(
            TAG,
            "Parsing database timestamp: $cleanValue"
        )


        // =========================================================
        // 1. CURRENT DATABASE FORMAT
        //
        // 2026-09-24 09:15:00
        // =========================================================

        try {

            return LocalDateTime.parse(
                cleanValue,
                databaseSpaceFormatter
            )

        } catch (_: DateTimeParseException) {
        }


        // =========================================================
        // 2. CURRENT DATABASE FORMAT WITH MILLISECONDS
        //
        // 2026-09-24 09:15:00.123
        // =========================================================

        try {

            return LocalDateTime.parse(
                cleanValue,
                databaseSpaceMillisFormatter
            )

        } catch (_: DateTimeParseException) {
        }


        // =========================================================
        // 3. API FORMAT
        //
        // 2026-09-24T09:15:00
        // =========================================================

        try {

            return LocalDateTime.parse(
                cleanValue,
                databaseIsoFormatter
            )

        } catch (_: DateTimeParseException) {
        }


        // =========================================================
        // 4. API FORMAT WITH MILLISECONDS
        //
        // 2026-09-24T09:15:00.123
        // =========================================================

        try {

            return LocalDateTime.parse(
                cleanValue,
                databaseIsoMillisFormatter
            )

        } catch (_: DateTimeParseException) {
        }


        // =========================================================
        // 5. OFFSET FORMAT
        //
        // 2026-09-24T09:15:00+05:30
        // =========================================================

        try {

            val offsetDateTime =
                OffsetDateTime.parse(
                    cleanValue
                )


            return offsetDateTime
                .atZoneSameInstant(
                    sriLankaZone
                )
                .toLocalDateTime()

        } catch (_: Exception) {
        }


        // =========================================================
        // 6. UTC FORMAT
        //
        // 2026-09-24T03:45:00Z
        // =========================================================

        try {

            return java.time.Instant
                .parse(cleanValue)
                .atZone(sriLankaZone)
                .toLocalDateTime()

        } catch (_: Exception) {
        }


        // =========================================================
        // 7. FINAL DEFAULT ISO PARSER
        // =========================================================

        try {

            return LocalDateTime.parse(
                cleanValue
            )

        } catch (_: Exception) {
        }


        Log.e(
            TAG,
            "FAILED TO PARSE TIMESTAMP: $cleanValue"
        )


        return null
    }


    // =============================================================
    // WORKING HOURS
    // =============================================================

    private fun calculateWorkingHours(
        checkIn: LocalDateTime?,
        checkOut: LocalDateTime?
    ): String {

        if (
            checkIn == null ||
            checkOut == null
        ) {
            return "00h 00m"
        }


        if (
            checkOut.isBefore(checkIn)
        ) {
            return "00h 00m"
        }


        val duration =
            Duration.between(
                checkIn,
                checkOut
            )


        val totalMinutes =
            duration
                .toMinutes()
                .coerceAtLeast(0)


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

        if (value.isNullOrBlank()) {
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

        } catch (_: Exception) {

            null
        }
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
    // TODAY TEXT
    // =============================================================

    private fun todayDateText(): String {

        return formatDate(
            LocalDate.now(
                sriLankaZone
            )
        )
    }


    // =============================================================
    // DEFAULT NOT MARKED
    // =============================================================

    private fun createNotMarkedRecord():
            AttendanceRecord {

        return AttendanceRecord(
            date = todayDateText(),
            checkIn = null,
            checkOut = null,
            status = "Not Marked",
            workingHours = "00h 00m"
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

        } catch (_: DateTimeParseException) {

            LocalDate.MIN
        }
    }


    // =============================================================
    // CLEAR ERROR
    // =============================================================

    fun clearError() {

        _errorMessage.value = null
    }


    // =============================================================
    // INTERNAL DATA
    // =============================================================

    private data class ParsedAttendance(
        val type: String,
        val dateTime: LocalDateTime
    )
}