package io.github.gaurav95998030.habittracker.features.motivation.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.gaurav95998030.habittracker.commons.ui.FeatureScreen
import io.github.gaurav95998030.habittracker.commons.ui.HSpace
import io.github.gaurav95998030.habittracker.commons.ui.HfCard
import io.github.gaurav95998030.habittracker.commons.ui.HfPanel
import io.github.gaurav95998030.habittracker.commons.ui.PhIcon
import io.github.gaurav95998030.habittracker.commons.ui.PhIconNamed
import io.github.gaurav95998030.habittracker.commons.ui.PhWeight
import io.github.gaurav95998030.habittracker.commons.ui.accentWash
import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit
import io.github.gaurav95998030.habittracker.features.motivation.domain.MotivationInsights
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.EnergyCheckin
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.EnergyLevel
import io.github.gaurav95998030.habittracker.ui.theme.Accents
import io.github.gaurav95998030.habittracker.ui.theme.AppMotion
import io.github.gaurav95998030.habittracker.ui.theme.AppShapes
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme
import io.github.gaurav95998030.habittracker.ui.theme.PhIcons
import io.github.gaurav95998030.habittracker.ui.theme.glow
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/** One-tap energy rating for today; tap the selected level again to clear it. */
@Composable
fun EnergyPicker(selected: EnergyLevel?, onSelect: (EnergyLevel?) -> Unit, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        EnergyLevel.entries.forEach { e ->
            val active = e == selected
            val col = Accents.colorOf(e.colorHex)
            val lift by animateDpAsState(if (active) (-3).dp else 0.dp, AppMotion.completion(), label = "energyLift")
            val bg by animateColorAsState(if (active) col.copy(alpha = 0.14f) else c.glassFill, label = "energyBg")
            val shape = RoundedCornerShape(16.dp)
            Column(
                Modifier
                    .weight(1f)
                    .offset(y = lift)
                    .glow(col.copy(alpha = 0.33f), 18.dp, 16.dp, enabled = active && c.isDark)
                    .clip(shape)
                    .background(bg)
                    .border(1.5.dp, if (active) col else c.glassBorder, shape)
                    .clickable(role = Role.RadioButton, onClickLabel = e.label) { onSelect(if (active) null else e) }
                    .padding(top = 14.dp, bottom = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PhIconNamed(e.icon, size = 24.dp, tint = if (active) col else c.textTertiary, weight = if (active) PhWeight.Fill else PhWeight.Bold)
                Text(e.label, style = AppTheme.textStyles.caption.copy(fontSize = 10.5.sp), fontWeight = FontWeight.SemiBold,
                    color = if (active) col else c.textTertiary, maxLines = 1)
            }
        }
    }
}

/** Check-in: today's energy, this week's energy vs completions, and the high/low-energy gap. */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CheckInScreen(habits: List<Habit>, checkins: List<EnergyCheckin>, onSetEnergy: (EnergyLevel?) -> Unit, onBack: () -> Unit) {
    val c = AppTheme.colors
    val today = LocalDate.now().toString()
    val selected = checkins.firstOrNull { it.date == today }?.level
    val insight = remember(habits, checkins) { MotivationInsights.energy(habits, checkins) }

    FeatureScreen("Check-in", "Takes one tap", onBack, icon = PhIcons.Heartbeat) {
        HfCard(Modifier.fillMaxWidth(), padding = androidx.compose.foundation.layout.PaddingValues(20.dp)) {
            Text("How’s your energy today?", style = AppTheme.textStyles.titleLarge.copy(fontSize = 21.sp, letterSpacing = (-0.3).sp), color = c.textPrimary)
            EnergyPicker(selected, onSetEnergy, Modifier.padding(top = 18.dp))
        }

        HfPanel("This week", "Energy vs. habits completed") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                insight.week.forEachIndexed { i, d ->
                    val e = d.checkin?.level
                    val isToday = i == insight.week.lastIndex
                    val ratio = if (d.total == 0) 0f else d.done.toFloat() / d.total
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val col = e?.let { Accents.colorOf(it.colorHex) }
                        Box(
                            Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(col?.copy(alpha = 0.14f) ?: c.heatEmpty)
                                .then(if (isToday && e == null) Modifier.drawBehind {
                                    val w = 1.5.dp.toPx()
                                    drawRoundRect(c.textTertiary, cornerRadius = CornerRadius(size.width / 2),
                                        topLeft = androidx.compose.ui.geometry.Offset(w / 2, w / 2),
                                        size = androidx.compose.ui.geometry.Size(size.width - w, size.height - w),
                                        style = Stroke(w, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 2.dp.toPx()))))
                                } else Modifier),
                            contentAlignment = Alignment.Center,
                        ) { if (e != null && col != null) PhIconNamed(e.icon, size = 15.dp, tint = col, weight = PhWeight.Fill) }
                        Box(Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(6.dp)).background(c.heatEmpty), contentAlignment = Alignment.BottomCenter) {
                            if (ratio > 0f) {
                                Box(Modifier.fillMaxWidth().fillMaxHeight(ratio).alpha(0.35f + 0.65f * ratio).clip(RoundedCornerShape(6.dp)).background(c.primary))
                            }
                        }
                        Text(d.date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()), style = AppTheme.textStyles.caption.copy(fontSize = 10.sp),
                            color = if (isToday) c.textPrimary else c.textTertiary)
                    }
                }
            }
        }

        val shape = RoundedCornerShape(AppShapes.card)
        Column(
            Modifier.padding(top = 14.dp).fillMaxWidth().clip(shape).background(accentWash(c.primary, 0.11f))
                .border(1.dp, c.primary.copy(alpha = 0.27f), shape).padding(18.dp),
        ) {
            val lift = insight.liftPercent
            if (lift != null && insight.highPercent != null && insight.lowPercent != null) {
                Text(
                    "${if (lift >= 0) "+" else ""}$lift%",
                    style = AppTheme.textStyles.displayLarge.copy(fontSize = 44.sp, lineHeight = 44.sp, letterSpacing = (-1.5).sp,
                        shadow = if (c.isDark) Shadow(c.primary.copy(alpha = 0.33f), blurRadius = 24f) else null),
                    color = c.primary,
                )
                Text(
                    if (lift >= 0) "You finish $lift% more habits on high-energy days." else "Interesting — you finish more on low-energy days.",
                    style = AppTheme.textStyles.bodyMedium.copy(lineHeight = 20.sp), color = c.textPrimary, modifier = Modifier.padding(top = 8.dp),
                )
                Column(Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    EnergyBar("High energy", insight.highPercent, c.primary)
                    EnergyBar("Low energy", insight.lowPercent, c.textTertiary)
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PhIcon(PhIcons.Lightbulb, size = 22.dp, tint = c.primary, weight = PhWeight.Fill)
                    HSpace(12.dp)
                    Text(
                        "Check in on a few high- and low-energy days and you’ll see how energy affects your habits.",
                        style = AppTheme.textStyles.bodyMedium.copy(lineHeight = 20.sp), color = c.textPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun EnergyBar(label: String, percent: Int, color: Color) {
    val c = AppTheme.colors
    Column {
        Row(Modifier.fillMaxWidth().padding(bottom = 5.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = AppTheme.textStyles.bodySmall, color = c.textSecondary)
            Text("$percent% done", style = AppTheme.textStyles.bodySmall, color = c.textSecondary)
        }
        Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(c.heatEmpty)) {
            Box(Modifier.fillMaxWidth(percent / 100f).fillMaxHeight().clip(CircleShape).background(color))
        }
    }
}

/** Insights entry: today's quick picker + link to the full check-in. */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EnergyPanel(checkins: List<EnergyCheckin>, onSetEnergy: (EnergyLevel?) -> Unit, onOpen: () -> Unit) {
    val c = AppTheme.colors
    val selected = checkins.firstOrNull { it.date == LocalDate.now().toString() }?.level
    HfPanel("Energy check-in", "How’s your energy today?") {
        EnergyPicker(selected, onSetEnergy)
        Row(
            Modifier.padding(top = 14.dp).fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable(onClickLabel = "Open check-in", onClick = onOpen).padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("See energy vs. habits", style = AppTheme.textStyles.bodyMedium, fontWeight = FontWeight.SemiBold, color = c.primary, modifier = Modifier.weight(1f))
            PhIcon(PhIcons.CaretRight, size = 16.dp, tint = c.primary)
        }
    }
}
