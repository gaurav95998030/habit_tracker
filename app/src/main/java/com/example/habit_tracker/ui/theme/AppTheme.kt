package com.example.habit_tracker.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

/**
 * Singleton object providing access to theme values throughout the app.
 * Access colors via AppTheme.colors and text styles via AppTheme.textStyles.
 *
 * Usage:
 * ```
 * Text(
 *     text = "Hello World",
 *     style = AppTheme.textStyles.titleLarge,
 *     color = AppTheme.colors.textPrimary
 * )
 * ```
 */
object AppTheme {
    /**
     * Retrieves the current [AppColors] from the composition.
     * Provides theme-aware semantic colors for light/dark mode.
     */
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current

    /**
     * Retrieves the current [AppTextStyles] from the composition.
     * Provides semantic text styles with consistent sizing and weights.
     */
    val textStyles: AppTextStyles
        @Composable
        @ReadOnlyComposable
        get() = LocalAppTextStyles.current
}

