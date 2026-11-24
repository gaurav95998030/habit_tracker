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

    /** Story keys already viewed (e.g. "boost:2026-11-24", "week:2026-W48", "month:2026-10"). */
    val seenStories: Flow<Set<String>>

    /** Daily-boost quotes the user bookmarked (quote text). */
    val savedQuotes: Flow<Set<String>>

    /** Running starter plans: plan id → start date ("yyyy-MM-dd"). */
    val activePlans: Flow<Map<String, String>>

    /** Opt-in for anonymous usage analytics, crash reports and performance data (default on). */
    val diagnosticsEnabled: Flow<Boolean>

    /** "Today" card on the lock screen. Off until the user explicitly consents. */
    val lockScreenEnabled: Flow<Boolean>

    /** Whether the one-time lock-screen consent sheet has been shown. */
    val lockScreenPrompted: Flow<Boolean>

    /** "yyyy-MM-dd" the user swiped the card away; it stays hidden for the rest of that day. */
    val lockScreenDismissedOn: Flow<String?>

    suspend fun setThemePreference(value: ThemePreference)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setUserName(name: String)
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setFocusCategories(categories: Set<String>)
    suspend fun setWidgetPromoSeen(seen: Boolean)
    suspend fun setQuietHours(value: QuietHours)
    suspend fun markStorySeen(key: String)
    suspend fun setQuoteSaved(quote: String, saved: Boolean)
    /** Start (non-null [startDate]) or end (null) a starter plan. */
    suspend fun setPlan(planId: String, startDate: String?)
    suspend fun setDiagnosticsEnabled(enabled: Boolean)
    suspend fun setLockScreenEnabled(enabled: Boolean)
    suspend fun setLockScreenPrompted(prompted: Boolean)
    suspend fun setLockScreenDismissedOn(date: String?)
}
