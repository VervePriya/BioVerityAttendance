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
fun ProfileScreen(
    onNavigate: (Int) -> Unit
) {

    Scaffold(
        containerColor = Background,
        bottomBar = {
            BottomNavigationBar(
                selectedIndex = 2,
                onItemSelected = onNavigate
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = "Profile"
            )
        }
    }
}