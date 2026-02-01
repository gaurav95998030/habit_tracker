package com.example.habit_tracker.features.habits.presentation.habit_list.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.habit_tracker.features.habits.HabitUtil.getDatesOfCurrentMonth
import com.example.habit_tracker.features.habits.presentation.habit_list.atoms.DayBox
import com.example.habit_tracker.ui.theme.AppTheme
import java.time.LocalDate

@Composable

fun ShowHabitGrid(
    completedDates: Set<LocalDate>
){
    val ROWS = 4
    val dates = remember { getDatesOfCurrentMonth() }
    val totalItems = dates.size
    val columns = (totalItems + ROWS - 1) / ROWS
    Box(
        modifier = Modifier
            .background(color = Color.DarkGray, shape = RoundedCornerShape(8.dp))
            .padding(6.dp)
    ){
        Column(

        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color.Red, RoundedCornerShape(16.dp))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column() {
                    Text("Habit Name", style = AppTheme.textStyles.labelSmall.copy(color = Color.White))
                    Text(
                        text = LocalDate.now().month.name.lowercase()
                            .replaceFirstChar { it.uppercase() },
                        style = AppTheme.textStyles.labelSmall.copy(color = Color.White)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color.Red, RoundedCornerShape(16.dp))
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(columns) { columnIndex ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(ROWS) { rowIndex ->
                            val itemIndex = columnIndex * ROWS + rowIndex
                            if (itemIndex < totalItems) {
                                val date = dates[itemIndex]
                                DayBox(
                                    size = 10,
                                    date = date,
                                    isCompleted = completedDates.contains(date)
                                )
                            } else {
                                Spacer(modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}