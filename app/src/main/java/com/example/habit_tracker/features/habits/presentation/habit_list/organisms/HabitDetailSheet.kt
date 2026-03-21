package com.example.habit_tracker.features.habits.presentation.habit_list.organisms

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.habit_tracker.features.habits.HabitUtil
import com.example.habit_tracker.features.habits.domain.model.Habit
import com.example.habit_tracker.features.habits.presentation.habit_list.molecules.HabitCalendar
import com.example.habit_tracker.features.habits.presentation.habit_list.molecules.ShowHabitTile
import com.example.habit_tracker.features.habits.presentation.viewmodel.HabitViewModel
import com.example.habit_tracker.ui.theme.AppTheme
import java.time.LocalDate
import java.time.YearMonth

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HabitDetailSheet(
    habit: Habit,
    viewModel: HabitViewModel,
    onDismiss: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val habitColor = remember(habit.color) { HabitUtil.hexToColor(habit.color) }
    val colors = AppTheme.colors

    val completedDatesSet = remember(habit.completedDates) {
        habit.completedDates.mapNotNull { dateStr ->
            runCatching { LocalDate.parse(dateStr) }.getOrNull()
        }.toSet()
    }

    val startDate = LocalDate.of(LocalDate.now().year, 1, 1)
    val endDate = LocalDate.of(LocalDate.now().year, 12, 31)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Habit Details",
                style = AppTheme.textStyles.titleMedium.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(onClick = onDismiss),
                    tint = colors.textSecondary
                )
            }
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            ShowHabitTile(
                habit = habit,
                onToggleCompleted = { date -> viewModel.updateHabit(habit.id, date) },
                onClick = {}
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "Completions",
                    value = "${habit.completedDates.size}",
                    accentColor = habitColor,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Best Streak",
                    value = "${habit.maxStreak}",
                    accentColor = habitColor,
                    modifier = Modifier.weight(1f)
                )
            }

            if (habit.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(20.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = colors.surface,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Description",
                            style = AppTheme.textStyles.labelMedium.copy(
                                color = colors.textSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = habit.description,
                            style = AppTheme.textStyles.bodyMedium.copy(
                                color = colors.textPrimary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Activity",
                style = AppTheme.textStyles.labelLarge.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            HabitCalendar(
                startDate = startDate,
                endDate = endDate,
                modifier = Modifier.fillMaxWidth(),
                dateBoxColor = colors.surface,
                dateBoxDotColor = habitColor,
                selectedDates = completedDatesSet,
                datesWithDots = emptySet(),
                onDateClick = { date -> viewModel.updateHabit(habit.id, date) },
                initialMonth = YearMonth.now()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    accentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    Surface(
        modifier = modifier,
        color = colors.surface,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 24.dp, height = 3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = AppTheme.textStyles.titleMedium.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = AppTheme.textStyles.caption.copy(
                    color = colors.textTertiary
                )
            )
        }
    }
}
