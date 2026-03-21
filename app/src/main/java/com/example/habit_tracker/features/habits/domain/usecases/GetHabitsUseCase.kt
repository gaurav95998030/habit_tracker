package com.example.habit_tracker.features.habits.domain.usecases

import com.example.habit_tracker.features.habits.domain.model.Habit
import com.example.habit_tracker.features.habits.domain.repository.HabitRepository
import jakarta.inject.Inject

class GetHabitsUseCase @Inject constructor(
    private val habitRepository: HabitRepository
) {
    suspend operator fun invoke(): List<Habit> {
        return habitRepository.getHabits()
    }
}