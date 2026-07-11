package com.quacko.billing.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = SimnTeal,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = SimnTealLight,
    onPrimaryContainer = SimnTealDark,
    secondary = SimnOrange,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = SimnOrangeLight,
    background = SurfaceLight,
    onBackground = Color(0xFF16201F),
    surface = CardLight,
    onSurface = Color(0xFF16201F),
    error = Danger
)

private val DarkColors = darkColorScheme(
    primary = SimnTealLight,
    onPrimary = SimnTealDark,
    primaryContainer = SimnTealDark,
    secondary = SimnOrange,
    onSecondary = Color(0xFF241202),
    background = SurfaceDark,
    surface = CardDark,
    error = Danger
)

@Composable
fun QuackoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = QuackoTypography,
        content = content
    )
}
