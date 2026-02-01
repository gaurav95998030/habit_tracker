package com.example.habit_tracker.features.habits.domain.repository

import com.example.habit_tracker.features.habits.domain.model.Habit

abstract class HabitRepository {
    abstract suspend  fun addHabit(habit: Habit)
    abstract  suspend fun getHabits():List<Habit>
    abstract suspend fun updateHabit(habit: Habit)
    abstract suspend fun updateHabitCompletion(habit: Habit)
}
