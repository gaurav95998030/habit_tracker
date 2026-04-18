package com.example.habit_tracker.features.habits.presentation.habit_list.molecules

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.habit_tracker.features.habits.domain.model.Habit
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.habit_tracker.features.habits.HabitUtil
import com.example.habit_tracker.features.habits.HabitUtil.getDatesOfCurrentYear
import com.example.habit_tracker.features.habits.presentation.habit_list.atoms.DayBox
import com.example.habit_tracker.ui.theme.AppTheme
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ShowHabitTile(
    habit: Habit,
    onToggleCompleted: (LocalDate) -> Unit = {},
    onClick: () -> Unit = {}
) {
    val ROWS = 4
    val LEADING_CONTEXT_COLS = 3
    val CELL_DP = 14
    val GAP_DP = 3
    val today = LocalDate.now()
    val dates = remember { getDatesOfCurrentYear() }
    val columns = (dates.size + ROWS - 1) / ROWS

    val todayIndex = remember(dates, today) {
        dates.indexOfFirst { it == today }.coerceAtLeast(0)
    }
    val todayColumn = todayIndex / ROWS

    val density = LocalDensity.current
    val initialScrollPx = remember(todayColumn, density) {
        with(density) {
            ((todayColumn - LEADING_CONTEXT_COLS).coerceAtLeast(0) * (CELL_DP + GAP_DP))
                .dp.toPx().toInt()
        }
    }
    val scrollState = rememberScrollState(initial = initialScrollPx)

    val habitColor = remember(habit.color) { HabitUtil.hexToColor(habit.color) }
    val completedLocalSet = remember(habit.completedDates) {
        habit.completedDates
            .mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
            .toHashSet()
    }
    val isCompletedToday = today in completedLocalSet

    val currentStreak = remember(completedLocalSet, today) {
        var s = 0
        var d = today
        while (d in completedLocalSet) {
            s++
            d = d.minusDays(1)
        }
        s
    }
    val weekDone = remember(completedLocalSet, today) {
        (0..6).count { today.minusDays(it.toLong()) in completedLocalSet }
    }
    val bestStreak = remember(completedLocalSet) {
        HabitUtil.maxStreak(completedLocalSet)
    }

    val checkBgColor by animateColorAsState(
        targetValue = if (isCompletedToday) habitColor else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "checkBg"
    )
    val checkScale by animateFloatAsState(
        targetValue = if (isCompletedToday) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "checkScale"
    )

    val onHabitColor = if (habitColor.luminance() > 0.4f) Color(0xFF1C1B1F) else Color.White
    val colors = AppTheme.colors

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = colors.surface,
        shadowElevation = 2.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(habitColor, habitColor.copy(alpha = 0.7f))
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )
                ) {
                    Text(
                        text = habit.name.take(1).uppercase(),
                        color = onHabitColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.name,
                        style = AppTheme.textStyles.labelLarge.copy(
                            color = colors.textPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    if (habit.category.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = habit.category,
                            style = AppTheme.textStyles.caption.copy(
                                color = colors.textTertiary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .scale(checkScale)
                        .size(36.dp)
                        .background(color = checkBgColor, shape = CircleShape)
                        .border(
                            width = 2.dp,
                            color = if (isCompletedToday) habitColor else colors.divider,
                            shape = CircleShape
                        )
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onToggleCompleted(LocalDate.now()) }
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Mark complete",
                        tint = if (isCompletedToday) onHabitColor else colors.divider,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 3.dp, height = 12.dp)
                        .background(habitColor, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(6.dp))
                if (currentStreak > 0) {
                    Text(
                        text = "🔥 $currentStreak day streak",
                        style = AppTheme.textStyles.caption.copy(
                            color = colors.textSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "·",
                        style = AppTheme.textStyles.caption.copy(color = colors.textTertiary)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Text(
                    text = "$weekDone / 7 this week",
                    style = AppTheme.textStyles.caption.copy(color = colors.textTertiary)
                )
                if (bestStreak > currentStreak) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "·  best $bestStreak",
                        style = AppTheme.textStyles.caption.copy(color = colors.textTertiary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(GAP_DP.dp)
            ) {
                repeat(columns) { columnIndex ->
                    Column(verticalArrangement = Arrangement.spacedBy(GAP_DP.dp)) {
                        repeat(ROWS) { rowIndex ->
                            val itemIndex = columnIndex * ROWS + rowIndex
                            if (itemIndex < dates.size) {
                                val date = dates[itemIndex]
                                DayBox(
                                    color = habitColor,
                                    size = CELL_DP,
                                    date = date,
                                    isCompleted = date in completedLocalSet,
                                    isToday = itemIndex == todayIndex
                                )
                            } else {
                                Spacer(modifier = Modifier.size(CELL_DP.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
