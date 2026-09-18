
package com.bioverity.attendance.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bioverity.attendance.ui.components.BottomNavigationBar
import com.bioverity.attendance.ui.theme.Background

@Composable
fun AttendanceScreen(
    onNavigate: (Int) -> Unit,
    onFaceRecognition: () -> Unit
) {

    Scaffold(
        containerColor = Background,
        bottomBar = {
            BottomNavigationBar(
                selectedIndex = 1,
                onItemSelected = onNavigate
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Button(
                onClick = {
                    onFaceRecognition()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Mark Attendance")
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text(
                text = "Attendance History",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

