package io.github.gaurav95998030.habittracker.features.widget

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.EntryPointAccessors
import io.github.gaurav95998030.habittracker.MainActivity
import io.github.gaurav95998030.habittracker.R
import io.github.gaurav95998030.habittracker.commons.CrashReporter
import io.github.gaurav95998030.habittracker.features.reminders.ReminderConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar

/**
 * The home-screen card, on the lock screen. Phones can't host lock-screen widgets, so this
 * is a silent, ongoing notification (public visibility) styled like the widget, with
 * tappable checks. Shown only after the user consents (Settings → Privacy, or the one-time
 * prompt); swiping it away hides it until tomorrow.
 */
object TodayLockScreen {

    private const val CHANNEL_ID = "today_card"
    private const val NOTIFICATION_ID = 0x7D0A
    private const val MAX_ROWS = 4
    const val ACTION_DISMISSED = "io.github.gaurav95998030.habittracker.ACTION_TODAY_DISMISSED"
    const val ACTION_DAY_ROLLOVER = "io.github.gaurav95998030.habittracker.ACTION_DAY_ROLLOVER"

    /** Post, update or remove the card to match current settings and habits. Safe to call often. */
    fun update(context: Context) {
        val app = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val ep = EntryPointAccessors.fromApplication(app, WidgetEntryPoint::class.java)
                val settings = ep.settingsRepository()
                val today = LocalDate.now().toString()
                val show = settings.lockScreenEnabled.first() &&
                    settings.lockScreenDismissedOn.first() != today &&
                    canNotify(app)
                val summary = if (show) TodayCard.summary(ep.habitRepository().getHabits()) else null
                if (summary == null || summary.total == 0) {
                    NotificationManagerCompat.from(app).cancel(NOTIFICATION_ID)
                } else {
                    post(app, summary)
                }
            } catch (e: Exception) {
                CrashReporter.record("TodayLockScreen", "update failed", e)
            }
        }
    }

    fun cancel(context: Context) = NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)

    fun canNotify(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun ensureChannel(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java) ?: return
        if (nm.getNotificationChannel(CHANNEL_ID) != null) return
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Today on lock screen", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Today's habits and progress on your lock screen"
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
            }
        )
    }

    private fun post(context: Context, s: TodaySummary) {
        ensureChannel(context)
        val title = if (s.allDone) "All done today 🔥" else "${s.done} of ${s.total} done today"

        val collapsed = RemoteViews(context.packageName, R.layout.notification_today_collapsed).apply {
            setTextViewText(R.id.today_count, title)
            setTextViewText(R.id.today_next, s.next?.let { "Next: ${it.name}" } ?: "")
            setProgressBar(R.id.today_progress, 100, s.percent, false)
        }
        val expanded = RemoteViews(context.packageName, R.layout.notification_today_expanded).apply {
            setTextViewText(R.id.today_count, title)
            setProgressBar(R.id.today_progress, 100, s.percent, false)
            removeAllViews(R.id.today_rows)
            // Unfinished habits first, so what's left today is always visible.
            val ordered = s.habits.sortedBy { io.github.gaurav95998030.habittracker.features.habits.domain.HabitStats.isDoneOn(it, LocalDate.now()) }
            ordered.take(MAX_ROWS).forEachIndexed { i, habit ->
                val row = RemoteViews(context.packageName, R.layout.widget_habit_item)
                TodayCard.bindRow(context, row, habit, CardSurface.Notification)
                if (i > 0) row.setViewPadding(R.id.widget_item_root, 0, dp(context, 10), 0, 0)
                row.setOnClickPendingIntent(R.id.widget_item_check_box, toggleIntent(context, habit.id))
                addView(R.id.today_rows, row)
            }
            if (s.total > MAX_ROWS) {
                setViewVisibility(R.id.today_more, View.VISIBLE)
                setTextViewText(R.id.today_more, "+${s.total - MAX_ROWS} more in app")
            } else setViewVisibility(R.id.today_more, View.GONE)
        }

        val open = PendingIntent.getActivity(
            context, NOTIFICATION_ID,
            (context.packageManager.getLaunchIntentForPackage(context.packageName) ?: Intent(context, MainActivity::class.java))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val dismissed = PendingIntent.getBroadcast(
            context, NOTIFICATION_ID,
            Intent(context, HabitWidgetProvider::class.java).setAction(ACTION_DISMISSED),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val n = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_bolt)
            .setColor(context.getColor(R.color.notif_primary))
            .setContentTitle(title)
            .setContentText(s.next?.let { "Next: ${it.name}" } ?: "Every habit checked off.")
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(collapsed)
            .setCustomBigContentView(expanded)
            .setContentIntent(open)
            .setDeleteIntent(dismissed)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setShowWhen(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        @Suppress("MissingPermission") // checked in canNotify()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, n)
    }

    private fun toggleIntent(context: Context, habitId: String): PendingIntent =
        PendingIntent.getBroadcast(
            context, habitId.hashCode() xor 0x70DA,
            Intent(context, HabitWidgetProvider::class.java)
                .setAction(HabitWidgetProvider.ACTION_WIDGET_TOGGLE)
                .putExtra(ReminderConstants.EXTRA_HABIT_ID, habitId),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

    /**
     * Refresh widget + card just after midnight so "today" rolls over (yesterday's ticks
     * clear, a dismissed card comes back). Re-armed each time it fires.
     */
    fun scheduleMidnightRefresh(context: Context) {
        val am = context.getSystemService(AlarmManager::class.java) ?: return
        val pi = PendingIntent.getBroadcast(
            context, NOTIFICATION_ID + 1,
            Intent(context, HabitWidgetProvider::class.java).setAction(ACTION_DAY_ROLLOVER),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val at = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 1); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        AlarmManagerCompat.setAndAllowWhileIdle(am, AlarmManager.RTC, at, pi)
    }

    private fun dp(context: Context, v: Int) = (v * context.resources.displayMetrics.density).toInt()
}
