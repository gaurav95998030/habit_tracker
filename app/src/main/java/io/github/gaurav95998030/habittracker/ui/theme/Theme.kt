package io.github.gaurav95998030.habittracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/** Material scheme derived from the Aurora tokens so stock M3 widgets match. */
private fun AppColors.toMaterialScheme(): ColorScheme {
    val base = if (isDark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primary.copy(alpha = 0.18f),
        onPrimaryContainer = textPrimary,
        secondary = secondary,
        onSecondary = onSecondary,
        tertiary = secondary,
        background = background,
        onBackground = onBackground,
        surface = surface2,
        onSurface = onSurface,
        surfaceVariant = surface,
        onSurfaceVariant = textSecondary,
        surfaceContainer = surface2,
        surfaceContainerHigh = surface2,
        surfaceContainerHighest = surface,
        surfaceContainerLow = surface,
        inverseSurface = textPrimary,
        inverseOnSurface = background,
        inversePrimary = primary,
        error = error,
        onError = onError,
        outline = textTertiary,
        outlineVariant = divider,
        scrim = scrim,
    )
}

/**
 * Root theme. Provides the Glowstreak tokens via [AppTheme] plus a matching
 * Material 3 theme. Dynamic color is intentionally not supported — the Aurora
 * palette is the source of truth.
 */
@Composable
fun Habit_TrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val appColors = if (darkTheme) DarkAppColors else LightAppColors
    CompositionLocalProvider(
        LocalAppColors provides appColors,
        LocalAppTextStyles provides DefaultAppTextStyles,
    ) {
        MaterialTheme(
            colorScheme = appColors.toMaterialScheme(),
            typography = Typography,
            content = content,
        )
    }
}
