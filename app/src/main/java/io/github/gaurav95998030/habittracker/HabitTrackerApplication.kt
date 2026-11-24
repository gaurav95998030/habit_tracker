package io.github.gaurav95998030.habittracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.github.gaurav95998030.habittracker.commons.CrashReporter
import io.github.gaurav95998030.habittracker.features.reminders.HabitReminderScheduler
import io.github.gaurav95998030.habittracker.features.widget.HabitWidgetProvider
import io.github.gaurav95998030.habittracker.features.widget.TodayLockScreen
import io.github.gaurav95998030.habittracker.features.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class HabitTrackerApplication : Application() {

    @Inject lateinit var reminderScheduler: HabitReminderScheduler
    @Inject lateinit var settingsRepository: SettingsRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // Respect the user's "Share usage & crash data" choice (Settings → Privacy).
        appScope.launch {
            CrashReporter.setCollectionEnabled(this@HabitTrackerApplication, settingsRepository.diagnosticsEnabled.first())
        }
        // Tag every Crashlytics report with the build flavour so dev noise is easy to
        // filter out (dev and prod are also separate Firebase apps).
        CrashReporter.setKey("environment", BuildConfig.ENVIRONMENT)
        CrashReporter.setKey("build_type", BuildConfig.BUILD_TYPE)
        // Keep the daily "missed yesterday?" check armed (idempotent; the receiver
        // checks the master switch and quiet hours when it fires).
        reminderScheduler.scheduleMissCheck()
        // Roll the widget + lock-screen card over to the new day at midnight, and bring
        // both up to date now (habits may have changed while the process was dead).
        TodayLockScreen.scheduleMidnightRefresh(this)
        HabitWidgetProvider.refresh(this)
    }
}
