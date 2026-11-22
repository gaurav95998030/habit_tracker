package io.github.gaurav95998030.habittracker.features.habits.data.local

import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit

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
    isActive = isActive,
    reminderEnabled = reminderEnabled,
    reminderTime = reminderTime,
    icon = icon,
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
    isActive = isActive,
    reminderEnabled = reminderEnabled,
    reminderTime = reminderTime,
    icon = icon,
)
