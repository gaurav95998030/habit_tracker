package io.github.gaurav95998030.habittracker.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.gaurav95998030.habittracker.features.habits.domain.repository.HabitRepository
import io.github.gaurav95998030.habittracker.features.reminders.HabitReminderScheduler
import io.github.gaurav95998030.habittracker.features.settings.domain.model.ThemePreference
import io.github.gaurav95998030.habittracker.features.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val habitRepository: HabitRepository,
    private val reminderScheduler: HabitReminderScheduler,
) : ViewModel() {

    val themePreference: StateFlow<ThemePreference> =
        settingsRepository.themePreference.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ThemePreference.Default,
        )

    val notificationsEnabled: StateFlow<Boolean> =
        settingsRepository.notificationsEnabled.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = true,
        )

    fun onThemeSelected(theme: ThemePreference) {
        viewModelScope.launch {
            settingsRepository.setThemePreference(theme)
        }
    }

    /**
     * Master switch handler.
     *
     * Persisting the flag is not enough — the AlarmManager has already-armed
     * intents that will fire regardless of any in-memory boolean. So when the
     * user flips OFF we explicitly `cancel(habitId)` every habit that had a
     * reminder armed, and when flipping ON we `schedule(habit)` them back.
     *
     * Per-habit `reminderEnabled` is preserved on the entity so we can restore
     * the exact set when the user toggles back on.
     */
    fun onNotificationsEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
            val habitsWithReminder = habitRepository.getHabitsWithReminder()
            if (enabled) {
                reminderScheduler.rescheduleAll(habitsWithReminder)
            } else {
                habitsWithReminder.forEach { reminderScheduler.cancel(it.id) }
            }
        }
    }
}
