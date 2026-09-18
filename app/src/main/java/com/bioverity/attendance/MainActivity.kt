package com.bioverity.attendance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.bioverity.attendance.navigation.AppNavigation
import com.bioverity.attendance.ui.theme.BioVerityAttendanceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BioVerityAttendanceTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

