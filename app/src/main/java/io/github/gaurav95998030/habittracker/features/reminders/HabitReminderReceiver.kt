package io.github.gaurav95998030.habittracker.features.reminders

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import io.github.gaurav95998030.habittracker.MainActivity
import io.github.gaurav95998030.habittracker.R
import io.github.gaurav95998030.habittracker.features.habits.HabitUtil
import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit
import io.github.gaurav95998030.habittracker.features.habits.domain.repository.HabitRepository
import io.github.gaurav95998030.habittracker.features.widget.HabitWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import io.github.gaurav95998030.habittracker.features.settings.domain.repository.SettingsRepository
import io.github.gaurav95998030.habittracker.features.motivation.domain.MotivationInsights
import io.github.gaurav95998030.habittracker.features.motivation.domain.repository.MotivationRepository
import java.util.Calendar
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class HabitReminderReceiver : BroadcastReceiver() {

    @Inject lateinit var scheduler: HabitReminderScheduler
    @Inject lateinit var repository: HabitRepository
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var motivationRepository: MotivationRepository

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ReminderConstants.ACTION_FIRE_REMINDER -> handleFire(context, intent)
            ReminderConstants.ACTION_MARK_DONE -> handleMarkDone(context, intent)
            ReminderConstants.ACTION_SNOOZE -> handleSnooze(context, intent)
            ReminderConstants.ACTION_FIRE_SNOOZED -> handleFire(context, intent, rearmDaily = false)
            ReminderConstants.ACTION_MISS_CHECK -> handleMissCheck(context)
        }
    }

    /**
     * Fired at the scheduled time. We load the habit fresh from the repository so the
     * streak line reflects the current completion state, post the notification, and
     * re-arm tomorrow's alarm (daily repeat).
     */
    private fun handleFire(context: Context, intent: Intent, rearmDaily: Boolean = true) {
        val habitId = intent.getStringExtra(ReminderConstants.EXTRA_HABIT_ID) ?: return
        val fallbackName = intent.getStringExtra(ReminderConstants.EXTRA_HABIT_NAME).orEmpty()
        val fallbackColor = intent.getStringExtra(ReminderConstants.EXTRA_HABIT_COLOR).orEmpty()
        val reminderTime = intent.getStringExtra(ReminderConstants.EXTRA_REMINDER_TIME)

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val habit = runCatching { repository.getHabits() }
                    .getOrNull()
                    ?.firstOrNull { it.id == habitId }

                val name = habit?.name?.ifBlank { fallbackName } ?: fallbackName
                val colorHex = habit?.color?.ifBlank { fallbackColor } ?: fallbackColor
                val streak = habit?.let { currentStreakOf(it) } ?: 0
                val alreadyDone = habit?.completedDates?.contains(LocalDate.now().toString()) == true

                // Paused habits and the quiet-hours window suppress the post, but the
                // daily alarm still re-arms below so tomorrow's reminder survives.
                val paused = habit?.isActive == false
                val quiet = runCatching {
                    val now = Calendar.getInstance()
                    settingsRepository.quietHours.first()
                        .contains(now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE))
                }.getOrDefault(false)

                if (!alreadyDone && !paused && !quiet) {
                    showNotification(context, habitId, name, colorHex, streak)
                }

                // Re-schedule for tomorrow so the daily reminder keeps firing — unless
                // the habit loaded as paused or with its reminder off. (A failed load
                // keeps the old behaviour and re-arms with the intent's fallbacks.)
                val stillWanted = habit == null || (habit.isActive && habit.reminderEnabled)
                if (rearmDaily && reminderTime != null && stillWanted) {
                    scheduler.schedule(
                        Habit(
                            id = habitId,
                            userId = "",
                            name = name,
                            description = habit?.description.orEmpty(),
                            completedDates = emptyList(),
                            color = colorHex,
                            category = "",
                            createdDate = "",
                            updatedDate = "",
                            maxStreak = 0,
                            isActive = true,
                            reminderEnabled = true,
                            reminderTime = reminderTime,
                        )
                    )
                }
            } finally {
                pending.finish()
            }
        }
    }

    /**
     * Fired by the "Mark done" notification action. Checks the habit off for today,
     * dismisses the notification, and refreshes the home-screen widget.
     */
    private fun handleMarkDone(context: Context, intent: Intent) {
        val habitId = intent.getStringExtra(ReminderConstants.EXTRA_HABIT_ID) ?: return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val habit = runCatching { repository.getHabits() }
                    .getOrNull()
                    ?.firstOrNull { it.id == habitId }
                if (habit != null) {
                    val today = LocalDate.now().toString()
                    if (!habit.completedDates.contains(today)) {
                        repository.updateHabitCompletion(
                            habit.copy(completedDates = habit.completedDates + today)
                        )
                    }
                }
                NotificationManagerCompat.from(context).cancel(habitId.hashCode())
                HabitWidgetProvider.refresh(context)
            } finally {
                pending.finish()
            }
        }
    }

    /**
     * Morning check: if any active habit was missed yesterday with no reason logged,
     * post one notification ("You missed X yesterday") that opens the reason sheet.
     * Always re-arms tomorrow's check.
     */
    private fun handleMissCheck(context: Context) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return@launch
                if (!settingsRepository.notificationsEnabled.first()) return@launch
                val now = Calendar.getInstance()
                val quiet = settingsRepository.quietHours.first()
                    .contains(now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE))
                if (quiet) return@launch
                val yesterday = LocalDate.now().minusDays(1).toString()
                val missed = MotivationInsights.missedYesterday(
                    repository.getHabits(),
                    motivationRepository.missLogsOn(yesterday),
                )
                if (missed.isNotEmpty()) showMissNotification(context, missed)
            } catch (e: Exception) {
                android.util.Log.e("HabitReminder", "miss check failed", e)
            } finally {
                scheduler.scheduleMissCheck()
                pending.finish()
            }
        }
    }

    private fun showMissNotification(context: Context, missed: List<Habit>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        scheduler.ensureNotificationChannel()
        val open = Intent(context, MainActivity::class.java).apply {
            action = ReminderConstants.ACTION_OPEN_MISS_LOG
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            context, ReminderConstants.MISS_NOTIFICATION_ID, open,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val title = if (missed.size == 1) "You missed ${missed[0].name} yesterday" else "You missed ${missed.size} habits yesterday"
        val body = if (missed.size == 1) "What got in the way? One tap is enough." else "What got in the way? Log a reason to spot patterns."
        val builder = NotificationCompat.Builder(context, ReminderConstants.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_reminder)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
        parseAccentColor(missed[0].color)?.let { builder.color = it }
        NotificationManagerCompat.from(context).notify(ReminderConstants.MISS_NOTIFICATION_ID, builder.build())
    }

    /** "Snooze 1h": dismiss now, re-post once in an hour via a one-shot alarm. */
    private fun handleSnooze(context: Context, intent: Intent) {
        val habitId = intent.getStringExtra(ReminderConstants.EXTRA_HABIT_ID) ?: return
        val name = intent.getStringExtra(ReminderConstants.EXTRA_HABIT_NAME).orEmpty()
        val color = intent.getStringExtra(ReminderConstants.EXTRA_HABIT_COLOR).orEmpty()
        NotificationManagerCompat.from(context).cancel(habitId.hashCode())
        scheduler.scheduleSnooze(habitId, name, color)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun currentStreakOf(habit: Habit): Int {
        val dates = habit.completedDates.mapNotNull {
            runCatching { LocalDate.parse(it) }.getOrNull()
        }.toSet()
        return HabitUtil.currentStreak(dates)
    }

    private fun showNotification(
        context: Context,
        habitId: String,
        habitName: String,
        habitColorHex: String,
        streak: Int,
    ) {
        scheduler.ensureNotificationChannel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        // Deep-link intent that tells MainActivity to open the detail sheet for this habit.
        // - Explicit class target (no implicit resolution that could match the launcher).
        // - Custom action + unique data URI prevent PendingIntent reuse across habits
        //   (PendingIntent equality is by filterEquals — includes action/data, NOT extras).
        // - NEW_TASK is required: PendingIntent.getActivity() is fired from the system /
        //   receiver context with no current activity task. With launchMode=singleTask
        //   in the manifest, this guarantees a single MainActivity instance and routes
        //   subsequent taps through onNewIntent with our extras intact.
        val openIntent = Intent(context, MainActivity::class.java).apply {
            action = ReminderConstants.ACTION_OPEN_HABIT_DETAIL
            data = android.net.Uri.Builder()
                .scheme(ReminderConstants.DEEPLINK_SCHEME)
                .authority(ReminderConstants.DEEPLINK_HOST)
                .appendPath(habitId)
                .build()
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(ReminderConstants.EXTRA_OPEN_HABIT_DETAIL, habitId)
        }
        val piFlags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        val contentIntent = PendingIntent.getActivity(
            context,
            habitId.hashCode() xor 0x0DEEF,
            openIntent,
            piFlags
        )

        // "Mark done" action → back to this receiver with ACTION_MARK_DONE.
        val markDoneIntent = Intent(context, HabitReminderReceiver::class.java).apply {
            action = ReminderConstants.ACTION_MARK_DONE
            putExtra(ReminderConstants.EXTRA_HABIT_ID, habitId)
        }
        val markDonePi = PendingIntent.getBroadcast(
            context,
            habitId.hashCode() xor 0x0D0E,
            markDoneIntent,
            piFlags
        )

        // "Snooze 1h" action → back to this receiver with ACTION_SNOOZE.
        val snoozeIntent = Intent(context, HabitReminderReceiver::class.java).apply {
            action = ReminderConstants.ACTION_SNOOZE
            putExtra(ReminderConstants.EXTRA_HABIT_ID, habitId)
            putExtra(ReminderConstants.EXTRA_HABIT_NAME, habitName)
            putExtra(ReminderConstants.EXTRA_HABIT_COLOR, habitColorHex)
        }
        val snoozePi = PendingIntent.getBroadcast(
            context,
            habitId.hashCode() xor 0x5A00,
            snoozeIntent,
            piFlags
        )

        val title = if (habitName.isNotBlank()) "Time for $habitName 🔥" else "Habit reminder"
        val body = when {
            streak >= 1 -> "You're on a $streak-day streak. Keep it alive — check in now."
            else -> "Time to check in and start a new streak."
        }
        val accentColor = parseAccentColor(habitColorHex)
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, ReminderConstants.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_reminder)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle(title)
                    .bigText(body)
            )
            .setContentIntent(contentIntent)
            .addAction(R.drawable.ic_notification_reminder, "Mark done", markDonePi)
            .addAction(R.drawable.ic_notification_reminder, "Snooze 1h", snoozePi)
            .setAutoCancel(true)
            // Pre-O these drive heads-up; on O+ the channel takes over.
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(true)

        if (accentColor != null) {
            builder.color = accentColor
            builder.setColorized(true)
        }

        NotificationManagerCompat.from(context).notify(habitId.hashCode(), builder.build())
    }

    /**
     * Hex strings in this app are stored as #AARRGGBB or #RRGGBB. NotificationCompat
     * expects an opaque ARGB int — we drop alpha and force FF so light colors stay visible.
     */
    private fun parseAccentColor(hex: String): Int? {
        if (hex.isBlank()) return null
        val cleaned = hex.removePrefix("#").removePrefix("0x")
        val rgb = when (cleaned.length) {
            8 -> cleaned.substring(2)
            6 -> cleaned
            else -> return null
        }
        return runCatching { Color.parseColor("#FF$rgb") }.getOrNull()
    }
}
