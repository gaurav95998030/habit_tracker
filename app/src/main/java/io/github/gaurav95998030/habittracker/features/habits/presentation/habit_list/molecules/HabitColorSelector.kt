package io.github.gaurav95998030.habittracker.features.habits.presentation.habit_list.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val ContainerColor = Color(0xFF161616)
private val OutlineColor = Color(0xFF2A2A2A)
private val SelectedRing = Color(0xFFFFFFFF)
private const val SWATCHES_PER_ROW = 8

@Composable
fun HabitColorPicker(
    colors: List<Color>,
    selectedColor: Color?,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = colors.chunked(SWATCHES_PER_ROW)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ContainerColor)
            .border(1.dp, OutlineColor, RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        rows.forEach { rowColors ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                rowColors.forEach { color ->
                    Swatch(
                        color = color,
                        isSelected = color == selectedColor,
                        onClick = { onColorSelected(color) }
                    )
                }
                // Pad incomplete row with invisible spacers so swatches align with row above
                if (rowColors.size < SWATCHES_PER_ROW) {
                    repeat(SWATCHES_PER_ROW - rowColors.size) {
                        Box(modifier = Modifier.size(36.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun Swatch(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.size(36.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .border(2.dp, SelectedRing, CircleShape)
            )
        }
        Box(
            modifier = Modifier
                .size(if (isSelected) 26.dp else 30.dp)
                .clip(CircleShape)
                .background(color)
                .clickable(onClick = onClick)
        )
    }
}
