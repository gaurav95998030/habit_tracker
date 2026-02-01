package com.example.habit_tracker.features.habits.presentation.states

import com.example.habit_tracker.features.habits.domain.model.Habit

data class HabitState(
    val isLoading: Boolean = false,
    val items: List<String> = emptyList(),
    val errorMsg: String? = null,
    val isAddingHabit: Boolean? = null,
    val habit: Habit? = null,
    val habitFormState: HabitFormState? = null,
    val addHabitError: String? = null,
    val habits: List<Habit>? = emptyList()
)
