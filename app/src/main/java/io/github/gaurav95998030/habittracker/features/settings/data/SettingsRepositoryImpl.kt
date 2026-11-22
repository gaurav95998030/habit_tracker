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
    }
}
