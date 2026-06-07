package io.github.gaurav95998030.habittracker.features.insights.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

internal fun Modifier.insightCard(
    surface: Color,
    border: Color,
    cornerRadius: Int = 24,
    horizontalPadding: Int = 18,
    verticalPadding: Int = 18,
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius.dp))
    .background(surface)
    .border(
        width = 1.dp,
        color = border,
        shape = RoundedCornerShape(cornerRadius.dp),
    )
    .padding(horizontal = horizontalPadding.dp, vertical = verticalPadding.dp)
