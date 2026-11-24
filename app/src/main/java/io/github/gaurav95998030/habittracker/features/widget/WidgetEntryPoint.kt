package io.github.gaurav95998030.habittracker.features.widget

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.gaurav95998030.habittracker.features.habits.domain.repository.HabitRepository

/**
 * Bridge for reaching Hilt-provided singletons from components Hilt can't inject
 * directly — the widget's [android.widget.RemoteViewsService.RemoteViewsFactory],
 * which is instantiated by the framework, not by us.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun habitRepository(): HabitRepository
    fun settingsRepository(): io.github.gaurav95998030.habittracker.features.settings.domain.repository.SettingsRepository
}
