package com.example.habit_tracker.features.habits.presentation.habit_list.molecules

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.habit_tracker.commons.ui.VerticalSpace
import java.time.LocalDate
import java.time.YearMonth

/* ----------------------------- PUBLIC API ----------------------------- */

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabitCalendar(
    startDate: LocalDate,
    endDate: LocalDate,
    modifier: Modifier = Modifier,
    dateBoxColor: Color = Color(0xFF2A2A2A),
    dateBoxDotColor: Color = Color(0xFFE57373),
    selectedDates: Set<LocalDate> = emptySet(),
    datesWithDots: Set<LocalDate> = emptySet(),
    onDateClick: (LocalDate) -> Unit = {},
    initialMonth: YearMonth? = null // Optional: specify which month to show initially
) {
    val months = remember(startDate, endDate) {
        generateMonthRange(startDate, endDate)
    }

    // Find initial page index
    val initialPage = remember(months, initialMonth) {
        if (initialMonth != null) {
            months.indexOf(initialMonth).coerceAtLeast(0)
        } else {
            // Default to current month or last month if current is not in range
            val currentMonth = YearMonth.now()
            val index = months.indexOf(currentMonth)
            if (index >= 0) index else months.lastIndex.coerceAtLeast(0)
        }
    }

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { months.size }
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
//pageSpacing = 16.dp,
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) { pageIndex ->
            MonthGrid(
                month = months[pageIndex],
                startDate = startDate,
                endDate = endDate,
                dateBoxColor = dateBoxColor,
                dateBoxDotColor = dateBoxDotColor,
                selectedDates = selectedDates,
                datesWithDots = datesWithDots,
                onDateClick = onDateClick
            )
        }
    }
}

/* ----------------------------- MONTH GRID ----------------------------- */

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MonthGrid(
    month: YearMonth,
    startDate: LocalDate,
    endDate: LocalDate,
    dateBoxColor: Color,
    dateBoxDotColor: Color,
    selectedDates: Set<LocalDate>,
    datesWithDots: Set<LocalDate>,
    onDateClick: (LocalDate) -> Unit
) {
    // Only rebuild when month or date range changes, NOT when selection changes
    val days = remember(month, startDate, endDate) {
        buildMonthCells(
            month = month,
            startDate = startDate,
            endDate = endDate
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MonthTitle(month)

        VerticalSpace(16.dp)

        // Day headers (S M T W T F S)
        DayHeaders()

        VerticalSpace(8.dp)

        // Create rows with stable keys
        days.chunked(7).forEachIndexed { weekIndex, week ->
            key("$month-week-$weekIndex") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    week.forEachIndexed { dayIndex, day ->
                        key("$month-$weekIndex-$dayIndex") {
                            if (day.date == null) {
                                Spacer(Modifier.size(40.dp))
                            } else {
                                DayCell(
                                    date = day.date,
                                    enabled = day.enabled,
                                    isSelected = day.date in selectedDates,
                                    hasDot = day.date in datesWithDots,
                                    dateBoxColor = dateBoxColor,
                                    dateBoxDotColor = dateBoxDotColor,
                                    onClick = onDateClick
                                )
                            }
                        }
                    }
                }
                if (weekIndex < days.chunked(7).lastIndex) {
                    VerticalSpace(12.dp)
                }
            }
        }
    }
}

/* ----------------------------- DAY HEADERS ----------------------------- */

@Composable
private fun DayHeaders() {
    val dayNames = listOf("S", "M", "T", "W", "T", "F", "S")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        dayNames.forEach { dayName ->
            Text(
                text = dayName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
                modifier = Modifier.size(40.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

/* ----------------------------- DAY CELL ----------------------------- */

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DayCell(
    date: LocalDate,
    enabled: Boolean,
    isSelected: Boolean,
    hasDot: Boolean,
    dateBoxColor: Color,
    dateBoxDotColor: Color,
    onClick: (LocalDate) -> Unit
) {
    val boxShape = remember { RoundedCornerShape(8.dp) }
    val dotShape = remember { RoundedCornerShape(50) }

    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                color = if (isSelected) dateBoxColor else Color.Transparent,
                shape = boxShape
            )
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = if (enabled) Color.Gray else Color.Transparent,
                shape = boxShape
            )
            .clickable(enabled = enabled) { onClick(date) },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = if (enabled) Color.White else Color.DarkGray,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )

            if (hasDot) {
                Spacer(Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(dateBoxDotColor, dotShape)
                )
            }
        }
    }
}

/* ----------------------------- UI MODELS ----------------------------- */

data class DayUiModel(
    val date: LocalDate?,
    val enabled: Boolean
)

/* ----------------------------- HELPERS ----------------------------- */

@RequiresApi(Build.VERSION_CODES.O)
private fun buildMonthCells(
    month: YearMonth,
    startDate: LocalDate,
    endDate: LocalDate
): List<DayUiModel> {
    val firstDayIndex = month.atDay(1).dayOfWeek.value % 7
    val daysInMonth = month.lengthOfMonth()
    val totalCells = ((firstDayIndex + daysInMonth + 6) / 7) * 7

    return List(totalCells) { index ->
        val dayNumber = index - firstDayIndex + 1
        if (dayNumber !in 1..daysInMonth) {
            DayUiModel(null, false)
        } else {
            val date = month.atDay(dayNumber)
            DayUiModel(
                date = date,
                enabled = date in startDate..endDate
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun generateMonthRange(
    start: LocalDate,
    end: LocalDate
): List<YearMonth> {
    val months = mutableListOf<YearMonth>()
    var cursor = YearMonth.from(start)
    val endMonth = YearMonth.from(end)

    while (!cursor.isAfter(endMonth)) {
        months.add(cursor)
        cursor = cursor.plusMonths(1)
    }
    return months
}

/* ----------------------------- STATIC UI ----------------------------- */

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MonthTitle(month: YearMonth) {
    Text(
        text = "${month.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${month.year}",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
    )
}