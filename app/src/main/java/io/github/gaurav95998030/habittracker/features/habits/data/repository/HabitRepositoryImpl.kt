package io.github.gaurav95998030.habittracker.features.habits.data.repository

import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit
import io.github.gaurav95998030.habittracker.features.habits.data.local.HabitDao
import io.github.gaurav95998030.habittracker.features.habits.data.local.HabitEntity
import io.github.gaurav95998030.habittracker.features.habits.data.local.toDomain
import io.github.gaurav95998030.habittracker.features.habits.data.local.toEntity
import io.github.gaurav95998030.habittracker.features.habits.domain.repository.HabitRepository


class HabitRepositoryImpl(
    private val dao: HabitDao,
): HabitRepository(

) {



  override  suspend fun addHabit(habit: Habit) {
      dao.insertHabit(habit.toEntity())
    }

    override suspend fun getHabits(): List<Habit> {
       val habit: List<HabitEntity> =  dao.getHabits();
        return habit.map { it.toDomain() }
    }
    override suspend fun updateHabit(habit: Habit) {
        dao.updateHabit(habit.toEntity())
    }

    override suspend fun updateHabitCompletion(habit: Habit) {
        dao.updateHabitCompletion(id = habit.id, completed = habit.completedDates)
    }
}