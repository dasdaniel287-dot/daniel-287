package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JervesColorScheme = darkColorScheme(
    primary = JervesCyanPrimary,
    onPrimary = Color(0xFF001F29),
    primaryContainer = JervesCyanContainer,
    onPrimaryContainer = JervesCyanPrimary,
    secondary = JervesCobalt,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF0A2540),
    onSecondaryContainer = Color(0xFFBAE6FD),
    tertiary = JervesNeonGreen,
    onTertiary = Color(0xFF00381E),
    tertiaryContainer = Color(0xFF004D2A),
    onTertiaryContainer = JervesNeonGreen,
    background = JervesBgDark,
    onBackground = JervesTextPrimary,
    surface = JervesSurfaceDark,
    onSurface = JervesTextPrimary,
    surfaceVariant = JervesSurfaceVariant,
    onSurfaceVariant = JervesTextSecondary,
    outline = JervesSurfaceBorder,
    outlineVariant = Color(0xFF162A43),
    error = JervesCrimson,
    onError = Color.White
)

@Composable
fun JervesTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = JervesColorScheme,
        typography = Typography,
        content = content
    )
}
