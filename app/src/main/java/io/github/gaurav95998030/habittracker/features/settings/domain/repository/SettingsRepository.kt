package io.github.gaurav95998030.habittracker.features.settings.domain.repository

import io.github.gaurav95998030.habittracker.features.settings.domain.model.QuietHours
import io.github.gaurav95998030.habittracker.features.settings.domain.model.ThemePreference
import kotlinx.coroutines.flow.Flow

/**
 * Persistence contract for app-wide user preferences. Reads are reactive (Flow)
 * so UI recomposes on change; writes are suspend so callers commit on a coroutine.
 *
 * `notificationsEnabled` is the **master switch** for habit reminders — it gates
 * every AlarmManager scheduling site (HabitViewModel.saveHabit, BootReceiver,
 * SettingsViewModel toggle handler). Per-habit `reminderEnabled` is independent;
 * a habit only fires when both flags are true. [quietHours] additionally
 * suppresses (but does not unschedule) reminders inside its window.
 */
interface SettingsRepository {
    val themePreference: Flow<ThemePreference>
    val notificationsEnabled: Flow<Boolean>

    /** Display name captured in onboarding ("" when skipped). */
    val userName: Flow<String>

    /** Whether onboarding has been finished (or skipped for a pre-existing install). */
    val onboardingCompleted: Flow<Boolean>

    /** Focus areas picked in onboarding; float to the front of the category list. */
    val focusCategories: Flow<Set<String>>

    /** Whether the one-time home-screen-widget nudge has been shown. */
    val widgetPromoSeen: Flow<Boolean>

    val quietHours: Flow<QuietHours>

    suspend fun setThemePreference(value: ThemePreference)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setUserName(name: String)
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setFocusCategories(categories: Set<String>)
    suspend fun setWidgetPromoSeen(seen: Boolean)
    suspend fun setQuietHours(value: QuietHours)
}
