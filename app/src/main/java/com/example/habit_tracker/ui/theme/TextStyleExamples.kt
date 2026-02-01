package com.example.habit_tracker.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Example composable demonstrating usage of AppTheme text styles.
 * Shows how to use AppTheme.textStyles with AppTheme.colors for theme-aware text.
 */
@Composable
fun TextStyleExamplesScreen(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = AppTheme.colors.background
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Title Large - Main screen titles
            Text(
                text = "My Habits",
                style = AppTheme.textStyles.titleLarge,
                color = AppTheme.colors.textPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Title Medium - Section headers
            Text(
                text = "Daily Goals",
                style = AppTheme.textStyles.titleMedium,
                color = AppTheme.colors.textPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Body Large - Primary content
            Text(
                text = "Morning Exercise",
                style = AppTheme.textStyles.bodyLarge,
                color = AppTheme.colors.textPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Body Medium - Standard text
            Text(
                text = "Complete 30 minutes of cardio exercise",
                style = AppTheme.textStyles.bodyMedium,
                color = AppTheme.colors.textSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Body Small - Supporting text
            Text(
                text = "Track your progress and build consistency",
                style = AppTheme.textStyles.bodySmall,
                color = AppTheme.colors.textTertiary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Label Medium - Labels and buttons
            Text(
                text = "MARK COMPLETE",
                style = AppTheme.textStyles.labelMedium,
                color = AppTheme.colors.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Caption - Timestamps and metadata
            Text(
                text = "Last completed 2 hours ago",
                style = AppTheme.textStyles.caption,
                color = AppTheme.colors.textTertiary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Example with success color
            Text(
                text = "7 day streak! 🔥",
                style = AppTheme.textStyles.bodyMedium,
                color = AppTheme.colors.success
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Example with warning color
            Text(
                text = "You missed yesterday's goal",
                style = AppTheme.textStyles.bodySmall,
                color = AppTheme.colors.warning
            )
        }
    }
}

/**
 * Preview of text styles in light theme
 */
@Preview(
    name = "Light Theme",
    showBackground = true
)
@Composable
fun TextStyleExamplesPreview() {
    Habit_TrackerTheme(darkTheme = false) {
        TextStyleExamplesScreen()
    }
}

/**
 * Preview of text styles in dark theme
 */
@Preview(
    name = "Dark Theme",
    showBackground = true
)
@Composable
fun TextStyleExamplesDarkPreview() {
    Habit_TrackerTheme(darkTheme = true) {
        TextStyleExamplesScreen()
    }
}

