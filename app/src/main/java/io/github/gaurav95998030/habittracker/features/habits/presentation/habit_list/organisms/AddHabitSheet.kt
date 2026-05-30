package io.github.gaurav95998030.habittracker.features.habits.presentation.habit_list.organisms

import AppTextFied
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.gaurav95998030.habittracker.commons.ui.AppButton
import io.github.gaurav95998030.habittracker.commons.ui.VerticalSpace
import io.github.gaurav95998030.habittracker.features.habits.presentation.habit_list.molecules.CategorySelector
import io.github.gaurav95998030.habittracker.features.habits.presentation.habit_list.molecules.HabitColorPicker
import io.github.gaurav95998030.habittracker.features.habits.presentation.viewmodel.HabitViewModel
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.gaurav95998030.habittracker.features.habits.HabitUtil

private val habitColors = listOf(
    Color(0xFFFF6F61),
    Color(0xFFFFA726),
    Color(0xFFFFD54F),
    Color(0xFF9CCC65),
    Color(0xFF26A69A),
    Color(0xFF29B6F6),
    Color(0xFF42A5F5),
    Color(0xFF7E57C2),
    Color(0xFFBA68C8),
    Color(0xFFEC407A),
    Color(0xFF8D6E63),
    Color(0xFFBDBDBD),
    Color(0xFF9E9E9E)
)

private val categories = listOf(
    "Health", "Fitness", "Workout", "Diet", "Hydration",
    "Sleep", "Meditation", "Mindfulness", "Mental Health",
    "Study", "Learning", "Reading", "Skill Building",
    "Work", "Productivity", "Career", "Time Management",
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
    val colors = AppTheme.colors
    val textStyles = AppTheme.textStyles
    val sheetBackground = if (isSystemInDarkTheme()) colors.background else Color.White

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
            .statusBarsPadding()
            .background(sheetBackground)
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isEditing) "Edit Habit" else "New Habit",
                    style = textStyles.titleMedium.copy(
                        color = colors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isEditing) "Update the details below" else "Build a routine you can stick to",
                    style = textStyles.bodySmall.copy(color = colors.textTertiary)
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "close",
                    modifier = Modifier.size(20.dp),
                    tint = colors.textSecondary
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
            SectionLabel(text = "Habit name", required = true)
            VerticalSpace(8.dp)
            AppTextFied(
                value = formState.name,
                onValueChange = viewModel::onNameChanged,
                label = "",
                placeholder = "e.g. Drink 2L water",
                singleLine = true,
                isError = formState.nameError.isNotEmpty(),
                errorMessage = formState.nameError.takeIf { it.isNotEmpty() },
                modifier = Modifier
            )

            VerticalSpace(20.dp)

            SectionLabel(text = "Description")
            VerticalSpace(8.dp)
            AppTextFied(
                value = formState.description,
                onValueChange = viewModel::onDescriptionChanged,
                label = "",
                placeholder = "Why does this habit matter to you?",
                singleLine = false,
                modifier = Modifier
            )

            VerticalSpace(20.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionLabel(text = "Accent color")
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(selectedColor)
                )
            }
            VerticalSpace(10.dp)
            HabitColorPicker(
                colors = habitColors,
                selectedColor = selectedColor,
                onColorSelected = { color ->
                    viewModel.onColorSelected(HabitUtil.colorToHex(color))
                }
            )

            VerticalSpace(20.dp)

            SectionLabel(text = "Category")
            VerticalSpace(10.dp)
            CategorySelector(
                categories = categories,
                selectedCategory = categorySelected,
                onCategorySelected = viewModel::onCategoryChanged
            )

            VerticalSpace(24.dp)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(sheetBackground)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            AppButton(
                text = when {
                    isSaving && isEditing -> "Saving"
                    isSaving -> "Adding"
                    isEditing -> "Save Habit"
                    else -> "Add Habit"
                },
                onClick = {
                    viewModel.saveHabit(onSaved = { onDismiss() })
                },
                enabled = canSave,
                loading = isSaving,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SectionLabel(
    text: String,
    required: Boolean = false
) {
    val colors = AppTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = text,
            style = AppTheme.textStyles.labelMedium.copy(
                color = colors.textSecondary,
                fontWeight = FontWeight.Medium
            )
        )
        if (required) {
            Text(
                text = " *",
                style = AppTheme.textStyles.labelMedium.copy(color = colors.error)
            )
        }
    }
}

@Composable
fun isKeyboardOpen(): Boolean {
    val ime = WindowInsets.ime
    return ime.getBottom(LocalDensity.current) > 0
}
