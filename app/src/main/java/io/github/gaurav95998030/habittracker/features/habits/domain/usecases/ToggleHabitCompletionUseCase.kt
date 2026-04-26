package io.github.gaurav95998030.habittracker.features.habits.domain.usecases

import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit
import io.github.gaurav95998030.habittracker.features.habits.domain.repository.HabitRepository
import jakarta.inject.Inject

class ToggleHabitCompletionUseCase @Inject constructor ( private val habitRepository: HabitRepository
) {
    suspend operator fun invoke(updatedHabit: Habit) {
        return habitRepository.updateHabitCompletion(updatedHabit)
    }
}