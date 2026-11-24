package io.github.gaurav95998030.habittracker.features.habits.presentation.states

import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit

/**
 * Which heatmap representation each habit tile renders on the home list.
 * [Week] shows the rounded 7-day strip from the new "Today" design.
 * [Year] keeps the dense yearly heatmap from the original tile.
 */
enum class HeatmapViewMode { Week, Year }

const val ALL_CATEGORIES = "All"

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
    val pendingDetailHabitId: String? = null,
    // Set when another screen (e.g. "Why you miss") wants Home to open the edit sheet.
    val pendingEditHabitId: String? = null,
    val heatmapViewMode: HeatmapViewMode = HeatmapViewMode.Week,
    val selectedCategory: String = ALL_CATEGORIES,
)
