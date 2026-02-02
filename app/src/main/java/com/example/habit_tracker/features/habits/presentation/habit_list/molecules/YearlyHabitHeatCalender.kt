package com.example.habit_tracker.features.habits.presentation.habit_list.molecules

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.*
import java.time.temporal.ChronoUnit


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun YearlyHabitHeatCalender(
    startDate: LocalDate,
    endDate: LocalDate,
    highlightedDates: List<LocalDate>,
    highlightColor: Color,
    streakMidColor: Color = Color(0xFFFF8A80),
    streakStrongColor: Color = Color(0xFFFFB3A7),
    defaultColor: Color = Color(0xFF4A2C2C),
    emptyCellColor: Color = Color(0xFF2A1A1A)
) {
    val days = remember(startDate, endDate) {
        generateDateRange(startDate, endDate)
    }

    val groupedByWeek = remember(days) {
        days.groupBy { weekStart(it) }
    }

    // compute streak levels once
    val streakLevels = remember(highlightedDates) {
        computeStreakLevels(highlightedDates.toSet())
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(8.dp)
    ) {

        groupedByWeek.forEach { (weekStartDate, weekDays) ->

            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 2.dp)
                ) {

                    if (weekStartDate.dayOfMonth <= 7) {
                        Text(
                            text = weekStartDate.month.name.take(3),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    WeekColumn(
                        weekDays = weekDays,
                        streakLevels = streakLevels,
                        highlightColor = highlightColor,
                        streakMidColor = streakMidColor,
                        streakStrongColor = streakStrongColor,
                        defaultColor = defaultColor,
                        emptyCellColor = emptyCellColor
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun generateDateRange(start: LocalDate, end: LocalDate): List<LocalDate> {
    val days = ChronoUnit.DAYS.between(start, end).toInt()
    return (0..days).map { start.plusDays(it.toLong()) }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun weekStart(date: LocalDate): LocalDate =
    date.with(DayOfWeek.MONDAY)

/**
 * Returns streak strength per highlighted date:
 * 1 = single day
 * 2 = small chain
 * 3+ = strong streak segment
 */
@RequiresApi(Build.VERSION_CODES.O)
private fun computeStreakLevels(
    highlighted: Set<LocalDate>
): Map<LocalDate, Int> {

    if (highlighted.isEmpty()) return emptyMap()

    val sorted = highlighted.sorted()
    val result = mutableMapOf<LocalDate, Int>()

    var streakList = mutableListOf<LocalDate>()

    @RequiresApi(Build.VERSION_CODES.O)
    fun commitStreak() {
        if (streakList.isEmpty()) return

        val size = streakList.size

        streakList.forEachIndexed { _, date ->
            result[date] = when {
                size >= 3 -> 3   // strong streak
                size == 2 -> 2   // small streak
                else -> 1        // single day
            }
        }
        streakList = mutableListOf()
    }

    streakList.add(sorted.first())

    for (i in 1 until sorted.size) {
        val prev = sorted[i - 1]
        val cur = sorted[i]

        if (cur == prev.plusDays(1)) {
            streakList.add(cur)
        } else {
            commitStreak()
            streakList.add(cur)
        }
    }

    commitStreak()
    return result
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun WeekColumn(
    weekDays: List<LocalDate>,
    streakLevels: Map<LocalDate, Int>,
    highlightColor: Color,
    streakMidColor: Color,
    streakStrongColor: Color,
    defaultColor: Color,
    emptyCellColor: Color
) {
    val visibleDays = listOf(
        DayOfWeek.TUESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.SATURDAY
    )

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        visibleDays.forEach { dow ->

            val date = weekDays.find { it.dayOfWeek == dow }

            val cellColor =
                when (val level = streakLevels[date]) {
                    3 -> streakStrongColor   // strong chain
                    2 -> streakMidColor      // small chain
                    1 -> highlightColor      // single highlight
                    else -> if (date == null) emptyCellColor else defaultColor
                }

            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(cellColor, RoundedCornerShape(4.dp))
            )
        }
    }
}
