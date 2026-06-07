package io.github.gaurav95998030.habittracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFCDF564),
    onPrimary = Color(0xFF0B1500),
    secondary = Color(0xFF7BC9FF),
    tertiary = Color(0xFFCDF564),
    background = Color(0xFF050505),
    surface = Color(0xFF111111),
    onBackground = Color(0xFFF2F2F2),
    onSurface = Color(0xFFF2F2F2),
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

/**
 * Main theme composable for the Habit Tracker app.
 * Provides both Material 3 theme and custom AppTheme (colors + text styles).
 *
 * @param darkTheme Whether to use dark theme. Defaults to system setting.
 * @param dynamicColor Whether to use dynamic color on Android 12+. Defaults to true.
 * @param content The composable content to be themed.
 */
@Composable
fun Habit_TrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+. Disabled by default so the
    // designed lime/green accent palette is the source of truth instead of the
    // device wallpaper.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Select AppColors based on theme
    val appColors = if (darkTheme) DarkAppColors else LightAppColors

    // Provide custom theme values via CompositionLocal
    CompositionLocalProvider(
        LocalAppColors provides appColors,
        LocalAppTextStyles provides DefaultAppTextStyles
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}