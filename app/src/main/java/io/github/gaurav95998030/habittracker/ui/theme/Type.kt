package io.github.gaurav95998030.habittracker.ui.theme

import androidx.compose.material3.Typography

/**
 * Material typography, derived from [DefaultAppTextStyles] so stock M3 components
 * (snackbars, dialogs, time picker) use the same faces as the custom UI.
 */
val Typography = Typography(
    displayLarge = DefaultAppTextStyles.displayLarge,
    headlineMedium = DefaultAppTextStyles.titleLarge,
    titleLarge = DefaultAppTextStyles.titleLarge,
    titleMedium = DefaultAppTextStyles.titleMedium,
    titleSmall = DefaultAppTextStyles.titleSmall,
    bodyLarge = DefaultAppTextStyles.bodyLarge,
    bodyMedium = DefaultAppTextStyles.bodyMedium,
    bodySmall = DefaultAppTextStyles.bodySmall,
    labelLarge = DefaultAppTextStyles.labelLarge,
    labelMedium = DefaultAppTextStyles.labelMedium,
    labelSmall = DefaultAppTextStyles.labelSmall,
)
