
package com.bioverity.attendance.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext

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
    // ATTENDANCE VIEW MODEL
    // ----------------------------------------------------

    val attendanceViewModel: AttendanceViewModel = viewModel()

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

                // Keep existing face-recognition
                // behaviour unchanged for now.

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

