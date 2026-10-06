
package com.bioverity.attendance.navigation

import android.net.Uri

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
import com.bioverity.attendance.ui.screens.LeaveApprovalScreen
import com.bioverity.attendance.ui.screens.LeaveScreen
import com.bioverity.attendance.ui.screens.NotificationsScreen
import com.bioverity.attendance.ui.screens.ProfileScreen

import com.bioverity.attendance.ui.screens.login.ForgotPasswordScreen
import com.bioverity.attendance.ui.screens.login.ResetPasswordScreen

import com.bioverity.attendance.viewmodel.AttendanceViewModel
import com.bioverity.attendance.viewmodel.LeaveViewModel
import com.bioverity.attendance.viewmodel.NotificationViewModel


@Composable
fun AppNavigation(
    resetUri: Uri? = null,
    onResetUriHandled: () -> Unit = {}
) {

    // ----------------------------------------------------
    // CONTEXT
    // ----------------------------------------------------

    val context = LocalContext.current


    // ----------------------------------------------------
    // VIEW MODELS
    // ----------------------------------------------------

    val attendanceViewModel: AttendanceViewModel =
        viewModel()

    val notificationViewModel: NotificationViewModel =
        viewModel()

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
    // FORGOT PASSWORD
    // ----------------------------------------------------

    var showForgotPassword by remember {
        mutableStateOf(false)
    }


    // ----------------------------------------------------
    // PASSWORD RESET TOKEN
    // ----------------------------------------------------

    var resetAccessToken by remember {
        mutableStateOf<String?>(null)
    }


    // ----------------------------------------------------
    // HANDLE RESET PASSWORD DEEP LINK
    // ----------------------------------------------------

    LaunchedEffect(resetUri) {

        val uri = resetUri

        if (uri != null) {

            // ------------------------------------------------
            // FIRST TRY THE URI FRAGMENT
            //
            // Example:
            //
            // bioverity://reset-password
            // #access_token=ABC&type=recovery
            // ------------------------------------------------

            val fragmentParameters =
                mutableMapOf<String, String>()

            val fragment =
                uri.fragment

            if (!fragment.isNullOrBlank()) {

                fragment
                    .split("&")
                    .forEach { parameter ->

                        val parts =
                            parameter.split(
                                "=",
                                limit = 2
                            )

                        if (parts.size == 2) {

                            val key =
                                Uri.decode(
                                    parts[0]
                                )

                            val value =
                                Uri.decode(
                                    parts[1]
                                )

                            fragmentParameters[key] =
                                value
                        }
                    }
            }


            // ------------------------------------------------
            // GET ACCESS TOKEN
            // ------------------------------------------------

            var accessToken =
                fragmentParameters["access_token"]

            var resetType =
                fragmentParameters["type"]


            // ------------------------------------------------
            // FALLBACK:
            // TRY NORMAL QUERY PARAMETERS TOO
            // ------------------------------------------------

            if (accessToken.isNullOrBlank()) {

                accessToken =
                    uri.getQueryParameter(
                        "access_token"
                    )
            }

            if (resetType.isNullOrBlank()) {

                resetType =
                    uri.getQueryParameter(
                        "type"
                    )
            }


            // ------------------------------------------------
            // DEBUG INFORMATION
            // ------------------------------------------------

            android.util.Log.d(
                "PasswordReset",
                "Reset URI received: $uri"
            )

            android.util.Log.d(
                "PasswordReset",
                "Reset type: $resetType"
            )

            android.util.Log.d(
                "PasswordReset",
                "Access token received: ${!accessToken.isNullOrBlank()}"
            )


            // ------------------------------------------------
            // ACCEPT RECOVERY TOKEN
            // ------------------------------------------------

            if (
                !accessToken.isNullOrBlank() &&
                (
                        resetType == "recovery" ||
                                resetType.isNullOrBlank()
                        )
            ) {

                resetAccessToken =
                    accessToken
            }


            // ------------------------------------------------
            // URI HAS BEEN PROCESSED
            // ------------------------------------------------

            onResetUriHandled()
        }
    }


    // ----------------------------------------------------
    // LOAD EXISTING SESSION
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
    // MAIN TAB
    // ----------------------------------------------------

    // 0 = Home
    // 1 = Attendance
    // 2 = Profile
    // 3 = Leave

    var selectedTab by remember {
        mutableIntStateOf(0)
    }


    // ----------------------------------------------------
    // SECONDARY SCREENS
    // ----------------------------------------------------

    var showNotifications by remember {
        mutableStateOf(false)
    }

    var showFaceRecognition by remember {
        mutableStateOf(false)
    }

    var showCalendar by remember {
        mutableStateOf(false)
    }

    var showLeaveApproval by remember {
        mutableStateOf(false)
    }


    // ----------------------------------------------------
    // PASSWORD RESET SCREEN
    //
    // IMPORTANT:
    // This is checked BEFORE LOGIN.
    // Therefore the reset screen can open even when
    // the user is currently logged out.
    // ----------------------------------------------------

    if (!resetAccessToken.isNullOrBlank()) {

        ResetPasswordScreen(

            accessToken =
                resetAccessToken!!,

            onPasswordUpdated = {

                resetAccessToken = null

                isLoggedIn = false

                selectedTab = 0

                showForgotPassword = false

                showFaceRecognition = false

                showNotifications = false

                showCalendar = false

                showLeaveApproval = false
            }
        )

        return
    }


    // ----------------------------------------------------
    // FORGOT PASSWORD SCREEN
    // ----------------------------------------------------

    if (showForgotPassword) {

        ForgotPasswordScreen(

            onBack = {

                showForgotPassword = false
            }
        )

        return
    }


    // ----------------------------------------------------
    // LOGIN SCREEN
    // ----------------------------------------------------

    if (!isLoggedIn) {

        LoginScreen(

            onLoginSuccess = {

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

                showLeaveApproval = false

                showForgotPassword = false
            },

            onForgotPassword = {

                showForgotPassword = true
            }
        )

        return
    }


    // ----------------------------------------------------
    // LEAVE APPROVAL SCREEN
    // ----------------------------------------------------

    if (showLeaveApproval) {

        if (
            !AppSession.isAdminOrManager(
                context
            )
        ) {

            showLeaveApproval = false

        } else {

            LeaveApprovalScreen(

                viewModel =
                    leaveViewModel,

                onBack = {

                    showLeaveApproval = false
                }
            )

            return
        }
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

            personId =
                personId,

            viewModel =
                notificationViewModel,

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
    // CALENDAR SCREEN
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
                },

                onLogout = {

                    AppSession.logout(
                        context
                    )

                    isLoggedIn = false

                    selectedTab = 0

                    showFaceRecognition = false

                    showNotifications = false

                    showCalendar = false

                    showLeaveApproval = false

                    showForgotPassword = false

                    resetAccessToken = null
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
                },

                isAdminOrManager =
                    AppSession.isAdminOrManager(
                        context
                    ),

                onLeaveApproval = {

                    showLeaveApproval = true
                }
            )
        }
    }
}

