package io.github.gaurav95998030.habittracker.features.insights.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme

@Composable
fun StatPairRow(
    avgWeeklyPercent: Int,
    totalCompletions: Int,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatTile(
            icon = Icons.AutoMirrored.Outlined.TrendingUp,
            value = avgWeeklyPercent,
            suffix = "%",
            label = "Avg / week",
            startAnimation = visible,
            modifier = Modifier.weight(1f),
        )
        StatTile(
            icon = Icons.Outlined.Done,
            value = totalCompletions,
            suffix = "",
            label = "Completions",
            startAnimation = visible,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatTile(
    icon: ImageVector,
    value: Int,
    suffix: String,
    label: String,
    startAnimation: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Column(
        modifier = modifier
            .insightCard(
                surface = colors.surface,
                border = colors.divider,
                cornerRadius = 22,
                horizontalPadding = 16,
                verticalPadding = 16,
            ),
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        AnimatedCounter(
            target = value,
            suffix = suffix,
            style = AppTheme.textStyles.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                lineHeight = 40.sp,
            ),
            color = colors.textPrimary,
            start = startAnimation,
            delayMillis = 120,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = AppTheme.textStyles.bodyMedium.copy(color = colors.textSecondary),
        )
    }
}
