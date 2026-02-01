package com.example.habit_tracker.features.habits.presentation.habit_list.organisms

import AppTextFied
import android.widget.Space
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.habit_tracker.commons.ui.AppButton
import com.example.habit_tracker.commons.ui.VerticalSpace
import com.example.habit_tracker.features.habits.presentation.habit_list.molecules.CategorySelector
import com.example.habit_tracker.features.habits.presentation.habit_list.molecules.HabitColorPicker
import com.example.habit_tracker.features.habits.presentation.viewmodel.HabitViewModel
import com.example.habit_tracker.ui.theme.AppTheme
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.habit_tracker.features.habits.HabitUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun AddHabitSheet(
    sheetState : SheetState,
    viewModel: HabitViewModel,
    onDismiss: () -> Unit,

){
    val habitColors = listOf(
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
    val categories = listOf(
        "Health",
        "Fitness",
        "Workout",
        "Diet",
        "Hydration",
        "Sleep",
        "Meditation",
        "Mindfulness",
        "Mental Health",

        "Study",
        "Learning",
        "Reading",
        "Skill Building",

        "Work",
        "Productivity",
        "Career",
        "Time Management",

        "Finance",
        "Budgeting",
        "Savings",

        "Routine",
        "Morning Routine",
        "Night Routine",

        "Self Care",
        "Personal Growth",
        "Gratitude",
        "Journaling",

        "Yoga",
        "Walking",
        "Steps",

        "Habits",      // 👈 added as requested
        "Lifestyle",
        "Other"
    )
    var selectedIndex by rememberSaveable { mutableStateOf(0) }
    var categorySelected by rememberSaveable { mutableStateOf<String?>(null) }

    val selectedColor = habitColors[selectedIndex]
    val scrollState = rememberScrollState()
    val formState = viewModel.formState
    val uiState by viewModel.uiState.collectAsStateWithLifecycle();
    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)   // 👈 enables scrolling when needed
            .imePadding()
            .padding(horizontal = 16.dp)
    ) {
        Row(

        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "close",
                modifier = Modifier.clickable(
                    onClick = {
                        onDismiss()
                    }
                ),
                tint = AppTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.width(16.dp))

            Text("Add a Habit")
        }
        VerticalSpace(16.dp)
        Text("Habit Name")
        AppTextFied(
            value = formState.name,
            onValueChange = {
                viewModel.onNameChanged(it)
            },
            label = "",
            modifier = Modifier
        )
        VerticalSpace(16.dp)
        Text("Description")
        AppTextFied(
            value = formState.description,
            onValueChange = {
                viewModel.onDescriptionChanged(it)
            },
            label = "",
            modifier = Modifier
        )
        VerticalSpace(16.dp)
        Text("Color")
        VerticalSpace(4.dp)
        HabitColorPicker(
            colors = habitColors,
            selectedColor = selectedColor,
            onColorSelected = { color ->
                selectedIndex = habitColors.indexOf(color)
                viewModel.onColorSelected(HabitUtil.colorToHex(color))
                // <-- you receive selected color here
            }
        )
        VerticalSpace(16.dp)
        Text("Category")
        VerticalSpace(4.dp)
        CategorySelector(
            categories = categories,
            selectedCategory = categorySelected,
            onCategorySelected = {
                categorySelected = it
                viewModel.onCategoryChanged(it)

            } // <-- callback here
        )
        VerticalSpace(16.dp)
        AppButton(
            text =if (uiState.isAddingHabit==true) "Adding" else "Add Habit",
            onClick = {
                viewModel.addHabit(
                    onHabitAdded = {
                        onDismiss()
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

    }
}