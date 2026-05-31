package io.github.gaurav95998030.habittracker.features.habits.presentation.habit_list.molecules

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme

@Composable
private fun shimmerBrush(): Brush {
    val base = AppTheme.colors.divider
    val isDark = AppTheme.colors.background.luminance() < 0.5f
    val highlight = if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.65f)
    val colors = listOf(base, highlight, base)

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translate by transition.animateFloat(
        initialValue = -400f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    return Brush.linearGradient(
        colors = colors,
        start = Offset(translate, 0f),
        end = Offset(translate + 400f, 0f)
    )
}

@Composable
private fun ShimmerBlock(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(6.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(shimmerBrush())
    )
}

@Composable
fun ShimmerHabitTilePlaceholder(
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = colors.surface,
        shadowElevation = 2.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ShimmerBlock(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(14.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    ShimmerBlock(
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .height(14.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ShimmerBlock(
                        modifier = Modifier
                            .fillMaxWidth(0.35f)
                            .height(10.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                ShimmerBlock(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            ShimmerBlock(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(10.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(28) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        repeat(4) {
                            ShimmerBlock(
                                modifier = Modifier.size(14.dp),
                                shape = RoundedCornerShape(3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShimmerHabitList(
    modifier: Modifier = Modifier,
    tileCount: Int = 5
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            repeat(tileCount) {
                ShimmerHabitTilePlaceholder()
            }
        }
    }
}
