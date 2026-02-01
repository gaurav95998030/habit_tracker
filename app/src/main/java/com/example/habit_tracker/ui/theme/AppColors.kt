package com.example.habit_tracker.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Immutable data class containing semantic color tokens for the app.
 * Provides theme-aware colors that adapt to light/dark mode.
 */
@Immutable
data class AppColors(
    val primary: Color,
    val primaryVariant: Color,
    val secondary: Color,
    val background: Color,
    val surface: Color,
    val error: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val onPrimary: Color,
    val onSecondary: Color,
    val onBackground: Color,
    val onSurface: Color,
    val onError: Color,
    val divider: Color,
    val success: Color,
    val warning: Color
)

/**
 * Light theme color palette
 */
internal val LightAppColors = AppColors(
    primary = Color(0xFF6750A4),
    primaryVariant = Color(0xFF625B71),
    secondary = Color(0xFF7D5260),
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    error = Color(0xFFB3261E),
    textPrimary = Color(0xFF1C1B1F),
    textSecondary = Color(0xFF49454F),
    textTertiary = Color(0xFF79747E),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    onError = Color(0xFFFFFFFF),
    divider = Color(0xFFE7E0EC),
    success = Color(0xFF2E7D32),
    warning = Color(0xFFF57C00)
)

/**
 * Dark theme color palette
 */
internal val DarkAppColors = AppColors(
    primary = Color(0xFFD0BCFF),
    primaryVariant = Color(0xFFCCC2DC),
    secondary = Color(0xFFEFB8C8),
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF1C1B1F),
    error = Color(0xFFF2B8B5),
    textPrimary = Color(0xFFE6E1E5),
    textSecondary = Color(0xFFCAC4D0),
    textTertiary = Color(0xFF938F99),
    onPrimary = Color(0xFF381E72),
    onSecondary = Color(0xFF492532),
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
    onError = Color(0xFF601410),
    divider = Color(0xFF49454F),
    success = Color(0xFF81C784),
    warning = Color(0xFFFFB74D)
)

/**
 * CompositionLocal to provide AppColors throughout the composition tree
 */
val LocalAppColors = staticCompositionLocalOf { LightAppColors }

