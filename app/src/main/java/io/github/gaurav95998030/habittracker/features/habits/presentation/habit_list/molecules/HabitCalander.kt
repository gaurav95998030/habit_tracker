package io.github.gaurav95998030.habittracker.features.habits.presentation.habit_list.molecules

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.gaurav95998030.habittracker.commons.ui.VerticalSpace
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

private val CELL_SIZE = 40.dp
private val CELL_SHAPE = RoundedCornerShape(12.dp)
private val DOT_SHAPE = CircleShape

/* ----------------------------- PUBLIC API ----------------------------- */

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabitCalendar(
    startDate: LocalDate,
    endDate: LocalDate,
    modifier: Modifier = Modifier,
    dateBoxColor: Color = AppTheme.colors.primary,
    dateBoxDotColor: Color = AppTheme.colors.primary,
    selectedDates: Set<LocalDate> = emptySet(),
    datesWithDots: Set<LocalDate> = emptySet(),
    onDateClick: (LocalDate) -> Unit = {},
    initialMonth: YearMonth? = null
) {
    val months = remember(startDate, endDate) {
        generateMonthRange(startDate, endDate)
    }

    val initialPage = remember(months, initialMonth) {
        if (initialMonth != null) {
            months.indexOf(initialMonth).coerceAtLeast(0)
        } else {
            val currentMonth = YearMonth.now()
            val index = months.indexOf(currentMonth)
            if (index >= 0) index else months.lastIndex.coerceAtLeast(0)
        }
    }

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { months.size }
    )
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        MonthNavigator(
            month = months.getOrNull(pagerState.currentPage) ?: YearMonth.now(),
            canGoPrev = pagerState.currentPage > 0,
            canGoNext = pagerState.currentPage < months.lastIndex,
            onPrev = {
                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
            },
            onNext = {
                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
            }
        )

        VerticalSpace(12.dp)

        DayHeaders()

        VerticalSpace(4.dp)

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
            pageSpacing = 12.dp,
            beyondViewportPageCount = 1,
            key = { pageIndex -> months[pageIndex].toString() }
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

/* ----------------------------- MONTH NAV ----------------------------- */

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MonthNavigator(
    month: YearMonth,
    canGoPrev: Boolean,
    canGoNext: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavChevron(
            icon = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
            contentDescription = "Previous month",
            enabled = canGoPrev,
            onClick = onPrev
        )
        Text(
            text = "${month.month.getDisplayName(JavaTextStyle.FULL, Locale.getDefault())} ${month.year}",
            style = AppTheme.textStyles.titleMedium.copy(
                color = colors.textPrimary,
                fontWeight = FontWeight.SemiBold
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        NavChevron(
            icon = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = "Next month",
            enabled = canGoNext,
            onClick = onNext
        )
    }
}

@Composable
private fun NavChevron(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors
    val tint by animateColorAsState(
        targetValue = if (enabled) colors.textPrimary else colors.textTertiary.copy(alpha = 0.4f),
        label = "chevron-tint"
    )
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint
        )
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
    val weeks = remember(month, startDate, endDate) {
        buildMonthCells(month, startDate, endDate).chunked(7)
    }
    val lastWeekIndex = weeks.lastIndex

    val monthSelected = remember(month, selectedDates) {
        selectedDates.filterTo(HashSet()) { YearMonth.from(it) == month }
    }
    val monthDots = remember(month, datesWithDots) {
        datesWithDots.filterTo(HashSet()) { YearMonth.from(it) == month }
    }
    val today = remember { LocalDate.now() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        weeks.forEachIndexed { weekIndex, week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                week.forEach { day ->
                    if (day.date == null) {
                        Spacer(Modifier.size(CELL_SIZE))
                    } else {
                        DayCell(
                            date = day.date,
                            enabled = day.enabled,
                            isSelected = day.date in monthSelected,
                            hasDot = day.date in monthDots,
                            isToday = day.date == today,
                            dateBoxColor = dateBoxColor,
                            dateBoxDotColor = dateBoxDotColor,
                            onClick = onDateClick
                        )
                    }
                }
            }
            if (weekIndex < lastWeekIndex) {
                VerticalSpace(8.dp)
            }
        }
    }
}

/* ----------------------------- DAY HEADERS ----------------------------- */

@Composable
private fun DayHeaders() {
    val colors = AppTheme.colors
    val dayNames = listOf("S", "M", "T", "W", "T", "F", "S")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        dayNames.forEachIndexed { index, dayName ->
            val isWeekend = index == 0 || index == 6
            Text(
                text = dayName,
                style = AppTheme.textStyles.labelSmall.copy(
                    color = if (isWeekend) colors.textTertiary else colors.textSecondary,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.size(CELL_SIZE),
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
    isToday: Boolean,
    dateBoxColor: Color,
    dateBoxDotColor: Color,
    onClick: (LocalDate) -> Unit
) {
    val colors = AppTheme.colors

    val targetBg = when {
        isSelected -> dateBoxColor
        isToday && enabled -> dateBoxColor.copy(alpha = 0.12f)
        else -> Color.Transparent
    }
    val bgColor by animateColorAsState(targetValue = targetBg, label = "cell-bg")

    val targetText = when {
        !enabled -> colors.textTertiary.copy(alpha = 0.5f)
        isSelected -> contrastOn(dateBoxColor)
        isToday -> dateBoxColor
        else -> colors.textPrimary
    }
    val textColor by animateColorAsState(targetValue = targetText, label = "cell-text")

    val borderWidth by animateDpAsState(
        targetValue = if (isToday && !isSelected) 1.5.dp else 0.dp,
        label = "cell-border"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .size(CELL_SIZE)
            .clip(CELL_SHAPE)
            .background(color = bgColor, shape = CELL_SHAPE)
            .border(width = borderWidth, color = dateBoxColor, shape = CELL_SHAPE)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                enabled = enabled
            ) { onClick(date) },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = AppTheme.textStyles.bodyMedium.copy(
                    color = textColor,
                    fontWeight = when {
                        isSelected -> FontWeight.Bold
                        isToday -> FontWeight.SemiBold
                        else -> FontWeight.Medium
                    }
                ),
                textAlign = TextAlign.Center
            )

            if (hasDot && !isSelected) {
                Spacer(Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(dateBoxDotColor, DOT_SHAPE)
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

private fun contrastOn(background: Color): Color {
    val luminance = 0.299 * background.red + 0.587 * background.green + 0.114 * background.blue
    return if (luminance > 0.55) Color(0xFF111111) else Color.White
}

@RequiresApi(Build.VERSION_CODES.O)
private fun buildMonthCells(
    month: YearMonth,
    startDate: LocalDate,
    endDate: LocalDate
): List<DayUiModel> {
    val firstDayIndex = month.atDay(1).dayOfWeek.sundayBasedIndex()
    val daysInMonth = month.lengthOfMonth()
    val totalCells = ((firstDayIndex + daysInMonth + 6) / 7) * 7
    val today = LocalDate.now()

    return List(totalCells) { index ->
        val dayNumber = index - firstDayIndex + 1
        if (dayNumber !in 1..daysInMonth) {
            DayUiModel(null, false)
        } else {
            val date = month.atDay(dayNumber)
            DayUiModel(
                date = date,
                enabled = date in startDate..endDate && !date.isAfter(today)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun DayOfWeek.sundayBasedIndex(): Int = value % 7

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
