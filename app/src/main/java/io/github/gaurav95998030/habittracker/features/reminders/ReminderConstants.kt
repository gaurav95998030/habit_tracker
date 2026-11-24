package io.github.gaurav95998030.habittracker.features.reminders

object ReminderConstants {
    // Bumped from v1 because notification-channel importance/sound cannot be
    // changed after creation on Android O+. A new id forces a fresh channel.
    const val CHANNEL_ID = "habit_reminders_v2"
    const val CHANNEL_NAME = "Habit reminders"
    const val CHANNEL_DESCRIPTION = "Daily reminders for your habits"

    const val ACTION_FIRE_REMINDER = "io.github.gaurav95998030.habittracker.ACTION_FIRE_REMINDER"
    const val ACTION_OPEN_HABIT_DETAIL = "io.github.gaurav95998030.habittracker.ACTION_OPEN_HABIT_DETAIL"

    // Fired by the "Mark done" notification action — checks the habit off for today.
    const val ACTION_MARK_DONE = "io.github.gaurav95998030.habittracker.ACTION_MARK_DONE"

    // Fired by the "Snooze 1h" notification action — re-posts the reminder an hour later.
    const val ACTION_SNOOZE = "io.github.gaurav95998030.habittracker.ACTION_SNOOZE"
    // One-shot alarm armed by snooze. Distinct action so it never replaces the daily alarm.
    const val ACTION_FIRE_SNOOZED = "io.github.gaurav95998030.habittracker.ACTION_FIRE_SNOOZED"
    const val SNOOZE_MINUTES = 60

    // Daily morning check: "You missed X yesterday — what got in the way?"
    const val ACTION_MISS_CHECK = "io.github.gaurav95998030.habittracker.ACTION_MISS_CHECK"
    const val ACTION_OPEN_MISS_LOG = "io.github.gaurav95998030.habittracker.ACTION_OPEN_MISS_LOG"
    const val MISS_CHECK_HOUR = 8
    const val MISS_NOTIFICATION_ID = 0x0315
    const val DEEPLINK_SCHEME = "habit"
    const val DEEPLINK_HOST = "detail"

    const val EXTRA_HABIT_ID = "extra_habit_id"
    const val EXTRA_HABIT_NAME = "extra_habit_name"
    const val EXTRA_HABIT_DESCRIPTION = "extra_habit_description"
    const val EXTRA_HABIT_COLOR = "extra_habit_color"
    const val EXTRA_REMINDER_TIME = "extra_reminder_time"

    // Deep link extras (read by MainActivity from notification contentIntent)
    const val EXTRA_OPEN_HABIT_DETAIL = "extra_open_habit_detail"
}
