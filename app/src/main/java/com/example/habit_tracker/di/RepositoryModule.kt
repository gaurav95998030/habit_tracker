package com.example.habit_tracker.di

import com.example.habit_tracker.features.habits.data.local.HabitDao
import com.example.habit_tracker.features.habits.data.repository.HabitRepositoryImpl
import com.example.habit_tracker.features.habits.domain.repository.HabitRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideHabitRepository(
        habitDao: HabitDao
    ): HabitRepository {
        return HabitRepositoryImpl(habitDao)
    }
}

