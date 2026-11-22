package io.github.gaurav95998030.habittracker.features.reminders.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.gaurav95998030.habittracker.features.habits.HabitUtil
import io.github.gaurav95998030.habittracker.features.habits.presentation.viewmodel.HabitViewModel
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NotificationCenterScreen(
    viewModel: HabitViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val habits = uiState.habits ?: emptyList()
    // Recomputed whenever the habit list changes; time is sampled at composition.
    val data = remember(habits) { NotificationCenterBuilder.build(habits) }

    NotificationCenterContent(
        data = data,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
private fun NotificationCenterContent(
    data: NotificationCenterUi,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
    ) {
        item("header") {
            NotificationHeader(onBack = onBack)
            Spacer(Modifier.height(20.dp))
        }

        item("summary") {
            ScheduledTodayCard(count = data.scheduledTodayCount)
            Spacer(Modifier.height(28.dp))
        }

        if (data.isEmpty) {
            item("empty") { EmptyState() }
        }

        data.sections.forEach { section ->
            item(key = "label_${section.label}") {
                SectionLabel(section.label)
                Spacer(Modifier.height(12.dp))
            }
            items(section.items, key = { "${section.label}_${it.habitId}" }) { item ->
                NotificationRow(item)
                Spacer(Modifier.height(12.dp))
            }
            item(key = "gap_${section.label}") {
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun NotificationHeader(onBack: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surface)
                .border(1.dp, colors.divider, RoundedCornerShape(16.dp))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = colors.textPrimary,
                modifier = Modifier.size(22.dp),
            )
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Notifications",
                style = AppTheme.textStyles.titleLarge.copy(
                    color = colors.textPrimary,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Every reminder, in one place",
                style = AppTheme.textStyles.bodyMedium.copy(color = colors.textSecondary),
            )
        }

        Spacer(Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(colors.primary.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun ScheduledTodayCard(count: Int) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.primary.copy(alpha = 0.10f))
            .border(1.dp, colors.primary.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(colors.primary.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(26.dp),
            )
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = "$count reminders scheduled today",
            style = AppTheme.textStyles.titleLarge.copy(
                color = colors.textPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 28.sp,
            ),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = AppTheme.textStyles.labelSmall.copy(
            color = AppTheme.colors.textTertiary,
            letterSpacing = 1.4.sp,
            fontWeight = FontWeight.SemiBold,
        ),
        modifier = Modifier.padding(start = 4.dp),
    )
}

@Composable
private fun NotificationRow(item: ReminderNotification) {
    val colors = AppTheme.colors
    val accent = remember(item.colorHex) {
        runCatching { HabitUtil.hexToColor(item.colorHex) }.getOrDefault(colors.primary)
    }
    val dim = if (item.status == ReminderStatus.Missed) 0.55f else 1f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surface.copy(alpha = 0.7f))
            .border(1.dp, colors.divider.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(accent.copy(alpha = 0.16f))
                .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                .alpha(dim),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(24.dp),
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = AppTheme.textStyles.bodyLarge.copy(
                    color = colors.textPrimary.copy(alpha = dim),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                ),
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = item.timeLabel,
                style = AppTheme.textStyles.bodyMedium.copy(
                    color = colors.textSecondary.copy(alpha = dim),
                ),
            )
        }

        Spacer(Modifier.width(8.dp))
        StatusTrailing(item.status)
    }
}

@Composable
private fun StatusTrailing(status: ReminderStatus) {
    val colors = AppTheme.colors
    when (status) {
        ReminderStatus.Missed -> Text(
            text = "Missed",
            style = AppTheme.textStyles.bodyMedium.copy(color = colors.textTertiary),
        )
        ReminderStatus.Done -> Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = colors.success,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "Done",
                style = AppTheme.textStyles.bodyMedium.copy(
                    color = colors.success,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        }
        ReminderStatus.Scheduled -> Unit
    }
}

@Composable
private fun EmptyState() {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(colors.surface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.NotificationsOff,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(32.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "No reminders yet",
            style = AppTheme.textStyles.bodyLarge.copy(
                color = colors.textPrimary,
                fontWeight = FontWeight.SemiBold,
            ),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Turn on a reminder for a habit and it\nwill show up here.",
            style = AppTheme.textStyles.bodyMedium.copy(color = colors.textTertiary),
        )
    }
}
