package io.github.gaurav95998030.habittracker.features.motivation.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.gaurav95998030.habittracker.commons.ui.FeatureScreen
import io.github.gaurav95998030.habittracker.commons.ui.HSpace
import io.github.gaurav95998030.habittracker.commons.ui.HfButton
import io.github.gaurav95998030.habittracker.commons.ui.HfButtonSize
import io.github.gaurav95998030.habittracker.commons.ui.HfButtonVariant
import io.github.gaurav95998030.habittracker.commons.ui.HfToggle
import io.github.gaurav95998030.habittracker.commons.ui.PhIcon
import io.github.gaurav95998030.habittracker.commons.ui.PhIconNamed
import io.github.gaurav95998030.habittracker.commons.ui.PhWeight
import io.github.gaurav95998030.habittracker.features.motivation.domain.PlanCatalog
import io.github.gaurav95998030.habittracker.features.motivation.domain.PlanHabit
import io.github.gaurav95998030.habittracker.features.motivation.domain.StarterPlan
import io.github.gaurav95998030.habittracker.ui.theme.Accents
import io.github.gaurav95998030.habittracker.ui.theme.AppShapes
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme
import io.github.gaurav95998030.habittracker.ui.theme.PhIcons
import io.github.gaurav95998030.habittracker.ui.theme.glow
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Starter plans: pick a 21/30-day program, toggle which of its habits to include, and
 * add them all at once. A running plan shows "Day N of D".
 *
 * @param activePlans plan id → start date ("yyyy-MM-dd").
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PlansScreen(
    activePlans: Map<String, String>,
    onStart: (StarterPlan, List<PlanHabit>) -> Unit,
    onEnd: (StarterPlan) -> Unit,
    onBack: () -> Unit,
) {
    val c = AppTheme.colors
    var selectedId by rememberSaveable { mutableStateOf(PlanCatalog.plans.first().id) }
    var off by rememberSaveable(selectedId) { mutableStateOf(setOf<String>()) }
    val plan = PlanCatalog.byId(selectedId) ?: PlanCatalog.plans.first()
    val color = Accents.colorOf(plan.colorHex)
    val chosen = plan.habits.filter { it.name !in off }
    val started = activePlans[plan.id]?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

    FeatureScreen("Starter plans", "Several habits, one tap", onBack, icon = PhIcons.RocketLaunch) {
        LazyRow(Modifier.padding(bottom = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
            items(PlanCatalog.plans, key = { it.id }) { p ->
                val active = p.id == selectedId
                val pc = Accents.colorOf(p.colorHex)
                val shape = RoundedCornerShape(AppShapes.card)
                Column(
                    Modifier.width(140.dp)
                        .glow(pc.copy(alpha = 0.2f), 22.dp, AppShapes.card, enabled = active && c.isDark)
                        .clip(shape)
                        .background(
                            when {
                                active && c.isDark -> Brush.linearGradient(listOf(pc.copy(alpha = 0.15f), c.surface))
                                active -> Brush.linearGradient(listOf(pc.copy(alpha = 0.08f), pc.copy(alpha = 0.08f)))
                                else -> Brush.linearGradient(listOf(c.surface, c.surface))
                            }
                        )
                        .border(1.5.dp, if (active) pc else c.glassBorder, shape)
                        .clickable { selectedId = p.id }
                        .padding(14.dp),
                ) {
                    PhIconNamed(p.icon, size = 26.dp, tint = pc, weight = PhWeight.Fill)
                    Text(p.name, style = AppTheme.textStyles.titleSmall, fontWeight = FontWeight.SemiBold, color = c.textPrimary, modifier = Modifier.padding(top = 12.dp))
                    Text("${p.days} days · ${p.habits.size} habits", style = AppTheme.textStyles.bodySmall, color = c.textTertiary, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }

        val shape = RoundedCornerShape(AppShapes.card)
        Column(Modifier.fillMaxWidth().clip(shape).background(c.surface).border(1.dp, c.glassBorder, shape)) {
            Column(
                Modifier.fillMaxWidth().drawBehind {
                    drawRect(
                        if (c.isDark) Brush.radialGradient(listOf(color.copy(alpha = 0.19f), Color.Transparent), center = Offset.Zero, radius = size.maxDimension * 0.9f)
                        else Brush.linearGradient(listOf(color.copy(alpha = 0.07f), color.copy(alpha = 0.07f)))
                    )
                }.padding(20.dp),
            ) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${plan.days}", style = AppTheme.textStyles.displayLarge.copy(fontSize = 52.sp, lineHeight = 52.sp, letterSpacing = (-2).sp), color = c.textPrimary)
                    Text("day program", style = AppTheme.textStyles.bodyMedium, color = c.textSecondary, modifier = Modifier.padding(bottom = 8.dp))
                }
                Text(plan.name, style = AppTheme.textStyles.titleLarge.copy(fontSize = 20.sp), color = c.textPrimary, modifier = Modifier.padding(top = 10.dp))
                Text(plan.blurb, style = AppTheme.textStyles.bodyMedium.copy(fontSize = 13.sp), color = c.textSecondary, modifier = Modifier.padding(top = 3.dp))
            }
            Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 16.dp)) {
                plan.habits.forEach { h ->
                    val hc = Accents.colorOf(h.colorHex)
                    val included = h.name !in off
                    HorizontalDivider(color = c.divider, thickness = 1.dp)
                    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(36.dp).clip(RoundedCornerShape(11.dp)).background(hc.copy(alpha = 0.13f)), contentAlignment = Alignment.Center) {
                            PhIconNamed(h.icon, size = 18.dp, tint = hc)
                        }
                        HSpace(12.dp)
                        Text(h.name, style = AppTheme.textStyles.bodyMedium, color = if (included) c.textPrimary else c.textTertiary, modifier = Modifier.weight(1f))
                        if (started == null) {
                            HfToggle(included, { on -> off = if (on) off - h.name else off + h.name }, accent = color)
                        }
                    }
                }
            }
        }

        if (started != null) {
            val day = (ChronoUnit.DAYS.between(started, LocalDate.now()) + 1).coerceAtLeast(1)
            val done = day > plan.days
            val sshape = RoundedCornerShape(AppShapes.card)
            Row(
                Modifier.padding(top = 18.dp).fillMaxWidth().clip(sshape).background(c.success.copy(alpha = 0.1f))
                    .border(1.dp, c.success.copy(alpha = 0.33f), sshape).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PhIcon(PhIcons.CheckCircle, size = 26.dp, tint = c.success, weight = PhWeight.Fill)
                HSpace(12.dp)
                Column {
                    Text(if (done) "Plan complete — ${plan.days} days" else "Day $day of ${plan.days}",
                        style = AppTheme.textStyles.titleMedium.copy(fontSize = 16.sp), fontWeight = FontWeight.SemiBold, color = c.textPrimary)
                    Text(if (done) "The habits stay — keep the streaks going." else "Its habits are on your Today list.",
                        style = AppTheme.textStyles.bodyMedium.copy(fontSize = 13.sp), color = c.textSecondary)
                }
            }
            HfButton(if (done) "Close plan" else "End plan", onClick = { onEnd(plan) }, variant = HfButtonVariant.Ghost,
                fullWidth = true, modifier = Modifier.padding(top = 8.dp))
        } else {
            HfButton(
                "Start plan · add ${chosen.size} ${if (chosen.size == 1) "habit" else "habits"}",
                onClick = { onStart(plan, chosen) },
                icon = PhIcons.RocketLaunch, size = HfButtonSize.Large, enabled = chosen.isNotEmpty(), fullWidth = true,
                modifier = Modifier.padding(top = 18.dp),
            )
        }
    }
}
