package io.github.gaurav95998030.habittracker.features.habits.domain.repository

import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit

abstract class HabitRepository {
    abstract suspend  fun addHabit(habit: Habit)
    abstract  suspend fun getHabits():List<Habit>
    abstract suspend fun updateHabit(habit: Habit)
    abstract suspend fun updateHabitCompletion(habit: Habit)
}
