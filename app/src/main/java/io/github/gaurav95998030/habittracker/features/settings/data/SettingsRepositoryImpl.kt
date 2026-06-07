package io.github.gaurav95998030.habittracker.features.settings.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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

    override suspend fun setThemePreference(value: ThemePreference) {
        dataStore.edit { it[Keys.Theme] = value.name }
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.NotificationsEnabled] = enabled }
    }

    private object Keys {
        val Theme = stringPreferencesKey("theme_preference")
        val NotificationsEnabled = booleanPreferencesKey("notifications_enabled")
    }
}
