package io.github.gaurav95998030.habittracker.features.insights.presentation

/** One rolling 7-day window. [percent] = completions / possible check-ins in that window. */
data class WeekBucket(
    val label: String,
    val percent: Int,
)

data class StreakEntry(
    val habitId: String,
    val name: String,
    val colorHex: String,
    val icon: String,
    val bestStreak: Int,
    val currentStreak: Int,
)

data class InsightsUiState(
    val avgWeeklyRatePercent: Int = 0,
    val totalCompletions: Int = 0,
    /** Oldest → newest; the last bucket is "Now". */
    val weekBuckets: List<WeekBucket> = emptyList(),
    /** Completions per weekday, Sunday-first (index 0 = Sunday). */
    val dayOfWeekCounts: List<Int> = List(7) { 0 },
    /** Index into [dayOfWeekCounts] of the strongest day. */
    val bestDayIndex: Int = 0,
    val longestStreaks: List<StreakEntry> = emptyList(),
    val isEmpty: Boolean = true,
)
