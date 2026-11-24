package io.github.gaurav95998030.habittracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.github.gaurav95998030.habittracker.commons.CrashReporter
import io.github.gaurav95998030.habittracker.features.reminders.HabitReminderScheduler
import javax.inject.Inject

@HiltAndroidApp
class HabitTrackerApplication : Application() {

    @Inject lateinit var reminderScheduler: HabitReminderScheduler

    override fun onCreate() {
        super.onCreate()
        // Tag every Crashlytics report with the build flavour so dev noise is easy to
        // filter out (dev and prod are also separate Firebase apps).
        CrashReporter.setKey("environment", BuildConfig.ENVIRONMENT)
        CrashReporter.setKey("build_type", BuildConfig.BUILD_TYPE)
        // Keep the daily "missed yesterday?" check armed (idempotent; the receiver
        // checks the master switch and quiet hours when it fires).
        reminderScheduler.scheduleMissCheck()
    }
}
