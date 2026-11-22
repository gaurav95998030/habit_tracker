package io.github.gaurav95998030.habittracker.features.reminders.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Delivery state of a single reminder occurrence, derived (not persisted).
 *
 * - [Scheduled] — today's reminder whose time is still in the future.
 * - [Done]      — the habit was completed on that day.
 * - [Missed]    — the reminder time has passed and the habit wasn't completed.
 */
enum class ReminderStatus { Scheduled, Done, Missed }

data class ReminderNotification(
    val habitId: String,
    val title: String,
    val timeLabel: String,
    val colorHex: String,
    val status: ReminderStatus,
)

/** A day's worth of reminder occurrences under a header like "TODAY" / "FRI, JUL 10". */
data class ReminderSection(
    val label: String,
    val items: List<ReminderNotification>,
)

data class NotificationCenterUi(
    val scheduledTodayCount: Int,
    val sections: List<ReminderSection>,
) {
    val isEmpty: Boolean get() = sections.isEmpty()
}

/**
 * Builds the Notification Center feed purely from the habit list — there is no
 * reminder-history table. For every habit with an enabled reminder we synthesise
 * one daily occurrence for each of the last [HISTORY_DAYS] days (never before the
 * habit was created), tag each with a [ReminderStatus], and group them by day.
 *
 * Mirrors the derive-from-habits approach used by InsightsCalculator, so the feed
 * stays in sync with the shared HabitViewModel state with no extra plumbing.
 */
@RequiresApi(Build.VERSION_CODES.O)
object NotificationCenterBuilder {

    private const val HISTORY_DAYS = 14
    private val TIME_PARSER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
    private val TIME_LABEL: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
    private val DAY_LABEL: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.US)

    fun build(
        habits: List<Habit>,
        now: LocalDateTime = LocalDateTime.now(),
    ): NotificationCenterUi {
        val today = now.toLocalDate()
        val reminderHabits = habits.filter { it.reminderEnabled && !it.reminderTime.isNullOrBlank() }

        val sections = (0 until HISTORY_DAYS).mapNotNull { offset ->
            val date = today.minusDays(offset.toLong())

            val items = reminderHabits
                .mapNotNull { habit ->
                    val time = parseTime(habit.reminderTime!!) ?: return@mapNotNull null
                    if (createdAfter(habit, date)) return@mapNotNull null

                    val occurrence = date.atTime(time)
                    val completed = habit.completedDates.contains(date.toString())
                    val status = when {
                        date.isEqual(today) && occurrence.isAfter(now) -> ReminderStatus.Scheduled
                        completed -> ReminderStatus.Done
                        else -> ReminderStatus.Missed
                    }

                    time to ReminderNotification(
                        habitId = habit.id,
                        title = "Time for ${habit.name}",
                        timeLabel = time.format(TIME_LABEL),
                        colorHex = habit.color,
                        status = status,
                    )
                }
                .sortedBy { it.first }
                .map { it.second }

            if (items.isEmpty()) null
            else ReminderSection(label = sectionLabel(date, today), items = items)
        }

        val scheduledToday = reminderHabits.count { habit ->
            parseTime(habit.reminderTime!!) != null && !createdAfter(habit, today)
        }

        return NotificationCenterUi(
            scheduledTodayCount = scheduledToday,
            sections = sections,
        )
    }

    private fun sectionLabel(date: LocalDate, today: LocalDate): String = when {
        date.isEqual(today) -> "TODAY"
        date.isEqual(today.minusDays(1)) -> "YESTERDAY"
        else -> date.format(DAY_LABEL).uppercase(Locale.US)
    }

    private fun parseTime(value: String): LocalTime? =
        runCatching { LocalTime.parse(value, TIME_PARSER) }.getOrNull()

    /** True when the habit was created after [date], so it had no reminder yet then. */
    private fun createdAfter(habit: Habit, date: LocalDate): Boolean {
        val created = runCatching {
            Instant.parse(habit.createdDate).atZone(ZoneId.systemDefault()).toLocalDate()
        }.getOrNull() ?: return false
        return created.isAfter(date)
    }
}
