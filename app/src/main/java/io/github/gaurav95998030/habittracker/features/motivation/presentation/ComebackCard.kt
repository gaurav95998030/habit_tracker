package io.github.gaurav95998030.habittracker.features.motivation.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.gaurav95998030.habittracker.commons.ui.HfButton
import io.github.gaurav95998030.habittracker.commons.ui.HfButtonSize
import io.github.gaurav95998030.habittracker.commons.ui.HfCard
import io.github.gaurav95998030.habittracker.commons.ui.PhIcon
import io.github.gaurav95998030.habittracker.commons.ui.PhWeight
import io.github.gaurav95998030.habittracker.features.motivation.domain.Comeback
import io.github.gaurav95998030.habittracker.ui.theme.AppShapes
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme
import io.github.gaurav95998030.habittracker.ui.theme.PhIcons
import io.github.gaurav95998030.habittracker.ui.theme.glow

/**
 * Comeback mode: after a 5+ day streak breaks, lead with recent consistency instead
 * of a zero. [doneToday] swaps the CTA for a confirmation once the user checks in.
 */
@Composable
fun ComebackCard(
    comeback: Comeback,
    color: Color,
    doneToday: Boolean,
    onStartStreak: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = AppTheme.colors
    val shape = RoundedCornerShape(AppShapes.card)
    Column(modifier.fillMaxWidth()) {
        Column(
            Modifier
                .fillMaxWidth()
                .glow(color.copy(alpha = 0.11f), 30.dp, AppShapes.card, enabled = c.isDark)
                .clip(shape)
                .background(c.surface)
                .drawBehind {
                    drawCircle(
                        Brush.radialGradient(listOf(color.copy(alpha = 0.16f), Color.Transparent),
                            center = androidx.compose.ui.geometry.Offset(size.width - 60f, 0f), radius = 300f),
                        radius = 300f, center = androidx.compose.ui.geometry.Offset(size.width - 60f, 0f),
                    )
                }
                .border(1.dp, color.copy(alpha = 0.27f), shape)
                .padding(22.dp),
        ) {
            Row(
                Modifier
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.11f))
                    .border(1.dp, color.copy(alpha = 0.27f), CircleShape)
                    .padding(horizontal = 11.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                PhIcon(PhIcons.ArrowUUpLeft, size = 13.dp, tint = color, weight = PhWeight.Bold)
                Text("COMEBACK MODE", style = AppTheme.textStyles.labelSmall.copy(letterSpacing = 0.4.sp), color = color, maxLines = 1)
            }
            Row(Modifier.padding(top = 18.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "${comeback.hitsLast10}",
                    style = AppTheme.textStyles.displayLarge.copy(
                        fontSize = 80.sp, lineHeight = 74.sp, letterSpacing = (-3).sp,
                        shadow = if (c.isDark) Shadow(color.copy(alpha = 0.27f), blurRadius = 30f) else null,
                    ),
                    color = c.textPrimary,
                )
                Text("of last 10 days", style = AppTheme.textStyles.titleLarge.copy(fontSize = 22.sp), fontWeight = FontWeight.Medium,
                    color = c.textSecondary, modifier = Modifier.padding(bottom = 6.dp))
            }
            Text(
                "Your ${comeback.brokenRun}-day streak reset, but you’ve shown up ${comeback.hitsLast10} of the last 10 days. " +
                    if (comeback.hitsLast10 >= 6) "That’s still a strong run." else "Every check-in counts.",
                style = AppTheme.textStyles.bodyMedium.copy(lineHeight = 21.sp),
                color = c.textSecondary,
                modifier = Modifier.padding(top = 12.dp),
            )
            Row(Modifier.fillMaxWidth().padding(top = 20.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                comeback.last10.forEachIndexed { i, hit ->
                    val isToday = i == comeback.last10.lastIndex
                    Box(
                        Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .glow(color.copy(alpha = 0.53f), 8.dp, 7.dp, enabled = hit && c.isDark)
                            .clip(RoundedCornerShape(7.dp))
                            .background(if (hit) color else c.heatEmpty)
                            .then(
                                if (isToday && !hit) Modifier.drawBehind {
                                    val w = 1.5.dp.toPx()
                                    drawRoundRect(
                                        c.textTertiary,
                                        topLeft = androidx.compose.ui.geometry.Offset(w / 2, w / 2),
                                        size = androidx.compose.ui.geometry.Size(size.width - w, size.height - w),
                                        cornerRadius = CornerRadius(7.dp.toPx()),
                                        style = Stroke(w, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 2.dp.toPx()))),
                                    )
                                } else Modifier
                            ),
                    )
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("10 days ago", style = AppTheme.textStyles.caption, color = c.textTertiary)
                Text("Today", style = AppTheme.textStyles.caption, color = c.textTertiary)
            }
        }

        Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MiniStat(PhIcons.Trophy, "${comeback.bestRunThisMonth}", "Best run this month", Modifier.weight(1f))
            MiniStat(PhIcons.Percent, "${comeback.consistency30}%", "30-day consistency", Modifier.weight(1f))
        }

        if (doneToday) {
            Row(
                Modifier.fillMaxWidth().padding(top = 14.dp).clip(shape).background(c.success.copy(alpha = 0.1f))
                    .border(1.dp, c.success.copy(alpha = 0.33f), shape).padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PhIcon(PhIcons.CheckCircle, size = 22.dp, tint = c.success, weight = PhWeight.Fill)
                Text("New streak started — day 1 is in.", style = AppTheme.textStyles.bodyMedium, color = c.textPrimary)
            }
        } else {
            HfButton("Start a new streak today", onClick = onStartStreak, icon = PhIcons.Fire, iconWeight = PhWeight.Fill,
                size = HfButtonSize.Large, fullWidth = true, modifier = Modifier.padding(top = 18.dp))
        }
        Text(
            "Comeback mode shows after any streak of 5+ days breaks.",
            style = AppTheme.textStyles.bodySmall,
            color = c.textTertiary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
    }
}

@Composable
private fun MiniStat(icon: String, value: String, label: String, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    HfCard(modifier) {
        PhIcon(icon, size = 17.dp, tint = c.textSecondary, weight = PhWeight.Fill)
        Text(value, style = AppTheme.textStyles.titleLarge.copy(fontSize = 28.sp, letterSpacing = (-1).sp), color = c.textPrimary, modifier = Modifier.padding(top = 8.dp))
        Text(label, style = AppTheme.textStyles.bodySmall, color = c.textTertiary)
    }
}
