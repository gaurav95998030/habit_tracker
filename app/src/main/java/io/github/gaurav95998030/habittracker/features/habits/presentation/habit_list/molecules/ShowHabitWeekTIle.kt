package io.github.gaurav95998030.habittracker.features.habits.presentation.habit_list.molecules

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.gaurav95998030.habittracker.features.habits.HabitUtil
import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme
import java.time.DayOfWeek
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ShowHabitWeekTile(
    habit: Habit,
    onToggleCompleted: (LocalDate) -> Unit = {},
    onClick: () -> Unit = {},
) {
    val today = LocalDate.now()
    val habitColor = remember(habit.color) { HabitUtil.hexToColor(habit.color) }
    val completedSet = remember(habit.completedDates) {
        habit.completedDates
            .mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
            .toHashSet()
    }
    val isCompletedToday = today in completedSet

    val weekDates = remember(today) {
        val monday = today.with(DayOfWeek.MONDAY)
        (0..6).map { monday.plusDays(it.toLong()) }
    }

    val currentStreak = remember(completedSet, today) {
        var s = 0
        var d = today
        while (d in completedSet) {
            s++
            d = d.minusDays(1)
        }
        s
    }

    val colors = AppTheme.colors
    val onHabitColor = if (habitColor.luminance() > 0.4f) Color(0xFF1C1B1F) else Color.White

    val checkBgColor by animateColorAsState(
        targetValue = if (isCompletedToday) habitColor else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "checkBg",
    )
    val checkScale by animateFloatAsState(
        targetValue = if (isCompletedToday) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "checkScale",
    )

    val borderStroke = remember(habitColor) {
        BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    habitColor.copy(alpha = 0.5f),
                    habitColor.copy(alpha = 0.15f),
                )
            ),
        )
    }
    val fillBrush = remember(habitColor) {
        Brush.verticalGradient(
            colors = listOf(
                habitColor.copy(alpha = 0.08f),
                Color.Transparent,
            )
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = colors.surface,
        shape = RoundedCornerShape(16.dp),
        border = borderStroke,
    ) {
        Column(
            modifier = Modifier
                .background(fillBrush)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBubble(
                    initial = habit.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    tint = habitColor,
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.name,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                    if (habit.category.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(habitColor),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = habit.category,
                                style = AppTheme.textStyles.bodySmall.copy(
                                    color = colors.textSecondary,
                                ),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .scale(checkScale)
                        .size(40.dp)
                        .background(color = checkBgColor, shape = CircleShape)
                        .border(
                            width = 1.5.dp,
                            color = if (isCompletedToday) habitColor else habitColor.copy(alpha = 0.4f),
                            shape = CircleShape,
                        )
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onToggleCompleted(today) },
                        ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Mark complete",
                        tint = if (isCompletedToday) onHabitColor else habitColor,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    WeekChipRow(
                        weekDates = weekDates,
                        completedSet = completedSet,
                        today = today,
                        habitColor = habitColor,
                        dividerColor = colors.divider,
                    )
                    WeekDayLabels(
                        weekDates = weekDates,
                        today = today,
                        textColor = colors.textTertiary,
                        accentColor = colors.textSecondary,
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                StreakBadge(streak = currentStreak, tint = habitColor)
            }
        }
    }
}

@Composable
private fun IconBubble(initial: String, tint: Color) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(tint.copy(alpha = 0.18f))
            .border(
                width = 1.dp,
                color = tint.copy(alpha = 0.4f),
                shape = RoundedCornerShape(14.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            color = tint,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun WeekChipRow(
    weekDates: List<LocalDate>,
    completedSet: Set<LocalDate>,
    today: LocalDate,
    habitColor: Color,
    dividerColor: Color,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        weekDates.forEach { date ->
            val isCompleted = date in completedSet
            val isToday = date == today
            val isFuture = date.isAfter(today)
            val bg = when {
                isCompleted -> habitColor
                isFuture -> dividerColor.copy(alpha = 0.4f)
                else -> habitColor.copy(alpha = 0.22f)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(bg)
                    .let { mod ->
                        if (isToday) mod.border(
                            width = 1.dp,
                            color = habitColor,
                            shape = RoundedCornerShape(6.dp),
                        ) else mod
                    }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun WeekDayLabels(
    weekDates: List<LocalDate>,
    today: LocalDate,
    textColor: Color,
    accentColor: Color,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        weekDates.forEach { date ->
            val label = when (date.dayOfWeek) {
                DayOfWeek.MONDAY -> "M"
                DayOfWeek.TUESDAY -> "T"
                DayOfWeek.WEDNESDAY -> "W"
                DayOfWeek.THURSDAY -> "T"
                DayOfWeek.FRIDAY -> "F"
                DayOfWeek.SATURDAY -> "S"
                DayOfWeek.SUNDAY -> "S"
            }
            Text(
                text = label,
                color = if (date == today) accentColor else textColor,
                fontSize = 10.sp,
                fontWeight = if (date == today) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StreakBadge(streak: Int, tint: Color) {
    val colors = AppTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = "🔥", fontSize = 14.sp)
        Text(
            text = streak.toString(),
            color = if (streak > 0) tint else colors.textTertiary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
        )
    }
}
