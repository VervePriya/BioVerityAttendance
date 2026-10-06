
package com.bioverity.attendance.ui.screens

import android.app.DatePickerDialog
import android.net.Uri
import android.provider.OpenableColumns

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Verified

import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.bioverity.attendance.data.model.LeaveApplication
import com.bioverity.attendance.viewmodel.LeaveViewModel

import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveScreen(
    viewModel: LeaveViewModel,
    onBack: () -> Unit,
    isAdminOrManager: Boolean = false,
    onLeaveApproval: () -> Unit = {}
) {

    val context = LocalContext.current


    /*
     * ============================================================
     * LEAVE APPLICATIONS
     * ============================================================
     */

    val leaveApplications by
    viewModel.leaveApplications
        .collectAsStateWithLifecycle()


    /*
     * ============================================================
     * LOADING
     * ============================================================
     */

    val isLoading by
    viewModel.isLoading
        .collectAsStateWithLifecycle()


    /*
     * ============================================================
     * SUBMIT LOADING
     * ============================================================
     */

    val isSubmitting by
    viewModel.isSubmitting
        .collectAsStateWithLifecycle()


    /*
     * ============================================================
     * ERROR
     * ============================================================
     */

    val errorMessage by
    viewModel.errorMessage
        .collectAsStateWithLifecycle()


    /*
     * ============================================================
     * SUCCESS
     * ============================================================
     */

    val successMessage by
    viewModel.successMessage
        .collectAsStateWithLifecycle()


    /*
     * ============================================================
     * SELECTED LEAVE DATE
     *
     * Database format:
     * yyyy-MM-dd
     * ============================================================
     */

    var selectedDate by remember {

        mutableStateOf(

            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            ).format(
                Calendar.getInstance().time
            )
        )
    }


    /*
     * ============================================================
     * DISPLAY DATE
     * ============================================================
     */

    var displayDate by remember {

        mutableStateOf(

            SimpleDateFormat(
                "dd MMMM yyyy",
                Locale.getDefault()
            ).format(
                Calendar.getInstance().time
            )
        )
    }


    /*
     * ============================================================
     * REASON
     * ============================================================
     */

    var reason by remember {
        mutableStateOf("")
    }


    /*
     * ============================================================
     * SELECTED FILE
     * ============================================================
     */

    var selectedFileUri by remember {
        mutableStateOf<Uri?>(null)
    }


    /*
     * ============================================================
     * SELECTED FILE NAME
     * ============================================================
     */

    var selectedFileName by remember {
        mutableStateOf<String?>(null)
    }


    /*
     * ============================================================
     * LOAD LEAVE APPLICATIONS
     * ============================================================
     */

    LaunchedEffect(Unit) {

        viewModel.loadLeaveApplications()
    }


    /*
     * ============================================================
     * FILE PICKER
     * ============================================================
     */

    val filePickerLauncher =
        rememberLauncherForActivityResult(

            contract =
                ActivityResultContracts.GetContent()

        ) { uri ->

            selectedFileUri = uri

            selectedFileName =
                uri?.let {

                    getFileName(
                        context,
                        it
                    )
                }
        }


    /*
     * ============================================================
     * SCREEN
     * ============================================================
     */

    Scaffold(

        topBar = {

            TopAppBar(

                title = {

                    Text(
                        text = "Leave",
                        fontWeight =
                            FontWeight.Bold
                    )
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBack
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.ArrowBack,
                            contentDescription =
                                "Back"
                        )
                    }
                },

                actions = {

                    /*
                     * Refresh
                     */
                    IconButton(

                        onClick = {

                            viewModel
                                .loadLeaveApplications()
                        }
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Refresh,
                            contentDescription =
                                "Refresh"
                        )
                    }
                }
            )
        }

    ) { paddingValues ->

        LazyColumn(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),

            contentPadding =
                PaddingValues(

                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 32.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(16.dp)
        ) {


            /*
             * ====================================================
             * ADMIN / MANAGER APPROVAL BUTTON
             * ====================================================
             */

            if (isAdminOrManager) {

                item {

                    Card(

                        modifier =
                            Modifier.fillMaxWidth(),

                        shape =
                            RoundedCornerShape(16.dp),

                        colors =
                            CardDefaults.cardColors(

                                containerColor =
                                    MaterialTheme
                                        .colorScheme
                                        .primaryContainer
                            )
                    ) {

                        Row(

                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Icon(

                                imageVector =
                                    Icons.Default.Verified,

                                contentDescription =
                                    null,

                                modifier =
                                    Modifier.size(28.dp),

                                tint =
                                    MaterialTheme
                                        .colorScheme
                                        .onPrimaryContainer
                            )

                            Spacer(
                                modifier =
                                    Modifier.size(12.dp)
                            )

                            Column(
                                modifier =
                                    Modifier.weight(1f)
                            ) {

                                Text(

                                    text =
                                        "Leave Approval",

                                    fontSize = 17.sp,

                                    fontWeight =
                                        FontWeight.Bold,

                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .onPrimaryContainer
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(3.dp)
                                )

                                Text(

                                    text =
                                        "Review and manage employee leave requests.",

                                    fontSize = 13.sp,

                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .onPrimaryContainer
                                )
                            }

                            Spacer(
                                modifier =
                                    Modifier.size(8.dp)
                            )

                            Button(

                                onClick =
                                    onLeaveApproval,

                                shape =
                                    RoundedCornerShape(10.dp)
                            ) {

                                Text(
                                    text = "Review"
                                )
                            }
                        }
                    }
                }
            }


            /*
             * ====================================================
             * PAGE HEADING
             * ====================================================
             */

            item {

                Text(

                    text =
                        "Apply for Leave",

                    fontSize = 22.sp,

                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    modifier =
                        Modifier.height(6.dp)
                )

                Text(

                    text =
                        "Submit a leave request for today or a future date.",

                    fontSize = 14.sp,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }


            /*
             * ====================================================
             * LEAVE DATE
             * ====================================================
             */

            item {

                Text(

                    text =
                        "Leave Date",

                    fontSize = 14.sp,

                    fontWeight =
                        FontWeight.SemiBold
                )

                Spacer(
                    modifier =
                        Modifier.height(6.dp)
                )

                OutlinedButton(

                    onClick = {

                        val calendar =
                            Calendar.getInstance()

                        val year =
                            calendar.get(
                                Calendar.YEAR
                            )

                        val month =
                            calendar.get(
                                Calendar.MONTH
                            )

                        val day =
                            calendar.get(
                                Calendar.DAY_OF_MONTH
                            )


                        val datePicker =
                            DatePickerDialog(

                                context,

                                { _, selectedYear,
                                  selectedMonth,
                                  selectedDay ->

                                    val selectedCalendar =
                                        Calendar.getInstance()

                                    selectedCalendar.set(

                                        selectedYear,

                                        selectedMonth,

                                        selectedDay
                                    )


                                    selectedDate =
                                        SimpleDateFormat(

                                            "yyyy-MM-dd",

                                            Locale.getDefault()

                                        ).format(
                                            selectedCalendar.time
                                        )


                                    displayDate =
                                        SimpleDateFormat(

                                            "dd MMMM yyyy",

                                            Locale.getDefault()

                                        ).format(
                                            selectedCalendar.time
                                        )
                                },

                                year,
                                month,
                                day
                            )


                        /*
                         * Prevent past dates.
                         */

                        val today =
                            Calendar.getInstance().apply {

                                set(
                                    Calendar.HOUR_OF_DAY,
                                    0
                                )

                                set(
                                    Calendar.MINUTE,
                                    0
                                )

                                set(
                                    Calendar.SECOND,
                                    0
                                )

                                set(
                                    Calendar.MILLISECOND,
                                    0
                                )
                            }


                        datePicker
                            .datePicker
                            .minDate =
                            today.timeInMillis


                        datePicker.show()
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(12.dp)
                ) {

                    Icon(

                        imageVector =
                            Icons.Default.CalendarMonth,

                        contentDescription =
                            null
                    )

                    Spacer(
                        modifier =
                            Modifier.size(8.dp)
                    )

                    Text(
                        text = displayDate
                    )
                }
            }


            /*
             * ====================================================
             * REASON
             * ====================================================
             */

            item {

                Text(

                    text =
                        "Reason for Leave",

                    fontSize = 14.sp,

                    fontWeight =
                        FontWeight.SemiBold
                )

                Spacer(
                    modifier =
                        Modifier.height(6.dp)
                )

                OutlinedTextField(

                    value =
                        reason,

                    onValueChange = {
                        reason = it
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    minLines = 4,

                    maxLines = 6,

                    placeholder = {

                        Text(
                            text =
                                "Enter reason for leave"
                        )
                    },

                    shape =
                        RoundedCornerShape(12.dp)
                )
            }


            /*
             * ====================================================
             * ATTACHMENT
             * ====================================================
             */

            item {

                Text(

                    text =
                        "Attachment",

                    fontSize = 14.sp,

                    fontWeight =
                        FontWeight.SemiBold
                )

                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )

                Text(

                    text =
                        "Optional. You can attach a PDF or image.",

                    fontSize = 13.sp,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )


                OutlinedButton(

                    onClick = {

                        filePickerLauncher
                            .launch("*/*")
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(12.dp)
                ) {

                    Icon(

                        imageVector =
                            Icons.Default.AttachFile,

                        contentDescription =
                            "Attach file"
                    )

                    Spacer(
                        modifier =
                            Modifier.size(8.dp)
                    )

                    Text(
                        text = "Choose File"
                    )
                }


                /*
                 * Selected file
                 */

                selectedFileName?.let { fileName ->

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    Card(

                        modifier =
                            Modifier.fillMaxWidth(),

                        shape =
                            RoundedCornerShape(10.dp)
                    ) {

                        Row(

                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Icon(

                                imageVector =
                                    Icons.Default.AttachFile,

                                contentDescription =
                                    null
                            )

                            Spacer(
                                modifier =
                                    Modifier.size(8.dp)
                            )

                            Text(

                                text =
                                    fileName,

                                modifier =
                                    Modifier.weight(1f),

                                maxLines = 1,

                                overflow =
                                    TextOverflow.Ellipsis
                            )

                            IconButton(

                                onClick = {

                                    selectedFileUri =
                                        null

                                    selectedFileName =
                                        null
                                }
                            ) {

                                Icon(

                                    imageVector =
                                        Icons.Default.Close,

                                    contentDescription =
                                        "Remove attachment"
                                )
                            }
                        }
                    }
                }
            }


            /*
             * ====================================================
             * SUBMIT
             * ====================================================
             */

            item {

                Button(

                    onClick = {

                        viewModel.submitLeave(

                            context = context,

                            leaveDate =
                                selectedDate,

                            reason =
                                reason,

                            fileUri =
                                selectedFileUri
                        )

                        /*
                         * Clear form.
                         */

                        reason = ""

                        selectedFileUri = null

                        selectedFileName = null
                    },

                    enabled =
                        reason
                            .trim()
                            .isNotEmpty() &&
                                !isSubmitting,

                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(12.dp),

                    contentPadding =
                        PaddingValues(
                            vertical = 14.dp
                        )
                ) {

                    if (isSubmitting) {

                        CircularProgressIndicator(

                            modifier =
                                Modifier.size(20.dp),

                            strokeWidth = 2.dp
                        )

                    } else {

                        Text(

                            text =
                                "Submit Application",

                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }
            }


            /*
             * ====================================================
             * ERROR MESSAGE
             * ====================================================
             */

            errorMessage?.let { error ->

                item {

                    Card(

                        modifier =
                            Modifier.fillMaxWidth(),

                        colors =
                            CardDefaults.cardColors(

                                containerColor =
                                    MaterialTheme
                                        .colorScheme
                                        .errorContainer
                            ),

                        shape =
                            RoundedCornerShape(12.dp)
                    ) {

                        Row(

                            modifier =
                                Modifier.padding(12.dp),

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Icon(

                                imageVector =
                                    Icons.Default.Error,

                                contentDescription =
                                    null,

                                tint =
                                    MaterialTheme
                                        .colorScheme
                                        .onErrorContainer
                            )

                            Spacer(
                                modifier =
                                    Modifier.size(8.dp)
                            )

                            Text(

                                text =
                                    error,

                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .onErrorContainer
                            )
                        }
                    }
                }
            }


            /*
             * ====================================================
             * SUCCESS MESSAGE
             * ====================================================
             */

            successMessage?.let { success ->

                item {

                    Card(

                        modifier =
                            Modifier.fillMaxWidth(),

                        colors =
                            CardDefaults.cardColors(

                                containerColor =
                                    MaterialTheme
                                        .colorScheme
                                        .primaryContainer
                            ),

                        shape =
                            RoundedCornerShape(12.dp)
                    ) {

                        Text(

                            text =
                                success,

                            modifier =
                                Modifier.padding(12.dp),

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onPrimaryContainer,

                            fontWeight =
                                FontWeight.SemiBold
                        )
                    }
                }
            }


            /*
             * ====================================================
             * MY APPLICATIONS
             * ====================================================
             */

            item {

                Divider()

                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )

                Text(

                    text =
                        "My Applications",

                    fontSize = 20.sp,

                    fontWeight =
                        FontWeight.Bold
                )
            }


            /*
             * ====================================================
             * APPLICATION LIST
             * ====================================================
             */

            if (isLoading) {

                item {

                    Box(

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(24.dp),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        CircularProgressIndicator()
                    }
                }

            } else if (
                leaveApplications.isEmpty()
            ) {

                item {

                    Text(

                        text =
                            "No leave applications yet.",

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant,

                        modifier =
                            Modifier.padding(
                                vertical = 16.dp
                            )
                    )
                }

            } else {

                items(

                    items =
                        leaveApplications,

                    key = {
                        it.id
                    }

                ) { leave ->

                    LeaveApplicationCard(
                        leave = leave
                    )
                }
            }
        }
    }
}


/*
 * ==============================================================
 * LEAVE APPLICATION CARD
 * ==============================================================
 */

@Composable
private fun LeaveApplicationCard(
    leave: LeaveApplication
) {

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(14.dp),

        colors =
            CardDefaults.cardColors(

                containerColor =
                    MaterialTheme
                        .colorScheme
                        .surface
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
    ) {

        Column(

            modifier =
                Modifier.padding(16.dp)
        ) {

            /*
             * Date + status
             */

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column(

                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(

                        text =
                            formatLeaveDate(
                                leave.leaveDate
                            ),

                        fontSize = 17.sp,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(4.dp)
                    )

                    Text(

                        text =
                            leave.reason,

                        fontSize = 14.sp,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }


                LeaveStatusBadge(
                    status =
                        leave.status
                )
            }


            /*
             * Attachment
             */

            if (
                !leave.attachmentUrl
                    .isNullOrBlank()
            ) {

                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )

                Row(

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Icon(

                        imageVector =
                            Icons.Default.AttachFile,

                        contentDescription =
                            null,

                        modifier =
                            Modifier.size(18.dp)
                    )

                    Spacer(
                        modifier =
                            Modifier.size(4.dp)
                    )

                    Text(

                        text =
                            "Attachment included",

                        fontSize = 12.sp,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }
            }


            /*
             * Created timestamp
             */

            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )

            Text(

                text =
                    "Submitted: ${
                        formatSriLankaDateTime(
                            leave.createdAt
                        )
                    }",

                fontSize = 12.sp,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )


            /*
             * Updated timestamp
             */

            Text(

                text =
                    "Last updated: ${
                        formatSriLankaDateTime(
                            leave.updatedAt
                        )
                    }",

                fontSize = 12.sp,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}


/*
 * ==============================================================
 * STATUS BADGE
 * ==============================================================
 */

@Composable
private fun LeaveStatusBadge(
    status: String
) {

    val normalizedStatus =
        status.uppercase()


    val backgroundColor =
        when (normalizedStatus) {

            "APPROVED" ->
                MaterialTheme
                    .colorScheme
                    .primaryContainer

            "REJECTED" ->
                MaterialTheme
                    .colorScheme
                    .errorContainer

            else ->
                MaterialTheme
                    .colorScheme
                    .secondaryContainer
        }


    val textColor =
        when (normalizedStatus) {

            "APPROVED" ->
                MaterialTheme
                    .colorScheme
                    .onPrimaryContainer

            "REJECTED" ->
                MaterialTheme
                    .colorScheme
                    .onErrorContainer

            else ->
                MaterialTheme
                    .colorScheme
                    .onSecondaryContainer
        }


    val icon =
        when (normalizedStatus) {

            "APPROVED" ->
                Icons.Default.CheckCircle

            "REJECTED" ->
                Icons.Default.Close

            else ->
                Icons.Default.Schedule
        }


    Box(

        modifier =
            Modifier
                .background(

                    color =
                        backgroundColor,

                    shape =
                        RoundedCornerShape(20.dp)
                )

                .padding(

                    horizontal = 10.dp,

                    vertical = 6.dp
                )
    ) {

        Row(

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Icon(

                imageVector =
                    icon,

                contentDescription =
                    null,

                modifier =
                    Modifier.size(15.dp),

                tint =
                    textColor
            )

            Spacer(
                modifier =
                    Modifier.size(4.dp)
            )

            Text(

                text =
                    normalizedStatus,

                fontSize = 11.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    textColor
            )
        }
    }
}


/*
 * ==============================================================
 * FORMAT LEAVE DATE
 * ==============================================================
 */

private fun formatLeaveDate(
    date: String
): String {

    return try {

        val inputFormat =
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            )

        val outputFormat =
            SimpleDateFormat(
                "dd MMM yyyy",
                Locale.getDefault()
            )

        val parsedDate =
            inputFormat.parse(date)

        if (parsedDate != null) {

            outputFormat.format(
                parsedDate
            )

        } else {

            date
        }

    } catch (_: Exception) {

        date
    }
}


/*
 * ==============================================================
 * FORMAT SUPABASE TIMESTAMP
 *
 * Converts the timestamp to Sri Lankan time.
 * ==============================================================
 */

private fun formatSriLankaDateTime(
    value: String?
): String {

    if (value.isNullOrBlank()) {
        return "-"
    }

    return try {

        val sriLankaZone =
            ZoneId.of("Asia/Colombo")

        val formatter =
            DateTimeFormatter.ofPattern(
                "dd MMM yyyy, hh:mm a",
                Locale.ENGLISH
            )

        OffsetDateTime
            .parse(value)
            .atZoneSameInstant(
                sriLankaZone
            )
            .format(formatter)

    } catch (_: Exception) {

        "-"
    }
}


/*
 * ==============================================================
 * GET FILE NAME
 * ==============================================================
 */

private fun getFileName(
    context: android.content.Context,
    uri: Uri
): String? {

    var fileName: String? = null

    context
        .contentResolver
        .query(
            uri,
            null,
            null,
            null,
            null
        )
        ?.use { cursor ->

            val nameIndex =
                cursor.getColumnIndex(
                    OpenableColumns.DISPLAY_NAME
                )

            if (
                nameIndex >= 0 &&
                cursor.moveToFirst()
            ) {

                fileName =
                    cursor.getString(
                        nameIndex
                    )
            }
        }

    return fileName
}

