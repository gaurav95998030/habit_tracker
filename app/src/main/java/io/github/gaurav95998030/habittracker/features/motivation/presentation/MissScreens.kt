package io.github.gaurav95998030.habittracker.features.motivation.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.gaurav95998030.habittracker.commons.ui.FeatureScreen
import io.github.gaurav95998030.habittracker.commons.ui.HSpace
import io.github.gaurav95998030.habittracker.commons.ui.HfBottomSheet
import io.github.gaurav95998030.habittracker.commons.ui.HfButton
import io.github.gaurav95998030.habittracker.commons.ui.HfButtonSize
import io.github.gaurav95998030.habittracker.commons.ui.HfButtonVariant
import io.github.gaurav95998030.habittracker.commons.ui.HfChip
import io.github.gaurav95998030.habittracker.commons.ui.HfPanel
import io.github.gaurav95998030.habittracker.commons.ui.PhIcon
import io.github.gaurav95998030.habittracker.commons.ui.PhIconNamed
import io.github.gaurav95998030.habittracker.commons.ui.PhWeight
import io.github.gaurav95998030.habittracker.commons.ui.accentWash
import io.github.gaurav95998030.habittracker.features.habits.HabitCatalog
import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit
import io.github.gaurav95998030.habittracker.features.habits.presentation.components.HabitIconBadge
import io.github.gaurav95998030.habittracker.features.motivation.domain.MotivationInsights
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.MissLog
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.MissReason
import io.github.gaurav95998030.habittracker.ui.theme.Accents
import io.github.gaurav95998030.habittracker.ui.theme.AppShapes
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme
import io.github.gaurav95998030.habittracker.ui.theme.PhIcons
import io.github.gaurav95998030.habittracker.ui.theme.glow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * "What got in the way?" — one row per habit missed yesterday, each with the five
 * reason chips. Opened from the morning notification or from Insights. Logged rows
 * stay visible (with Undo) until the sheet closes.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MissLogSheet(
    habits: List<Habit>,
    logs: List<MissLog>,
    onLog: (habitId: String, date: String, reason: MissReason?) -> Unit,
    onDismiss: () -> Unit,
) {
    val c = AppTheme.colors
    val yesterday = remember { LocalDate.now().minusDays(1) }
    // Snapshot of who missed yesterday (ignoring logs), so rows don't vanish once logged.
    val missed = remember(habits) { MotivationInsights.missedYesterday(habits, emptyList()) }
    val logged = logs.filter { it.date == yesterday.toString() }.associateBy { it.habitId }

    HfBottomSheet(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 22.dp, end = 22.dp, top = 6.dp, bottom = 22.dp)) {
            Text(
                if (missed.size == 1) "You missed ${missed[0].name} yesterday" else "Yesterday’s misses",
                style = AppTheme.textStyles.titleLarge.copy(fontSize = 21.sp, letterSpacing = (-0.3).sp),
                color = c.textPrimary,
            )
            Text(
                if (missed.isEmpty()) "Nothing missed yesterday — nice." else "What got in the way? One tap is enough.",
                style = AppTheme.textStyles.bodyMedium, color = c.textSecondary, modifier = Modifier.padding(top = 4.dp),
            )
            Column(
                Modifier.padding(top = 18.dp).weight(1f, fill = false).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                missed.forEach { h ->
                    MissRow(h, logged[h.id]?.reason, onPick = { onLog(h.id, yesterday.toString(), it) })
                }
            }
            HfButton("Done", onClick = onDismiss, variant = HfButtonVariant.Secondary, fullWidth = true, modifier = Modifier.padding(top = 18.dp))
        }
    }
}

@Composable
private fun MissRow(habit: Habit, picked: MissReason?, onPick: (MissReason?) -> Unit) {
    val c = AppTheme.colors
    val color = Accents.colorOf(habit.color)
    val shape = RoundedCornerShape(AppShapes.tile)
    Column(Modifier.fillMaxWidth().clip(shape).background(c.surface).border(1.dp, c.glassBorder, shape).padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            HabitIconBadge(HabitCatalog.iconFor(habit), color, size = 40.dp, radius = 12.dp, iconSize = 20.dp)
            HSpace(12.dp)
            Text(habit.name, style = AppTheme.textStyles.titleSmall.copy(fontSize = 16.sp), color = c.textPrimary, modifier = Modifier.weight(1f))
        }
        AnimatedContent(targetState = picked, label = "missPick") { p ->
            if (p != null) {
                Row(
                    Modifier.padding(top = 12.dp).fillMaxWidth().clip(RoundedCornerShape(14.dp))
                        .background(c.primary.copy(alpha = 0.09f)).border(1.dp, c.primary.copy(alpha = 0.27f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    PhIcon(PhIcons.CheckCircle, size = 20.dp, tint = c.primary, weight = PhWeight.Fill)
                    Text("Logged as ${p.label}. You’ll see patterns in Insights.", style = AppTheme.textStyles.bodyMedium.copy(fontSize = 13.sp),
                        color = c.textPrimary, modifier = Modifier.weight(1f))
                    Text("Undo", style = AppTheme.textStyles.bodySmall, fontWeight = FontWeight.SemiBold, color = c.textTertiary,
                        modifier = Modifier.clip(CircleShape).clickable { onPick(null) }.padding(4.dp))
                }
            } else {
                FlowRow(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MissReason.entries.forEach { r -> HfChip(label = r.label, icon = r.icon, active = false, onClick = { onPick(r) }) }
                }
            }
        }
    }
}

/** "Why you miss": top reasons, misses by weekday, and a suggestion when a pattern shows. */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MissPatternsScreen(
    habits: List<Habit>,
    logs: List<MissLog>,
    onBack: () -> Unit,
    onLogYesterday: () -> Unit,
    onEditHabit: (Habit) -> Unit,
) {
    val c = AppTheme.colors
    val p = remember(habits, logs) { MotivationInsights.missPatterns(habits, logs) }
    val pendingYesterday = remember(habits, logs) { MotivationInsights.missedYesterday(habits, logs).size }
    FeatureScreen("Why you miss", "${p.total} misses logged · last 90 days", onBack, icon = PhIcons.ChartBarHorizontal) {
        if (pendingYesterday > 0) {
            HfButton(
                "Log yesterday’s misses ($pendingYesterday)", onClick = onLogYesterday, variant = HfButtonVariant.Secondary,
                icon = PhIcons.PencilSimpleLine, fullWidth = true,
            )
        }
        if (p.total == 0) {
            Column(Modifier.fillMaxWidth().padding(vertical = 60.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                PhIcon(PhIcons.ChartBarHorizontal, size = 44.dp, tint = c.textTertiary, weight = PhWeight.Bold)
                Text("No misses logged yet", style = AppTheme.textStyles.titleLarge.copy(fontSize = 20.sp), color = c.textPrimary, modifier = Modifier.padding(top = 16.dp))
                Text(
                    "When you miss a habit, tap a reason from the morning reminder. Patterns show up here.",
                    style = AppTheme.textStyles.bodyMedium.copy(lineHeight = 21.sp), color = c.textSecondary, textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            return@FeatureScreen
        }

        HfPanel("Top reasons") {
            val max = (p.reasons.firstOrNull()?.count ?: 1).coerceAtLeast(1)
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                p.reasons.forEachIndexed { i, rc ->
                    val col = if (i == 0) c.warning else c.textTertiary
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        PhIconNamed(rc.reason.icon, size = 18.dp, tint = if (i == 0) c.warning else c.textSecondary)
                        Column(Modifier.weight(1f)) {
                            Row(Modifier.fillMaxWidth().padding(bottom = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(rc.reason.label, style = AppTheme.textStyles.bodyMedium.copy(fontSize = 13.sp), color = c.textPrimary)
                                Text("${rc.count}", style = AppTheme.textStyles.bodyMedium.copy(fontSize = 13.sp), color = c.textTertiary)
                            }
                            Box(Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(c.heatEmpty)) {
                                Box(
                                    Modifier.fillMaxWidth(rc.count.toFloat() / max).fillMaxHeight()
                                        .glow(col.copy(alpha = 0.66f), 8.dp, 999.dp, enabled = i == 0 && c.isDark)
                                        .clip(CircleShape).background(col),
                                )
                            }
                        }
                    }
                }
            }
        }

        val days = (1..7).map { DayOfWeek.of(it) }
        val maxDay = (p.byWeekday.maxOrNull() ?: 0).coerceAtLeast(1)
        HfPanel("Misses by day", if (p.byWeekday[p.topDayIndex] > 0) "${days[p.topDayIndex].getDisplayName(TextStyle.FULL, Locale.getDefault())} stands out" else null) {
            Row(Modifier.fillMaxWidth().height(90.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
                days.forEachIndexed { i, d ->
                    val top = i == p.topDayIndex
                    Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.Bottom)) {
                        Box(
                            Modifier.weight(1f, fill = false).fillMaxHeight(maxOf(p.byWeekday[i].toFloat() / maxDay, 0.04f)).fillMaxWidth()
                                .alpha(if (top) 1f else 0.4f).clip(RoundedCornerShape(6.dp)).background(if (top) c.warning else c.textTertiary),
                        )
                        Text(d.getDisplayName(TextStyle.NARROW, Locale.getDefault()), style = AppTheme.textStyles.caption.copy(fontSize = 10.sp),
                            fontWeight = if (top) FontWeight.SemiBold else FontWeight.Normal, color = if (top) c.textPrimary else c.textTertiary)
                    }
                }
            }
        }

        p.suggestion?.let { s ->
            val dayName = s.day.getDisplayName(TextStyle.FULL, Locale.getDefault())
            val shape = RoundedCornerShape(AppShapes.card)
            Row(
                Modifier.padding(top = 14.dp).fillMaxWidth().clip(shape).background(accentWash(c.primary, 0.09f))
                    .border(1.dp, c.primary.copy(alpha = 0.27f), shape).padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PhIcon(PhIcons.Lightbulb, size = 22.dp, tint = c.primary, weight = PhWeight.Fill)
                Column {
                    Text("${s.reason.label} ${dayName}s", style = AppTheme.textStyles.titleSmall, fontWeight = FontWeight.SemiBold, color = c.textPrimary)
                    val habitPart = s.habit?.let { " Try a different reminder time for ${it.name}." } ?: ""
                    Text(
                        "Most “${s.reason.label.lowercase()}” misses land on ${dayName}s.$habitPart",
                        style = AppTheme.textStyles.bodyMedium.copy(fontSize = 13.sp, lineHeight = 19.sp), color = c.textSecondary,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                    s.habit?.let { h ->
                        HfButton("Change ${h.name} reminder", onClick = { onEditHabit(h) }, variant = HfButtonVariant.Secondary,
                            size = HfButtonSize.Small, icon = PhIcons.Clock, modifier = Modifier.padding(top = 12.dp))
                    }
                }
            }
        }
    }
}

/** Insights entry for "Why you miss". */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MissSummaryPanel(habits: List<Habit>, logs: List<MissLog>, onOpen: () -> Unit) {
    val c = AppTheme.colors
    val p = remember(habits, logs) { MotivationInsights.missPatterns(habits, logs) }
    val pending = remember(habits, logs) { MotivationInsights.missedYesterday(habits, logs).size }
    HfPanel("Why you miss", if (p.total == 0) "Log a reason when you miss — patterns show here" else "${p.total} misses logged · last 90 days",
        modifier = Modifier.clickable(onClickLabel = "Open why you miss", onClick = onOpen)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val top = p.reasons.firstOrNull()
            PhIconNamed(top?.reason?.icon ?: "ph-chart-bar-horizontal", size = 20.dp, tint = if (top != null) c.warning else c.textTertiary)
            HSpace(10.dp)
            Text(
                when {
                    top != null -> "Top reason: ${top.reason.label} (${top.count})"
                    pending > 0 -> "$pending ${if (pending == 1) "habit" else "habits"} missed yesterday"
                    else -> "No misses logged yet"
                },
                style = AppTheme.textStyles.bodyMedium, color = c.textPrimary, modifier = Modifier.weight(1f),
            )
            PhIcon(PhIcons.CaretRight, size = 18.dp, tint = c.textTertiary)
        }
    }
}
