package io.github.gaurav95998030.habittracker.features.insights.presentation

import java.time.DayOfWeek

data class WeekBucket(
    val label: String,
    val completions: Int,
)

data class StreakEntry(
    val habitId: String,
    val name: String,
    val colorHex: String,
    val category: String,
    val bestStreak: Int,
)

data class InsightsUiState(
    val avgWeeklyRatePercent: Int = 0,
    val totalCompletions: Int = 0,
    val weekBuckets: List<WeekBucket> = emptyList(),
    val bestDay: DayOfWeek = DayOfWeek.MONDAY,
    val bestDayCount: Int = 0,
    val longestStreaks: List<StreakEntry> = emptyList(),
    val isEmpty: Boolean = true,
)
