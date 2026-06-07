package io.github.gaurav95998030.habittracker.features.settings.domain.repository

import io.github.gaurav95998030.habittracker.features.settings.domain.model.ThemePreference
import kotlinx.coroutines.flow.Flow

/**
 * Persistence contract for app-wide user preferences. Reads are reactive (Flow)
 * so UI recomposes on change; writes are suspend so callers commit on a coroutine.
 *
 * `notificationsEnabled` is the **master switch** for habit reminders — it gates
 * every AlarmManager scheduling site (HabitViewModel.saveHabit, BootReceiver,
 * SettingsViewModel toggle handler). Per-habit `reminderEnabled` is independent;
 * a habit only fires when both flags are true.
 */
interface SettingsRepository {
    val themePreference: Flow<ThemePreference>
    val notificationsEnabled: Flow<Boolean>

    suspend fun setThemePreference(value: ThemePreference)
    suspend fun setNotificationsEnabled(enabled: Boolean)
}
