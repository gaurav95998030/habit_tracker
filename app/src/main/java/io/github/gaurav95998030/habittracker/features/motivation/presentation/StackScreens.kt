package io.github.gaurav95998030.habittracker.features.motivation.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.gaurav95998030.habittracker.commons.ui.FeatureScreen
import io.github.gaurav95998030.habittracker.commons.ui.HSpace
import io.github.gaurav95998030.habittracker.commons.ui.HfBottomSheet
import io.github.gaurav95998030.habittracker.commons.ui.HfButton
import io.github.gaurav95998030.habittracker.commons.ui.HfButtonSize
import io.github.gaurav95998030.habittracker.commons.ui.HfButtonVariant
import io.github.gaurav95998030.habittracker.commons.ui.HfField
import io.github.gaurav95998030.habittracker.commons.ui.HfIconButton
import io.github.gaurav95998030.habittracker.commons.ui.HfSectionLabel
import io.github.gaurav95998030.habittracker.commons.ui.PhIcon
import io.github.gaurav95998030.habittracker.commons.ui.PhIconNamed
import io.github.gaurav95998030.habittracker.commons.ui.PhWeight
import io.github.gaurav95998030.habittracker.commons.ui.RingStat
import io.github.gaurav95998030.habittracker.commons.ui.accentWash
import io.github.gaurav95998030.habittracker.features.habits.HabitCatalog
import io.github.gaurav95998030.habittracker.features.habits.HabitUtil
import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit
import io.github.gaurav95998030.habittracker.features.motivation.domain.MotivationInsights
import io.github.gaurav95998030.habittracker.features.motivation.domain.StackProgress
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.HabitStack
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.StackStep
import io.github.gaurav95998030.habittracker.ui.theme.Accents
import io.github.gaurav95998030.habittracker.ui.theme.AppShapes
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme
import io.github.gaurav95998030.habittracker.ui.theme.PhIcons
import io.github.gaurav95998030.habittracker.ui.theme.glow

/**
 * A habit stack: steps run in order; only the next one has "Done" (which checks that
 * habit off for today), later steps stay locked until it's done.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StackScreen(
    stack: HabitStack,
    habits: List<Habit>,
    onCompleteStep: (Habit) -> Unit,
    onEdit: () -> Unit,
    onBack: () -> Unit,
) {
    val c = AppTheme.colors
    val p = remember(stack, habits) { MotivationInsights.stackProgress(stack, habits) }
    FeatureScreen(
        stack.name, "Each habit unlocks the next", onBack, icon = PhIcons.Stack,
        trailing = { HfIconButton(PhIcons.PencilSimple, onClick = onEdit, size = 40.dp, iconSize = 18.dp, contentDescription = "Edit stack") },
    ) {
        if (p.steps.isEmpty()) {
            Text("This stack has no steps yet.", style = AppTheme.textStyles.bodyMedium, color = c.textSecondary)
            HfButton("Add steps", onClick = onEdit, icon = PhIcons.Plus, modifier = Modifier.padding(top = 16.dp))
            return@FeatureScreen
        }
        val shape = RoundedCornerShape(AppShapes.card)
        Row(
            Modifier.fillMaxWidth().padding(bottom = 22.dp).clip(shape).background(accentWash(c.primary, 0.1f))
                .border(1.dp, if (c.isDark) c.primary.copy(alpha = 0.2f) else c.glassBorder, shape).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RingStat(fraction = p.done.toFloat() / p.steps.size, value = "${p.done}/${p.steps.size}", size = 64.dp)
            HSpace(14.dp)
            Column(Modifier.weight(1f)) {
                val next = p.next
                Text(
                    if (p.complete) "Stack complete" else "Next: ${next!!.second.name}",
                    style = AppTheme.textStyles.titleMedium.copy(fontSize = 17.sp), fontWeight = FontWeight.SemiBold, color = c.textPrimary,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
                Text(
                    when {
                        p.complete -> "All ${p.steps.size} done, in order."
                        p.done == 0 -> "Start with your anchor habit."
                        else -> "After ${p.steps[p.done - 1].second.name.lowercase()}, do this."
                    },
                    style = AppTheme.textStyles.bodyMedium.copy(fontSize = 13.sp), color = c.textSecondary, modifier = Modifier.padding(top = 3.dp),
                )
            }
        }
        StackTimeline(p, onCompleteStep)
        HfButton("Edit stack", onClick = onEdit, variant = HfButtonVariant.Secondary, icon = PhIcons.PencilSimple,
            fullWidth = true, modifier = Modifier.padding(top = 22.dp))
    }
}

@Composable
private fun StackTimeline(p: StackProgress, onCompleteStep: (Habit) -> Unit) {
    val c = AppTheme.colors
    Column {
        p.steps.forEachIndexed { i, (step, habit) ->
            val color = Accents.colorOf(habit.color)
            val isDone = i < p.done
            val isNext = i == p.done
            val locked = i > p.done
            val prevName = p.steps.getOrNull(i - 1)?.second?.name?.lowercase()
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                // Node + connector line.
                Box(Modifier.width(44.dp).fillMaxHeight()) {
                    if (i < p.steps.lastIndex) {
                        Box(
                            Modifier.align(Alignment.TopCenter).padding(top = 44.dp).width(2.dp).fillMaxHeight()
                                .glow(color.copy(alpha = 0.53f), 8.dp, 1.dp, enabled = isDone && c.isDark)
                                .clip(CircleShape).background(if (isDone) color else c.divider),
                        )
                    }
                    Box(
                        Modifier.size(44.dp)
                            .glow(color.copy(alpha = 0.53f), 16.dp, 22.dp, enabled = isDone && c.isDark)
                            .clip(CircleShape)
                            .background(if (isDone) color else c.surface)
                            .then(if (!isDone) Modifier.border(2.dp, if (isNext) color else c.glassBorder, CircleShape) else Modifier),
                        contentAlignment = Alignment.Center,
                    ) {
                        when {
                            isDone -> PhIcon(PhIcons.Check, size = 20.dp, tint = Accents.inkFor(color), weight = PhWeight.Bold)
                            locked -> PhIcon(PhIcons.LockSimple, size = 20.dp, tint = c.textTertiary, weight = PhWeight.Bold)
                            else -> PhIconNamed(HabitCatalog.iconFor(habit), size = 20.dp, tint = color)
                        }
                    }
                }
                HSpace(14.dp)
                val shape = RoundedCornerShape(AppShapes.tile)
                Row(
                    Modifier.weight(1f).padding(bottom = if (i < p.steps.lastIndex) 14.dp else 0.dp)
                        .alpha(if (locked) 0.5f else 1f)
                        .glow(color.copy(alpha = 0.13f), 24.dp, AppShapes.tile, enabled = isNext && c.isDark)
                        .clip(shape).background(c.surface)
                        .border(1.dp, if (isNext) color.copy(alpha = 0.4f) else c.glassBorder, shape)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            (if (i == 0) "Anchor" else "After $prevName").uppercase(),
                            style = AppTheme.textStyles.labelSmall.copy(letterSpacing = 0.4.sp),
                            color = if (i == 0) c.warning else c.textTertiary, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            habit.name,
                            style = AppTheme.textStyles.titleSmall.copy(fontSize = 16.sp, textDecoration = if (isDone) TextDecoration.LineThrough else null),
                            color = c.textPrimary, modifier = Modifier.padding(top = 2.dp),
                        )
                        Text(
                            when {
                                locked -> "Unlocks after $prevName"
                                i == 0 && habit.reminderEnabled && habit.reminderTime != null -> HabitUtil.formatTime12h(habit.reminderTime)
                                step.minutes != null -> "~${step.minutes} min"
                                else -> habit.category
                            },
                            style = AppTheme.textStyles.bodySmall, color = c.textTertiary, modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    if (isNext) HfButton("Done", onClick = { onCompleteStep(habit) }, icon = PhIcons.Check, size = HfButtonSize.Small)
                }
            }
        }
    }
}

private val MinuteOptions = listOf<Int?>(null, 2, 5, 10, 15, 30)

/**
 * Create / edit a stack: name, ordered steps (move up/down, remove, optional minutes),
 * and a list of habits to add. [initial] null = new stack.
 */
@Composable
fun StackEditorSheet(
    initial: HabitStack?,
    habits: List<Habit>,
    onSave: (name: String, steps: List<StackStep>) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    val c = AppTheme.colors
    var name by rememberSaveable { mutableStateOf(initial?.name ?: "Morning stack") }
    var steps by remember { mutableStateOf(initial?.steps?.filter { s -> habits.any { it.id == s.habitId } } ?: emptyList()) }
    val byId = habits.associateBy { it.id }
    val available = habits.filter { h -> h.isActive && steps.none { it.habitId == h.id } }

    HfBottomSheet(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth().imePadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(if (initial == null) "New stack" else "Edit stack", style = AppTheme.textStyles.titleLarge.copy(fontSize = 21.sp),
                    color = c.textPrimary, modifier = Modifier.weight(1f))
                HfIconButton(PhIcons.X, onClick = onDismiss, contentDescription = "Close")
            }
            Column(
                Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(start = 22.dp, end = 22.dp, top = 18.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                HfField(name, { name = it }, label = "Stack name", placeholder = "e.g. Morning stack", icon = PhIcons.Stack)
                Column {
                    HfSectionLabel("Steps, in order")
                    if (steps.isEmpty()) Text("Add habits below — the first one is your anchor.", style = AppTheme.textStyles.bodyMedium, color = c.textTertiary)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        steps.forEachIndexed { i, s ->
                            val h = byId[s.habitId] ?: return@forEachIndexed
                            val color = Accents.colorOf(h.color)
                            val shape = RoundedCornerShape(14.dp)
                            Row(
                                Modifier.fillMaxWidth().clip(shape).background(c.surface).border(1.dp, c.glassBorder, shape).padding(start = 12.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("${i + 1}", style = AppTheme.textStyles.titleSmall, fontWeight = FontWeight.SemiBold, color = c.textTertiary, modifier = Modifier.width(18.dp))
                                PhIconNamed(HabitCatalog.iconFor(h), size = 18.dp, tint = color)
                                HSpace(10.dp)
                                Text(h.name, style = AppTheme.textStyles.bodyMedium, color = c.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                if (i > 0) {
                                    Text(
                                        s.minutes?.let { "~$it min" } ?: "+ min",
                                        style = AppTheme.textStyles.caption, fontWeight = FontWeight.SemiBold,
                                        color = if (s.minutes != null) c.primary else c.textTertiary,
                                        modifier = Modifier.clip(CircleShape).clickable(onClickLabel = "Change duration") {
                                            val nextMin = MinuteOptions[(MinuteOptions.indexOf(s.minutes) + 1) % MinuteOptions.size]
                                            steps = steps.toMutableList().also { it[i] = s.copy(minutes = nextMin) }
                                        }.padding(horizontal = 8.dp, vertical = 6.dp),
                                    )
                                }
                                SmallIcon(PhIcons.CaretUp, "Move up", enabled = i > 0) {
                                    steps = steps.toMutableList().also { val t = it[i]; it[i] = it[i - 1]; it[i - 1] = t }
                                }
                                SmallIcon(PhIcons.CaretDown, "Move down", enabled = i < steps.lastIndex) {
                                    steps = steps.toMutableList().also { val t = it[i]; it[i] = it[i + 1]; it[i + 1] = t }
                                }
                                SmallIcon(PhIcons.X, "Remove") { steps = steps.filterIndexed { j, _ -> j != i } }
                            }
                        }
                    }
                }
                if (available.isNotEmpty()) {
                    Column {
                        HfSectionLabel("Add a habit")
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            available.forEach { h ->
                                Row(
                                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                        .clickable(onClickLabel = "Add ${h.name}") { steps = steps + StackStep(h.id, steps.size, null) }
                                        .padding(horizontal = 6.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    PhIconNamed(HabitCatalog.iconFor(h), size = 18.dp, tint = Accents.colorOf(h.color))
                                    HSpace(10.dp)
                                    Text(h.name, style = AppTheme.textStyles.bodyMedium, color = c.textPrimary, modifier = Modifier.weight(1f))
                                    PhIcon(PhIcons.Plus, size = 18.dp, tint = c.primary, weight = PhWeight.Bold)
                                }
                            }
                        }
                    }
                }
                if (onDelete != null) {
                    HfButton("Delete stack", onClick = onDelete, variant = HfButtonVariant.Danger, icon = PhIcons.Trash, fullWidth = true)
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HfButton("Cancel", onClick = onDismiss, variant = HfButtonVariant.Secondary)
                HfButton(
                    if (initial == null) "Create stack" else "Save", onClick = { onSave(name, steps) },
                    icon = PhIcons.Check, enabled = steps.size >= 2, fullWidth = true, modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SmallIcon(glyph: String, label: String, enabled: Boolean = true, onClick: () -> Unit) {
    val c = AppTheme.colors
    Box(
        Modifier.size(32.dp).clip(CircleShape).clickable(enabled = enabled, onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { PhIcon(glyph, size = 16.dp, tint = if (enabled) c.textSecondary else c.textTertiary.copy(alpha = 0.4f), weight = PhWeight.Bold) }
}

/** Compact stack card for Home: name, progress and the next step. */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StackCard(stack: HabitStack, habits: List<Habit>, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    val p = remember(stack, habits) { MotivationInsights.stackProgress(stack, habits) }
    val shape = RoundedCornerShape(AppShapes.tile)
    Row(
        modifier.fillMaxWidth().clip(shape).background(c.surface).border(1.dp, c.glassBorder, shape)
            .clickable(onClickLabel = "Open ${stack.name}", onClick = onOpen).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(c.primary.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
            PhIcon(PhIcons.Stack, size = 22.dp, tint = c.primary, weight = PhWeight.Fill)
        }
        HSpace(13.dp)
        Column(Modifier.weight(1f)) {
            Text(stack.name, style = AppTheme.textStyles.titleMedium.copy(fontSize = 16.sp), color = c.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                when {
                    p.steps.isEmpty() -> "No steps yet"
                    p.complete -> "Stack complete · ${p.steps.size}/${p.steps.size}"
                    else -> "Next: ${p.next!!.second.name} · ${p.done}/${p.steps.size}"
                },
                style = AppTheme.textStyles.bodySmall, color = if (p.complete) c.success else c.textSecondary,
                maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 2.dp),
            )
        }
        // Mini progress dots.
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            p.steps.forEachIndexed { i, (_, h) ->
                Box(Modifier.size(8.dp).clip(CircleShape).background(if (i < p.done) Accents.colorOf(h.color) else c.heatEmpty))
            }
        }
    }
}
