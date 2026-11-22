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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.gaurav95998030.habittracker.commons.ui.HSpace
import io.github.gaurav95998030.habittracker.commons.ui.HfButton
import io.github.gaurav95998030.habittracker.commons.ui.HfButtonSize
import io.github.gaurav95998030.habittracker.commons.ui.HfIconButton
import io.github.gaurav95998030.habittracker.commons.ui.HfSectionLabel
import io.github.gaurav95998030.habittracker.commons.ui.PhIcon
import io.github.gaurav95998030.habittracker.commons.ui.PhIconNamed
import io.github.gaurav95998030.habittracker.commons.ui.PhWeight
import io.github.gaurav95998030.habittracker.commons.ui.accentWash
import io.github.gaurav95998030.habittracker.features.habits.HabitCatalog
import io.github.gaurav95998030.habittracker.features.habits.HabitUtil
import io.github.gaurav95998030.habittracker.features.habits.presentation.viewmodel.HabitViewModel
import io.github.gaurav95998030.habittracker.ui.theme.Accents
import io.github.gaurav95998030.habittracker.ui.theme.AppShapes
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme
import io.github.gaurav95998030.habittracker.ui.theme.PhIcons

/**
 * Every reminder from the last 9 days, grouped by day, derived from the habit list.
 * Due-now items have an inline "Done"; tapping a habit's icon opens its detail.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NotificationCenterScreen(
    viewModel: HabitViewModel,
    userName: String,
    onBack: () -> Unit,
    onOpenHabit: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = AppTheme.colors
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val habits = uiState.habits.orEmpty()
    // Recomputed whenever the habit list changes; time is sampled at composition.
    val data = remember(habits) { NotificationCenterBuilder.build(habits, HabitCatalog::iconFor) }

    Box(modifier.fillMaxSize().background(c.background)) {
        // Aurora wash.
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-80).dp)
                .size(360.dp, 280.dp)
                .background(Brush.radialGradient(listOf(c.primary.copy(alpha = if (c.isDark) 0.09f else 0.06f), Color.Transparent))),
        )
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(
                Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HfIconButton(PhIcons.ArrowLeft, onClick = onBack, size = 40.dp, iconSize = 18.dp, tint = c.textPrimary, contentDescription = "Back")
                HSpace(12.dp)
                Column(Modifier.weight(1f)) {
                    Text("Notifications", style = AppTheme.textStyles.titleLarge.copy(fontSize = 22.sp, letterSpacing = (-0.4).sp), color = c.textPrimary)
                    Text("Every reminder, in one place", style = AppTheme.textStyles.bodySmall, color = c.textTertiary)
                }
                PhIcon(PhIcons.BellRinging, size = 24.dp, tint = c.primary, weight = PhWeight.Fill)
            }

            if (data.isEmpty) {
                EmptyReminders()
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 32.dp),
                ) {
                    item(key = "summary") {
                        TodaySummary(data, userName, Modifier.padding(bottom = 22.dp))
                    }
                    data.sections.forEach { section ->
                        item(key = "h-${section.date}") {
                            HfSectionLabel(section.label, Modifier.padding(start = 2.dp), bottom = 10.dp)
                        }
                        items(section.items, key = { "${section.date}-${it.key}" }) { n ->
                            ReminderRow(
                                n = n,
                                onOpen = { onOpenHabit(n.habitId) },
                                onDone = { viewModel.toggleToday(n.habitId) },
                                modifier = Modifier.padding(bottom = 10.dp),
                            )
                        }
                        item(key = "s-${section.date}") { Box(Modifier.height(12.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TodaySummary(data: NotificationCenterUi, userName: String, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    val shape = RoundedCornerShape(AppShapes.card)
    val pending = data.todayPending
    Row(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(accentWash(c.primary, 0.1f))
            .border(1.dp, if (c.isDark) c.primary.copy(alpha = 0.2f) else c.glassBorder, shape)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(c.primary.copy(alpha = 0.13f)),
            contentAlignment = Alignment.Center,
        ) {
            PhIcon(if (pending > 0) PhIcons.BellRinging else PhIcons.CheckFat, size = 24.dp, tint = c.primary, weight = PhWeight.Fill)
        }
        HSpace(14.dp)
        Column(Modifier.weight(1f)) {
            Text(
                if (pending > 0) "$pending reminder${if (pending > 1) "s" else ""} waiting" else "You’re all caught up",
                style = AppTheme.textStyles.titleMedium.copy(fontSize = 16.sp, letterSpacing = (-0.2).sp),
                color = c.textPrimary,
            )
            Text(
                "${data.todayDone} of ${data.todayTotal} of today’s reminders done${if (userName.isNotBlank()) ", ${userName.trim()}" else ""}.",
                style = AppTheme.textStyles.bodyMedium.copy(fontSize = 13.sp),
                color = c.textSecondary,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
    }
}

@Composable
private fun ReminderRow(n: ReminderNotification, onOpen: () -> Unit, onDone: () -> Unit, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    val color = Accents.colorOf(n.colorHex)
    val (label, statusColor, icon) = when (n.status) {
        ReminderStatus.Done -> Triple("Marked done · ${n.streak}-day streak", c.success, PhIcons.CheckCircle)
        ReminderStatus.Missed -> Triple("You didn't check in", c.textTertiary, PhIcons.XCircle)
        ReminderStatus.Pending -> Triple("Keep your ${n.streak}-day streak alive", c.warning, PhIcons.BellRinging)
        ReminderStatus.Upcoming -> Triple("Reminder scheduled", c.textSecondary, PhIcons.Clock)
    }
    val shape = RoundedCornerShape(AppShapes.tile)
    Row(
        modifier
            .fillMaxWidth()
            .alpha(if (n.status == ReminderStatus.Missed) 0.62f else 1f)
            .clip(shape)
            .background(c.surface)
            .border(1.dp, if (n.status == ReminderStatus.Pending) c.warning.copy(alpha = 0.27f) else c.glassBorder, shape)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(color.copy(alpha = 0.13f))
                .clickable(onClickLabel = "Open ${n.habitName}", onClick = onOpen),
            contentAlignment = Alignment.Center,
        ) {
            PhIconNamed(n.icon, size = 23.dp, tint = color)
        }
        HSpace(13.dp)
        Column(Modifier.weight(1f)) {
            Text(
                "Time for ${n.habitName}",
                style = AppTheme.textStyles.titleSmall.copy(letterSpacing = (-0.1).sp),
                color = c.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(Modifier.padding(top = 3.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PhIcon(PhIcons.Clock, size = 13.dp, tint = c.textTertiary)
                Text(HabitUtil.formatTime12h(n.time), style = AppTheme.textStyles.bodySmall, color = c.textTertiary)
                Box(Modifier.size(3.dp).alpha(0.6f).clip(CircleShape).background(c.textTertiary))
                Text(label, style = AppTheme.textStyles.bodySmall, color = statusColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        if (n.status == ReminderStatus.Pending) {
            HfButton("Done", onClick = onDone, icon = PhIcons.Check, size = HfButtonSize.Small)
        } else {
            PhIcon(icon, size = 18.dp, tint = statusColor, weight = PhWeight.Fill)
        }
    }
}

@Composable
private fun EmptyReminders() {
    val c = AppTheme.colors
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 70.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(88.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(c.surface)
                .border(1.dp, c.glassBorder, RoundedCornerShape(26.dp)),
            contentAlignment = Alignment.Center,
        ) {
            PhIcon(PhIcons.BellSlash, size = 42.dp, tint = c.textTertiary, weight = PhWeight.Fill)
        }
        Text("No reminders yet", style = AppTheme.textStyles.titleLarge.copy(fontSize = 20.sp), color = c.textPrimary, modifier = Modifier.padding(top = 20.dp))
        Text(
            "Turn on reminders for a habit and they'll show up here.",
            style = AppTheme.textStyles.bodyMedium.copy(lineHeight = 21.sp),
            color = c.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp).widthIn(max = 240.dp),
        )
    }
}
