package com.example.habit_tracker.features.habits.presentation.habit_list.molecules

import com.example.habit_tracker.features.habits.domain.model.Habit
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.habit_tracker.R
import com.example.habit_tracker.features.habits.HabitUtil
import com.example.habit_tracker.features.habits.HabitUtil.getDatesOfCurrentYear
import com.example.habit_tracker.features.habits.presentation.habit_list.atoms.DayBox
import com.example.habit_tracker.ui.theme.AppColors
import com.example.habit_tracker.ui.theme.AppTheme
import java.time.LocalDate

@Composable

fun ShowHabitTile(
    habit: Habit,
    onToggleCompleted: (LocalDate) -> Unit = {}
) {
    val ROWS = 4
    val dates = remember { getDatesOfCurrentYear() }
    val TOTAL_ITEMS = dates.size
    val columns = (TOTAL_ITEMS + ROWS - 1) / ROWS

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Red, RoundedCornerShape(16.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                habit.name,
                style = AppTheme.textStyles.labelSmall.copy(color = Color.White)
            )
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(R.drawable.assignment_turned_in_24px),
                contentDescription = null,
                modifier = Modifier.clickable(
                    onClick = {
                        onToggleCompleted(LocalDate.now())
                    }
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(columns) { columnIndex ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(ROWS) { rowIndex ->
                        val itemIndex = columnIndex * ROWS + rowIndex

                        if (itemIndex < TOTAL_ITEMS) {
                            val date = dates[itemIndex]
                            DayBox(
                                color = HabitUtil.hexToColor(habit.color),
                                size = 10,
                                date = date,
                                isCompleted = habit.completedDates.contains(date.toString())
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
