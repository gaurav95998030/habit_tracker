package io.github.gaurav95998030.habittracker.features.habits.presentation.habit_list.organisms

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.gaurav95998030.habittracker.features.habits.HabitUtil
import io.github.gaurav95998030.habittracker.features.habits.presentation.habit_list.molecules.AlertsToggleField
import io.github.gaurav95998030.habittracker.features.habits.presentation.habit_list.molecules.CategorySelector
import io.github.gaurav95998030.habittracker.features.habits.presentation.habit_list.molecules.HabitColorPicker
import io.github.gaurav95998030.habittracker.features.habits.presentation.habit_list.molecules.ReminderTimeField
import io.github.gaurav95998030.habittracker.features.habits.presentation.viewmodel.HabitViewModel

private val SheetBackground = Color(0xFF000000)
private val FieldBackground = Color(0xFF161616)
private val FieldBorder = Color(0xFF242424)
private val LimeAccent = Color(0xFFC9FF3E)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFFD7D7D7)
private val TextPlaceholder = Color(0xFF6E6E6E)
private val ErrorRed = Color(0xFFFF6B6B)

private val habitColors = listOf(
    // Row 1
    Color(0xFFC9FF3E), // lime
    Color(0xFF00D9FF), // cyan
    Color(0xFFE0B3E8), // lavender
    Color(0xFFFFAFA0), // coral
    Color(0xFFFFA94D), // orange
    Color(0xFF87E5F0), // sky cyan
    Color(0xFFD6F58A), // pale green
    Color(0xFF7FB8FF), // sky blue
    // Row 2
    Color(0xFFFF6FA6), // hot pink
    Color(0xFFFFE066), // yellow
    Color(0xFFBD8FFF), // purple
    Color(0xFF4DE8C0), // teal
    Color(0xFF98F5C0), // mint
    Color(0xFFFFD1A1), // peach
    Color(0xFF8C95FF), // indigo
    Color(0xFFFF5CD4)  // magenta
)

private val categories = listOf(
    "Health", "Work", "Focus", "Social",
    "Fitness", "Workout", "Diet", "Hydration",
    "Sleep", "Meditation", "Mindfulness", "Mental Health",
    "Study", "Learning", "Reading", "Skill Building",
    "Productivity", "Career", "Time Management",
    "Finance", "Budgeting", "Savings",
    "Routine", "Morning Routine", "Night Routine",
    "Self Care", "Personal Growth", "Gratitude", "Journaling",
    "Yoga", "Walking", "Steps",
    "Habits", "Lifestyle", "Other"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitSheet(
    sheetState: SheetState,
    viewModel: HabitViewModel,
    onDismiss: () -> Unit,
) {
    val keyboardOpen = isKeyboardOpen()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LightStatusBarEffect()

    BackHandler {
        if (keyboardOpen) {
            keyboardController?.hide()
            focusManager.clearFocus()
        } else {
            onDismiss()
        }
    }

    val formState = viewModel.formState
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isEditing = formState.editingHabitId != null

    LaunchedEffect(Unit) {
        if (formState.color.isEmpty()) {
            viewModel.onColorSelected(HabitUtil.colorToHex(habitColors[0]))
        }
    }

    val selectedColor = remember(formState.color) {
        if (formState.color.isNotEmpty()) {
            runCatching { HabitUtil.hexToColor(formState.color) }.getOrNull() ?: habitColors[0]
        } else habitColors[0]
    }
    val categorySelected = formState.category.takeIf { it.isNotEmpty() }
    val isSaving = uiState.isAddingHabit == true
    val canSave = formState.name.isNotBlank() && !isSaving

    val scrollState = rememberScrollState()
    Column(
        Modifier
            .fillMaxSize()
            .background(SheetBackground)
            .imePadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEditing) "Edit Habit" else "New Habit",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1E1E))
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "close",
                    modifier = Modifier.size(18.dp),
                    tint = TextSecondary
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            FieldLabel("Habit Name")
            Spacer(Modifier.height(10.dp))
            DarkTextField(
                value = formState.name,
                onValueChange = viewModel::onNameChanged,
                placeholder = "e.g. Morning Meditation",
                singleLine = true,
                minHeight = 56.dp
            )
            if (formState.nameError.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = formState.nameError,
                    color = ErrorRed,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(20.dp))

            FieldLabel("Description (Optional)")
            Spacer(Modifier.height(10.dp))
            DarkTextField(
                value = formState.description,
                onValueChange = viewModel::onDescriptionChanged,
                placeholder = "Why are you doing this?",
                singleLine = false,
                minHeight = 88.dp
            )

            Spacer(Modifier.height(24.dp))

            FieldLabel("Category")
            Spacer(Modifier.height(12.dp))
            CategorySelector(
                categories = categories,
                selectedCategory = categorySelected,
                onCategorySelected = viewModel::onCategoryChanged,
                accentColor = selectedColor
            )

            Spacer(Modifier.height(24.dp))

            FieldLabel("Visual Theme")
            Spacer(Modifier.height(12.dp))
            HabitColorPicker(
                colors = habitColors,
                selectedColor = selectedColor,
                onColorSelected = { color ->
                    viewModel.onColorSelected(HabitUtil.colorToHex(color))
                }
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    FieldLabel("Reminder Time")
                    Spacer(Modifier.height(10.dp))
                    ReminderTimeField(
                        time = formState.reminderTime,
                        onTimeSelected = viewModel::onReminderTimeChanged
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    FieldLabel("Alerts")
                    Spacer(Modifier.height(10.dp))
                    AlertsToggleField(
                        enabled = formState.reminderEnabled,
                        onEnabledChanged = viewModel::onReminderEnabledChanged
                    )
                }
            }

            Spacer(Modifier.height(28.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SheetBackground)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            CreateHabitButton(
                text = when {
                    isSaving && isEditing -> "Saving"
                    isSaving -> "Creating"
                    isEditing -> "Save Habit"
                    else -> "Create Habit"
                },
                enabled = canSave,
                loading = isSaving,
                backgroundColor = selectedColor,
                onClick = { viewModel.saveHabit(onSaved = { onDismiss() }) }
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        color = TextPrimary,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun DarkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean,
    minHeight: androidx.compose.ui.unit.Dp
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        textStyle = TextStyle(
            color = TextPrimary,
            fontSize = 15.sp
        ),
        cursorBrush = SolidColor(LimeAccent),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .clip(RoundedCornerShape(14.dp))
            .background(FieldBackground)
            .border(1.dp, FieldBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp),
        decorationBox = { innerTextField ->
            Box(contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = TextPlaceholder,
                        fontSize = 15.sp
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun CreateHabitButton(
    text: String,
    enabled: Boolean,
    loading: Boolean,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    val bg = if (enabled) backgroundColor else Color(0xFF2A2A2A)
    val contentColor = if (enabled) Color.Black else Color(0xFF777777)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(bg)
            .clickable(enabled = enabled && !loading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(22.dp),
                color = contentColor
            )
        } else {
            Text(
                text = text,
                color = contentColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun isKeyboardOpen(): Boolean {
    val ime = WindowInsets.ime
    return ime.getBottom(LocalDensity.current) > 0
}

@Composable
private fun LightStatusBarEffect() {
    val view = LocalView.current
    DisposableEffect(view) {
        val activity = generateSequence<android.content.Context>(view.context) {
            (it as? android.content.ContextWrapper)?.baseContext
        }.firstOrNull { it is android.app.Activity } as? android.app.Activity
        val window = activity?.window
        if (window == null) {
            onDispose {}
        } else {
            val controller = androidx.core.view.WindowCompat.getInsetsController(window, view)
            val wasLight = controller.isAppearanceLightStatusBars
            controller.isAppearanceLightStatusBars = false
            onDispose { controller.isAppearanceLightStatusBars = wasLight }
        }
    }
}
