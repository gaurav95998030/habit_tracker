package com.example.habit_tracker.util

import com.example.habit_tracker.domain.model.HeatmapRange
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests for DateRangeCalculator.
 * Validates date range calculations for all HeatmapRange types.
 */
class DateRangeCalculatorTest {
    
    @Test
    fun `calculateDateRange YEARLY returns full current year`() {
        val referenceDate = LocalDate.of(2024, 6, 15)
        val range = DateRangeCalculator.calculateDateRange(
            HeatmapRange.YEARLY,
            referenceDate
        )
        
        assertEquals("2024-01-01", range.startDate)
        assertEquals("2024-12-31", range.endDate)
    }
    
    @Test
    fun `calculateDateRange LAST_30_DAYS returns 30 days including today`() {
        val referenceDate = LocalDate.of(2024, 6, 15)
        val range = DateRangeCalculator.calculateDateRange(
            HeatmapRange.LAST_30_DAYS,
            referenceDate
        )
        
        assertEquals("2024-05-17", range.startDate) // 29 days before June 15
        assertEquals("2024-06-15", range.endDate)
    }
    
    @Test
    fun `calculateDateRange LAST_7_DAYS returns 7 days including today`() {
        val referenceDate = LocalDate.of(2024, 6, 15)
        val range = DateRangeCalculator.calculateDateRange(
            HeatmapRange.LAST_7_DAYS,
            referenceDate
        )
        
        assertEquals("2024-06-09", range.startDate) // 6 days before June 15
        assertEquals("2024-06-15", range.endDate)
    }
    
    @Test
    fun `generateDatesInRange produces all dates in range`() {
        val dateRange = com.example.habit_tracker.domain.model.DateRange(
            startDate = "2024-01-01",
            endDate = "2024-01-05"
        )
        
        val dates = DateRangeCalculator.generateDatesInRange(dateRange)
        
        assertEquals(5, dates.size)
        assertEquals("2024-01-01", dates[0])
        assertEquals("2024-01-05", dates[4])
    }
    
    @Test
    fun `generateDatesInRange handles single day range`() {
        val dateRange = com.example.habit_tracker.domain.model.DateRange(
            startDate = "2024-01-01",
            endDate = "2024-01-01"
        )
        
        val dates = DateRangeCalculator.generateDatesInRange(dateRange)
        
        assertEquals(1, dates.size)
        assertEquals("2024-01-01", dates[0])
    }
    
    @Test
    fun `generateDatesInRange handles month boundaries`() {
        val dateRange = com.example.habit_tracker.domain.model.DateRange(
            startDate = "2024-01-30",
            endDate = "2024-02-02"
        )
        
        val dates = DateRangeCalculator.generateDatesInRange(dateRange)
        
        assertEquals(4, dates.size)
        assertEquals("2024-01-30", dates[0])
        assertEquals("2024-02-02", dates[3])
    }
}

