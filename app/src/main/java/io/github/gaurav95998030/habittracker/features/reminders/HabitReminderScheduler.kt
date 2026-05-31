package io.github.gaurav95998030.habittracker.features.reminders

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.AlarmManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = ContextCompat.getSystemService(context, NotificationManager::class.java) ?: return
        if (nm.getNotificationChannel(ReminderConstants.CHANNEL_ID) != null) return

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val audioAttrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val channel = NotificationChannel(
            ReminderConstants.CHANNEL_ID,
            ReminderConstants.CHANNEL_NAME,
            // HIGH = heads-up banner + sound, more attention-grabbing than DEFAULT.
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = ReminderConstants.CHANNEL_DESCRIPTION
            enableLights(true)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 250, 250, 250)
            setSound(soundUri, audioAttrs)
            lockscreenVisibility = NotificationManager.IMPORTANCE_DEFAULT
            setShowBadge(true)
        }
        nm.createNotificationChannel(channel)
    }

    fun schedule(habit: Habit) {
        val time = habit.reminderTime ?: return
        if (!habit.reminderEnabled) return
        val (hour, minute) = parseTime(time) ?: return
        ensureNotificationChannel()

        val triggerAt = nextTriggerMillis(hour, minute)
        val pi = buildPendingIntent(habit, flags = pendingIntentFlags(mutable = false))

        // setAndAllowWhileIdle is inexact (~minute granularity) but doze-friendly and
        // does not require the SCHEDULE_EXACT_ALARM permission on Android 12+.
        AlarmManagerCompat.setAndAllowWhileIdle(
            alarmManager,
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            pi
        )
    }

    fun cancel(habitId: String) {
        val intent = Intent(context, HabitReminderReceiver::class.java).apply {
            action = ReminderConstants.ACTION_FIRE_REMINDER
        }
        val pi = PendingIntent.getBroadcast(
            context,
            habitId.hashCode(),
            intent,
            pendingIntentFlags(mutable = false) or PendingIntent.FLAG_NO_CREATE
        )
        if (pi != null) {
            alarmManager.cancel(pi)
            pi.cancel()
        }
    }

    fun rescheduleAll(habits: List<Habit>) {
        habits.filter { it.reminderEnabled && it.reminderTime != null }.forEach { schedule(it) }
    }

    private fun buildPendingIntent(habit: Habit, flags: Int): PendingIntent {
        val intent = Intent(context, HabitReminderReceiver::class.java).apply {
            action = ReminderConstants.ACTION_FIRE_REMINDER
            putExtra(ReminderConstants.EXTRA_HABIT_ID, habit.id)
            putExtra(ReminderConstants.EXTRA_HABIT_NAME, habit.name)
            putExtra(ReminderConstants.EXTRA_HABIT_DESCRIPTION, habit.description)
            putExtra(ReminderConstants.EXTRA_HABIT_COLOR, habit.color)
            putExtra(ReminderConstants.EXTRA_REMINDER_TIME, habit.reminderTime)
        }
        return PendingIntent.getBroadcast(
            context,
            habit.id.hashCode(),
            intent,
            flags or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun pendingIntentFlags(mutable: Boolean): Int {
        // FLAG_IMMUTABLE / FLAG_MUTABLE are available since API 23 (minSdk = 24 here).
        return if (mutable) PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_IMMUTABLE
    }

    private fun parseTime(value: String): Pair<Int, Int>? {
        val parts = value.split(":")
        if (parts.size != 2) return null
        val h = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        if (h !in 0..23 || m !in 0..59) return null
        return h to m
    }

    private fun nextTriggerMillis(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis
    }
}
