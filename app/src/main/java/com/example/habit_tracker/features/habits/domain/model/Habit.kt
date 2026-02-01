package com.example.habit_tracker.features.habits.domain.model

data class Habit(
    val id: String,
    val userId: String,
    val name: String,
    val description: String,
    val completedDates: List<String>,
    val color: String,
    val category: String,
    val createdDate: String,
    val updatedDate: String,
    val maxStreak: Int,
    val isActive: Boolean
)
