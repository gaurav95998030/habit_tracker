package io.github.gaurav95998030.habittracker.features.reminders

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import io.github.gaurav95998030.habittracker.MainActivity
import io.github.gaurav95998030.habittracker.R
import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit
import javax.inject.Inject

@AndroidEntryPoint
class HabitReminderReceiver : BroadcastReceiver() {

    @Inject lateinit var scheduler: HabitReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ReminderConstants.ACTION_FIRE_REMINDER) return

        val habitId = intent.getStringExtra(ReminderConstants.EXTRA_HABIT_ID) ?: return
        val habitName = intent.getStringExtra(ReminderConstants.EXTRA_HABIT_NAME).orEmpty()
        val habitDescription = intent.getStringExtra(ReminderConstants.EXTRA_HABIT_DESCRIPTION).orEmpty()
        val habitColor = intent.getStringExtra(ReminderConstants.EXTRA_HABIT_COLOR).orEmpty()
        val reminderTime = intent.getStringExtra(ReminderConstants.EXTRA_REMINDER_TIME)

        showNotification(context, habitId, habitName, habitDescription, habitColor)

        // Re-schedule for the next day (daily repeat).
        if (reminderTime != null) {
            scheduler.schedule(
                Habit(
                    id = habitId,
                    userId = "",
                    name = habitName,
                    description = habitDescription,
                    completedDates = emptyList(),
                    color = habitColor,
                    category = "",
                    createdDate = "",
                    updatedDate = "",
                    maxStreak = 0,
                    isActive = true,
                    reminderEnabled = true,
                    reminderTime = reminderTime
                )
            )
        }
    }

    private fun showNotification(
        context: Context,
        habitId: String,
        habitName: String,
        habitDescription: String,
        habitColorHex: String
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
            data = Uri.Builder()
                .scheme(ReminderConstants.DEEPLINK_SCHEME)
                .authority(ReminderConstants.DEEPLINK_HOST)
                .appendPath(habitId)
                .build()
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(ReminderConstants.EXTRA_OPEN_HABIT_DETAIL, habitId)
        }
        val contentRequestCode = habitId.hashCode() xor 0x0DEEF
        val piFlags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        val contentIntent = PendingIntent.getActivity(
            context,
            contentRequestCode,
            openIntent,
            piFlags
        )

        val title = if (habitName.isNotBlank()) habitName else "Habit reminder"
        val body = habitDescription.ifBlank { "Tap to check in and keep your streak going." }
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
