package com.example.habit_tracker.domain.mapper

import com.example.habit_tracker.domain.model.HabitCompletion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for CompletionMapper.
 * Validates mapping logic from HabitCompletion to UI-friendly structures.
 */
class CompletionMapperTest {
    
    @Test
    fun `toCompletedDateSet filters only completed dates`() {
        val completions = listOf(
            HabitCompletion("habit1", "2024-01-01", true),
            HabitCompletion("habit1", "2024-01-02", false),
            HabitCompletion("habit1", "2024-01-03", true),
            HabitCompletion("habit1", "2024-01-04", false)
        )
        
        val result = CompletionMapper.toCompletedDateSet(completions)
        
        assertEquals(2, result.size)
        assertTrue(result.contains("2024-01-01"))
        assertTrue(result.contains("2024-01-03"))
    }
    
    @Test
    fun `toCompletedDateSet returns empty set for no completions`() {
        val result = CompletionMapper.toCompletedDateSet(emptyList())
        
        assertTrue(result.isEmpty())
    }
    
    @Test
    fun `toCompletedDateSet returns empty set when all incomplete`() {
        val completions = listOf(
            HabitCompletion("habit1", "2024-01-01", false),
            HabitCompletion("habit1", "2024-01-02", false)
        )
        
        val result = CompletionMapper.toCompletedDateSet(completions)
        
        assertTrue(result.isEmpty())
    }
    
    @Test
    fun `toCompletionMap includes all dates with default false`() {
        val completions = listOf(
            HabitCompletion("habit1", "2024-01-01", true),
            HabitCompletion("habit1", "2024-01-03", true)
        )
        
        val allDates = listOf("2024-01-01", "2024-01-02", "2024-01-03", "2024-01-04")
        
        val result = CompletionMapper.toCompletionMap(completions, allDates)
        
        assertEquals(4, result.size)
        assertEquals(true, result["2024-01-01"])
        assertEquals(false, result["2024-01-02"])
        assertEquals(true, result["2024-01-03"])
        assertEquals(false, result["2024-01-04"])
    }
    
    @Test
    fun `toCompletionMap handles empty completions list`() {
        val allDates = listOf("2024-01-01", "2024-01-02")
        
        val result = CompletionMapper.toCompletionMap(emptyList(), allDates)
        
        assertEquals(2, result.size)
        assertEquals(false, result["2024-01-01"])
        assertEquals(false, result["2024-01-02"])
    }
}

