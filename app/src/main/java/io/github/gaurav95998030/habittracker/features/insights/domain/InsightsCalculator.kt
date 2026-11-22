package io.github.gaurav95998030.habittracker.features.insights.domain

import android.os.Build
import androidx.annotation.RequiresApi
import io.github.gaurav95998030.habittracker.features.habits.HabitCatalog
import io.github.gaurav95998030.habittracker.features.habits.HabitUtil
import io.github.gaurav95998030.habittracker.features.habits.domain.HabitStats
import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit
import io.github.gaurav95998030.habittracker.features.insights.presentation.InsightsUiState
import io.github.gaurav95998030.habittracker.features.insights.presentation.StreakEntry
import io.github.gaurav95998030.habittracker.features.insights.presentation.WeekBucket
import java.time.LocalDate
import kotlin.math.roundToInt

/**
 * Pure stats over the active habits (paused habits are excluded), matching the
 * design's StatsScreen:
 *
 * - **Weekly completion rate** — 8 rolling 7-day windows ending today. Each window's
 *   rate is completions ÷ possible check-ins, where a habit only counts from the day
 *   it was created. "Avg / week" is the mean of those 8 rates.
 * - **Best day of the week** — all-time completions bucketed by weekday.
 * - **Longest streaks** — every active habit ranked by its best-ever run.
 */
@RequiresApi(Build.VERSION_CODES.O)
object InsightsCalculator {

    private const val WEEKS_WINDOW = 8

    fun build(habits: List<Habit>, today: LocalDate = LocalDate.now()): InsightsUiState {
        val active = habits.filter { it.isActive }
        if (active.isEmpty()) return InsightsUiState(isEmpty = true)

        val completions = active.associate { it.id to HabitStats.completedSet(it) }
        val created = active.associate { it.id to HabitStats.createdOn(it) }

        val weeks = (WEEKS_WINDOW - 1 downTo 0).map { w ->
            var done = 0
            var slots = 0
            for (d in 0 until 7) {
                val day = today.minusDays(w * 7L + d)
                active.forEach { h ->
                    val since = created[h.id]
                    if (since == null || !since.isAfter(day)) {
                        slots++
                        if (day in completions[h.id].orEmpty()) done++
                    }
                }
            }
            WeekBucket(
                label = if (w == 0) "Now" else "${w}w",
                percent = if (slots == 0) 0 else (done * 100f / slots).roundToInt(),
            )
        }

        val dow = IntArray(7)
        completions.values.forEach { set -> set.forEach { dow[it.dayOfWeek.value % 7]++ } }
        val best = dow.indices.maxByOrNull { dow[it] } ?: 0

        val board = active.map { h ->
            val set = completions[h.id].orEmpty()
            StreakEntry(
                habitId = h.id,
                name = h.name.ifBlank { "Untitled" },
                colorHex = h.color,
                icon = HabitCatalog.iconFor(h),
                bestStreak = HabitUtil.maxStreak(set),
                currentStreak = HabitUtil.currentStreak(set, today),
            )
        }.sortedByDescending { it.bestStreak }

        return InsightsUiState(
            avgWeeklyRatePercent = weeks.map { it.percent }.average().roundToInt(),
            totalCompletions = completions.values.sumOf { it.size },
            weekBuckets = weeks,
            dayOfWeekCounts = dow.toList(),
            bestDayIndex = best,
            longestStreaks = board,
            isEmpty = false,
        )
    }
}
