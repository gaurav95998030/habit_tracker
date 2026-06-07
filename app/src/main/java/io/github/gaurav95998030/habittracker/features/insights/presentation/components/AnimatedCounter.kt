package io.github.gaurav95998030.habittracker.features.insights.presentation.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

/**
 * Counts up from 0 to [target] when the composable enters composition or [start] flips true.
 */
@Composable
fun AnimatedCounter(
    target: Int,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    suffix: String = "",
    durationMillis: Int = 900,
    delayMillis: Int = 0,
    start: Boolean = true,
) {
    var play by remember { mutableStateOf(false) }
    LaunchedEffect(start, target) {
        if (start) play = true
    }
    val animated by animateIntAsState(
        targetValue = if (play) target else 0,
        animationSpec = tween(
            durationMillis = durationMillis,
            delayMillis = delayMillis,
            easing = LinearOutSlowInEasing,
        ),
        label = "animatedCounter",
    )
    Text(
        text = "$animated$suffix",
        style = style.copy(color = color),
        modifier = modifier,
    )
}
