package com.example.habit_tracker.features.habits.data.local

import com.example.habit_tracker.features.habits.domain.model.Habit

fun Habit.toEntity() = HabitEntity(
    pId = 0,              // let Room auto-generate
    id = id,
    userId = userId,
    name = name,
    description = description,
    completedDates = completedDates,
    color = color,
    category = category,
    createdDate = createdDate,
    updatedDate = updatedDate,
    maxStreak = maxStreak,
    isActive = isActive
)

fun HabitEntity.toDomain() = Habit(
    id = id,              // use business id
    userId = userId,
    name = name,
    description = description,
    completedDates = completedDates,
    color = color,
    category = category,
    createdDate = createdDate,
    updatedDate = updatedDate,
    maxStreak = maxStreak,
    isActive = isActive
)

