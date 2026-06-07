package io.github.gaurav95998030.habittracker.features.habits.presentation.habit_list.molecules

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme

@Composable
fun ProgressRingCard(
    completed: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val safeTotal = total.coerceAtLeast(0)
    val safeCompleted = completed.coerceIn(0, safeTotal)
    val remaining = (safeTotal - safeCompleted).coerceAtLeast(0)
    val progress = if (safeTotal == 0) 0f else safeCompleted.toFloat() / safeTotal.toFloat()

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "ringProgress",
    )

    val (headline, sub) = headlineFor(safeCompleted, safeTotal, remaining)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colors.primary.copy(alpha = 0.10f),
                        colors.surface.copy(alpha = 0.85f),
                    )
                )
            )
            .border(
                width = 1.dp,
                color = colors.primary.copy(alpha = 0.35f),
                shape = RoundedCornerShape(24.dp),
            )
            .padding(horizontal = 18.dp, vertical = 18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(
                progress = animatedProgress,
                completed = safeCompleted,
                total = safeTotal,
                trackColor = colors.divider,
                progressColor = colors.primary,
                textColor = colors.textPrimary,
            )

            Spacer(modifier = Modifier.width(18.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = headline,
                    color = colors.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = sub,
                    style = AppTheme.textStyles.bodyMedium.copy(color = colors.textSecondary)
                )
            }
        }
    }
}

@Composable
private fun ProgressRing(
    progress: Float,
    completed: Int,
    total: Int,
    trackColor: Color,
    progressColor: Color,
    textColor: Color,
) {
    Box(
        modifier = Modifier.size(78.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(78.dp)) {
            val stroke = 8.dp.toPx()
            val inset = stroke / 2
            val arcSize = androidx.compose.ui.geometry.Size(
                size.width - stroke,
                size.height - stroke,
            )
            val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            if (progress > 0f) {
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progress.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
        }
        Text(
            text = if (total == 0) "0" else "$completed/$total",
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
        )
    }
}

private fun headlineFor(completed: Int, total: Int, remaining: Int): Pair<String, String> {
    if (total == 0) {
        return "Get started" to "Add your first habit to begin tracking."
    }
    if (completed == 0) {
        return "Let's begin" to "$total habit${plural(total)} waiting for you today."
    }
    if (completed >= total) {
        return "All done" to "You hit every habit today — nice work."
    }
    val ratio = completed.toFloat() / total.toFloat()
    val headline = when {
        ratio >= 0.66f -> "Almost there"
        ratio >= 0.33f -> "Halfway there"
        else -> "Off to a start"
    }
    val sub = "$remaining habit${plural(remaining)} left to keep your streaks alive."
    return headline to sub
}

private fun plural(n: Int) = if (n == 1) "" else "s"
