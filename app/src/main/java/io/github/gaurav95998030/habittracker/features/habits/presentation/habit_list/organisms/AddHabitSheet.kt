package io.github.gaurav95998030.habittracker.features.habits.presentation.habit_list.organisms

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import io.github.gaurav95998030.habittracker.commons.ui.HfBottomSheet
import io.github.gaurav95998030.habittracker.commons.ui.HfButton
import io.github.gaurav95998030.habittracker.commons.ui.HfButtonVariant
import io.github.gaurav95998030.habittracker.commons.ui.HfChip
import io.github.gaurav95998030.habittracker.commons.ui.HfField
import io.github.gaurav95998030.habittracker.commons.ui.HfIconButton
import io.github.gaurav95998030.habittracker.commons.ui.HfSectionLabel
import io.github.gaurav95998030.habittracker.commons.ui.HfSwatches
import io.github.gaurav95998030.habittracker.commons.ui.HfTimePickerDialog
import io.github.gaurav95998030.habittracker.commons.ui.HfToggle
import io.github.gaurav95998030.habittracker.commons.ui.PhIcon
import io.github.gaurav95998030.habittracker.commons.ui.PhWeight
import io.github.gaurav95998030.habittracker.features.habits.HabitCatalog
import io.github.gaurav95998030.habittracker.features.habits.HabitUtil
import io.github.gaurav95998030.habittracker.features.habits.presentation.states.HabitFormState
import io.github.gaurav95998030.habittracker.ui.theme.Accents
import io.github.gaurav95998030.habittracker.ui.theme.AppShapes
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme
import io.github.gaurav95998030.habittracker.ui.theme.PhIcons

/** Callbacks from the form into HabitViewModel. */
data class HabitFormActions(
    val onNameChanged: (String) -> Unit,
    val onDescriptionChanged: (String) -> Unit,
    val onCategoryChanged: (String) -> Unit,
    val onColorSelected: (String) -> Unit,
    val onReminderEnabledChanged: (Boolean) -> Unit,
    val onReminderTimeSelected: (String) -> Unit,
    val onSave: () -> Unit,
)

/**
 * New / edit habit bottom sheet: name, description, category, accent, daily reminder.
 * Driven entirely by [form] (HabitViewModel.formState).
 */
@Composable
fun AddHabitSheet(
    form: HabitFormState,
    focusCategories: Set<String>,
    isSaving: Boolean,
    actions: HabitFormActions,
    onDismiss: () -> Unit,
) {
    val c = AppTheme.colors
    val editing = form.editingHabitId != null
    val accent = Accents.colorOf(form.color)
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    fun enableReminder(enabled: Boolean) {
        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        actions.onReminderEnabledChanged(enabled)
    }

    // Focus areas picked in onboarding float to the front; a legacy category on an
    // edited habit is kept as an extra chip so editing never silently changes it.
    val categories = remember(focusCategories, form.category) {
        val ordered = HabitCatalog.categories.sortedBy { if (it.name in focusCategories) 0 else 1 }
        if (form.category.isNotBlank() && ordered.none { it.name == form.category })
            ordered + io.github.gaurav95998030.habittracker.features.habits.HabitCategory(form.category, HabitCatalog.categoryIcon(form.category))
        else ordered
    }

    HfBottomSheet(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth().imePadding()) {
            Row(
                Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    if (editing) "Edit habit" else "New habit",
                    style = AppTheme.textStyles.titleLarge.copy(fontSize = 21.sp, letterSpacing = (-0.4).sp),
                    color = c.textPrimary,
                )
                HfIconButton(PhIcons.X, onClick = onDismiss, contentDescription = "Close")
            }

            Column(
                Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                HfField(
                    value = form.name,
                    onValueChange = actions.onNameChanged,
                    label = "Habit name",
                    placeholder = "e.g. Morning run",
                    icon = PhIcons.PencilSimple,
                )
                HfField(
                    value = form.description,
                    onValueChange = actions.onDescriptionChanged,
                    label = "Description",
                    placeholder = "Optional — why does this matter?",
                    icon = PhIcons.TextAlignLeft,
                    multiline = true,
                )
                Column {
                    HfSectionLabel("Category")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        categories.forEach { cat ->
                            HfChip(
                                label = cat.name,
                                icon = cat.icon,
                                active = form.category == cat.name,
                                accent = accent,
                                onClick = { actions.onCategoryChanged(cat.name) },
                            )
                        }
                    }
                }
                Column {
                    HfSectionLabel("Accent color")
                    HfSwatches(selectedHex = form.color, onSelect = actions.onColorSelected)
                }
                Column {
                    HfSectionLabel("Reminder")
                    ReminderCard(
                        enabled = form.reminderEnabled,
                        time = form.reminderTime ?: HabitCatalog.DEFAULT_REMINDER_TIME,
                        accent = accent,
                        onEnabledChange = ::enableReminder,
                        onTimeSelected = actions.onReminderTimeSelected,
                    )
                }
            }

            HorizontalDivider(color = c.divider, thickness = 1.dp)
            Row(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 22.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HfButton("Cancel", onClick = onDismiss, variant = HfButtonVariant.Secondary)
                HfButton(
                    text = if (editing) "Save changes" else "Create habit",
                    onClick = actions.onSave,
                    icon = if (editing) PhIcons.Check else PhIcons.Plus,
                    enabled = form.name.isNotBlank() && !isSaving,
                    modifier = Modifier.weight(1f),
                    fullWidth = true,
                )
            }
        }
    }
}

@Composable
private fun ReminderCard(
    enabled: Boolean,
    time: String,
    accent: Color,
    onEnabledChange: (Boolean) -> Unit,
    onTimeSelected: (String) -> Unit,
) {
    val c = AppTheme.colors
    var showTimes by rememberSaveable { mutableStateOf(false) }
    var showCustom by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(AppShapes.field)
    val caret by animateFloatAsState(if (showTimes) 180f else 0f, label = "caret")

    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(c.fieldFill)
            .border(1.dp, c.glassBorder, shape),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PhIcon(PhIcons.Bell, size = 20.dp, tint = if (enabled) accent else c.textTertiary, weight = PhWeight.Bold)
            Text("Daily reminder", style = AppTheme.textStyles.bodyLarge.copy(fontSize = 15.sp), color = c.textPrimary, modifier = Modifier.weight(1f))
            HfToggle(checked = enabled, onCheckedChange = onEnabledChange, accent = accent)
        }
        AnimatedVisibility(enabled) {
            Column {
                HorizontalDivider(color = c.divider, thickness = 1.dp)
                Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Row(
                        Modifier.fillMaxWidth().clickable { showTimes = !showTimes },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Time", style = AppTheme.textStyles.bodyMedium, color = c.textSecondary)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                HabitUtil.formatTime12h(time),
                                style = AppTheme.textStyles.titleMedium.copy(fontSize = 17.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = accent,
                            )
                            PhIcon(PhIcons.CaretDown, size = 14.dp, tint = c.textTertiary, weight = PhWeight.Bold, modifier = Modifier.rotate(caret))
                        }
                    }
                    AnimatedVisibility(showTimes) {
                        val custom = time !in HabitCatalog.quickTimes
                        FlowRow(
                            Modifier.padding(top = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            HabitCatalog.quickTimes.forEach { t ->
                                TimeChip(HabitUtil.formatTime12h(t), selected = t == time, accent = accent) { onTimeSelected(t) }
                            }
                            TimeChip(
                                if (custom) "${HabitUtil.formatTime12h(time)} ✎" else "Custom…",
                                selected = custom, accent = accent,
                            ) { showCustom = true }
                        }
                    }
                }
            }
        }
    }

    if (showCustom) {
        HfTimePickerDialog(
            title = "Reminder time",
            initial = time,
            onDismiss = { showCustom = false },
            onConfirm = { onTimeSelected(it); showCustom = false },
        )
    }
}

@Composable
private fun TimeChip(label: String, selected: Boolean, accent: Color, onClick: () -> Unit) {
    val c = AppTheme.colors
    val shape = RoundedCornerShape(10.dp)
    Box(
        Modifier
            .clip(shape)
            .background(if (selected) accent.copy(alpha = 0.13f) else Color.Transparent)
            .border(1.dp, if (selected) accent else c.glassBorder, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(label, style = AppTheme.textStyles.labelLarge.copy(fontSize = 13.sp), color = if (selected) accent else c.textSecondary)
    }
}
