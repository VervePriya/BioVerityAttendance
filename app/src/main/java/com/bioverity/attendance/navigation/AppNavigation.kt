
package com.bioverity.attendance.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bioverity.attendance.ui.screens.AttendanceScreen
import com.bioverity.attendance.ui.screens.FaceRecognitionScreen
import com.bioverity.attendance.ui.screens.HomeScreen
import com.bioverity.attendance.ui.screens.ProfileScreen
import com.bioverity.attendance.viewmodel.AttendanceViewModel

@Composable
fun AppNavigation() {

    var selectedTab by remember {
        mutableIntStateOf(0)
    }

    var showFaceRecognition by remember {
        mutableStateOf(false)
    }

    val viewModel: AttendanceViewModel = viewModel()

    if (showFaceRecognition) {

        FaceRecognitionScreen(
            onBack = {
                showFaceRecognition = false
            },
            onFaceDetected = {
                // Face detection callback.
                // Actual employee recognition will be connected later.
            }
        )

    } else {

        when (selectedTab) {

            0 -> HomeScreen(
                viewModel = viewModel,
                onNavigate = {
                    selectedTab = it
                }
            )

            1 -> AttendanceScreen(
                onNavigate = {
                    selectedTab = it
                },
                onFaceRecognition = {
                    showFaceRecognition = true
                }
            )

            2 -> ProfileScreen(
                onNavigate = {
                    selectedTab = it
                }
            )
        }
    }
}

