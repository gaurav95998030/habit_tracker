package io.github.gaurav95998030.habittracker.features.habits.presentation.states

import io.github.gaurav95998030.habittracker.features.habits.HabitCatalog
import io.github.gaurav95998030.habittracker.ui.theme.Accents

/**
 * Add/edit habit form. Defaults are the design's blank form: "Mind", Lime accent,
 * daily reminder on at 7:30 AM.
 */
data class HabitFormState(
    val name: String = "",
    val nameError: String = "",
    val description: String = "",
    val descriptionError: String = "",
    val category: String = HabitCatalog.DEFAULT_CATEGORY,
    val categoryError: String = "",
    val color: String = Accents.Lime.hex,
    val colorError: String = "",
    val isValid: Boolean = false,
    val editingHabitId: String? = null,
    val reminderEnabled: Boolean = true,
    // HH:mm 24h format, null when no time chosen yet
    val reminderTime: String? = HabitCatalog.DEFAULT_REMINDER_TIME,
)
