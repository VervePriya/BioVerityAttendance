package com.bioverity.attendance.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun BottomNavigationBar(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {

    NavigationBar {

        NavigationBarItem(
            selected = selectedIndex == 0,
            onClick = { onItemSelected(0) },
            icon = {
                Icon(
                    Icons.Default.Home,
                    contentDescription = "Home"
                )
            },
            label = {
                Text("Home")
            }
        )

        NavigationBarItem(
            selected = selectedIndex == 1,
            onClick = { onItemSelected(1) },
            icon = {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = "Attendance"
                )
            },
            label = {
                Text("Attendance")
            }
        )

        NavigationBarItem(
            selected = selectedIndex == 2,
            onClick = { onItemSelected(2) },
            icon = {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile"
                )
            },
            label = {
                Text("Profile")
            }
        )
    }
}