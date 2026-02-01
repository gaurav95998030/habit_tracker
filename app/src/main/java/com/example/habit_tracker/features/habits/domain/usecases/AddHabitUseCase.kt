package com.example.habit_tracker.features.habits.domain.usecases

import com.example.habit_tracker.features.habits.domain.model.Habit
import com.example.habit_tracker.features.habits.domain.repository.HabitRepository

class AddHabitUseCase(
    private val habitRepository: HabitRepository
) {
    suspend operator fun invoke(habit: Habit) {
        habitRepository.addHabit(habit)
    }
}