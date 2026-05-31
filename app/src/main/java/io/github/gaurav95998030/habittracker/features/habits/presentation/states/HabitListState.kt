package io.github.gaurav95998030.habittracker.features.habits.presentation.states

import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit

data class HabitState(
    val isLoading: Boolean = false,
    val items: List<String> = emptyList(),
    val errorMsg: String? = null,
    val isAddingHabit: Boolean? = null,
    val habit: Habit? = null,
    val habitFormState: HabitFormState? = null,
    val addHabitError: String? = null,
    val habits: List<Habit>? = emptyList(),
    // Set when a notification deep link or external trigger wants the detail
    // sheet for a specific habit to open. Consumed by the UI after the sheet shows.
    val pendingDetailHabitId: String? = null
)
