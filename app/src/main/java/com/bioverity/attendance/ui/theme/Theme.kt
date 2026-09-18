package com.bioverity.attendance.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AppColorScheme = lightColorScheme(
    primary = Navy,
    secondary = Teal,
    background = Background,
    surface = CardWhite,
    onPrimary = CardWhite,
    onSecondary = CardWhite,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun BioVerityAttendanceTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = AppTypography,
        content = content
    )
}