package com.example.habit_tracker.features.habits.presentation.habit_list.organisms

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.habit_tracker.commons.ui.VerticalSpace
import com.example.habit_tracker.features.habits.domain.model.Habit
import com.example.habit_tracker.features.habits.presentation.habit_list.molecules.HabitCalendar
import com.example.habit_tracker.features.habits.presentation.habit_list.molecules.ShowHabitTile
import com.example.habit_tracker.features.habits.presentation.habit_list.molecules.YearlyHabitHeatCalender
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable

fun HabitDetailSheet(
    habit: Habit
){
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        ShowHabitTile(
            habit,
            onToggleCompleted = { date->
                //viewModel.updateHabit(habits[index].id, date)
            },
            onClick = {

            }
        )

        HabitCalendar(
            startDate = LocalDate.of(2025, 1, 1),
            endDate = LocalDate.of(2026, 1, 31),
            modifier = Modifier.padding(16.dp),
            dateBoxColor = Color(0xFF2A2A2A),
            dateBoxDotColor = Color(0xFFE57373),
            selectedDates = setOf(
                LocalDate.of(2025, 1, 2),
                LocalDate.of(2025, 1, 5),
                LocalDate.of(2025, 1, 10)
            ),
            datesWithDots = setOf(
                LocalDate.of(2025, 1, 3),
                LocalDate.of(2025, 1, 6),
                LocalDate.of(2025, 1, 12)
            ),
            onDateClick = { date ->
                println("Clicked date: $date")
            }
        )

        VerticalSpace(20.dp)
    }
}