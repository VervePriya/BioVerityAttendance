package com.bioverity.attendance.ui.screens

import android.Manifest
import android.util.Log

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bioverity.attendance.ui.components.AttendanceStatusCard
import com.bioverity.attendance.ui.components.AttendanceSummaryCard
import com.bioverity.attendance.ui.components.BottomNavigationBar
import com.bioverity.attendance.ui.components.RecentAttendanceItem
import com.bioverity.attendance.ui.theme.Background
import com.bioverity.attendance.ui.theme.CardWhite
import com.bioverity.attendance.ui.theme.Navy
import com.bioverity.attendance.ui.theme.Success
import com.bioverity.attendance.ui.theme.SuccessLight
import com.bioverity.attendance.ui.theme.Teal
import com.bioverity.attendance.ui.theme.TealLight
import com.bioverity.attendance.ui.theme.TextPrimary
import com.bioverity.attendance.ui.theme.TextSecondary
import com.bioverity.attendance.viewmodel.AttendanceViewModel
import com.bioverity.attendance.viewmodel.LocationUiState
import com.bioverity.attendance.viewmodel.LocationViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


@Composable
fun HomeScreen(
    viewModel: AttendanceViewModel,
    onNavigate: (Int) -> Unit,
    locationViewModel: LocationViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val employee by viewModel.employee.collectAsState()
    val attendance by viewModel.todayAttendance.collectAsState()
    val recentAttendance by viewModel.recentAttendance.collectAsState()
    val monthlyAttendance by viewModel.monthlyAttendance.collectAsState()
    val locationState by locationViewModel.locationState.collectAsState()

    Log.d(
        "HOME_EMPLOYEE",
        "name=${employee.name}, imageUrl=${employee.imageUrl}"
    )

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val granted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (granted) {
                locationViewModel.checkLocation()
            }
        }

    Scaffold(
        containerColor = Background,

        bottomBar = {
            BottomNavigationBar(
                selectedIndex = 0,
                onItemSelected = onNavigate
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {

            Spacer(modifier = Modifier.height(18.dp))

            // =========================================================
            // HEADER
            // =========================================================

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    EmployeeAvatar(
                        name = employee.name,
                        imageUrl = employee.imageUrl
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {

                        Text(
                            text = getGreeting(),
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = employee.name.ifBlank { "Employee" },
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                IconButton(
                    onClick = {}
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = "Notifications",
                        tint = Navy
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // =========================================================
            // DATE + TIME
            // =========================================================

            DashboardDateCard()

            Spacer(modifier = Modifier.height(22.dp))

            // =========================================================
            // WELCOME / DASHBOARD TITLE
            // =========================================================

            Text(
                text = "Your Attendance",
                color = TextPrimary,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Manage your attendance and view today's status.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // =========================================================
            // TODAY'S ATTENDANCE
            // =========================================================

            AttendanceStatusCard(
                attendance = attendance,

                onMarkAttendance = {
                    // Attendance screen handles Check In / Check Out
                    onNavigate(1)
                }
            )

            Spacer(modifier = Modifier.height(22.dp))

            // =========================================================
            // WORK LOCATION
            // =========================================================

            SectionHeader(
                title = "Work Location",
                subtitle = "Attendance location verification"
            )

            Spacer(modifier = Modifier.height(10.dp))

            LocationCard(
                locationState = locationState,

                onVerify = {

                    if (
                        locationState is
                                LocationUiState.PermissionRequired
                    ) {

                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )

                    } else {

                        locationViewModel.checkLocation()
                    }
                }
            )

            Spacer(modifier = Modifier.height(22.dp))

            // =========================================================
            // MONTHLY OVERVIEW
            // =========================================================

            SectionHeader(
                title = "Monthly Overview",
                subtitle = "Your attendance at a glance"
            )

            Spacer(modifier = Modifier.height(10.dp))

            AttendanceSummaryCard(
                attendanceRecords = monthlyAttendance
            )

            Spacer(modifier = Modifier.height(22.dp))

            // =========================================================
            // RECENT ACTIVITY
            // =========================================================

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column {

                    Text(
                        text = "Recent Activity",
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Latest attendance activity",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                TextButton(
                    onClick = {
                        onNavigate(1)
                    }
                ) {

                    Text(
                        text = "View All",
                        color = Teal
                    )

                    Spacer(modifier = Modifier.width(2.dp))

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Teal,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (recentAttendance.isEmpty()) {

                EmptyRecentAttendance()

            } else {

                /*
                 * Home shows only a small preview.
                 *
                 * The complete attendance history remains
                 * on the Attendance screen.
                 */
                recentAttendance
                    .take(2)
                    .forEach { record ->

                        RecentAttendanceItem(
                            record = record
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )
                    }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


// =============================================================
// EMPLOYEE AVATAR
// =============================================================

@Composable
private fun EmployeeAvatar(
    name: String,
    imageUrl: String?
) {

    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(TealLight),
        contentAlignment = Alignment.Center
    ) {

        if (!imageUrl.isNullOrBlank()) {

            AsyncImage(
                model = imageUrl,
                contentDescription = "$name profile photo",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,

                onSuccess = {
                    Log.d(
                        "HOME_IMAGE",
                        "IMAGE LOADED SUCCESSFULLY: $imageUrl"
                    )
                },

                onError = {
                    Log.e(
                        "HOME_IMAGE",
                        "IMAGE LOAD FAILED: $imageUrl | " +
                                it.result.throwable
                    )
                }
            )

        } else {

            Text(
                text = name
                    .ifBlank { "Employee" }
                    .take(1)
                    .uppercase(),

                color = Teal,

                style = MaterialTheme.typography.titleLarge,

                fontWeight = FontWeight.Bold
            )
        }
    }
}


// =============================================================
// DASHBOARD DATE CARD
// =============================================================

@Composable
private fun DashboardDateCard() {

    val currentDate = LocalDate.now()

    val dateText =
        currentDate.format(
            DateTimeFormatter.ofPattern(
                "EEEE, dd MMMM yyyy"
            )
        )

    val timeText =
        LocalTime.now().format(
            DateTimeFormatter.ofPattern(
                "hh:mm a"
            )
        )

    Card(
        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(18.dp),

        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        ),

        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 18.dp,
                    vertical = 16.dp
                ),

            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = TealLight,
                        shape = CircleShape
                    ),

                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Today's date",
                    tint = Teal,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = dateText,
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "Today",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {

                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = Teal,
                    modifier = Modifier.size(17.dp)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = timeText,
                    color = TextPrimary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}


// =============================================================
// SECTION HEADER
// =============================================================

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String
) {

    Column {

        Text(
            text = title,
            color = TextPrimary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = subtitle,
            color = TextSecondary,
            style = MaterialTheme.typography.bodySmall
        )
    }
}


// =============================================================
// LOCATION CARD
// =============================================================

@Composable
private fun LocationCard(
    locationState: LocationUiState,
    onVerify: () -> Unit
) {

    var iconColor: Color
    var iconBackground: Color

    var title: String
    var subtitle: String

    var showVerifyButton = false
    var verified = false

    when (locationState) {

        LocationUiState.NotChecked -> {

            iconColor = Teal
            iconBackground = TealLight

            title = "Location not verified"
            subtitle = "Verify your location before attendance"

            showVerifyButton = true
        }

        LocationUiState.Checking -> {

            iconColor = Teal
            iconBackground = TealLight

            title = "Checking location"
            subtitle = "Getting your current location..."
        }

        LocationUiState.PermissionRequired -> {

            iconColor = MaterialTheme.colorScheme.error
            iconBackground = MaterialTheme.colorScheme.errorContainer

            title = "Location permission required"
            subtitle = "Allow location access to continue"

            showVerifyButton = true
        }

        LocationUiState.LocationUnavailable -> {

            iconColor = MaterialTheme.colorScheme.error
            iconBackground = MaterialTheme.colorScheme.errorContainer

            title = "Location unavailable"
            subtitle = "Turn on your phone location"

            showVerifyButton = true
        }

        is LocationUiState.Verified -> {

            iconColor = Success
            iconBackground = SuccessLight

            title = "Location verified"

            subtitle =
                "Office confirmed • " +
                        "${locationState.distanceMeters.toInt()} m away"

            verified = true
        }

        is LocationUiState.OutsideOffice -> {

            iconColor = MaterialTheme.colorScheme.error
            iconBackground = MaterialTheme.colorScheme.errorContainer

            title = "Outside office"

            subtitle =
                "${locationState.distanceMeters.toInt()} m from office"

            showVerifyButton = true
        }

        is LocationUiState.Error -> {

            iconColor = MaterialTheme.colorScheme.error
            iconBackground = MaterialTheme.colorScheme.errorContainer

            title = "Location check failed"
            subtitle = locationState.message

            showVerifyButton = true
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(18.dp),

        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        ),

        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),

            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        color = iconBackground,
                        shape = CircleShape
                    ),

                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector =
                        if (verified) {
                            Icons.Default.Verified
                        } else {
                            Icons.Default.LocationOn
                        },

                    contentDescription = "Location",

                    tint = iconColor,

                    modifier = Modifier.size(23.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = title,
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = subtitle,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (showVerifyButton) {

                TextButton(
                    onClick = onVerify
                ) {

                    Text(
                        text = "Verify",
                        color = Teal,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}


// =============================================================
// EMPTY RECENT ATTENDANCE
// =============================================================

@Composable
private fun EmptyRecentAttendance() {

    Card(
        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(16.dp),

        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        ),

        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),

            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = TealLight,
                        shape = CircleShape
                    ),

                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = Teal,
                    modifier = Modifier.size(21.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {

                Text(
                    text = "No recent activity",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "Your attendance activity will appear here.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}


// =============================================================
// GREETING
// =============================================================

private fun getGreeting(): String {

    return when (LocalTime.now().hour) {

        in 5..11 -> "Good Morning"

        in 12..16 -> "Good Afternoon"

        else -> "Good Evening"
    }
}