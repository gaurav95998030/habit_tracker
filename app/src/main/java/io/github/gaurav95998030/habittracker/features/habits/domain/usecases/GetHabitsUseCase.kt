package io.github.gaurav95998030.habittracker.features.habits.domain.usecases

import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit
import io.github.gaurav95998030.habittracker.features.habits.domain.repository.HabitRepository
import jakarta.inject.Inject

class GetHabitsUseCase @Inject constructor(
    private val habitRepository: HabitRepository
) {
    suspend operator fun invoke(): List<Habit> {
        return habitRepository.getHabits()
    }
}