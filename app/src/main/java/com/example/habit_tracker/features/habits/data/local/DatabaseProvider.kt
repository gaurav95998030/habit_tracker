package com.example.habit_tracker.features.habits.data.local

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    fun provide(context: Context): HabitDatabase =
        Room.databaseBuilder(
            context,
            HabitDatabase::class.java,
            "habit_db"
        ).build()
}
