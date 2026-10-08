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


data class AttendanceOverview(
    val present: Int = 0,
    val late: Int = 0,
    val shortHours: Int = 0,
    val leave: Int = 0,
    val absent: Int = 0,
    val notMarked: Int = 0,
    val checkOutRequired: Int = 0,
    val attendanceRate: Int = 0
)

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

        private const val LEAVE =
            "Leave"

        private const val SHORT_HOURS =
            "Short Hours"

        private const val CHECKOUT_REQUIRED =
            "Check-out Required"

        private const val REQUIRED_WORKING_MINUTES =
            8 * 60

        private const val NOT_MARKED =
            "Not Marked"

        private const val DEFAULT_WORKING_HOURS =
            "00h 00m"

        private const val DEFAULT_SHORT_BY =
            "00h 00m"

        private const val MAX_RECENT_RECORDS =
            7
    }

    private val sriLankaZone: ZoneId =
        ZoneId.of(TIME_ZONE)

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

    private val databaseDateFormatter =
        DateTimeFormatter.ofPattern(
            "yyyy-MM-dd",
            Locale.ENGLISH
        )

    private val lateTime =
        LocalTime.of(9, 0)

    // ---------------------------------------------------------
    // EMPLOYEE
    // ---------------------------------------------------------

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


    // ---------------------------------------------------------
    // TODAY ATTENDANCE
    // ---------------------------------------------------------

    private val _todayAttendance =
        MutableStateFlow(
            createNotMarkedRecord()
        )

    val todayAttendance: StateFlow<AttendanceRecord> =
        _todayAttendance.asStateFlow()


    // ---------------------------------------------------------
    // RECENT ATTENDANCE
    // ---------------------------------------------------------

    private val _recentAttendance =
        MutableStateFlow(
            emptyList<AttendanceRecord>()
        )

    val recentAttendance: StateFlow<List<AttendanceRecord>> =
        _recentAttendance.asStateFlow()


    // ---------------------------------------------------------
    // MONTHLY ATTENDANCE
    // ---------------------------------------------------------

    private val _monthlyAttendance =
        MutableStateFlow(
            emptyList<AttendanceRecord>()
        )

    val monthlyAttendance: StateFlow<List<AttendanceRecord>> =
        _monthlyAttendance.asStateFlow()

    private val _attendanceOverview =
        MutableStateFlow(AttendanceOverview())

    val attendanceOverview: StateFlow<AttendanceOverview> =
        _attendanceOverview.asStateFlow()

    private fun calculateAttendanceOverview(
        records: List<AttendanceRecord>
    ): AttendanceOverview {

        val present = records.count {
            it.status.equals(
                PRESENT,
                ignoreCase = true
            )
        }

        val late = records.count {
            it.status.equals(
                LATE,
                ignoreCase = true
            )
        }

        val shortHours = records.count {
            it.status.equals(
                SHORT_HOURS,
                ignoreCase = true
            )
        }

        val leave = records.count {
            it.status.equals(
                LEAVE,
                ignoreCase = true
            )
        }

        val absent = records.count {
            it.status.equals(
                ABSENT,
                ignoreCase = true
            )
        }

        val notMarked = records.count {
            it.status.equals(
                NOT_MARKED,
                ignoreCase = true
            )
        }

        val checkOutRequired = records.count {
            it.status.equals(
                CHECKOUT_REQUIRED,
                ignoreCase = true
            )
        }

        val attendedDays =
            present +
                    late +
                    shortHours

        val applicableDays =
            present +
                    late +
                    shortHours +
                    absent

        val attendanceRate =
            if (applicableDays > 0) {
                kotlin.math.round(
                    attendedDays * 100.0 /
                            applicableDays
                ).toInt()
            } else {
                0
            }

        return AttendanceOverview(
            present = present,
            late = late,
            shortHours = shortHours,
            leave = leave,
            absent = absent,
            notMarked = notMarked,
            checkOutRequired = checkOutRequired,
            attendanceRate = attendanceRate
        )
    }


    // ---------------------------------------------------------
    // LOADING
    // ---------------------------------------------------------

    private val _isLoading =
        MutableStateFlow(false)

    val isLoading: StateFlow<Boolean> =
        _isLoading.asStateFlow()


    // ---------------------------------------------------------
    // ERROR
    // ---------------------------------------------------------

    private val _errorMessage =
        MutableStateFlow<String?>(null)

    val errorMessage: StateFlow<String?> =
        _errorMessage.asStateFlow()


    // ---------------------------------------------------------
    // TODAY WORK PLAN
    // ---------------------------------------------------------

    private val _todayWorkPlan =
        MutableStateFlow("")

    val todayWorkPlan: StateFlow<String> =
        _todayWorkPlan.asStateFlow()

    fun setTodayWorkPlan(
        workPlan: String
    ) {
        _todayWorkPlan.value =
            workPlan
    }

    fun clearTodayWorkPlan() {
        _todayWorkPlan.value = ""
    }


    // ---------------------------------------------------------
    // TODAY CHECKOUT NOTE
    // ---------------------------------------------------------

    private val _todayCheckoutNote =
        MutableStateFlow("")

    val todayCheckoutNote: StateFlow<String> =
        _todayCheckoutNote.asStateFlow()

    fun setTodayCheckoutNote(
        note: String
    ) {
        _todayCheckoutNote.value =
            note
    }

    fun clearTodayCheckoutNote() {
        _todayCheckoutNote.value = ""
    }


    // =========================================================
    // LOAD EMPLOYEE
    // =========================================================

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


    // =========================================================
    // LOAD ATTENDANCE + APPROVED LEAVE
    // =========================================================

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

                // -------------------------------------------------
                // LOAD ATTENDANCE
                // -------------------------------------------------

                Log.d(
                    TAG,
                    "Fetching attendance for personId=$personId"
                )

                val attendanceResponse =
                    ApiClient.api.getAttendance(
                        personId
                    )

                Log.d(
                    TAG,
                    "Attendance API success=${attendanceResponse.success}"
                )

                Log.d(
                    TAG,
                    "Attendance records count=" +
                            attendanceResponse.records.size
                )

                if (!attendanceResponse.success) {

                    setError(
                        attendanceResponse.error
                            ?: attendanceResponse.reason
                            ?: "Unable to load attendance"
                    )

                    return@launch
                }


                // -------------------------------------------------
                // LOAD EMPLOYEE LEAVE APPLICATIONS
                // -------------------------------------------------

                Log.d(
                    TAG,
                    "Fetching leave applications for personId=$personId"
                )

                val leaveResponse =
                    ApiClient.api.getLeaveApplications(
                        personId
                    )

                Log.d(
                    TAG,
                    "Leave API success=${leaveResponse.success}"
                )

                Log.d(
                    TAG,
                    "Leave records count=${leaveResponse.leaves.size}"
                )


                // -------------------------------------------------
                // GET APPROVED LEAVE DATES ONLY
                // -------------------------------------------------

                val approvedLeaveDates =
                    if (leaveResponse.success) {

                        leaveResponse.leaves
                            .filter {
                                it.status
                                    .trim()
                                    .uppercase(
                                        Locale.ENGLISH
                                    ) == "APPROVED"
                            }
                            .mapNotNull {
                                parseLeaveDate(
                                    it.leaveDate
                                )
                            }
                            .toSet()

                    } else {

                        emptySet()
                    }


                Log.d(
                    TAG,
                    "Approved leave dates=$approvedLeaveDates"
                )


                // -------------------------------------------------
                // LOG DATABASE ATTENDANCE
                // -------------------------------------------------

                attendanceResponse.records.forEach { record ->

                    Log.d(
                        TAG,
                        "DB RECORD: " +
                                "type=${record.attendance_type}, " +
                                "time=${record.attendance_time}, " +
                                "name=${record.person_name}"
                    )
                }


                // -------------------------------------------------
                // BUILD MONTHLY ATTENDANCE
                // -------------------------------------------------
                val monthly =
                    buildMonthlyAttendance(
                        databaseRecords =
                            attendanceResponse.records,
                        approvedLeaveDates =
                            approvedLeaveDates
                    )

                _monthlyAttendance.value =
                    monthly

                _attendanceOverview.value =
                    calculateAttendanceOverview(
                        monthly
                    )

                // -------------------------------------------------
                // TODAY RECORD
                // -------------------------------------------------

                val today =
                    todayDateText()

                val todayRecord =
                    monthly.firstOrNull {
                        it.date == today
                    }

                _todayAttendance.value =
                    todayRecord
                        ?: createNotMarkedRecord()


                // -------------------------------------------------
                // RECENT ATTENDANCE
                // -------------------------------------------------

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


    // =========================================================
    // REFRESH ATTENDANCE
    // =========================================================

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


    // =========================================================
    // MARK ATTENDANCE
    // =========================================================

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


        // ---------------------------------------------------------
        // CHECK IN
        // ---------------------------------------------------------

        if (
            currentAttendance.checkIn == null
        ) {

            val newRecord =
                AttendanceRecord(
                    date =
                        currentDateText,
                    checkIn =
                        currentTimeText,
                    checkOut =
                        null,
                    status =
                        CHECKOUT_REQUIRED,
                    workingHours =
                        DEFAULT_WORKING_HOURS,
                    shortBy =
                        DEFAULT_SHORT_BY
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


        // ---------------------------------------------------------
        // CHECK OUT
        // ---------------------------------------------------------

        if (
            currentAttendance.checkOut == null
        ) {

            val checkInTime =
                parseTime(
                    currentAttendance.checkIn
                )

            if (checkInTime == null) {

                Log.e(
                    TAG,
                    "Unable to parse check-in time: " +
                            currentAttendance.checkIn
                )

                return
            }

            val checkInDateTime =
                LocalDateTime.of(
                    currentDate,
                    checkInTime
                )

            val workingMinutes =
                calculateWorkingMinutes(
                    checkIn =
                        checkInDateTime,
                    checkOut =
                        now
                )

            val workingHours =
                formatWorkingHours(
                    workingMinutes
                )

            val status =
                calculateStatus(
                    checkInTime =
                        checkInTime,
                    workingMinutes =
                        workingMinutes
                )

            val shortByMinutes =
                calculateShortMinutes(
                    workingMinutes
                )

            val shortBy =
                formatWorkingHours(
                    shortByMinutes
                )

            val updatedRecord =
                currentAttendance.copy(
                    date =
                        currentDateText,
                    checkIn =
                        currentAttendance.checkIn,
                    checkOut =
                        currentTimeText,
                    status =
                        status,
                    workingHours =
                        workingHours,
                    shortBy =
                        shortBy
                )

            updateAttendanceState(
                updatedRecord
            )

            Log.d(
                TAG,
                "Local CHECK_OUT state updated: $updatedRecord"
            )

            Log.d(
                TAG,
                "Working minutes=$workingMinutes"
            )

            Log.d(
                TAG,
                "Working hours=$workingHours"
            )

            Log.d(
                TAG,
                "Short by=$shortBy"
            )

            return
        }


        // ---------------------------------------------------------
        // ALREADY CHECKED OUT
        // ---------------------------------------------------------

        Log.d(
            TAG,
            "Attendance already has CHECK_IN and CHECK_OUT"
        )
    }


    // =========================================================
    // UPDATE ATTENDANCE STATE
    // =========================================================

    private fun updateAttendanceState(
        record: AttendanceRecord
    ) {

        _todayAttendance.value =
            record

        updateRecentAttendance(
            record
        )

        updateMonthlyAttendance(
            record
        )
    }


    // =========================================================
    // UPDATE RECENT ATTENDANCE
    // =========================================================

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


    // =========================================================
    // UPDATE MONTHLY ATTENDANCE
    // =========================================================

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

        val sortedList =
            updatedList
                .sortedByDescending {
                    parseDisplayDate(
                        it.date
                    )
                }

        _monthlyAttendance.value =
            sortedList

        _attendanceOverview.value =
            calculateAttendanceOverview(
                sortedList
            )
    }

    // =========================================================
    // BUILD MONTHLY ATTENDANCE
    // =========================================================

    private fun buildMonthlyAttendance(
        databaseRecords: List<AttendanceDbRecord>,
        approvedLeaveDates: Set<LocalDate>
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


        // ---------------------------------------------------------
        // GROUP DATABASE ATTENDANCE BY DATE
        // ---------------------------------------------------------

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


            if (
                localDate.isBefore(
                    firstDayOfMonth
                )
            ) {
                continue
            }


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


        // ---------------------------------------------------------
        // CREATE MONTHLY RECORDS
        // ---------------------------------------------------------

        return createMonthlyRecords(
            firstDayOfMonth =
                firstDayOfMonth,
            today =
                today,
            grouped =
                grouped,
            approvedLeaveDates =
                approvedLeaveDates
        )
    }


    // =========================================================
    // CREATE MONTHLY RECORDS
    // =========================================================

    private fun createMonthlyRecords(
        firstDayOfMonth: LocalDate,
        today: LocalDate,
        grouped: Map<
                LocalDate,
                List<ParsedAttendance>
                >,
        approvedLeaveDates: Set<LocalDate>
    ): List<AttendanceRecord> {

        val result =
            mutableListOf<AttendanceRecord>()

        var currentDate =
            firstDayOfMonth


        while (
            !currentDate.isAfter(today)
        ) {


            // -----------------------------------------------------
            // SKIP WEEKENDS
            // -----------------------------------------------------

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


            val checkInRecord =
                dayRecords
                    .filter {
                        it.type == CHECK_IN
                    }
                    .minByOrNull {
                        it.dateTime
                    }


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

                val status =

                    when {

                        // -----------------------------------------
                        // APPROVED LEAVE
                        // -----------------------------------------

                        currentDate in approvedLeaveDates -> {

                            LEAVE
                        }


                        // -----------------------------------------
                        // TODAY
                        // -----------------------------------------

                        currentDate == today -> {

                            NOT_MARKED
                        }


                        // -----------------------------------------
                        // PREVIOUS WORKING DAY WITHOUT ATTENDANCE
                        // -----------------------------------------

                        else -> {

                            ABSENT
                        }
                    }


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
                            status,
                        workingHours =
                            DEFAULT_WORKING_HOURS,
                        shortBy =
                            DEFAULT_SHORT_BY
                    )
                )

                Log.d(
                    TAG,
                    "Date=$currentDate status=$status"
                )

                currentDate =
                    currentDate.plusDays(
                        1
                    )

                continue
            }


            // =====================================================
            // CHECK-IN EXISTS
            // =====================================================

            val checkInTime =
                checkInRecord
                    .dateTime
                    .toLocalTime()


            val workingMinutes =

                if (
                    checkOutRecord != null &&
                    !checkOutRecord.dateTime.isBefore(
                        checkInRecord.dateTime
                    )
                ) {

                    Duration
                        .between(
                            checkInRecord.dateTime,
                            checkOutRecord.dateTime
                        )
                        .toMinutes()
                        .coerceAtLeast(0)

                } else {

                    null
                }


            // =====================================================
            // STATUS
            // =====================================================

            val status =

                if (
                    checkOutRecord != null &&
                    workingMinutes != null
                ) {

                    calculateStatus(
                        checkInTime =
                            checkInTime,
                        workingMinutes =
                            workingMinutes
                    )

                } else {

                    CHECKOUT_REQUIRED
                }


            // =====================================================
            // DISPLAY CHECK-IN
            // =====================================================

            val displayCheckIn =
                formatTime(
                    checkInTime
                )


            // =====================================================
            // DISPLAY CHECK-OUT
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


            // =====================================================
            // SHORT BY
            // =====================================================

            val shortBy =

                if (
                    workingMinutes != null
                ) {

                    formatWorkingHours(
                        calculateShortMinutes(
                            workingMinutes
                        )
                    )

                } else {

                    DEFAULT_SHORT_BY
                }


            if (
                workingMinutes != null
            ) {

                Log.d(
                    TAG,
                    "Date=$currentDate, " +
                            "worked=$workingHours, " +
                            "shortBy=$shortBy, " +
                            "status=$status"
                )
            }


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
                        workingHours,
                    shortBy =
                        shortBy
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


    // =========================================================
    // PARSE LEAVE DATE
    // =========================================================

    private fun parseLeaveDate(
        value: String?
    ): LocalDate? {

        if (
            value.isNullOrBlank()
        ) {
            return null
        }

        val cleanValue =
            value.trim()


        // ---------------------------------------------------------
        // yyyy-MM-dd
        // ---------------------------------------------------------

        try {

            return LocalDate.parse(
                cleanValue,
                databaseDateFormatter
            )

        } catch (
            exception: DateTimeParseException
        ) {

            Log.d(
                TAG,
                "Leave date yyyy-MM-dd parsing failed: $cleanValue"
            )
        }


        // ---------------------------------------------------------
        // ISO timestamp
        // ---------------------------------------------------------

        try {

            return Instant
                .parse(
                    cleanValue
                )
                .atZone(
                    sriLankaZone
                )
                .toLocalDate()

        } catch (
            exception: Exception
        ) {

            Log.d(
                TAG,
                "Leave date ISO parsing failed: $cleanValue"
            )
        }


        // ---------------------------------------------------------
        // ISO offset date/time
        // ---------------------------------------------------------

        try {

            return OffsetDateTime
                .parse(
                    cleanValue
                )
                .atZoneSameInstant(
                    sriLankaZone
                )
                .toLocalDate()

        } catch (
            exception: Exception
        ) {

            Log.d(
                TAG,
                "Leave date offset parsing failed: $cleanValue"
            )
        }


        Log.w(
            TAG,
            "Unable to parse leave date: $cleanValue"
        )

        return null
    }


    // =========================================================
    // PARSE DATABASE DATE/TIME
    // =========================================================

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


        // ---------------------------------------------------------
        // LOCAL TIMESTAMP
        // ---------------------------------------------------------

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

        } catch (
            exception: Exception
        ) {

            Log.d(
                TAG,
                "Local timestamp parsing failed: $cleanValue"
            )
        }


        // ---------------------------------------------------------
        // OFFSET TIMESTAMP
        // ---------------------------------------------------------

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

        } catch (
            exception: Exception
        ) {

            Log.d(
                TAG,
                "Offset timestamp parsing failed: $cleanValue"
            )
        }


        // ---------------------------------------------------------
        // UTC INSTANT
        // ---------------------------------------------------------

        try {

            return Instant
                .parse(
                    cleanValue
                )
                .atZone(
                    sriLankaZone
                )
                .toLocalDateTime()

        } catch (
            exception: Exception
        ) {

            Log.e(
                TAG,
                "Unable to parse timestamp: $cleanValue",
                exception
            )
        }


        return null
    }


    // =========================================================
    // TIMEZONE DETECTION
    // =========================================================

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


    // =========================================================
    // CALCULATE STATUS
    // =========================================================

    private fun calculateStatus(
        checkInTime: LocalTime,
        workingMinutes: Long? = null
    ): String {

        // ---------------------------------------------------------
        // SHORT HOURS HAS PRIORITY
        // ---------------------------------------------------------

        if (
            workingMinutes != null &&
            workingMinutes < REQUIRED_WORKING_MINUTES
        ) {

            return SHORT_HOURS
        }


        // ---------------------------------------------------------
        // LATE
        // ---------------------------------------------------------

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


    // =========================================================
    // CALCULATE WORKING MINUTES
    // =========================================================

    private fun calculateWorkingMinutes(
        checkIn: LocalDateTime?,
        checkOut: LocalDateTime?
    ): Long {

        if (
            checkIn == null ||
            checkOut == null
        ) {
            return 0
        }


        if (
            checkOut.isBefore(
                checkIn
            )
        ) {
            return 0
        }


        return Duration
            .between(
                checkIn,
                checkOut
            )
            .toMinutes()
            .coerceAtLeast(0)
    }


    // =========================================================
    // CALCULATE SHORT MINUTES
    // =========================================================

    private fun calculateShortMinutes(
        workingMinutes: Long
    ): Long {

        return (
                REQUIRED_WORKING_MINUTES -
                        workingMinutes
                )
            .coerceAtLeast(0)
    }


    // =========================================================
    // FORMAT WORKING HOURS
    // =========================================================

    private fun formatWorkingHours(
        totalMinutes: Long
    ): String {

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


    // =========================================================
    // CALCULATE WORKING HOURS
    // =========================================================

    private fun calculateWorkingHours(
        checkIn: LocalDateTime?,
        checkOut: LocalDateTime?
    ): String {

        val workingMinutes =
            calculateWorkingMinutes(
                checkIn =
                    checkIn,
                checkOut =
                    checkOut
            )

        return formatWorkingHours(
            workingMinutes
        )
    }


    // =========================================================
    // PARSE DISPLAY TIME
    // =========================================================

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

        } catch (
            exception: Exception
        ) {

            Log.w(
                TAG,
                "Unable to parse display time: $value"
            )

            null
        }
    }


    // =========================================================
    // FORMAT TIME
    // =========================================================

    private fun formatTime(
        time: LocalTime
    ): String {

        return time.format(
            timeFormatter
        )
    }


    // =========================================================
    // FORMAT DATE
    // =========================================================

    private fun formatDate(
        date: LocalDate
    ): String {

        return date.format(
            dateFormatter
        )
    }


    // =========================================================
    // TODAY DATE
    // =========================================================

    private fun todayDateText(): String {

        return formatDate(
            LocalDate.now(
                sriLankaZone
            )
        )
    }


    // =========================================================
    // WORKING DAY
    // =========================================================

    private fun isWorkingDay(
        date: LocalDate
    ): Boolean {

        return date.dayOfWeek.value in 1..5
    }


    // =========================================================
    // CREATE NOT MARKED RECORD
    // =========================================================

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
                DEFAULT_WORKING_HOURS,
            shortBy =
                DEFAULT_SHORT_BY
        )
    }


    // =========================================================
    // PARSE DISPLAY DATE
    // =========================================================

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


    // =========================================================
    // ERROR
    // =========================================================

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


    // =========================================================
    // INTERNAL ATTENDANCE MODEL
    // =========================================================

    private data class ParsedAttendance(
        val type: String,
        val dateTime: LocalDateTime
    )
}