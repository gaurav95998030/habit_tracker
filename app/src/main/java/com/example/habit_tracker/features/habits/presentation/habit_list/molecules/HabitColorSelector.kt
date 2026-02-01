package com.example.habit_tracker.features.habits.presentation.habit_list.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun HabitColorPicker(
    colors: List<Color>,
    selectedColor: Color?,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        colors.forEach { color ->

            val isSelected = color == selectedColor

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color = color, shape = RoundedCornerShape(16.dp))
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onBackground
                        else
                            MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onColorSelected(color) }
            )
        }
    }
}
