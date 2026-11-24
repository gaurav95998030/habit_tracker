package io.github.gaurav95998030.habittracker.features.habits.presentation.habit_list.organisms

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.gaurav95998030.habittracker.commons.ui.HSpace
import io.github.gaurav95998030.habittracker.commons.ui.HfBottomSheet
import io.github.gaurav95998030.habittracker.commons.ui.HfButton
import io.github.gaurav95998030.habittracker.commons.ui.HfButtonVariant
import io.github.gaurav95998030.habittracker.commons.ui.HfCard
import io.github.gaurav95998030.habittracker.commons.ui.HfIconButton
import io.github.gaurav95998030.habittracker.commons.ui.HfSectionLabel
import io.github.gaurav95998030.habittracker.commons.ui.PhIcon
import io.github.gaurav95998030.habittracker.commons.ui.PhWeight
import io.github.gaurav95998030.habittracker.features.habits.HabitCatalog
import io.github.gaurav95998030.habittracker.features.habits.HabitUtil
import io.github.gaurav95998030.habittracker.features.habits.domain.HabitStats
import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit
import io.github.gaurav95998030.habittracker.features.motivation.domain.ReviewStats
import io.github.gaurav95998030.habittracker.features.motivation.presentation.ComebackCard
import io.github.gaurav95998030.habittracker.features.habits.presentation.components.HabitIconBadge
import io.github.gaurav95998030.habittracker.features.habits.presentation.components.MonthCalendar
import io.github.gaurav95998030.habittracker.features.habits.presentation.components.YearHeatmap
import io.github.gaurav95998030.habittracker.ui.theme.Accents
import io.github.gaurav95998030.habittracker.ui.theme.AppShapes
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme
import io.github.gaurav95998030.habittracker.ui.theme.PhIcons
import io.github.gaurav95998030.habittracker.ui.theme.glow
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * Habit detail: streak stats, yearly heatmap, tappable month calendar, and
 * Edit / Delete / Pause actions.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HabitDetailSheet(
    habit: Habit,
    onDismiss: () -> Unit,
    onToggleDay: (LocalDate) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetActive: (Boolean) -> Unit,
    onStartStreak: () -> Unit,
) {
    val c = AppTheme.colors
    val today = LocalDate.now()
    val color = Accents.colorOf(habit.color)
    val completed = remember(habit.completedDates) { HabitStats.completedSet(habit) }
    val current = remember(completed, today) { HabitUtil.currentStreak(completed, today) }
    val best = remember(completed) { maxOf(HabitUtil.maxStreak(completed), current) }
    var monthOffset by rememberSaveable(habit.id) { mutableIntStateOf(0) }
    val month = YearMonth.from(today).plusMonths(monthOffset.toLong())
    val comeback = remember(habit.completedDates, habit.isActive, today) { ReviewStats.comeback(habit, today) }

    HfBottomSheet(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth()) {
            // Header with an accent glow wash.
            Box(Modifier.fillMaxWidth()) {
                if (c.isDark) {
                    Box(
                        Modifier
                            .matchParentSize()
                            .drawBehind {
                                // Elliptical wash from the top centre, fully faded by the header's bottom edge.
                                scale(scaleX = size.width / size.height * 0.8f, scaleY = 1f, pivot = Offset(size.width / 2f, 0f)) {
                                    drawCircle(
                                        Brush.radialGradient(
                                            listOf(color.copy(alpha = 0.15f), Color.Transparent),
                                            center = Offset(size.width / 2f, 0f),
                                            radius = size.height,
                                        ),
                                        radius = size.height,
                                        center = Offset(size.width / 2f, 0f),
                                    )
                                }
                            },
                    )
                }
                Row(Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 6.dp, bottom = 18.dp)) {
                    HabitIconBadge(
                        icon = HabitCatalog.iconFor(habit), color = color,
                        size = 56.dp, radius = 16.dp, iconSize = 28.dp, glowing = true,
                    )
                    HSpace(14.dp)
                    Column(Modifier.weight(1f)) {
                        Text(
                            habit.name,
                            style = AppTheme.textStyles.titleLarge.copy(fontSize = 22.sp, letterSpacing = (-0.4).sp),
                            color = c.textPrimary,
                        )
                        Row(Modifier.padding(top = 3.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(Modifier.size(7.dp).clip(CircleShape).background(color))
                            val meta = buildString {
                                append(habit.category)
                                if (habit.reminderEnabled && habit.reminderTime != null) append(" · ${HabitUtil.formatTime12h(habit.reminderTime)}")
                                if (!habit.isActive) append(" · Paused")
                            }
                            Text(meta, style = AppTheme.textStyles.bodyMedium.copy(fontSize = 13.sp), color = c.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        if (habit.description.isNotBlank()) {
                            Text(
                                habit.description,
                                style = AppTheme.textStyles.bodyMedium.copy(fontSize = 13.sp, lineHeight = 19.sp),
                                color = c.textTertiary,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                    }
                    HfIconButton(PhIcons.X, onClick = onDismiss, contentDescription = "Close")
                }
            }

            Column(
                Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(start = 22.dp, end = 22.dp, bottom = 22.dp),
            ) {
                if (comeback != null) {
                    ComebackCard(
                        comeback = comeback,
                        color = color,
                        doneToday = today in completed,
                        onStartStreak = onStartStreak,
                        modifier = Modifier.padding(bottom = 22.dp),
                    )
                }
                Row(Modifier.fillMaxWidth().padding(bottom = 22.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatBlock(current, "Current", color, PhIcons.Fire, hero = true, modifier = Modifier.weight(1f))
                    StatBlock(best, "Best ever", c.textPrimary, PhIcons.Trophy, modifier = Modifier.weight(1f))
                    StatBlock(completed.size, "Total", c.textPrimary, PhIcons.CheckFat, modifier = Modifier.weight(1f))
                }

                HfSectionLabel("This year")
                HfCard(Modifier.fillMaxWidth().padding(bottom = 22.dp)) {
                    YearHeatmap(completed = completed, color = color, today = today)
                }

                Row(
                    Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    HfSectionLabel("${month.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())} ${month.year}", bottom = 0.dp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        HfIconButton(PhIcons.CaretLeft, onClick = { monthOffset-- }, size = 32.dp, iconSize = 15.dp,
                            shape = RoundedCornerShape(10.dp), contentDescription = "Previous month")
                        HfIconButton(PhIcons.CaretRight, onClick = { if (monthOffset < 0) monthOffset++ }, size = 32.dp, iconSize = 15.dp,
                            shape = RoundedCornerShape(10.dp), enabled = monthOffset < 0, contentDescription = "Next month")
                    }
                }
                HfCard(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    MonthCalendar(month = month, completed = completed, color = color, onToggle = onToggleDay, today = today)
                }
                Text(
                    "Tap any day to toggle · future days locked",
                    style = AppTheme.textStyles.bodySmall,
                    color = c.textTertiary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 22.dp),
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HfButton("Edit", onClick = onEdit, variant = HfButtonVariant.Secondary, icon = PhIcons.PencilSimple,
                        modifier = Modifier.weight(1f), fullWidth = true)
                    HfButton("Delete", onClick = onDelete, variant = HfButtonVariant.Danger, icon = PhIcons.Trash,
                        modifier = Modifier.weight(1f), fullWidth = true)
                }
                HfButton(
                    text = if (habit.isActive) "Pause habit" else "Resume habit",
                    onClick = { onSetActive(!habit.isActive) },
                    variant = HfButtonVariant.Ghost,
                    icon = if (habit.isActive) PhIcons.PauseCircle else PhIcons.PlayCircle,
                    fullWidth = true,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}


@Composable
private fun StatBlock(
    value: Int,
    label: String,
    accent: Color,
    icon: String,
    modifier: Modifier = Modifier,
    hero: Boolean = false,
    radius: Dp = AppShapes.card,
) {
    val c = AppTheme.colors
    val shape = RoundedCornerShape(radius)
    Column(
        modifier
            .glow(accent.copy(alpha = 0.13f), 26.dp, radius, enabled = hero && c.isDark)
            .clip(shape)
            .background(if (hero && c.isDark) Brush.linearGradient(listOf(accent.copy(alpha = 0.12f), c.surface)) else Brush.linearGradient(listOf(c.surface, c.surface)))
            .border(1.dp, if (hero) accent.copy(alpha = 0.27f) else c.glassBorder, shape)
            .padding(horizontal = 14.dp, vertical = 16.dp),
    ) {
        PhIcon(icon, size = 17.dp, tint = accent, weight = PhWeight.Fill)
        Text(
            value.toString(),
            style = AppTheme.textStyles.titleLarge.copy(fontSize = 32.sp, lineHeight = 32.sp, letterSpacing = (-1.5).sp),
            color = c.textPrimary,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(label, style = AppTheme.textStyles.caption, fontWeight = FontWeight.Normal, color = c.textTertiary, modifier = Modifier.padding(top = 4.dp))
    }
}
