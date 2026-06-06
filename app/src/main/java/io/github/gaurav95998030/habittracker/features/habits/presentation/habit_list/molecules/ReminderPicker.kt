package io.github.gaurav95998030.habittracker.features.habits.presentation.habit_list.molecules

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

private val FieldBg = Color(0xFF161616)
private val FieldBorder = Color(0xFF2A2A2A)
private val LimeAccent = Color(0xFFC9FF3E)
private val TextWhite = Color(0xFFEFEFEF)
private val TextMuted = Color(0xFF8A8A8A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderTimeField(
    time: String?,
    onTimeSelected: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showTimePicker by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(FieldBg)
            .border(1.dp, FieldBorder, RoundedCornerShape(14.dp))
            .clickable { showTimePicker = true }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = formatTime12h(time),
            color = TextWhite,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        ClockIcon(tint = TextMuted)
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
            tonalElevation = 4.dp
        )
    }
}

@Composable
fun AlertsToggleField(
    enabled: Boolean,
    onEnabledChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

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

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(FieldBg)
            .border(1.dp, FieldBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Enable",
            color = TextWhite,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Switch(
            checked = enabled,
            onCheckedChange = { checked ->
                if (checked) requestNotificationPermissionIfNeeded()
                onEnabledChanged(checked)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = LimeAccent,
                checkedBorderColor = LimeAccent,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = Color(0xFF222222),
                uncheckedBorderColor = FieldBorder
            )
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

@Composable
private fun ClockIcon(tint: Color) {
    Canvas(modifier = Modifier.size(20.dp)) {
        val stroke = 1.6.dp.toPx()
        val r = size.minDimension / 2f - stroke
        val cx = size.width / 2f
        val cy = size.height / 2f
        drawCircle(
            color = tint,
            radius = r,
            center = androidx.compose.ui.geometry.Offset(cx, cy),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
        )
        drawLine(
            color = tint,
            start = androidx.compose.ui.geometry.Offset(cx, cy),
            end = androidx.compose.ui.geometry.Offset(cx, cy - r * 0.55f),
            strokeWidth = stroke,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = androidx.compose.ui.geometry.Offset(cx, cy),
            end = androidx.compose.ui.geometry.Offset(cx + r * 0.45f, cy),
            strokeWidth = stroke,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

private fun formatTime12h(time: String?): String {
    val (h24, m) = parseInitialTime(time)
    val period = if (h24 < 12) "AM" else "PM"
    val h12 = when {
        h24 == 0 -> 12
        h24 > 12 -> h24 - 12
        else -> h24
    }
    val hh = h12.toString().padStart(2, '0')
    val mm = m.toString().padStart(2, '0')
    return "$hh:$mm $period"
}
