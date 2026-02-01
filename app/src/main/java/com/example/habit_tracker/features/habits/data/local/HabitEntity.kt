package com.example.habit_tracker.features.habits.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val pId: Long = 0,          // auto-generated PK (unique per row)
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

