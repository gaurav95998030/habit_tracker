package com.example.habit_tracker.features.habits.data.remote.firestore

import com.example.habit_tracker.features.habits.domain.model.Habit

interface HabitRemoteDataSource {
    suspend fun createHabit(habit: Habit)
    suspend fun getHabits(): List<Habit>
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(id: String)
}
