package io.github.gaurav95998030.habittracker.features.insights.presentation.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.gaurav95998030.habittracker.features.insights.presentation.WeekBucket
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme

@Composable
fun CompletionRateCard(
    buckets: List<WeekBucket>,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val maxCount = (buckets.maxOfOrNull { it.completions } ?: 0).coerceAtLeast(1)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .insightCard(
                surface = colors.surface,
                border = colors.divider,
            ),
    ) {
        Text(
            text = "Completion rate",
            style = AppTheme.textStyles.titleMedium.copy(
                color = colors.textPrimary,
                fontWeight = FontWeight.SemiBold,
            ),
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Last ${buckets.size} weeks",
            style = AppTheme.textStyles.bodyMedium.copy(color = colors.textSecondary),
        )
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            buckets.forEachIndexed { index, bucket ->
                val fraction = bucket.completions.toFloat() / maxCount.toFloat()
                Bar(
                    fraction = fraction,
                    indexDelay = index * 70,
                    visible = visible,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            buckets.forEach { bucket ->
                Text(
                    text = bucket.label,
                    style = AppTheme.textStyles.labelSmall.copy(color = colors.textTertiary),
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun Bar(
    fraction: Float,
    indexDelay: Int,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    var play by remember { mutableStateOf(false) }
    LaunchedEffect(visible) { if (visible) play = true }

    val animatedFraction by animateFloatAsState(
        targetValue = if (play) fraction.coerceIn(0.08f, 1f) else 0f,
        animationSpec = tween(
            durationMillis = 700,
            delayMillis = indexDelay,
            easing = LinearOutSlowInEasing,
        ),
        label = "barGrow",
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(
            modifier = Modifier
                .width(28.dp)
                .fillMaxHeight(animatedFraction)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colors.primary,
                            colors.primaryVariant.copy(alpha = 0.85f),
                        )
                    )
                ),
        )
    }
}
