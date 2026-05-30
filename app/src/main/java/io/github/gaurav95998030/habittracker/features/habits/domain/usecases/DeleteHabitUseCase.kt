package io.github.gaurav95998030.habittracker.features.habits.domain.usecases

import io.github.gaurav95998030.habittracker.features.habits.domain.repository.HabitRepository
import jakarta.inject.Inject

class DeleteHabitUseCase @Inject constructor(
    private val habitRepository: HabitRepository
) {
    suspend operator fun invoke(habitId: String) {
        habitRepository.deleteHabit(habitId)
    }
}
