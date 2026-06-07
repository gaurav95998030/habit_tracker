package io.github.gaurav95998030.habittracker.features.insights.domain

import android.os.Build
import androidx.annotation.RequiresApi
import io.github.gaurav95998030.habittracker.features.habits.HabitUtil
import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit
import io.github.gaurav95998030.habittracker.features.insights.presentation.InsightsUiState
import io.github.gaurav95998030.habittracker.features.insights.presentation.StreakEntry
import io.github.gaurav95998030.habittracker.features.insights.presentation.WeekBucket
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
object InsightsCalculator {

    private const val WEEKS_WINDOW = 8
    private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun build(habits: List<Habit>, today: LocalDate = LocalDate.now()): InsightsUiState {
        val active = habits.filter { it.isActive }
        val parsedCompletions: Map<String, Set<LocalDate>> = active.associate { habit ->
            habit.id to habit.completedDates.mapNotNull { runCatching { LocalDate.parse(it, DATE_FORMATTER) }.getOrNull() }.toSet()
        }

        val totalCompletions = parsedCompletions.values.sumOf { it.size }

        val weekBuckets = buildWeekBuckets(parsedCompletions.values, today)
        val avgWeeklyRate = averageWeeklyRate(weekBuckets, active.size)
        val bestDay = bestDayOfWeek(parsedCompletions.values, today)
        val streaks = longestStreaks(active, parsedCompletions)

        return InsightsUiState(
            avgWeeklyRatePercent = avgWeeklyRate,
            totalCompletions = totalCompletions,
            weekBuckets = weekBuckets,
            bestDay = bestDay.first,
            bestDayCount = bestDay.second,
            longestStreaks = streaks,
            isEmpty = active.isEmpty(),
        )
    }

    private fun buildWeekBuckets(allDates: Collection<Set<LocalDate>>, today: LocalDate): List<WeekBucket> {
        val mondayThisWeek = today.with(DayOfWeek.MONDAY)
        val flat = allDates.flatten()
        return (WEEKS_WINDOW - 1 downTo 0).map { weeksAgo ->
            val weekStart = mondayThisWeek.minusWeeks(weeksAgo.toLong())
            val weekEnd = weekStart.plusDays(6)
            val count = flat.count { !it.isBefore(weekStart) && !it.isAfter(weekEnd) }
            val label = when (weeksAgo) {
                0 -> "Now"
                else -> "${weeksAgo}w"
            }
            WeekBucket(label = label, completions = count)
        }
    }

    private fun averageWeeklyRate(buckets: List<WeekBucket>, habitCount: Int): Int {
        if (habitCount == 0 || buckets.isEmpty()) return 0
        val maxPerWeek = habitCount * 7
        val avgFraction = buckets.map { it.completions.toFloat() / maxPerWeek }.average()
        return (avgFraction * 100).coerceIn(0.0, 100.0).toInt()
    }

    private fun bestDayOfWeek(allDates: Collection<Set<LocalDate>>, today: LocalDate): Pair<DayOfWeek, Int> {
        val windowStart = today.minusDays((WEEKS_WINDOW * 7L) - 1)
        val tally = mutableMapOf<DayOfWeek, Int>()
        DayOfWeek.values().forEach { tally[it] = 0 }
        allDates.flatten().forEach { date ->
            if (!date.isBefore(windowStart) && !date.isAfter(today)) {
                tally[date.dayOfWeek] = (tally[date.dayOfWeek] ?: 0) + 1
            }
        }
        val top = tally.maxByOrNull { it.value }
        return (top?.key ?: DayOfWeek.MONDAY) to (top?.value ?: 0)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun longestStreaks(
        habits: List<Habit>,
        parsedCompletions: Map<String, Set<LocalDate>>,
    ): List<StreakEntry> {
        val entries = habits.map { habit ->
            val dates = parsedCompletions[habit.id] ?: emptySet()
            StreakEntry(
                habitId = habit.id,
                name = habit.name.ifBlank { "Untitled" },
                colorHex = habit.color,
                category = habit.category,
                bestStreak = HabitUtil.maxStreak(dates),
            )
        }
        val sorted = entries.sortedByDescending { it.bestStreak }
        return sorted.take(10)
    }
}
