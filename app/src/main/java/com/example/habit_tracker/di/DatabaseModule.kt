package com.example.habit_tracker.di

import android.content.Context
import androidx.room.Room
import com.example.habit_tracker.features.habits.data.local.HabitDao
import com.example.habit_tracker.features.habits.data.local.HabitDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideHabitDatabase(
        @ApplicationContext context: Context
    ): HabitDatabase {
        return Room.databaseBuilder(
            context,
            HabitDatabase::class.java,
            "habit_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideHabitDao(database: HabitDatabase): HabitDao {
        return database.habitDao()
    }
}

