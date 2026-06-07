package io.github.gaurav95998030.habittracker.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.gaurav95998030.habittracker.features.settings.data.SettingsRepositoryImpl
import io.github.gaurav95998030.habittracker.features.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

private const val DATASTORE_FILE_NAME = "habit_tracker_preferences.preferences_pb"

@Module
@InstallIn(SingletonComponent::class)
object SettingsModule {

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
            produceFile = { context.preferencesDataStoreFile() },
        )
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        dataStore: DataStore<Preferences>,
    ): SettingsRepository = SettingsRepositoryImpl(dataStore)

    private fun Context.preferencesDataStoreFile() =
        java.io.File(filesDir, "datastore/$DATASTORE_FILE_NAME")
}
