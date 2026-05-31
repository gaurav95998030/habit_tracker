package io.github.gaurav95998030.habittracker.features.habits.presentation.habit_list.molecules

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderPicker(
    enabled: Boolean,
    time: String?,
    onEnabledChanged: (Boolean) -> Unit,
    onTimeSelected: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val textStyles = AppTheme.textStyles
    val context = LocalContext.current

    var showTimePicker by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* result handled implicitly — reminder still saved either way */ }

    fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surface)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Daily reminder",
                    style = textStyles.bodyMedium.copy(
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = if (enabled && time != null)
                        "Notifies you at $time every day"
                    else
                        "Get a notification to keep your streak alive",
                    style = textStyles.bodySmall.copy(color = colors.textTertiary)
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = { checked ->
                    if (checked) requestNotificationPermissionIfNeeded()
                    onEnabledChanged(checked)
                }
            )
        }

        if (enabled) {
            Row(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .clickable { showTimePicker = true }
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Time",
                    style = textStyles.bodyMedium.copy(color = colors.textSecondary)
                )
                Text(
                    text = time ?: "Pick a time",
                    style = textStyles.bodyMedium.copy(
                        color = colors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }

    if (showTimePicker) {
        val (initialHour, initialMinute) = parseInitialTime(time)
        val timePickerState = rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = false
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onTimeSelected(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            title = { Text("Reminder time") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            tonalElevation = 4.dp,
            modifier = Modifier.padding(PaddingValues(0.dp))
        )
    }
}

private fun parseInitialTime(time: String?): Pair<Int, Int> {
    if (time == null) return 9 to 0
    val parts = time.split(":")
    if (parts.size != 2) return 9 to 0
    val h = parts[0].toIntOrNull()?.coerceIn(0, 23) ?: 9
    val m = parts[1].toIntOrNull()?.coerceIn(0, 59) ?: 0
    return h to m
}
