package io.github.gaurav95998030.habittracker.features.habits.presentation.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.gaurav95998030.habittracker.commons.ui.PhIcon
import io.github.gaurav95998030.habittracker.commons.ui.PhIconNamed
import io.github.gaurav95998030.habittracker.commons.ui.PhWeight
import io.github.gaurav95998030.habittracker.features.habits.domain.HabitStats
import io.github.gaurav95998030.habittracker.ui.theme.Accents
import io.github.gaurav95998030.habittracker.ui.theme.AppMotion
import io.github.gaurav95998030.habittracker.ui.theme.AppShapes
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme
import io.github.gaurav95998030.habittracker.ui.theme.PhIcons
import io.github.gaurav95998030.habittracker.ui.theme.glow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

// Habit visualisations — Compose ports of the design's viz.jsx.

/** Sun..Sat, matching the design's week columns. */
private val WEEK: List<DayOfWeek> = listOf(
    DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY,
)

private fun DayOfWeek.initial(): String = getDisplayName(TextStyle.NARROW, Locale.getDefault())
private fun DayOfWeek.short(): String = getDisplayName(TextStyle.SHORT, Locale.getDefault())

/** Tinted rounded-square holding a habit's icon. */
@Composable
fun HabitIconBadge(
    icon: String,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
    radius: Dp = 14.dp,
    iconSize: Dp = 24.dp,
    glowing: Boolean = false,
) {
    val c = AppTheme.colors
    val shape = RoundedCornerShape(radius)
    Box(
        modifier
            .size(size)
            .glow(color.copy(alpha = 0.2f), 24.dp, radius, enabled = glowing && c.isDark)
            .clip(shape)
            .background(color.copy(alpha = if (c.isDark) 0.13f else 0.12f))
            .then(if (c.isDark) Modifier.border(1.dp, color.copy(alpha = 0.2f), shape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        PhIconNamed(icon, size = iconSize, tint = color, weight = PhWeight.Bold)
    }
}

/** Last 7 days (oldest → today) as rounded dots; today is dashed while open. */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeeklyGrid(
    completed: Set<LocalDate>,
    color: Color,
    modifier: Modifier = Modifier,
    dotSize: Dp = 22.dp,
    showLabels: Boolean = true,
    today: LocalDate = LocalDate.now(),
) {
    val c = AppTheme.colors
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        (6 downTo 0).forEach { offset ->
            val day = today.minusDays(offset.toLong())
            val done = day in completed
            val isToday = offset == 0
            val fill by animateColorAsState(if (done) color else c.heatEmpty, tween(400), label = "dot")
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(
                    Modifier
                        .size(dotSize)
                        .glow(color.copy(alpha = 0.6f), 10.dp, AppShapes.dot, enabled = done && c.isDark)
                        .clip(RoundedCornerShape(AppShapes.dot))
                        .background(fill)
                        .then(
                            if (isToday && !done) Modifier.drawBehind {
                                val w = 1.5.dp.toPx()
                                drawRoundRect(
                                    color = c.textTertiary,
                                    topLeft = androidx.compose.ui.geometry.Offset(w / 2, w / 2),
                                    size = androidx.compose.ui.geometry.Size(size.width - w, size.height - w),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(AppShapes.dot.toPx()),
                                    style = Stroke(w, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 2.dp.toPx()))),
                                )
                            } else Modifier
                        ),
                )
                if (showLabels) {
                    Text(
                        day.dayOfWeek.initial(),
                        style = AppTheme.textStyles.caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
                        color = if (isToday) c.textSecondary else c.textTertiary,
                    )
                }
            }
        }
    }
}

/**
 * The hero interaction: tap to complete. On completion it pops (overshoot), emits a
 * glow pulse ring and a light haptic. Un-completing just fades the fill.
 */
@Composable
fun CheckCircle(
    done: Boolean,
    color: Color,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
    habitName: String = "",
) {
    val c = AppTheme.colors
    val haptics = LocalHapticFeedback.current
    val pop = remember { Animatable(1f) }
    val pulse = remember { Animatable(0f) }
    var burstKey by remember { mutableIntStateOf(0) }
    val ink = Accents.inkFor(color)
    val fillAlpha by animateFloatAsState(if (done) 1f else 0f, tween(180), label = "checkFill")

    LaunchedEffect(burstKey) {
        if (burstKey == 0) return@LaunchedEffect
        launch {
            pop.snapTo(0.7f)
            pop.animateTo(1f, keyframes {
                durationMillis = 500
                1.18f at 275 using AppMotion.overshoot
                1f at 500
            })
        }
        pulse.snapTo(0f)
        pulse.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
    }

    Box(
        modifier
            .size(size)
            .semantics {
                contentDescription = if (habitName.isNotBlank()) "Complete $habitName today" else "Complete today"
                stateDescription = if (done) "Done" else "Not done"
            },
        contentAlignment = Alignment.Center,
    ) {
        // Glow pulse ring.
        if (pulse.value in 0.001f..0.999f) {
            Box(
                Modifier
                    .size(size + 8.dp)
                    .scale(0.85f + 1.05f * pulse.value)
                    .alpha(0.9f * (1f - pulse.value))
                    .border(2.dp, color, CircleShape),
            )
        }
        Box(
            Modifier
                .size(size)
                .scale(pop.value)
                .glow(color.copy(alpha = 0.66f * fillAlpha), 20.dp, size / 2, enabled = c.isDark && done)
                .clip(CircleShape)
                .background(color.copy(alpha = fillAlpha))
                .then(if (!done) Modifier.border(2.dp, c.textTertiary, CircleShape) else Modifier)
                .clickable(role = Role.Checkbox) {
                    if (!done) {
                        burstKey++
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    onToggle()
                },
            contentAlignment = Alignment.Center,
        ) {
            PhIcon(PhIcons.Check, size = size * 0.5f, tint = ink, weight = PhWeight.Bold, modifier = Modifier.alpha(fillAlpha))
        }
    }
}

/** GitHub-style yearly heatmap (default 27 weeks), scrolled to the current week. */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun YearHeatmap(
    completed: Set<LocalDate>,
    color: Color,
    modifier: Modifier = Modifier,
    weeks: Int = 27,
    cell: Dp = 13.dp,
    gap: Dp = 4.dp,
    today: LocalDate = LocalDate.now(),
) {
    val c = AppTheme.colors
    val levels = remember(completed) { HabitStats.heatLevels(completed) }
    val endSaturday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))
    val columns = remember(today, weeks) {
        (weeks - 1 downTo 0).map { w ->
            val sunday = endSaturday.minusDays(w * 7L + 6)
            (0..6).map { sunday.plusDays(it.toLong()) }
        }
    }
    val monthMarks = remember(columns) {
        var last = -1
        columns.mapIndexedNotNull { i, col ->
            val m = col.first().monthValue
            if (m != last) { last = m; i to col.first().month.getDisplayName(TextStyle.SHORT, Locale.getDefault()) } else null
        }
    }
    val scroll = rememberScrollState()
    // Open on the current week (the right edge), like a contributions graph.
    LaunchedEffect(scroll.maxValue) { scroll.scrollTo(scroll.maxValue) }
    val labelW = 22.dp
    val monthRowH = 18.dp
    val captionStyle = AppTheme.textStyles.caption.copy(fontSize = 10.sp)

    Column(modifier) {
        Row {
            // Weekday labels stay pinned while the weeks scroll.
            Column(Modifier.width(labelW).padding(top = monthRowH), verticalArrangement = Arrangement.spacedBy(gap)) {
                WEEK.forEachIndexed { i, d ->
                    Box(Modifier.height(cell), contentAlignment = Alignment.CenterStart) {
                        if (i % 2 == 1) Text(d.short(), style = captionStyle.copy(fontSize = 9.sp, lineHeight = 9.sp), color = c.textTertiary, maxLines = 1)
                    }
                }
            }
            Column(Modifier.horizontalScroll(scroll)) {
                Box(Modifier.height(monthRowH)) {
                    monthMarks.forEach { (col, label) ->
                        Text(
                            label,
                            style = captionStyle.copy(fontWeight = FontWeight.SemiBold),
                            color = c.textTertiary,
                            maxLines = 1,
                            modifier = Modifier.offset(x = (cell + gap) * col),
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                    columns.forEach { col ->
                        Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                            col.forEach { day ->
                                val level = levels[day] ?: 0
                                val done = level > 0
                                // Opacity lives in the colour (not an alpha layer) so the glow isn't clipped.
                                val fill = when {
                                    day.isAfter(today) -> Color.Transparent
                                    done -> color.copy(alpha = HabitStats.levelAlpha(level))
                                    else -> c.heatEmpty
                                }
                                Box(
                                    Modifier
                                        .size(cell)
                                        .glow(color.copy(alpha = if (level >= 3) 0.6f else 0.33f), (4 + level * 2).dp, AppShapes.heat, enabled = done && c.isDark)
                                        .clip(RoundedCornerShape(AppShapes.heat))
                                        .background(fill),
                                )
                            }
                        }
                    }
                }
            }
        }
        // Legend, also pinned.
        Row(
            Modifier.padding(start = labelW, top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Less", style = captionStyle, color = c.textTertiary)
            Box(Modifier.size(cell).clip(RoundedCornerShape(AppShapes.heat)).background(c.heatEmpty))
            (1..4).forEach { l ->
                Box(Modifier.size(cell).clip(RoundedCornerShape(AppShapes.heat)).background(color.copy(alpha = HabitStats.levelAlpha(l))))
            }
            Text("More", style = captionStyle, color = c.textTertiary)
        }
    }
}

/** Tappable month calendar. Future days are locked. */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthCalendar(
    month: YearMonth,
    completed: Set<LocalDate>,
    color: Color,
    onToggle: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
) {
    val c = AppTheme.colors
    val ink = Accents.inkFor(color)
    val lead = month.atDay(1).dayOfWeek.value % 7 // Sunday-first
    val cells: List<LocalDate?> = List(lead) { null } + (1..month.lengthOfMonth()).map { month.atDay(it) }
    val rows = cells.chunked(7)

    Column(modifier) {
        Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            WEEK.forEach {
                Text(
                    it.initial(),
                    style = AppTheme.textStyles.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                    color = c.textTertiary,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            rows.forEach { week ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    (0 until 7).forEach { i ->
                        val day = week.getOrNull(i)
                        if (day == null) {
                            Spacer(Modifier.weight(1f).aspectRatio(1f))
                        } else {
                            val done = day in completed
                            val future = day.isAfter(today)
                            val isToday = day == today
                            val fill by animateColorAsState(
                                if (done) color else if (c.isDark) Color.White.copy(alpha = 0.035f) else Color(0xFF0C1110).copy(alpha = 0.04f),
                                tween(400), label = "calCell",
                            )
                            val shape = RoundedCornerShape(10.dp)
                            Box(
                                Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .alpha(if (future) 0.25f else 1f)
                                    .glow(color.copy(alpha = 0.53f), 12.dp, 10.dp, enabled = done && c.isDark)
                                    .clip(shape)
                                    .background(fill)
                                    .then(if (isToday) Modifier.border(1.5.dp, color, shape) else Modifier)
                                    .clickable(enabled = !future, role = Role.Checkbox) { onToggle(day) }
                                    .semantics { stateDescription = if (done) "Done" else "Not done" },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    day.dayOfMonth.toString(),
                                    style = AppTheme.textStyles.bodyMedium.copy(fontSize = 13.sp),
                                    fontWeight = if (done) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (done) ink else c.textSecondary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Big trophy-feel streak number (onboarding welcome hero). */
@Composable
fun StreakBadge(
    value: Int,
    modifier: Modifier = Modifier,
    color: Color = AppTheme.colors.primary,
    label: String = "day streak",
) {
    val c = AppTheme.colors
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        PhIcon(PhIcons.Fire, size = 34.dp, tint = color, weight = PhWeight.Fill)
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                value.toString(),
                style = AppTheme.textStyles.displayLarge.copy(
                    fontSize = 64.sp, lineHeight = 64.sp, letterSpacing = (-2).sp,
                    shadow = if (c.isDark) androidx.compose.ui.graphics.Shadow(color.copy(alpha = 0.27f), blurRadius = 30f) else null,
                ),
                color = c.textPrimary,
            )
            Text(label, style = AppTheme.textStyles.bodyMedium.copy(fontWeight = FontWeight.Medium), color = c.textSecondary,
                maxLines = 1, softWrap = false, modifier = Modifier.padding(bottom = 10.dp))
        }
    }
}
