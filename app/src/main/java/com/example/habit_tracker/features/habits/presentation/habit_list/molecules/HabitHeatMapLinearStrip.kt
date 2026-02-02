package com.example.habit_tracker.features.habits.presentation.habit_list.molecules

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.*
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HabitHeatmapLinearStrip(
    startDate: LocalDate,
    endDate: LocalDate,
    highlightedDates: List<LocalDate>,
    highlightColor: Color,
    streakMidColor: Color = Color(0xFFFF8A80),
    streakStrongColor: Color = Color(0xFFFFB3A7),
    defaultColor: Color = Color(0xFF4A2C2C),
    outsideRangeColor: Color = Color(0xFF241818),
    rows: Int = 4   // 👈 configurable — you asked for 4
) {
    val days = remember(startDate, endDate) {
        generateDateRange(startDate, endDate)
    }

    val streakLevels = remember(highlightedDates) {
        computeStreakLevels(highlightedDates.toSet())
    }

    // chunk days into vertical columns
    val columns = remember(days, rows) {
        days.chunked(rows)
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {

        items(columns) { columnDays ->

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                (0 until rows).forEach { i ->

                    val date = columnDays.getOrNull(i)

                    val color =
                        when (streakLevels[date]) {
                            3 -> streakStrongColor
                            2 -> streakMidColor
                            1 -> highlightColor
                            else -> if (date == null) outsideRangeColor else defaultColor
                        }

                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(color, RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }
}



@RequiresApi(Build.VERSION_CODES.O)
private fun generateDateRange(start: LocalDate, end: LocalDate): List<LocalDate> {
    val d = ChronoUnit.DAYS.between(start, end).toInt()
    return (0..d).map { start.plusDays(it.toLong()) }
}

/**
 * 1 = single day
 * 2 = small streak
 * 3+ = strong chain
 */
@RequiresApi(Build.VERSION_CODES.O)
private fun computeStreakLevels(
    highlighted: Set<LocalDate>
): Map<LocalDate, Int> {

    if (highlighted.isEmpty()) return emptyMap()

    val sorted = highlighted.sorted()
    val result = mutableMapOf<LocalDate, Int>()
    var streak = mutableListOf<LocalDate>()

    fun commit() {
        if (streak.isEmpty()) return
        val size = streak.size
        streak.forEach { date ->
            result[date] = when {
                size >= 3 -> 3
                size == 2 -> 2
                else -> 1
            }
        }
        streak = mutableListOf()
    }

    streak.add(sorted.first())

    for (i in 1 until sorted.size) {
        val prev = sorted[i - 1]
        val cur  = sorted[i]

        if (cur == prev.plusDays(1)) {
            streak.add(cur)
        } else {
            commit()
            streak.add(cur)
        }
    }

    commit()
    return result
}
