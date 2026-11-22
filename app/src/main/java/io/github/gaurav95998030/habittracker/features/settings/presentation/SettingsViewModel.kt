package io.github.gaurav95998030.habittracker.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.gaurav95998030.habittracker.features.habits.domain.repository.HabitRepository
import io.github.gaurav95998030.habittracker.features.reminders.HabitReminderScheduler
import io.github.gaurav95998030.habittracker.features.settings.domain.model.QuietHours
import io.github.gaurav95998030.habittracker.features.settings.domain.model.ThemePreference
import io.github.gaurav95998030.habittracker.features.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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

    val userName: StateFlow<String> =
        settingsRepository.userName.stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val focusCategories: StateFlow<Set<String>> =
        settingsRepository.focusCategories.stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val widgetPromoSeen: StateFlow<Boolean> =
        settingsRepository.widgetPromoSeen.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val quietHours: StateFlow<QuietHours> =
        settingsRepository.quietHours.stateIn(viewModelScope, SharingStarted.Eagerly, QuietHours())

    private val _onboardingCompleted = MutableStateFlow<Boolean?>(null)

    /**
     * `null` while the first DataStore read is in flight (render nothing → no flash
     * of onboarding for returning users), then the persisted flag.
     */
    val onboardingCompleted: StateFlow<Boolean?> = _onboardingCompleted.asStateFlow()

    init {
        viewModelScope.launch {
            var completed = settingsRepository.onboardingCompleted.first()
            if (!completed) {
                // Installs that predate onboarding already have habits — don't make
                // them walk through the welcome flow after an update.
                val hasHabits = runCatching { habitRepository.getHabits().isNotEmpty() }.getOrDefault(false)
                if (hasHabits) {
                    settingsRepository.setOnboardingCompleted(true)
                    completed = true
                }
            }
            _onboardingCompleted.value = completed
            settingsRepository.onboardingCompleted.collect { _onboardingCompleted.value = it }
        }
    }

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
     * the exact set when the user toggles back on. Paused habits stay unarmed.
     */
    fun onNotificationsEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
            val habitsWithReminder = habitRepository.getHabitsWithReminder()
            if (enabled) {
                reminderScheduler.rescheduleAll(habitsWithReminder.filter { it.isActive })
            } else {
                habitsWithReminder.forEach { reminderScheduler.cancel(it.id) }
            }
        }
    }

    fun onUserNameChanged(name: String) {
        viewModelScope.launch { settingsRepository.setUserName(name) }
    }

    fun onQuietHoursChanged(value: QuietHours) {
        viewModelScope.launch { settingsRepository.setQuietHours(value) }
    }

    fun markWidgetPromoSeen() {
        viewModelScope.launch { settingsRepository.setWidgetPromoSeen(true) }
    }

    /** Persists the onboarding answers. The first habit is created by HabitViewModel. */
    fun completeOnboarding(name: String, categories: Set<String>) {
        viewModelScope.launch {
            settingsRepository.setUserName(name)
            settingsRepository.setFocusCategories(categories)
            settingsRepository.setOnboardingCompleted(true)
        }
    }

    /** Settings → "Replay onboarding". */
    fun replayOnboarding() {
        viewModelScope.launch { settingsRepository.setOnboardingCompleted(false) }
    }
}
