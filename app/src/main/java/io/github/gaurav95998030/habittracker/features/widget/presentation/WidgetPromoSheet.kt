package io.github.gaurav95998030.habittracker.features.widget.presentation

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.gaurav95998030.habittracker.commons.ui.HSpace
import io.github.gaurav95998030.habittracker.commons.ui.HfBottomSheet
import io.github.gaurav95998030.habittracker.commons.ui.HfButton
import io.github.gaurav95998030.habittracker.commons.ui.HfButtonSize
import io.github.gaurav95998030.habittracker.commons.ui.HfButtonVariant
import io.github.gaurav95998030.habittracker.commons.ui.LogoMark
import io.github.gaurav95998030.habittracker.commons.ui.PhIcon
import io.github.gaurav95998030.habittracker.commons.ui.PhIconNamed
import io.github.gaurav95998030.habittracker.commons.ui.PhWeight
import io.github.gaurav95998030.habittracker.features.habits.HabitCatalog
import io.github.gaurav95998030.habittracker.features.habits.HabitUtil
import io.github.gaurav95998030.habittracker.features.habits.domain.HabitStats
import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit
import io.github.gaurav95998030.habittracker.features.widget.HabitWidgetProvider
import io.github.gaurav95998030.habittracker.ui.theme.Accents
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme
import io.github.gaurav95998030.habittracker.ui.theme.PhIcons
import io.github.gaurav95998030.habittracker.ui.theme.glow
import java.time.LocalDate

/**
 * One-time nudge (and Settings entry) inviting the user to add the home-screen
 * widget. "Add to home screen" asks the launcher to pin the existing widget.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WidgetPromoSheet(habits: List<Habit>, onDismiss: () -> Unit) {
    val c = AppTheme.colors
    val context = LocalContext.current
    val benefits = listOf(
        Triple(PhIcons.HandTap, "One-tap check-ins", "Mark a habit done straight from your home screen."),
        Triple(PhIcons.Eye, "Streaks at a glance", "See today’s progress without unlocking into the app."),
        Triple(PhIcons.Lightning, "Less friction, more done", "The fewer taps to log, the longer your streaks last."),
    )

    HfBottomSheet(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding()) {
            // Widget on a faux wallpaper.
            Box(
                Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.radialGradient(
                            if (c.isDark) listOf(Color(0xFF16261D), Color(0xFF0A1410)) else listOf(Color(0xFFE9F3E6), Color(0xFFD8E6D6)),
                            center = androidx.compose.ui.geometry.Offset(120f, 0f),
                            radius = 1100f,
                        )
                    )
                    .padding(horizontal = 18.dp, vertical = 26.dp),
            ) {
                WidgetPreview(habits = habits, maxRows = 3)
            }

            Column(
                Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 24.dp, end = 24.dp, top = 22.dp),
            ) {
                Row(
                    Modifier
                        .clip(CircleShape)
                        .background(c.primary.copy(alpha = 0.08f))
                        .border(1.dp, c.primary.copy(alpha = 0.27f), CircleShape)
                        .padding(horizontal = 11.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    PhIcon(PhIcons.SquaresFour, size = 13.dp, tint = c.primary, weight = PhWeight.Fill)
                    Text("HOME SCREEN WIDGET", style = AppTheme.textStyles.labelSmall.copy(letterSpacing = 0.4.sp), color = c.primary)
                }
                Text(
                    "Track without\nopening the app",
                    style = AppTheme.textStyles.titleLarge.copy(fontSize = 25.sp, lineHeight = 29.sp, letterSpacing = (-0.5).sp),
                    color = c.textPrimary,
                    modifier = Modifier.padding(top = 14.dp),
                )
                Text(
                    "Add the HabitForge widget to your home screen and check habits off in a single tap.",
                    style = AppTheme.textStyles.bodyMedium.copy(lineHeight = 21.sp),
                    color = c.textSecondary,
                    modifier = Modifier.padding(top = 10.dp),
                )
                Column(Modifier.padding(top = 22.dp, bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    benefits.forEach { (icon, title, desc) ->
                        Row(horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                            Box(
                                Modifier.size(38.dp).clip(RoundedCornerShape(11.dp)).background(c.primary.copy(alpha = 0.11f)),
                                contentAlignment = Alignment.Center,
                            ) { PhIcon(icon, size = 19.dp, tint = c.primary, weight = PhWeight.Bold) }
                            Column {
                                Text(title, style = AppTheme.textStyles.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = c.textPrimary)
                                Text(desc, style = AppTheme.textStyles.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp), color = c.textSecondary,
                                    modifier = Modifier.padding(top = 1.dp))
                            }
                        }
                    }
                }
            }

            Column(Modifier.padding(start = 24.dp, end = 24.dp, top = 14.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                HfButton(
                    "Add to home screen",
                    onClick = { requestPinWidget(context); onDismiss() },
                    icon = PhIcons.PlusSquare, size = HfButtonSize.Large, fullWidth = true,
                )
                HfButton("Maybe later", onClick = onDismiss, variant = HfButtonVariant.Ghost, fullWidth = true)
            }
        }
    }
}

/** Ask the launcher to pin the habits widget; falls back to manual instructions. */
private fun requestPinWidget(context: Context) {
    val manager = context.getSystemService(AppWidgetManager::class.java)
    val provider = ComponentName(context, HabitWidgetProvider::class.java)
    val pinned = manager != null && manager.isRequestPinAppWidgetSupported &&
        runCatching { manager.requestPinAppWidget(provider, null, null) }.getOrDefault(false)
    if (!pinned) {
        Toast.makeText(
            context,
            "Long-press your home screen → Widgets → HabitForge to add it.",
            Toast.LENGTH_LONG,
        ).show()
    }
}

/** Live Compose preview of the widget card (frosted glass, today's rows). */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WidgetPreview(habits: List<Habit>, modifier: Modifier = Modifier, maxRows: Int = 3) {
    val c = AppTheme.colors
    val today = LocalDate.now()
    val active = habits.filter { it.isActive }
    val rows = active.take(maxRows)
    val doneCount = active.count { HabitStats.isDoneOn(it, today) }
    val fraction = if (active.isEmpty()) 0f else doneCount.toFloat() / active.size
    val animated by animateFloatAsState(fraction, tween(500), label = "widgetProgress")
    val shape = RoundedCornerShape(26.dp)

    Column(
        modifier
            .fillMaxWidth()
            .glow(Color.Black.copy(alpha = if (c.isDark) 0.45f else 0.18f), 40.dp, 26.dp)
            .clip(shape)
            .background(if (c.isDark) Color(0xFF0D1412).copy(alpha = 0.92f) else Color.White.copy(alpha = 0.92f))
            .border(1.dp, if (c.isDark) Color(0xFFC2FF4D).copy(alpha = 0.16f) else Color(0xFF0C1110).copy(alpha = 0.08f), shape)
            .padding(16.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            LogoMark(22.dp, glowing = false)
            HSpace(9.dp)
            Text("HabitForge", style = AppTheme.textStyles.titleSmall.copy(fontSize = 14.sp), fontWeight = FontWeight.SemiBold, color = c.textPrimary,
                modifier = Modifier.weight(1f))
            Text("$doneCount/${active.size}", style = AppTheme.textStyles.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = c.primary)
            HSpace(4.dp)
            Text("today", style = AppTheme.textStyles.bodySmall, color = c.textTertiary)
        }
        Box(Modifier.fillMaxWidth().height(5.dp).clip(CircleShape).background(c.heatEmpty)) {
            Box(
                Modifier
                    .fillMaxWidth(animated.coerceIn(0f, 1f))
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(c.primary, c.secondary))),
            )
        }
        Column(Modifier.padding(top = 14.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
            if (rows.isEmpty()) {
                Text("Your habits will show up here.", style = AppTheme.textStyles.bodySmall, color = c.textTertiary)
            }
            rows.forEach { h ->
                val color = Accents.colorOf(h.color)
                val done = HabitStats.isDoneOn(h, today)
                val streak = HabitUtil.currentStreak(HabitStats.completedSet(h), today)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                    Box(
                        Modifier.size(30.dp).clip(RoundedCornerShape(9.dp)).background(color.copy(alpha = 0.13f)),
                        contentAlignment = Alignment.Center,
                    ) { PhIconNamed(HabitCatalog.iconFor(h), size = 16.dp, tint = color) }
                    Text(
                        h.name,
                        style = AppTheme.textStyles.bodyMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium,
                            textDecoration = if (done) TextDecoration.LineThrough else null),
                        color = if (done) c.textSecondary else c.textPrimary,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (streak > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            PhIcon(PhIcons.Fire, size = 12.dp, tint = color, weight = PhWeight.Fill)
                            Text(streak.toString(), style = AppTheme.textStyles.titleSmall.copy(fontSize = 12.sp), fontWeight = FontWeight.SemiBold, color = c.textSecondary)
                        }
                    }
                    Box(
                        Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(if (done) color else Color.Transparent)
                            .then(if (!done) Modifier.border(2.dp, c.textTertiary, CircleShape) else Modifier),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (done) PhIcon(PhIcons.Check, size = 15.dp, tint = Accents.inkFor(color), weight = PhWeight.Bold)
                    }
                }
            }
            if (active.size > maxRows) {
                Text("+${active.size - maxRows} more in app", style = AppTheme.textStyles.caption, color = c.textTertiary,
                    modifier = Modifier.fillMaxWidth().offset(y = 1.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}
