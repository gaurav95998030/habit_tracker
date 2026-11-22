package io.github.gaurav95998030.habittracker.features.insights.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.gaurav95998030.habittracker.commons.ui.BarDatum
import io.github.gaurav95998030.habittracker.commons.ui.HSpace
import io.github.gaurav95998030.habittracker.commons.ui.HfCard
import io.github.gaurav95998030.habittracker.commons.ui.PhIcon
import io.github.gaurav95998030.habittracker.commons.ui.PhIconNamed
import io.github.gaurav95998030.habittracker.commons.ui.PhWeight
import io.github.gaurav95998030.habittracker.commons.ui.WeekBars
import io.github.gaurav95998030.habittracker.features.habits.presentation.viewmodel.HabitViewModel
import io.github.gaurav95998030.habittracker.features.insights.domain.InsightsCalculator
import io.github.gaurav95998030.habittracker.ui.theme.Accents
import io.github.gaurav95998030.habittracker.ui.theme.AppMotion
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme
import io.github.gaurav95998030.habittracker.ui.theme.PhIcons
import io.github.gaurav95998030.habittracker.ui.theme.glow
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

/** Sunday-first, matching [InsightsUiState.dayOfWeekCounts]. */
private val WEEK = listOf(
    DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY,
)

/** Insights: weekly completion rate, best weekday, streak leaderboard. */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun InsightsScreen(
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier,
) {
    val c = AppTheme.colors
    val habitState by viewModel.uiState.collectAsStateWithLifecycle()
    val habits = habitState.habits.orEmpty()
    val state = remember(habits) { InsightsCalculator.build(habits) }

    Column(
        modifier
            .fillMaxSize()
            .background(c.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 120.dp),
    ) {
        Text(
            "Insights",
            style = AppTheme.textStyles.titleLarge.copy(fontSize = 26.sp, letterSpacing = (-0.6).sp),
            color = c.textPrimary,
            modifier = Modifier.padding(bottom = 18.dp),
        )
        if (state.isEmpty) {
            EmptyInsights()
            return@Column
        }

        Row(Modifier.fillMaxWidth().padding(bottom = 14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(PhIcons.TrendUp, "Avg / week", "${state.avgWeeklyRatePercent}%", c.primary, Modifier.weight(1f))
            StatCard(PhIcons.CheckFat, "Completions", state.totalCompletions.toString(), c.secondary, Modifier.weight(1f))
        }

        Panel("Completion rate", "Last 8 weeks") {
            WeekBars(state.weekBuckets.map { BarDatum(it.label, it.percent.toFloat()) })
        }

        Panel("Best day of the week", "When you show up most") {
            val bestDay = WEEK[state.bestDayIndex]
            Text(
                bestDay.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                style = AppTheme.textStyles.displayLarge.copy(
                    fontSize = 40.sp, lineHeight = 44.sp, letterSpacing = (-1).sp,
                    shadow = if (c.isDark) Shadow(c.primary.copy(alpha = 0.33f), blurRadius = 24f) else null,
                ),
                color = c.primary,
            )
            val max = (state.dayOfWeekCounts.maxOrNull() ?: 0).coerceAtLeast(1)
            Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                WEEK.forEachIndexed { i, d ->
                    val best = i == state.bestDayIndex
                    val ratio by animateFloatAsState(state.dayOfWeekCounts[i].toFloat() / max, tween(600, easing = AppMotion.emphasized), label = "dow")
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(c.heatEmpty),
                            contentAlignment = Alignment.BottomCenter,
                        ) {
                            if (ratio > 0f) {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(ratio)
                                        .alpha(if (best) 1f else 0.4f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (best) c.primary else c.textTertiary),
                                )
                            }
                        }
                        Text(
                            d.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                            style = AppTheme.textStyles.caption.copy(fontSize = 10.sp),
                            fontWeight = if (best) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (best) c.textPrimary else c.textTertiary,
                        )
                    }
                }
            }
        }

        Panel("Longest streaks", "Your personal records") {
            val top = (state.longestStreaks.firstOrNull()?.bestStreak ?: 0).coerceAtLeast(1)
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                state.longestStreaks.forEachIndexed { i, s ->
                    StreakRow(rank = i + 1, entry = s, fraction = (s.bestStreak.toFloat() / top).coerceIn(0f, 1f))
                }
            }
        }
    }
}

@Composable
private fun StreakRow(rank: Int, entry: StreakEntry, fraction: Float) {
    val c = AppTheme.colors
    val color = Accents.colorOf(entry.colorHex)
    val animated by animateFloatAsState(fraction, tween(700, easing = AppMotion.emphasized), label = "streakBar")
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(rank.toString(), style = AppTheme.textStyles.titleSmall.copy(fontSize = 14.sp), fontWeight = FontWeight.SemiBold,
            color = c.textTertiary, modifier = Modifier.width(16.dp))
        HSpace(13.dp)
        Box(
            Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(color.copy(alpha = 0.13f)),
            contentAlignment = Alignment.Center,
        ) { PhIconNamed(entry.icon, size = 18.dp, tint = color) }
        HSpace(13.dp)
        Column(Modifier.weight(1f)) {
            Text(entry.name, style = AppTheme.textStyles.bodyMedium.copy(fontWeight = FontWeight.Medium), color = c.textPrimary,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Box(Modifier.padding(top = 6.dp).fillMaxWidth().height(5.dp).clip(CircleShape).background(c.heatEmpty)) {
                Box(
                    Modifier
                        .fillMaxWidth(animated)
                        .height(5.dp)
                        .glow(color.copy(alpha = 0.66f), 8.dp, 999.dp, enabled = c.isDark && animated > 0f)
                        .clip(CircleShape)
                        .background(color),
                )
            }
        }
        HSpace(13.dp)
        Column(horizontalAlignment = Alignment.End) {
            Text(entry.bestStreak.toString(), style = AppTheme.textStyles.titleMedium, fontWeight = FontWeight.SemiBold, color = c.textPrimary)
            Text("best", style = AppTheme.textStyles.caption.copy(fontSize = 10.sp), color = c.textTertiary)
        }
    }
}

@Composable
private fun StatCard(icon: String, label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    HfCard(modifier) {
        PhIcon(icon, size = 20.dp, tint = accent, weight = PhWeight.Bold)
        Text(
            value,
            style = AppTheme.textStyles.titleLarge.copy(fontSize = 30.sp, lineHeight = 34.sp, letterSpacing = (-1).sp),
            color = c.textPrimary,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(label, style = AppTheme.textStyles.bodySmall, color = c.textSecondary)
    }
}

@Composable
private fun Panel(title: String, subtitle: String?, content: @Composable ColumnScope.() -> Unit) {
    val c = AppTheme.colors
    HfCard(Modifier.fillMaxWidth().padding(top = 14.dp), padding = androidx.compose.foundation.layout.PaddingValues(18.dp)) {
        Column(Modifier.padding(bottom = 16.dp)) {
            Text(title, style = AppTheme.textStyles.titleMedium.copy(fontSize = 16.sp, letterSpacing = (-0.2).sp), color = c.textPrimary)
            if (subtitle != null) Text(subtitle, style = AppTheme.textStyles.bodySmall, color = c.textTertiary, modifier = Modifier.padding(top = 2.dp))
        }
        content()
    }
}

@Composable
private fun EmptyInsights() {
    val c = AppTheme.colors
    Column(Modifier.fillMaxWidth().padding(vertical = 60.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        PhIcon(PhIcons.ChartLineUp, size = 48.dp, tint = c.primary, weight = PhWeight.Bold)
        Text("Nothing to chart yet", style = AppTheme.textStyles.titleLarge.copy(fontSize = 20.sp), color = c.textPrimary,
            modifier = Modifier.padding(top = 18.dp))
        Text(
            "Add a habit and check it off — your trends, best day and streak records will build up here.",
            style = AppTheme.textStyles.bodyMedium.copy(lineHeight = 21.sp),
            color = c.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp).widthIn(max = 260.dp),
        )
    }
}
