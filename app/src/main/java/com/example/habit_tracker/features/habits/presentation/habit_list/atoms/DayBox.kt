package com.example.habit_tracker.features.habits.presentation.habit_list.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@Composable
fun DayBox(
    date: LocalDate,
    isCompleted: Boolean,
    size: Int = 20,
    color: Color? = Color.Green,
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(
                color = if (isCompleted) color!! else Color.Gray,
                shape = RoundedCornerShape(2.dp)
            )
    )
}

