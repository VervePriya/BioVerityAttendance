package com.bioverity.attendance.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

import com.bioverity.attendance.data.session.AppSession
import com.bioverity.attendance.ui.LoginScreen
import com.bioverity.attendance.ui.screens.AttendanceScreen
import com.bioverity.attendance.ui.screens.CalendarScreen
import com.bioverity.attendance.ui.screens.FaceRecognitionScreen
import com.bioverity.attendance.ui.screens.HomeScreen
import com.bioverity.attendance.ui.screens.LeaveScreen
import com.bioverity.attendance.ui.screens.NotificationsScreen
import com.bioverity.attendance.ui.screens.ProfileScreen
import com.bioverity.attendance.viewmodel.AttendanceViewModel
import com.bioverity.attendance.viewmodel.LeaveViewModel
import com.bioverity.attendance.viewmodel.NotificationViewModel


@Composable
fun AppNavigation() {

    // ----------------------------------------------------
    // CONTEXT
    // ----------------------------------------------------

    val context = LocalContext.current


    // ----------------------------------------------------
    // ATTENDANCE VIEW MODEL
    // ----------------------------------------------------

    // Shared by:
    // Home
    // Attendance
    // Calendar
    // Face Recognition

    val attendanceViewModel: AttendanceViewModel =
        viewModel()


    // ----------------------------------------------------
    // NOTIFICATION VIEW MODEL
    // ----------------------------------------------------

    // Shared by:
    // Home
    // Notifications

    val notificationViewModel: NotificationViewModel =
        viewModel()


    // ----------------------------------------------------
    // LEAVE VIEW MODEL
    // ----------------------------------------------------

    // Used by:
    // Leave Screen

    val leaveViewModel: LeaveViewModel =
        viewModel()


    // ----------------------------------------------------
    // LOGIN SESSION
    // ----------------------------------------------------

    var isLoggedIn by remember {

        mutableStateOf(
            AppSession.isLoggedIn(context)
        )
    }


    // ----------------------------------------------------
    // LOAD EXISTING SESSION EMPLOYEE
    // ----------------------------------------------------

    LaunchedEffect(isLoggedIn) {

        if (isLoggedIn) {

            attendanceViewModel.loadEmployee(
                context
            )

            val personId =
                AppSession.getPersonId(context)

            if (personId != null) {

                leaveViewModel.setPersonId(
                    personId
                )
            }
        }
    }


    // ----------------------------------------------------
    // MAIN SCREEN
    // ----------------------------------------------------

    // 0 = Home
    // 1 = Attendance
    // 2 = Profile
    // 3 = Leave

    var selectedTab by remember {

        mutableIntStateOf(0)
    }


    // ----------------------------------------------------
    // NOTIFICATIONS
    // ----------------------------------------------------

    var showNotifications by remember {

        mutableStateOf(false)
    }


    // ----------------------------------------------------
    // FACE RECOGNITION
    // ----------------------------------------------------

    var showFaceRecognition by remember {

        mutableStateOf(false)
    }


    // ----------------------------------------------------
    // ATTENDANCE CALENDAR
    // ----------------------------------------------------

    var showCalendar by remember {

        mutableStateOf(false)
    }


    // ----------------------------------------------------
    // LOGIN SCREEN
    // ----------------------------------------------------

    if (!isLoggedIn) {

        LoginScreen(

            onLoginSuccess = {

                // LoginViewModel has already saved:
                //
                // name
                // email
                // employee ID
                // image URL
                // person ID
                //
                // into AppSession.

                attendanceViewModel.loadEmployee(
                    context
                )

                val personId =
                    AppSession.getPersonId(context)

                if (personId != null) {

                    leaveViewModel.setPersonId(
                        personId
                    )
                }

                isLoggedIn = true

                selectedTab = 0

                showFaceRecognition = false

                showNotifications = false

                showCalendar = false
            }
        )

        return
    }


    // ----------------------------------------------------
    // NOTIFICATIONS SCREEN
    // ----------------------------------------------------

    if (showNotifications) {

        val personId =
            AppSession.getPersonId(context)

        if (personId == null) {

            showNotifications = false

            return
        }

        NotificationsScreen(

            personId = personId,

            viewModel = notificationViewModel,

            onBack = {

                showNotifications = false
            }
        )

        return
    }


    // ----------------------------------------------------
    // FACE RECOGNITION SCREEN
    // ----------------------------------------------------

    if (showFaceRecognition) {

        FaceRecognitionScreen(

            onBack = {

                showFaceRecognition = false
            },

            onFaceDetected = {
                // Existing face-recognition behaviour
            },

            attendanceViewModel =
                attendanceViewModel
        )

        return
    }


    // ----------------------------------------------------
    // ATTENDANCE CALENDAR SCREEN
    // ----------------------------------------------------

    if (showCalendar) {

        CalendarScreen(

            viewModel =
                attendanceViewModel,

            onBack = {

                showCalendar = false
            }
        )

        return
    }


    // ----------------------------------------------------
    // MAIN APPLICATION
    // ----------------------------------------------------

    when (selectedTab) {


        // =================================================
        // HOME
        // =================================================

        0 -> {

            HomeScreen(

                viewModel =
                    attendanceViewModel,

                onNavigate = { index ->

                    selectedTab = index
                },

                onNotificationsClick = {

                    showNotifications = true
                }
            )
        }


        // =================================================
        // ATTENDANCE
        // =================================================

        1 -> {

            AttendanceScreen(

                viewModel =
                    attendanceViewModel,

                onNavigate = {

                    selectedTab = it
                },

                onFaceRecognition = {

                    showFaceRecognition = true
                },

                onCalendarClick = {

                    showCalendar = true
                }
            )
        }


        // =================================================
        // PROFILE
        // =================================================

        2 -> {

            ProfileScreen(

                personId =
                    AppSession.getPersonId(
                        context
                    ),

                onNavigate = {

                    selectedTab = it
                }
            )
        }


        // =================================================
        // LEAVE
        // =================================================

        3 -> {

            LeaveScreen(

                viewModel =
                    leaveViewModel,

                onBack = {

                    selectedTab = 0
                }
            )
        }
    }
}

