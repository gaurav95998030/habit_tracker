package io.github.gaurav95998030.habittracker.features.habits.presentation.habit_list.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
    isToday: Boolean = false,
) {
    val shape = RoundedCornerShape(2.dp)
    val fill = if (isCompleted) color ?: Color.Green else Color(0xFFE4E4E7)
    val borderColor = when {
        isToday && isCompleted -> Color.White
        isToday -> color ?: Color.Gray
        else -> Color.Transparent
    }
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(color = fill, shape = shape)
    )
}

