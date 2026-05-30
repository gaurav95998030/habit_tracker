package io.github.gaurav95998030.habittracker.features.habits.domain.usecases

import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit
import io.github.gaurav95998030.habittracker.features.habits.domain.repository.HabitRepository
import jakarta.inject.Inject

class UpdateHabitUseCase @Inject constructor(
    private val habitRepository: HabitRepository
) {
    suspend operator fun invoke(habit: Habit) {
        habitRepository.updateHabit(habit)
    }
}
