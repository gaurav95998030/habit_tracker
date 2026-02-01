package com.example.habit_tracker.features.habits.presentation.states

data class HabitFormState(
    val name: String = "",
    val nameError:String ="",
    val description: String = "",
    val descriptionError: String = "",
    val category: String = "",
    val categoryError: String = "",
    val color: String = "", // or Int
    val colorError: String = "", // or Int
    val isValid: Boolean = false
)
