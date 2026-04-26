package io.github.gaurav95998030.habittracker.features.habits.data.remote.firestore

import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit

interface HabitRemoteDataSource {
    suspend fun createHabit(habit: Habit)
    suspend fun getHabits(): List<Habit>
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(id: String)
}
