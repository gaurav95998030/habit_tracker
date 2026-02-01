package com.example.habit_tracker.features.habits.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [HabitEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(ListConverter::class)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
}
