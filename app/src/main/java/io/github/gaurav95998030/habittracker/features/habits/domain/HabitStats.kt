package io.github.gaurav95998030.habittracker.features.habits.domain

import android.os.Build
import androidx.annotation.RequiresApi
import io.github.gaurav95998030.habittracker.features.habits.HabitUtil
import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Pure streak / heat math over a habit's `yyyy-MM-dd` completion strings — the
 * Kotlin port of the design's data.jsx + viz.jsx helpers.
 */
@RequiresApi(Build.VERSION_CODES.O)
object HabitStats {

    fun completedSet(habit: Habit): Set<LocalDate> =
        habit.completedDates.mapNotNullTo(HashSet()) { runCatching { LocalDate.parse(it) }.getOrNull() }

    fun isDoneOn(habit: Habit, date: LocalDate): Boolean = habit.completedDates.contains(date.toString())

    /** Consecutive days ending today (or yesterday while today is still open). */
    fun currentStreak(habit: Habit, today: LocalDate = LocalDate.now()): Int =
        HabitUtil.currentStreak(completedSet(habit), today)

    fun bestStreak(habit: Habit): Int = HabitUtil.maxStreak(completedSet(habit))

    /**
     * Run-length intensity per completed day: the length of the run ending on that
     * day maps to level 1..4 (≥3, ≥7, ≥14 days). Drives heatmap opacity + glow.
     */
    fun heatLevels(done: Set<LocalDate>): Map<LocalDate, Int> {
        // Ascending order guarantees yesterday's run is known before today's.
        val runs = HashMap<LocalDate, Int>(done.size)
        for (d in done.sorted()) runs[d] = (runs[d.minusDays(1)] ?: 0) + 1
        return runs.mapValues { (_, run) ->
            when {
                run >= 14 -> 4
                run >= 7 -> 3
                run >= 3 -> 2
                else -> 1
            }
        }
    }

    /** Opacity for each heat level (0 = empty). */
    fun levelAlpha(level: Int): Float = when (level) {
        1 -> 0.42f
        2 -> 0.62f
        3 -> 0.82f
        4 -> 1f
        else -> 0f
    }

    /** Local date the habit was created (createdDate is an ISO instant; tolerant of plain dates). */
    fun createdOn(habit: Habit): LocalDate? =
        runCatching { Instant.parse(habit.createdDate).atZone(ZoneId.systemDefault()).toLocalDate() }.getOrNull()
            ?: runCatching { LocalDate.parse(habit.createdDate.take(10)) }.getOrNull()
}
