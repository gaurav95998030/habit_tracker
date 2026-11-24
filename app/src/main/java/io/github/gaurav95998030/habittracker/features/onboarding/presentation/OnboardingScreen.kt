package io.github.gaurav95998030.habittracker.features.onboarding.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.gaurav95998030.habittracker.commons.ui.HfButton
import io.github.gaurav95998030.habittracker.commons.ui.HfButtonSize
import io.github.gaurav95998030.habittracker.commons.ui.HfButtonVariant
import io.github.gaurav95998030.habittracker.commons.ui.HfField
import io.github.gaurav95998030.habittracker.commons.ui.HfSectionLabel
import io.github.gaurav95998030.habittracker.commons.ui.HfSwatches
import io.github.gaurav95998030.habittracker.commons.ui.Logo
import io.github.gaurav95998030.habittracker.commons.ui.PhIcon
import io.github.gaurav95998030.habittracker.commons.ui.PhIconNamed
import io.github.gaurav95998030.habittracker.commons.ui.PhWeight
import io.github.gaurav95998030.habittracker.features.habits.HabitCatalog
import io.github.gaurav95998030.habittracker.features.habits.presentation.components.StreakBadge
import io.github.gaurav95998030.habittracker.ui.theme.Accents
import io.github.gaurav95998030.habittracker.ui.theme.AppMotion
import io.github.gaurav95998030.habittracker.ui.theme.AppShapes
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme
import io.github.gaurav95998030.habittracker.ui.theme.PhIcons
import io.github.gaurav95998030.habittracker.ui.theme.glow

/** What onboarding collected. [habitIcon] is a Phosphor name. */
data class OnboardingResult(
    val name: String,
    val categories: Set<String>,
    val habitName: String,
    val habitCategory: String,
    val habitColorHex: String,
    val habitIcon: String?,
)

private const val STEPS = 4

/** Welcome → name → focus areas → first habit. */
@Composable
fun OnboardingScreen(onFinish: (OnboardingResult) -> Unit) {
    val c = AppTheme.colors
    var step by rememberSaveable { mutableIntStateOf(0) }
    var name by rememberSaveable { mutableStateOf("") }
    var picked by rememberSaveable { mutableStateOf(listOf<String>()) }
    var habitName by rememberSaveable { mutableStateOf("") }
    var habitColor by rememberSaveable { mutableStateOf(Accents.Lime.hex) }
    var habitCategory by rememberSaveable { mutableStateOf("Body") }
    var habitIcon by rememberSaveable { mutableStateOf<String?>("ph-sneaker-move") }

    BackHandler(enabled = step > 0) { step-- }

    fun finish() = onFinish(
        OnboardingResult(
            name = name.trim(),
            categories = picked.toSet(),
            habitName = habitName.trim().ifBlank { "Morning run" },
            habitCategory = habitCategory,
            habitColorHex = habitColor,
            habitIcon = habitIcon,
        )
    )

    Box(Modifier.fillMaxSize().background(c.background)) {
        // Aurora glow backdrop.
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-120).dp)
                .size(400.dp)
                .background(Brush.radialGradient(listOf(c.primary.copy(alpha = if (c.isDark) 0.13f else 0.08f), Color.Transparent))),
        )
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
            ProgressDots(step, Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 10.dp))
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    val dir = if (targetState > initialState) 1 else -1
                    (slideInHorizontally(tween(300, easing = AppMotion.emphasized)) { it / 6 * dir } + fadeIn(tween(300))) togetherWith
                        (slideOutHorizontally(tween(300, easing = AppMotion.emphasized)) { -it / 6 * dir } + fadeOut(tween(200)))
                },
                label = "onboardingStep",
                modifier = Modifier.weight(1f),
            ) { s ->
                when (s) {
                    0 -> WelcomeStep(onNext = { step = 1 })
                    1 -> NameStep(name, { name = it }, onNext = { step = 2 }, onSkip = { name = ""; step = 2 })
                    2 -> CategoryStep(picked, { cat -> picked = if (cat in picked) picked - cat else picked + cat }, onNext = { step = 3 })
                    else -> FirstHabitStep(
                        userName = name.trim(),
                        habitName = habitName,
                        onHabitNameChange = { habitName = it; habitIcon = HabitCatalog.starters.firstOrNull { s -> s.name == it }?.icon ?: habitIcon },
                        colorHex = habitColor,
                        onColor = { habitColor = it },
                        onStarter = { st ->
                            habitName = st.name
                            habitColor = Accents.all.firstOrNull { a -> Accents.sameColor(st.colorHex, a) }?.hex ?: habitColor
                            habitCategory = st.category
                            habitIcon = st.icon
                        },
                        onFinish = ::finish,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressDots(step: Int, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)) {
        repeat(STEPS) { i ->
            val width by animateDpAsState(if (i == step) 24.dp else 7.dp, AppMotion.sheetEntry(), label = "dotW")
            val color by animateColorAsState(if (i <= step) c.primary else c.heatEmpty, label = "dotC")
            Box(
                Modifier
                    .size(width, 7.dp)
                    .glow(c.primary, 10.dp, 999.dp, enabled = i == step && c.isDark)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}

@Composable
private fun StepTitle(title: String, subtitle: String) {
    val c = AppTheme.colors
    Text(title, style = AppTheme.textStyles.titleLarge.copy(fontSize = 28.sp, lineHeight = 32.sp, letterSpacing = (-0.6).sp), color = c.textPrimary)
    Text(subtitle, style = AppTheme.textStyles.bodyMedium, color = c.textSecondary, modifier = Modifier.padding(top = 10.dp))
}

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    val c = AppTheme.colors
    Column(Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 30.dp)) {
    Column(
        Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Logo(size = 34.dp, modifier = Modifier.padding(top = 20.dp))
        Box(Modifier.padding(vertical = 46.dp).size(220.dp), contentAlignment = Alignment.Center) {
            listOf(c.primary, c.secondary, Accents.Sky.color).forEachIndexed { i, ring ->
                val a = 0.5f - i * 0.12f
                Box(
                    Modifier
                        .padding((i * 28).dp)
                        .fillMaxSize()
                        .glow(ring.copy(alpha = 0.27f * a * 2), 30.dp, 999.dp, enabled = c.isDark)
                        .border(2.dp, ring.copy(alpha = a), CircleShape),
                )
            }
            // Wider than the rings, as in the design — let it overflow instead of wrapping.
            StreakBadge(value = 147, modifier = Modifier.wrapContentWidth(unbounded = true))
        }
        Text(
            "Light up the days.\nThe years follow.",
            style = AppTheme.textStyles.titleLarge.copy(fontSize = 32.sp, lineHeight = 36.sp, letterSpacing = (-0.8).sp, textAlign = TextAlign.Center),
            color = c.textPrimary,
        )
        Text(
            "Build habits that stick with streaks you can see, feel, and won't want to break.",
            style = AppTheme.textStyles.bodyLarge.copy(fontSize = 15.sp, lineHeight = 22.sp, textAlign = TextAlign.Center),
            color = c.textSecondary,
            modifier = Modifier.padding(top = 14.dp).widthIn(max = 280.dp),
        )
    }
        HfButton("Get started", onClick = onNext, icon = PhIcons.ArrowRight, size = HfButtonSize.Large, fullWidth = true,
            modifier = Modifier.padding(bottom = 10.dp))
    }
}

@Composable
private fun NameStep(name: String, onName: (String) -> Unit, onNext: () -> Unit, onSkip: () -> Unit) {
    val c = AppTheme.colors
    Column(Modifier.fillMaxSize().padding(24.dp)) {
    Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
        StepTitle("First, what should\nwe call you?", "We’ll use it to greet you every morning.")
        Box(Modifier.fillMaxWidth().padding(top = 44.dp, bottom = 28.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .size(104.dp)
                    .glow(c.primary.copy(alpha = 0.15f), 40.dp, 52.dp, enabled = c.isDark)
                    .clip(CircleShape)
                    .background(if (c.isDark) Brush.radialGradient(listOf(c.primary.copy(alpha = 0.2f), c.surface)) else Brush.linearGradient(listOf(c.primary.copy(alpha = 0.08f), c.primary.copy(alpha = 0.08f))))
                    .border(1.dp, c.glassBorder, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                val initial = name.trim().firstOrNull()?.uppercaseChar()?.toString()
                if (initial != null) {
                    Text(initial, style = AppTheme.textStyles.displayLarge.copy(fontSize = 46.sp,
                        shadow = if (c.isDark) Shadow(c.primary.copy(alpha = 0.4f), blurRadius = 24f) else null), color = c.primary)
                } else {
                    PhIcon(PhIcons.User, size = 46.dp, tint = c.primary, weight = PhWeight.Fill)
                }
            }
        }
        HfField(value = name, onValueChange = onName, placeholder = "Your name", icon = PhIcons.User, capitalization = KeyboardCapitalization.Words)
    }
        HfButton("Continue", onClick = onNext, enabled = name.isNotBlank(), icon = PhIcons.ArrowRight, size = HfButtonSize.Large, fullWidth = true,
            modifier = Modifier.padding(top = 8.dp))
        HfButton("Skip for now", onClick = onSkip, variant = HfButtonVariant.Ghost, size = HfButtonSize.Small, fullWidth = true,
            modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun CategoryStep(picked: List<String>, onToggle: (String) -> Unit, onNext: () -> Unit) {
    val c = AppTheme.colors
    Column(Modifier.fillMaxSize().padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 24.dp)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            StepTitle("What do you want\nto work on?", "Pick a few areas. You can change these anytime.")
            Column(Modifier.padding(top = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                HabitCatalog.categories.chunked(2).forEachIndexed { row, pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        pair.forEachIndexed { col, cat ->
                            val accent = Accents.all[(row * 2 + col) % Accents.all.size].color
                            val sel = cat.name in picked
                            val shape = RoundedCornerShape(AppShapes.card)
                            Column(
                                Modifier
                                    .weight(1f)
                                    .glow(accent.copy(alpha = 0.2f), 24.dp, AppShapes.card, enabled = sel && c.isDark)
                                    .clip(shape)
                                    .background(
                                        when {
                                            sel && c.isDark -> Brush.linearGradient(listOf(accent.copy(alpha = 0.15f), c.surface))
                                            sel -> Brush.linearGradient(listOf(accent.copy(alpha = 0.08f), accent.copy(alpha = 0.08f)))
                                            else -> Brush.linearGradient(listOf(c.surface, c.surface))
                                        }
                                    )
                                    .border(1.5.dp, if (sel) accent else c.glassBorder, shape)
                                    .clickable { onToggle(cat.name) }
                                    .padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    PhIconNamed(cat.icon, size = 26.dp, tint = accent)
                                    if (sel) PhIcon(PhIcons.CheckCircle, size = 22.dp, tint = accent, weight = PhWeight.Fill)
                                }
                                Text(cat.name, style = AppTheme.textStyles.titleMedium.copy(fontSize = 16.sp), color = c.textPrimary)
                            }
                        }
                    }
                }
            }
        }
        HfButton(
            if (picked.isEmpty()) "Pick at least one" else "Continue with ${picked.size}",
            onClick = onNext, enabled = picked.isNotEmpty(), icon = PhIcons.ArrowRight,
            size = HfButtonSize.Large, fullWidth = true, modifier = Modifier.padding(top = 18.dp),
        )
    }
}

@Composable
private fun FirstHabitStep(
    userName: String,
    habitName: String,
    onHabitNameChange: (String) -> Unit,
    colorHex: String,
    onColor: (String) -> Unit,
    onStarter: (io.github.gaurav95998030.habittracker.features.habits.StarterHabit) -> Unit,
    onFinish: () -> Unit,
) {
    val c = AppTheme.colors
    Column(Modifier.fillMaxSize().padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 24.dp)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            StepTitle(
                if (userName.isNotBlank()) "Nice to meet you, $userName." else "Your first habit",
                "Start with one. Momentum compounds.",
            )
            HfField(value = habitName, onValueChange = onHabitNameChange, placeholder = "Name your habit", icon = PhIcons.PencilSimple,
                modifier = Modifier.padding(top = 22.dp))
            HfSectionLabel("Or pick a starter", Modifier.padding(top = 20.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HabitCatalog.starters.forEach { s ->
                    val color = Accents.colorOf(s.colorHex)
                    val sel = habitName == s.name
                    Row(
                        Modifier
                            .clip(CircleShape)
                            .background(if (sel) color.copy(alpha = 0.13f) else c.glassFill)
                            .border(1.dp, if (sel) color else c.glassBorder, CircleShape)
                            .clickable { onStarter(s) }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        PhIconNamed(s.icon, size = 16.dp, tint = color)
                        Text(s.name, style = AppTheme.textStyles.labelLarge.copy(fontSize = 13.sp), color = if (sel) color else c.textSecondary)
                    }
                }
            }
            HfSectionLabel("Accent", Modifier.padding(top = 24.dp))
            HfSwatches(selectedHex = colorHex, onSelect = onColor)
            Spacer(Modifier.height(24.dp).width(1.dp))
        }
        HfButton("Start my streak", onClick = onFinish, icon = PhIcons.Lightning, iconWeight = PhWeight.Fill,
            size = HfButtonSize.Large, fullWidth = true)
    }
}
