package io.github.gaurav95998030.habittracker.features.settings.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import io.github.gaurav95998030.habittracker.features.settings.domain.model.QuietHours
import io.github.gaurav95998030.habittracker.features.settings.domain.model.ThemePreference
import io.github.gaurav95998030.habittracker.features.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override val themePreference: Flow<ThemePreference> =
        dataStore.data.map { prefs ->
            ThemePreference.fromStorageValue(prefs[Keys.Theme])
        }

    override val notificationsEnabled: Flow<Boolean> =
        dataStore.data.map { prefs ->
            // Default ON so a fresh install matches user expectation that habit
            // reminders work out of the box.
            prefs[Keys.NotificationsEnabled] ?: true
        }

    override val userName: Flow<String> =
        dataStore.data.map { it[Keys.UserName].orEmpty() }

    override val onboardingCompleted: Flow<Boolean> =
        dataStore.data.map { it[Keys.OnboardingCompleted] ?: false }

    override val focusCategories: Flow<Set<String>> =
        dataStore.data.map { it[Keys.FocusCategories] ?: emptySet() }

    override val widgetPromoSeen: Flow<Boolean> =
        dataStore.data.map { it[Keys.WidgetPromoSeen] ?: false }

    override val quietHours: Flow<QuietHours> =
        dataStore.data.map { prefs ->
            QuietHours(
                enabled = prefs[Keys.QuietEnabled] ?: false,
                start = prefs[Keys.QuietStart] ?: QuietHours.DEFAULT_START,
                end = prefs[Keys.QuietEnd] ?: QuietHours.DEFAULT_END,
            )
        }

    override val seenStories: Flow<Set<String>> =
        dataStore.data.map { it[Keys.SeenStories] ?: emptySet() }

    override val savedQuotes: Flow<Set<String>> =
        dataStore.data.map { it[Keys.SavedQuotes] ?: emptySet() }

    override val diagnosticsEnabled: Flow<Boolean> =
        dataStore.data.map { it[Keys.DiagnosticsEnabled] ?: true }

    override suspend fun setDiagnosticsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.DiagnosticsEnabled] = enabled }
    }

    override val activePlans: Flow<Map<String, String>> =
        dataStore.data.map { prefs ->
            prefs[Keys.ActivePlans].orEmpty().mapNotNull { e ->
                e.split("|").takeIf { it.size == 2 }?.let { it[0] to it[1] }
            }.toMap()
        }

    override suspend fun setPlan(planId: String, startDate: String?) {
        dataStore.edit { prefs ->
            val others = prefs[Keys.ActivePlans].orEmpty().filterNot { it.startsWith("$planId|") }.toSet()
            prefs[Keys.ActivePlans] = if (startDate == null) others else others + "$planId|$startDate"
        }
    }

    override suspend fun markStorySeen(key: String) {
        dataStore.edit { prefs ->
            // Bound the set (old days/weeks are never shown again). Stored sets aren't
            // ordered, so trim the *others* and always keep the key just added.
            val others = (prefs[Keys.SeenStories].orEmpty() - key).toList().takeLast(59)
            prefs[Keys.SeenStories] = others.toSet() + key
        }
    }

    override suspend fun setQuoteSaved(quote: String, saved: Boolean) {
        dataStore.edit { prefs ->
            val cur = prefs[Keys.SavedQuotes].orEmpty()
            prefs[Keys.SavedQuotes] = if (saved) cur + quote else cur - quote
        }
    }

    override suspend fun setThemePreference(value: ThemePreference) {
        dataStore.edit { it[Keys.Theme] = value.name }
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.NotificationsEnabled] = enabled }
    }

    override suspend fun setUserName(name: String) {
        dataStore.edit { it[Keys.UserName] = name.trim() }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[Keys.OnboardingCompleted] = completed }
    }

    override suspend fun setFocusCategories(categories: Set<String>) {
        dataStore.edit { it[Keys.FocusCategories] = categories }
    }

    override suspend fun setWidgetPromoSeen(seen: Boolean) {
        dataStore.edit { it[Keys.WidgetPromoSeen] = seen }
    }

    override suspend fun setQuietHours(value: QuietHours) {
        dataStore.edit {
            it[Keys.QuietEnabled] = value.enabled
            it[Keys.QuietStart] = value.start
            it[Keys.QuietEnd] = value.end
        }
    }

    private object Keys {
        val Theme = stringPreferencesKey("theme_preference")
        val NotificationsEnabled = booleanPreferencesKey("notifications_enabled")
        val UserName = stringPreferencesKey("user_name")
        val OnboardingCompleted = booleanPreferencesKey("onboarding_completed")
        val FocusCategories = stringSetPreferencesKey("focus_categories")
        val WidgetPromoSeen = booleanPreferencesKey("widget_promo_seen")
        val QuietEnabled = booleanPreferencesKey("quiet_hours_enabled")
        val QuietStart = stringPreferencesKey("quiet_hours_start")
        val QuietEnd = stringPreferencesKey("quiet_hours_end")
        val SeenStories = stringSetPreferencesKey("seen_stories")
        val SavedQuotes = stringSetPreferencesKey("saved_quotes")
        val ActivePlans = stringSetPreferencesKey("active_plans")
        val DiagnosticsEnabled = booleanPreferencesKey("diagnostics_enabled")
    }
}
