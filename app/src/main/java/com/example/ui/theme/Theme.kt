package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val IndustrialColorScheme = darkColorScheme(
    primary = ThemePrimary,
    onPrimary = ThemeOnPrimary,
    secondary = ThemeSecondary,
    tertiary = ThemeTertiary,
    error = ThemeError,
    background = ThemeBackground,
    surface = ThemeSurface,
    surfaceVariant = ThemeSurfaceVariant,
    onBackground = ThemeOnBackground,
    onSurface = ThemeOnSurface,
    onSurfaceVariant = ThemeOnBackground
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = IndustrialColorScheme,
        typography = Typography,
        content = content
    )
}
