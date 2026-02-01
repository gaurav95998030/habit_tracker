package com.example.habit_tracker.features.habits.presentation.habit_list.molecules

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import java.time.*
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HabitCalendar(
    startDate: LocalDate,
    endDate: LocalDate,
    modifier: Modifier = Modifier,
    dateBoxColor: Color = Color(0xFF2A2A2A),
    dateBoxDotColor: Color = Color(0xFFE57373),
    selectedDates: Set<LocalDate> = emptySet(),
    datesWithDots: Set<LocalDate> = emptySet(),
    onDateClick: (LocalDate) -> Unit = {}
) {
    val months = remember(startDate, endDate) {
        generateMonthRange(startDate, endDate)
    }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        items(months) { month ->
            MonthGrid(
                month = month,
                startDate = startDate,
                endDate = endDate,
                dateBoxColor = dateBoxColor,
                dateBoxDotColor = dateBoxDotColor,
                selectedDates = selectedDates,
                datesWithDots = datesWithDots,
                onDateClick = onDateClick
            )

            Spacer(Modifier.width(12.dp))
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

    while (!cursor.isAfter(YearMonth.from(end))) {
        months.add(cursor)
        cursor = cursor.plusMonths(1)
    }

    return months
}

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
    Column(
        modifier = Modifier
            .width(280.dp)
            .padding(8.dp)
    ) {
        Text(
            text = "${month.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${month.year}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val firstDayOfMonth = month.atDay(1)
        val firstDayIndex = firstDayOfMonth.dayOfWeek.value % 7
        val daysInMonth = month.lengthOfMonth()

        val totalCells = firstDayIndex + daysInMonth
        val rows = (totalCells + 6) / 7

        Column {
            repeat(rows) { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(7) { col ->
                        val index = row * 7 + col
                        val dayNumber = index - firstDayIndex + 1

                        if (dayNumber in 1..daysInMonth) {
                            val date = month.atDay(dayNumber)

                            val enabled = !date.isBefore(startDate) && !date.isAfter(endDate)
                            val isSelected = selectedDates.contains(date)
                            val hasDot = datesWithDots.contains(date)

                            DayCell(
                                date = date,
                                enabled = enabled,
                                isSelected = isSelected,
                                hasDot = hasDot,
                                dateBoxColor = dateBoxColor,
                                dateBoxDotColor = dateBoxDotColor,
                                onClick = { onDateClick(date) }
                            )
                        } else {
                            Spacer(Modifier.size(36.dp))
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DayCell(
    date: LocalDate,
    enabled: Boolean,
    isSelected: Boolean,
    hasDot: Boolean,
    dateBoxColor: Color,
    dateBoxDotColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(
                color = if (isSelected) dateBoxColor else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = if (enabled) Color.Gray else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = if (enabled) Color.White else Color.DarkGray
            )

            if (hasDot) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(dateBoxDotColor, RoundedCornerShape(50))
                )
            }
        }
    }
}
