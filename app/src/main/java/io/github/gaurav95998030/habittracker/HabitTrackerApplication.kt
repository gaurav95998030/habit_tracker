package io.github.gaurav95998030.habittracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.github.gaurav95998030.habittracker.features.reminders.HabitReminderScheduler
import javax.inject.Inject

@HiltAndroidApp
class HabitTrackerApplication : Application() {

    @Inject lateinit var reminderScheduler: HabitReminderScheduler

    override fun onCreate() {
        super.onCreate()
        // Keep the daily "missed yesterday?" check armed (idempotent; the receiver
        // checks the master switch and quiet hours when it fires).
        reminderScheduler.scheduleMissCheck()
    }
}
