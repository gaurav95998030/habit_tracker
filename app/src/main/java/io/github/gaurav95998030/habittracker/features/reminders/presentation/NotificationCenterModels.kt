package io.github.gaurav95998030.habittracker.features.reminders.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import io.github.gaurav95998030.habittracker.features.habits.domain.HabitStats
import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Delivery state of a single reminder occurrence, derived (not persisted).
 *
 * - [Upcoming] — today's reminder whose time is still in the future.
 * - [Pending]  — today's reminder has fired and the habit isn't done yet ("Due now").
 * - [Done]     — the habit was completed on that day.
 * - [Missed]   — a past day's reminder that wasn't followed by a completion.
 */
enum class ReminderStatus { Upcoming, Pending, Done, Missed }

data class ReminderNotification(
    val habitId: String,
    val habitName: String,
    val icon: String,
    val colorHex: String,
    val time: String,
    val status: ReminderStatus,
    /** Streak as of that day: through the day if done, else up to the day before. */
    val streak: Int,
) {
    val key: String get() = "$habitId-$time-$status"
}

/** A day's worth of reminder occurrences under a header like "Today" / "Friday, Jul 10". */
data class ReminderSection(
    val date: LocalDate,
    val label: String,
    val items: List<ReminderNotification>,
)

data class NotificationCenterUi(
    val sections: List<ReminderSection>,
    val todayTotal: Int,
    val todayDone: Int,
    val todayPending: Int,
) {
    val isEmpty: Boolean get() = sections.isEmpty()
}

/**
 * Builds the Notification Center feed purely from the habit list — there is no
 * reminder-history table. For every active habit with an enabled reminder we
 * synthesise one daily occurrence for each of the last [HISTORY_DAYS] days (never
 * before the habit was created), tag each with a [ReminderStatus], and group by
 * day, newest first (latest reminder time first within a day).
 *
 * Mirrors the design's buildNotifications(), and stays in sync with the shared
 * HabitViewModel state with no extra plumbing.
 */
@RequiresApi(Build.VERSION_CODES.O)
object NotificationCenterBuilder {

    private const val HISTORY_DAYS = 9
    private val TIME_PARSER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)

    fun build(
        habits: List<Habit>,
        iconFor: (Habit) -> String,
        now: LocalDateTime = LocalDateTime.now(),
    ): NotificationCenterUi {
        val today = now.toLocalDate()
        val reminderHabits = habits.filter { it.isActive && it.reminderEnabled && !it.reminderTime.isNullOrBlank() }
        val completedSets = reminderHabits.associate { it.id to HabitStats.completedSet(it) }

        val sections = (0 until HISTORY_DAYS).mapNotNull { offset ->
            val date = today.minusDays(offset.toLong())
            val items = reminderHabits.mapNotNull { habit ->
                val time = parseTime(habit.reminderTime!!) ?: return@mapNotNull null
                val created = HabitStats.createdOn(habit)
                if (created != null && created.isAfter(date)) return@mapNotNull null

                val done = completedSets[habit.id].orEmpty()
                val isDone = date in done
                val status = when {
                    offset == 0 && isDone -> ReminderStatus.Done
                    offset == 0 && !time.isAfter(now.toLocalTime()) -> ReminderStatus.Pending
                    offset == 0 -> ReminderStatus.Upcoming
                    isDone -> ReminderStatus.Done
                    else -> ReminderStatus.Missed
                }
                time to ReminderNotification(
                    habitId = habit.id,
                    habitName = habit.name,
                    icon = iconFor(habit),
                    colorHex = habit.color,
                    time = habit.reminderTime,
                    status = status,
                    streak = streakAsOf(done, if (isDone) date else date.minusDays(1)),
                )
            }
                .sortedByDescending { it.first }
                .map { it.second }

            if (items.isEmpty()) null else ReminderSection(date, sectionLabel(date, today), items)
        }

        val todayItems = sections.firstOrNull { it.date == today }?.items.orEmpty()
        return NotificationCenterUi(
            sections = sections,
            todayTotal = todayItems.size,
            todayDone = todayItems.count { it.status == ReminderStatus.Done },
            todayPending = todayItems.count { it.status == ReminderStatus.Pending },
        )
    }

    /** Count of reminders that are due now and not yet done — the Home bell badge. */
    fun pendingCount(habits: List<Habit>, now: LocalDateTime = LocalDateTime.now()): Int =
        build(habits, iconFor = { "" }, now = now).todayPending

    private fun streakAsOf(done: Set<LocalDate>, end: LocalDate): Int {
        var cursor = end
        var n = 0
        while (cursor in done) { n++; cursor = cursor.minusDays(1) }
        return n
    }

    private fun sectionLabel(date: LocalDate, today: LocalDate): String = when (date) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> "${date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())}, " +
            "${date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())} ${date.dayOfMonth}"
    }

    private fun parseTime(value: String): LocalTime? =
        runCatching { LocalTime.parse(value, TIME_PARSER) }.getOrNull()
}
