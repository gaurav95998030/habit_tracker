package io.github.gaurav95998030.habittracker.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic color tokens — the HabitForge "Aurora" palette (1:1 with the design's
 * tokens.jsx). Dark is the hero theme; light is its daylight counterpart.
 *
 * The aurora-specific tokens ([surface2], [glassFill], [glassBorder], [heatEmpty],
 * [scrim]) carry translucency, so they read correctly layered over any background.
 */
@Immutable
data class AppColors(
    val isDark: Boolean,
    val primary: Color,
    val primaryVariant: Color,
    val secondary: Color,
    val background: Color,
    /** Base card surface. */
    val surface: Color,
    /** Raised / sheet surface. */
    val surface2: Color,
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
    val warning: Color,
    val glassBorder: Color,
    val glassFill: Color,
    /** Empty cell in heatmaps, week dots and ring tracks. */
    val heatEmpty: Color,
    val scrim: Color,
    /** Background of text fields and grouped inputs. */
    val fieldFill: Color,
)

internal val DarkAppColors = AppColors(
    isDark = true,
    primary = Color(0xFFC2FF4D),
    primaryVariant = Color(0xFF9BE000),
    secondary = Color(0xFF34E5A0),
    background = Color(0xFF070A0B),
    surface = Color(0xFF10171A),
    surface2 = Color(0xFF161F23),
    error = Color(0xFFFF5D52),
    textPrimary = Color(0xFFF2F7F4),
    textSecondary = Color(0xFF9AACA8),
    textTertiary = Color(0xFF5E716D),
    onPrimary = Color(0xFF0A1400),
    onSecondary = Color(0xFF00271B),
    onBackground = Color(0xFFF2F7F4),
    onSurface = Color(0xFFF2F7F4),
    onError = Color(0xFF2A0400),
    divider = Color.White.copy(alpha = 0.08f),
    success = Color(0xFF34E5A0),
    warning = Color(0xFFFFB23E),
    glassBorder = Color.White.copy(alpha = 0.09f),
    glassFill = Color.White.copy(alpha = 0.04f),
    heatEmpty = Color.White.copy(alpha = 0.05f),
    scrim = Color(0xFF020405).copy(alpha = 0.72f),
    fieldFill = Color.White.copy(alpha = 0.04f),
)

private val Ink = Color(0xFF0C1110)

internal val LightAppColors = AppColors(
    isDark = false,
    primary = Color(0xFF2BAA4A),
    primaryVariant = Color(0xFF1F8A3B),
    secondary = Color(0xFF0FB39A),
    background = Color(0xFFF4F7F2),
    surface = Color(0xFFFFFFFF),
    surface2 = Color(0xFFFBFDF9),
    error = Color(0xFFD6362B),
    textPrimary = Ink,
    textSecondary = Color(0xFF4C5A56),
    textTertiary = Color(0xFF849490),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Ink,
    onSurface = Ink,
    onError = Color(0xFFFFFFFF),
    divider = Ink.copy(alpha = 0.10f),
    success = Color(0xFF118A5E),
    warning = Color(0xFFB6760A),
    glassBorder = Ink.copy(alpha = 0.08f),
    glassFill = Color.White.copy(alpha = 0.7f),
    heatEmpty = Ink.copy(alpha = 0.06f),
    scrim = Ink.copy(alpha = 0.42f),
    fieldFill = Color.White,
)

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }
