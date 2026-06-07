package io.github.gaurav95998030.habittracker.features.insights.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.gaurav95998030.habittracker.features.habits.HabitUtil
import io.github.gaurav95998030.habittracker.features.insights.presentation.StreakEntry
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme

@Composable
fun LongestStreaksCard(
    entries: List<StreakEntry>,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val maxStreak = (entries.maxOfOrNull { it.bestStreak } ?: 0).coerceAtLeast(1)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .insightCard(
                surface = colors.surface,
                border = colors.divider,
            ),
    ) {
        Text(
            text = "Longest streaks",
            style = AppTheme.textStyles.titleMedium.copy(
                color = colors.textPrimary,
                fontWeight = FontWeight.SemiBold,
            ),
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Your personal records",
            style = AppTheme.textStyles.bodyMedium.copy(color = colors.textSecondary),
        )
        Spacer(modifier = Modifier.height(18.dp))

        if (entries.isEmpty()) {
            Text(
                text = "No streaks yet — start completing habits to see them here.",
                style = AppTheme.textStyles.bodyMedium.copy(color = colors.textTertiary),
            )
        } else {
            entries.forEachIndexed { index, entry ->
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(400, delayMillis = index * 70)) +
                        slideInHorizontally(
                            initialOffsetX = { it / 6 },
                            animationSpec = tween(450, delayMillis = index * 70, easing = LinearOutSlowInEasing),
                        ),
                ) {
                    StreakRow(
                        rank = index + 1,
                        entry = entry,
                        maxStreak = maxStreak,
                        startAnimation = visible,
                        animationDelayMillis = 250 + index * 70,
                    )
                }
                if (index != entries.lastIndex) {
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }
}

@Composable
private fun StreakRow(
    rank: Int,
    entry: StreakEntry,
    maxStreak: Int,
    startAnimation: Boolean,
    animationDelayMillis: Int,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val accent = runCatching { HabitUtil.hexToColor(entry.colorHex) }.getOrDefault(colors.primary)
    val target = entry.bestStreak.toFloat() / maxStreak.toFloat()

    var play by remember { mutableStateOf(false) }
    LaunchedEffect(startAnimation) { if (startAnimation) play = true }
    val animatedFraction by animateFloatAsState(
        targetValue = if (play) target.coerceIn(0f, 1f) else 0f,
        animationSpec = tween(
            durationMillis = 700,
            delayMillis = animationDelayMillis,
            easing = LinearOutSlowInEasing,
        ),
        label = "streakProgress",
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$rank",
            style = AppTheme.textStyles.bodyMedium.copy(
                color = colors.textTertiary,
                fontWeight = FontWeight.SemiBold,
            ),
            modifier = Modifier.width(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.LocalFireDepartment,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.name,
                style = AppTheme.textStyles.bodyLarge.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                ),
                maxLines = 1,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(colors.divider.copy(alpha = 0.7f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedFraction)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(accent),
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${entry.bestStreak}",
                style = AppTheme.textStyles.titleMedium.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                ),
            )
            Text(
                text = "best",
                style = AppTheme.textStyles.labelSmall.copy(color = colors.textTertiary),
            )
        }
    }
}
