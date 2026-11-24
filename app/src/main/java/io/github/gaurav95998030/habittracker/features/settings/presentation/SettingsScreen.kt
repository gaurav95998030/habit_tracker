package io.github.gaurav95998030.habittracker.features.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.gaurav95998030.habittracker.BuildConfig
import io.github.gaurav95998030.habittracker.commons.ui.HSpace
import io.github.gaurav95998030.habittracker.commons.ui.HfBottomSheet
import io.github.gaurav95998030.habittracker.commons.ui.HfButton
import io.github.gaurav95998030.habittracker.commons.ui.HfField
import io.github.gaurav95998030.habittracker.commons.ui.HfSectionLabel
import io.github.gaurav95998030.habittracker.commons.ui.HfSegRadio
import io.github.gaurav95998030.habittracker.commons.ui.HfTimePickerDialog
import io.github.gaurav95998030.habittracker.commons.ui.HfToggle
import io.github.gaurav95998030.habittracker.commons.ui.PhIcon
import io.github.gaurav95998030.habittracker.commons.ui.PhWeight
import io.github.gaurav95998030.habittracker.commons.ui.SegOption
import io.github.gaurav95998030.habittracker.features.habits.HabitUtil
import io.github.gaurav95998030.habittracker.features.settings.domain.model.QuietHours
import io.github.gaurav95998030.habittracker.features.settings.domain.model.ThemePreference
import io.github.gaurav95998030.habittracker.ui.theme.AppShapes
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme
import io.github.gaurav95998030.habittracker.ui.theme.PhIcons
import io.github.gaurav95998030.habittracker.ui.theme.glow

/**
 * Settings: profile, theme, reminders (widget, notification center, master switch,
 * quiet hours) and about.
 *
 * Uses the Activity-scoped [SettingsViewModel] passed from the host so the theme
 * and onboarding state stay in one place.
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onOpenNotifications: () -> Unit,
    onOpenWidget: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onRequestLockScreen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = AppTheme.colors
    val theme by viewModel.themePreference.collectAsStateWithLifecycle()
    val remindersOn by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val quiet by viewModel.quietHours.collectAsStateWithLifecycle()
    val savedQuotes by viewModel.savedQuotes.collectAsStateWithLifecycle()
    val diagnostics by viewModel.diagnosticsEnabled.collectAsStateWithLifecycle()
    val lockScreen by viewModel.lockScreenEnabled.collectAsStateWithLifecycle()
    // Turning it on always goes through the consent sheet; turning it off is immediate.
    val setLockScreen: (Boolean) -> Unit = { on -> if (on) onRequestLockScreen() else viewModel.disableLockScreen() }
    var showQuotes by rememberSaveable { mutableStateOf(false) }

    var editingName by rememberSaveable { mutableStateOf(false) }
    var editingQuiet by rememberSaveable { mutableStateOf(false) }
    var showWhatsNew by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier
            .fillMaxSize()
            .background(c.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 120.dp),
    ) {
        Text(
            "Settings",
            style = AppTheme.textStyles.titleLarge.copy(fontSize = 26.sp, letterSpacing = (-0.6).sp),
            color = c.textPrimary,
            modifier = Modifier.padding(bottom = 20.dp),
        )

        ProfileCard(userName = userName, onEdit = { editingName = true }, modifier = Modifier.padding(bottom = 22.dp))

        SettingsGroup("Appearance") {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                Text("Theme", style = AppTheme.textStyles.bodyLarge.copy(fontSize = 15.sp), color = c.textPrimary,
                    modifier = Modifier.padding(bottom = 12.dp))
                HfSegRadio(
                    options = listOf(
                        SegOption(ThemePreference.Light, "Light", PhIcons.Sun),
                        SegOption(ThemePreference.Dark, "Dark", PhIcons.Moon),
                        SegOption(ThemePreference.System, "System", PhIcons.DeviceMobile),
                    ),
                    selected = theme,
                    onSelect = viewModel::onThemeSelected,
                )
            }
        }

        SettingsGroup("Notifications") {
            SettingsRow(PhIcons.SquaresFour, "Home screen widget", "Track habits without opening the app", onClick = onOpenWidget, first = true)
            SettingsRow(PhIcons.BellRinging, "Notification center", "Every reminder you’ve received", onClick = onOpenNotifications)
            SettingsRow(PhIcons.Bell, "Habit reminders", "Push at each habit's set time",
                trailing = { HfToggle(remindersOn, viewModel::onNotificationsEnabledChanged) },
                onClick = { viewModel.onNotificationsEnabledChanged(!remindersOn) })
            SettingsRow(
                PhIcons.Clock, "Quiet hours",
                if (quiet.enabled) "${HabitUtil.formatTime12h(quiet.start)} – ${HabitUtil.formatTime12h(quiet.end)}" else "Off",
                onClick = { editingQuiet = true },
            )
        }

        SettingsGroup("Privacy") {
            SettingsRow(PhIcons.ChartLineUp, "Share usage & crash data", "Anonymous analytics, crash reports and performance",
                trailing = { HfToggle(diagnostics, viewModel::onDiagnosticsChanged) },
                onClick = { viewModel.onDiagnosticsChanged(!diagnostics) }, first = true)
            SettingsRow(PhIcons.LockSimple, "Today on lock screen", "Shows your habit names on the lock screen",
                trailing = { HfToggle(lockScreen, setLockScreen) },
                onClick = { setLockScreen(!lockScreen) })
            SettingsRow(PhIcons.Eye, "Privacy policy", "What the app stores and why", onClick = onOpenPrivacy)
        }

        SettingsGroup("About") {
            SettingsRow(PhIcons.Sparkle, "What's new", "Aurora redesign", onClick = { showWhatsNew = true }, first = true)
            SettingsRow(PhIcons.BookmarkSimple, "Saved quotes",
                if (savedQuotes.isEmpty()) "Bookmark quotes from your daily boost" else "${savedQuotes.size} saved",
                onClick = { showQuotes = true })
            SettingsRow(PhIcons.ArrowCounterClockwise, "Replay onboarding", onClick = viewModel::replayOnboarding)
            SettingsRow(PhIcons.Info, "Version", "${BuildConfig.VERSION_NAME} (build ${BuildConfig.VERSION_CODE})", trailing = {})
            if (BuildConfig.DEBUG) {
                // Debug builds only: verify Crashlytics end to end (report appears after relaunch).
                SettingsRow(PhIcons.Lightning, "Send test crash", "Debug only · Crashlytics check", onClick = {
                    throw RuntimeException("Glowstreak test crash (debug)")
                })
            }
        }

        Column(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("GLOWSTREAK", style = AppTheme.textStyles.titleSmall.copy(letterSpacing = 0.5.sp), fontWeight = FontWeight.SemiBold, color = c.textTertiary)
            Text("Light up the days. The years follow.", style = AppTheme.textStyles.caption, color = c.textTertiary, modifier = Modifier.padding(top = 4.dp))
        }
    }

    if (editingName) {
        EditNameSheet(initial = userName, onDismiss = { editingName = false }, onSave = { viewModel.onUserNameChanged(it); editingName = false })
    }
    if (editingQuiet) {
        QuietHoursSheet(initial = quiet, onDismiss = { editingQuiet = false }, onSave = { viewModel.onQuietHoursChanged(it); editingQuiet = false })
    }
    if (showQuotes) {
        SavedQuotesSheet(quotes = savedQuotes, onRemove = { viewModel.setQuoteSaved(it, false) }, onDismiss = { showQuotes = false })
    }
    if (showWhatsNew) {
        AlertDialog(
            onDismissRequest = { showWhatsNew = false },
            containerColor = if (c.isDark) c.surface2 else c.surface,
            title = { Text("What's new", style = AppTheme.textStyles.titleMedium, color = c.textPrimary) },
            text = {
                Text(
                    "• The new Aurora look, in dark and light\n" +
                        "• Glowing streaks, yearly heatmap and month calendar\n" +
                        "• Pause habits without losing history\n" +
                        "• Notification center, snooze and quiet hours\n" +
                        "• Home-screen widget nudge",
                    style = AppTheme.textStyles.bodyMedium.copy(lineHeight = 22.sp),
                    color = c.textSecondary,
                )
            },
            confirmButton = {
                TextButton(onClick = { showWhatsNew = false }) { Text("Nice", color = c.primary, style = AppTheme.textStyles.labelLarge) }
            },
        )
    }
}

@Composable
private fun SavedQuotesSheet(quotes: Set<String>, onRemove: (String) -> Unit, onDismiss: () -> Unit) {
    val c = AppTheme.colors
    HfBottomSheet(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 22.dp, end = 22.dp, top = 6.dp, bottom = 22.dp)) {
            Text("Saved quotes", style = AppTheme.textStyles.titleLarge.copy(fontSize = 21.sp), color = c.textPrimary)
            if (quotes.isEmpty()) {
                Text("Tap Save on a daily-boost quote and it lands here.", style = AppTheme.textStyles.bodyMedium, color = c.textSecondary,
                    modifier = Modifier.padding(top = 10.dp))
            }
            Column(
                Modifier.padding(top = 16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                quotes.sorted().forEach { q ->
                    val shape = RoundedCornerShape(AppShapes.tile)
                    Row(
                        Modifier.fillMaxWidth().clip(shape).background(c.surface).border(1.dp, c.glassBorder, shape).padding(16.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        PhIcon(PhIcons.Quotes, size = 20.dp, tint = c.primary, weight = PhWeight.Fill)
                        Text(q, style = AppTheme.textStyles.bodyMedium.copy(lineHeight = 21.sp), color = c.textPrimary, modifier = Modifier.weight(1f))
                        PhIcon(
                            PhIcons.X, size = 16.dp, tint = c.textTertiary, weight = PhWeight.Bold,
                            modifier = Modifier.clip(CircleShape).clickable(onClickLabel = "Remove quote") { onRemove(q) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileCard(userName: String, onEdit: () -> Unit, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    val shape = RoundedCornerShape(AppShapes.card)
    val initial = userName.trim().firstOrNull()?.uppercaseChar()?.toString()
    Row(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (c.isDark) Brush.linearGradient(listOf(c.primary.copy(alpha = 0.09f), c.surface)) else Brush.linearGradient(listOf(c.surface, c.surface)))
            .border(1.dp, c.glassBorder, shape)
            .clickable(onClickLabel = "Edit name", onClick = onEdit)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(54.dp)
                .glow(c.primary.copy(alpha = 0.13f), 24.dp, 27.dp, enabled = c.isDark)
                .clip(CircleShape)
                .background(if (c.isDark) Brush.radialGradient(listOf(c.primary.copy(alpha = 0.25f), c.surface)) else Brush.linearGradient(listOf(c.primary.copy(alpha = 0.09f), c.primary.copy(alpha = 0.09f))))
                .border(1.dp, c.glassBorder, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (initial != null) Text(initial, style = AppTheme.textStyles.titleLarge.copy(fontSize = 22.sp), color = c.primary)
            else PhIcon(PhIcons.User, size = 24.dp, tint = c.primary, weight = PhWeight.Bold)
        }
        HSpace(14.dp)
        Column(Modifier.weight(1f)) {
            Text(userName.ifBlank { "Add your name" }, style = AppTheme.textStyles.titleMedium.copy(letterSpacing = (-0.2).sp),
                fontWeight = FontWeight.SemiBold, color = c.textPrimary)
            Text("Glowstreak member · glowing daily", style = AppTheme.textStyles.bodySmall, color = c.textTertiary)
        }
        PhIcon(PhIcons.PencilSimple, size = 18.dp, tint = c.textTertiary)
    }
}

@Composable
private fun SettingsGroup(label: String, content: @Composable ColumnScope.() -> Unit) {
    val c = AppTheme.colors
    val shape = RoundedCornerShape(AppShapes.card)
    Column(Modifier.padding(bottom = 22.dp)) {
        HfSectionLabel(label, Modifier.padding(start = 4.dp), bottom = 8.dp)
        Column(
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(c.surface)
                .border(1.dp, c.glassBorder, shape),
            content = content,
        )
    }
}

@Composable
private fun SettingsRow(
    icon: String,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    first: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
) {
    val c = AppTheme.colors
    if (!first) HorizontalDivider(color = c.divider, thickness = 1.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PhIcon(icon, size = 20.dp, tint = c.textSecondary, weight = PhWeight.Bold)
        Column(Modifier.weight(1f)) {
            Text(title, style = AppTheme.textStyles.bodyLarge.copy(fontSize = 15.sp), color = c.textPrimary)
            if (subtitle != null) Text(subtitle, style = AppTheme.textStyles.bodySmall, color = c.textTertiary, modifier = Modifier.padding(top = 1.dp))
        }
        if (trailing != null) trailing() else PhIcon(PhIcons.CaretRight, size = 18.dp, tint = c.textTertiary)
    }
}

@Composable
private fun EditNameSheet(initial: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    val c = AppTheme.colors
    var name by remember { mutableStateOf(initial) }
    HfBottomSheet(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(start = 22.dp, end = 22.dp, top = 6.dp, bottom = 22.dp)) {
            Text("Your name", style = AppTheme.textStyles.titleLarge.copy(fontSize = 21.sp), color = c.textPrimary)
            Text("We’ll use it to greet you every morning.", style = AppTheme.textStyles.bodyMedium, color = c.textSecondary,
                modifier = Modifier.padding(top = 6.dp, bottom = 18.dp))
            HfField(value = name, onValueChange = { name = it }, placeholder = "Your name", icon = PhIcons.User,
                capitalization = KeyboardCapitalization.Words)
            HfButton("Save", onClick = { onSave(name) }, icon = PhIcons.Check, fullWidth = true, modifier = Modifier.padding(top = 20.dp))
        }
    }
}

@Composable
private fun QuietHoursSheet(initial: QuietHours, onDismiss: () -> Unit, onSave: (QuietHours) -> Unit) {
    val c = AppTheme.colors
    var value by remember { mutableStateOf(initial) }
    var picking by remember { mutableStateOf<String?>(null) } // "start" | "end"
    HfBottomSheet(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 22.dp, end = 22.dp, top = 6.dp, bottom = 22.dp)) {
            Text("Quiet hours", style = AppTheme.textStyles.titleLarge.copy(fontSize = 21.sp), color = c.textPrimary)
            Text("Reminders that fall inside this window stay silent. Your schedule isn’t changed.",
                style = AppTheme.textStyles.bodyMedium, color = c.textSecondary, modifier = Modifier.padding(top = 6.dp, bottom = 18.dp))
            val shape = RoundedCornerShape(AppShapes.field)
            Column(Modifier.fillMaxWidth().clip(shape).background(c.fieldFill).border(1.dp, c.glassBorder, shape)) {
                Row(Modifier.fillMaxWidth().padding(16.dp, 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    PhIcon(PhIcons.MoonStars, size = 20.dp, tint = if (value.enabled) c.primary else c.textTertiary, weight = PhWeight.Bold)
                    HSpace(12.dp)
                    Text("Silence reminders", style = AppTheme.textStyles.bodyLarge.copy(fontSize = 15.sp), color = c.textPrimary, modifier = Modifier.weight(1f))
                    HfToggle(value.enabled, { value = value.copy(enabled = it) })
                }
                listOf("start" to "From", "end" to "Until").forEach { (key, label) ->
                    HorizontalDivider(color = c.divider, thickness = 1.dp)
                    Row(
                        Modifier.fillMaxWidth().clickable(enabled = value.enabled) { picking = key }.padding(16.dp, 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(label, style = AppTheme.textStyles.bodyMedium, color = c.textSecondary, modifier = Modifier.weight(1f))
                        Text(
                            HabitUtil.formatTime12h(if (key == "start") value.start else value.end),
                            style = AppTheme.textStyles.titleMedium.copy(fontSize = 17.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = if (value.enabled) c.primary else c.textTertiary,
                            textAlign = TextAlign.End,
                        )
                    }
                }
            }
            HfButton("Save", onClick = { onSave(value) }, icon = PhIcons.Check, fullWidth = true, modifier = Modifier.padding(top = 20.dp))
        }
    }
    picking?.let { key ->
        HfTimePickerDialog(
            title = if (key == "start") "Quiet from" else "Quiet until",
            initial = if (key == "start") value.start else value.end,
            onDismiss = { picking = null },
            onConfirm = {
                value = if (key == "start") value.copy(start = it) else value.copy(end = it)
                picking = null
            },
        )
    }
}
