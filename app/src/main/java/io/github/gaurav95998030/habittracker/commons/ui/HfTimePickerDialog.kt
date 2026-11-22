package io.github.gaurav95998030.habittracker.commons.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme

/** Themed M3 time picker dialog. [initial] and the confirmed value are "HH:mm". */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HfTimePickerDialog(
    title: String,
    initial: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val c = AppTheme.colors
    val parts = initial.split(":")
    val state = rememberTimePickerState(
        initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 7,
        initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 30,
        is24Hour = false,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (c.isDark) c.surface2 else c.surface,
        title = { Text(title, style = AppTheme.textStyles.titleMedium, color = c.textPrimary) },
        text = {
            TimePicker(
                state = state,
                colors = TimePickerDefaults.colors(
                    clockDialColor = c.glassFill,
                    selectorColor = c.primary,
                    timeSelectorSelectedContainerColor = c.primary.copy(alpha = 0.2f),
                    timeSelectorSelectedContentColor = c.textPrimary,
                    timeSelectorUnselectedContainerColor = c.glassFill,
                    timeSelectorUnselectedContentColor = c.textSecondary,
                    periodSelectorSelectedContainerColor = c.primary.copy(alpha = 0.2f),
                    periodSelectorSelectedContentColor = c.textPrimary,
                    periodSelectorUnselectedContentColor = c.textSecondary,
                    periodSelectorBorderColor = c.glassBorder,
                ),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm("%02d:%02d".format(state.hour, state.minute)) }) {
                Text("Set", color = c.primary, style = AppTheme.textStyles.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = c.textSecondary, style = AppTheme.textStyles.labelLarge) }
        },
    )
}
