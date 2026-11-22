package io.github.gaurav95998030.habittracker.commons.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.gaurav95998030.habittracker.ui.theme.AppMotion
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme
import io.github.gaurav95998030.habittracker.ui.theme.glow

/**
 * Progress ring with a centred value. [fraction] is 0..1; animates from 0 on first
 * composition and on every change.
 */
@Composable
fun RingStat(
    fraction: Float,
    value: String,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    accent: Color = AppTheme.colors.primary,
    label: String? = null,
) {
    val c = AppTheme.colors
    var target by remember { mutableStateOf(0f) }
    LaunchedEffect(fraction) { target = fraction.coerceIn(0f, 1f) }
    val animated by animateFloatAsState(target, tween(800, easing = AppMotion.emphasized), label = "ring")
    Box(
        modifier
            .size(size)
            .semantics { contentDescription = "$value${label?.let { " $it" } ?: ""}" },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 9.dp.toPx()
            val inset = 6.dp.toPx()
            val arcSize = androidx.compose.ui.geometry.Size(this.size.width - inset * 2, this.size.height - inset * 2)
            val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)
            drawArc(c.heatEmpty, 0f, 360f, false, topLeft, arcSize, style = Stroke(stroke))
            if (animated > 0f) {
                if (c.isDark) {
                    // Soft halo under the progress arc (the design's drop-shadow glow).
                    drawArc(accent.copy(alpha = 0.25f), -90f, 360f * animated, false, topLeft, arcSize,
                        style = Stroke(inset * 2f, cap = StrokeCap.Round))
                }
                drawArc(accent, -90f, 360f * animated, false, topLeft, arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Round))
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                value,
                style = AppTheme.textStyles.titleLarge.copy(fontSize = (size.value * 0.26f).sp, letterSpacing = (-1).sp, lineHeight = (size.value * 0.3f).sp),
                color = c.textPrimary,
            )
            if (label != null) Text(label, style = AppTheme.textStyles.caption.copy(fontSize = 10.sp), color = c.textTertiary)
        }
    }
}

data class BarDatum(val label: String, val value: Float)

/** Vertical bars normalised to the max value; opacity scales with height. */
@Composable
fun WeekBars(
    data: List<BarDatum>,
    modifier: Modifier = Modifier,
    accent: Color = AppTheme.colors.primary,
    height: Dp = 120.dp,
) {
    val c = AppTheme.colors
    val max = (data.maxOfOrNull { it.value } ?: 0f).coerceAtLeast(1f)
    Row(
        modifier.fillMaxWidth().height(height),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        data.forEach { d ->
            val ratio = d.value / max
            val animated by animateFloatAsState(ratio, tween(600, easing = AppMotion.emphasized), label = "bar")
            Column(
                Modifier.weight(1f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.Bottom),
            ) {
                Box(
                    Modifier
                        .weight(1f, fill = false)
                        .fillMaxHeight(animated.coerceAtLeast(0.001f))
                        .heightIn(min = 4.dp)
                        .widthIn(max = 28.dp)
                        .fillMaxWidth()
                        .alpha(0.35f + 0.65f * ratio)
                        .glow(accent.copy(alpha = 0.4f), 10.dp, 6.dp, enabled = c.isDark)
                        .clip(RoundedCornerShape(6.dp))
                        .background(accent),
                )
                Text(d.label, style = AppTheme.textStyles.caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium), color = c.textTertiary)
            }
        }
    }
}
