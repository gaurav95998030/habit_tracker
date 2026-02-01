package com.example.habit_tracker.features.habits.presentation.habit_list.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.habit_tracker.features.habits.presentation.habit_list.molecules.ShowHabitGrid
import java.time.LocalDate

@Composable

fun HabitMonthList(){
    val dummyCompletedDates = remember {
        val today = LocalDate.now()
        setOf(
            today.minusDays(1),
            today.minusDays(2),
            today.minusDays(4),
            today.minusDays(7)
        )
    }
    Box(
        modifier = Modifier.padding(horizontal = 16.dp)
    ){
        Column() {
            Spacer(
                modifier = Modifier.height(40.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                repeat(5){
                    ShowHabitGrid(
                        completedDates = dummyCompletedDates
                    )
                }
            }
        }

    }
}