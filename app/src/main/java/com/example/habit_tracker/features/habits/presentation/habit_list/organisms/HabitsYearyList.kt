package com.example.habit_tracker.features.habits.presentation.habit_list.organisms

import com.example.habit_tracker.features.habits.domain.model.Habit
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.habit_tracker.features.habits.presentation.habit_list.molecules.HabitCalendar
import com.example.habit_tracker.features.habits.presentation.habit_list.molecules.HabitHeatmapLinearStrip
import com.example.habit_tracker.features.habits.presentation.habit_list.molecules.ShowHabitTile
import com.example.habit_tracker.features.habits.presentation.habit_list.molecules.YearlyHabitHeatCalender
import com.example.habit_tracker.features.habits.presentation.viewmodel.HabitViewModel
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable

fun HabitYearlyLists(
    viewModel: HabitViewModel,
    habits : List<Habit> = emptyList()
){
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(){
                Spacer(modifier = Modifier.height(20.dp))
            }
            items(habits.size
            ) {index->
                ShowHabitTile(
                    habits[index],
                    onToggleCompleted = { date->
                        viewModel.updateHabit(habits[index].id, date)
                    }
                )
            }

            item {
                HabitHeatmapLinearStrip(
                    startDate = LocalDate.parse("2025-07-01"),
                    endDate = LocalDate.parse("2026-02-01"),
                    highlightedDates = listOf(
                        LocalDate.parse("2025-08-01"),
                        LocalDate.parse("2025-08-02"),
                        LocalDate.parse("2025-08-03"),
                        LocalDate.parse("2025-10-10")
                    ),
                    highlightColor = Color(0xFFCC6B6B)
                )


            }
        }
    }
}