package com.bioverity.attendance.navigation

import androidx.compose.runtime.Composable
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
import com.bioverity.attendance.ui.screens.FaceRecognitionScreen
import com.bioverity.attendance.ui.screens.HomeScreen
import com.bioverity.attendance.ui.screens.ProfileScreen
import com.bioverity.attendance.viewmodel.AttendanceViewModel

@Composable
fun AppNavigation() {

    // ----------------------------------------------------
    // ATTENDANCE VIEW MODEL
    // ----------------------------------------------------

    // IMPORTANT:
    // This single instance is shared by:
    // Home
    // Attendance
    // Face Recognition
    val attendanceViewModel: AttendanceViewModel = viewModel()

    // ----------------------------------------------------
    // CONTEXT
    // ----------------------------------------------------

    val context = LocalContext.current

    // ----------------------------------------------------
    // LOGIN SESSION
    // ----------------------------------------------------

    var isLoggedIn by remember {
        mutableStateOf(
            AppSession.isLoggedIn(context)
        )
    }

    // ----------------------------------------------------
    // TAB
    // ----------------------------------------------------

    var selectedTab by remember {
        mutableIntStateOf(0)
    }

    // ----------------------------------------------------
    // FACE RECOGNITION
    // ----------------------------------------------------

    var showFaceRecognition by remember {
        mutableStateOf(false)
    }

    // ----------------------------------------------------
    // LOGIN SCREEN
    // ----------------------------------------------------

    if (!isLoggedIn) {

        LoginScreen(
            onLoginSuccess = {

                // LoginViewModel has already saved the
                // authenticated employee into AppSession.

                isLoggedIn = true
                selectedTab = 0
                showFaceRecognition = false
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

            // IMPORTANT:
            // Pass the SAME AttendanceViewModel used
            // by the Attendance screen.
            attendanceViewModel = attendanceViewModel
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
                viewModel = attendanceViewModel,

                onNavigate = {
                    selectedTab = it
                }
            )
        }

        // =================================================
        // ATTENDANCE
        // =================================================

        1 -> {

            AttendanceScreen(

                viewModel = attendanceViewModel,

                onNavigate = {
                    selectedTab = it
                },

                onFaceRecognition = {
                    showFaceRecognition = true
                }
            )
        }

        // =================================================
        // PROFILE
        // =================================================

        2 -> {

            ProfileScreen(

                personId = AppSession.getPersonId(context),

                onNavigate = {
                    selectedTab = it
                }
            )
        }
    }
}