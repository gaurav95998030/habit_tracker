package io.github.gaurav95998030.habittracker.features.insights.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme
import java.time.DayOfWeek
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

@Composable
fun BestDayCard(
    bestDay: DayOfWeek,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dayName = bestDay.getDisplayName(JavaTextStyle.FULL, Locale.getDefault())

    var play by remember { mutableStateOf(false) }
    LaunchedEffect(visible) { if (visible) play = true }
    val alpha by animateFloatAsState(
        targetValue = if (play) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing),
        label = "bestDayAlpha",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .insightCard(
                surface = colors.surface,
                border = colors.divider,
            ),
    ) {
        Text(
            text = "Best day of the week",
            style = AppTheme.textStyles.titleMedium.copy(
                color = colors.textPrimary,
                fontWeight = FontWeight.SemiBold,
            ),
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "When you show up most",
            style = AppTheme.textStyles.bodyMedium.copy(color = colors.textSecondary),
        )
        Spacer(modifier = Modifier.height(18.dp))

        Box(modifier = Modifier.fillMaxWidth().alpha(alpha)) {
            Text(
                text = dayName,
                style = AppTheme.textStyles.titleLarge.copy(
                    color = colors.primary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 52.sp,
                    lineHeight = 60.sp,
                ),
            )
        }
        Spacer(modifier = Modifier.height(18.dp))

        DayOfWeekStrip(highlighted = bestDay, visible = visible)
    }
}

@Composable
private fun DayOfWeekStrip(
    highlighted: DayOfWeek,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val ordered = listOf(
        DayOfWeek.SUNDAY,
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
    )
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ordered.forEachIndexed { index, day ->
            val initial = day.getDisplayName(JavaTextStyle.NARROW, Locale.getDefault())
            val isHighlighted = day == highlighted
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(400, delayMillis = 100 + index * 50)) +
                        scaleIn(initialScale = 0.7f, animationSpec = tween(400, delayMillis = 100 + index * 50)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isHighlighted) colors.primary else colors.divider.copy(alpha = 0.5f)
                            ),
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = initial,
                    style = AppTheme.textStyles.labelSmall.copy(
                        color = if (isHighlighted) colors.textPrimary else colors.textTertiary,
                        fontWeight = if (isHighlighted) FontWeight.SemiBold else FontWeight.Normal,
                    ),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
