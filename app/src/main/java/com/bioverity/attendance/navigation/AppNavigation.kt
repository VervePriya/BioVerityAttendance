package com.bioverity.attendance.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bioverity.attendance.ui.LoginScreen
import com.bioverity.attendance.ui.screens.AttendanceScreen
import com.bioverity.attendance.ui.screens.FaceRecognitionScreen
import com.bioverity.attendance.ui.screens.HomeScreen
import com.bioverity.attendance.ui.screens.ProfileScreen
import com.bioverity.attendance.viewmodel.AttendanceViewModel

@Composable
fun AppNavigation() {

    // Temporary login state.
    // Supabase authentication will replace this later.
    var isLoggedIn by remember {
        mutableStateOf(false)
    }

    // Currently selected bottom navigation tab.
    var selectedTab by remember {
        mutableIntStateOf(0)
    }

    // Controls the face recognition screen.
    var showFaceRecognition by remember {
        mutableStateOf(false)
    }

    val viewModel: AttendanceViewModel = viewModel()

    // ---------------------------------------------------------
    // LOGIN
    // ---------------------------------------------------------
    if (!isLoggedIn) {

        LoginScreen(
            onLoginSuccess = {
                isLoggedIn = true
                selectedTab = 0
            }
        )

        return
    }

    // ---------------------------------------------------------
    // FACE RECOGNITION
    // ---------------------------------------------------------
    if (showFaceRecognition) {

        FaceRecognitionScreen(
            onBack = {
                showFaceRecognition = false
            },
            onFaceDetected = {
                // Actual employee face recognition
                // will be connected later.
            }
        )

        return
    }

    // ---------------------------------------------------------
    // MAIN APP
    // ---------------------------------------------------------
    when (selectedTab) {

        // HOME
        0 -> HomeScreen(
            viewModel = viewModel,
            onNavigate = {
                selectedTab = it
            }
        )

        // ATTENDANCE
        1 -> AttendanceScreen(
            onNavigate = {
                selectedTab = it
            },
            onFaceRecognition = {
                showFaceRecognition = true
            }
        )

        // PROFILE
        2 -> ProfileScreen(
            onNavigate = {
                selectedTab = it
            }
        )
    }
}