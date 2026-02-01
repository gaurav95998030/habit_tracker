package com.example.habit_tracker.di

import com.example.habit_tracker.features.habits.domain.repository.HabitRepository
import com.example.habit_tracker.features.habits.domain.usecases.AddHabitUseCase
import com.example.habit_tracker.features.habits.domain.usecases.GetHabitsUseCase
import com.example.habit_tracker.features.habits.domain.usecases.ToggleHabitCompletionUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides
    @ViewModelScoped
    fun provideAddHabitUseCase(
        habitRepository: HabitRepository
    ): AddHabitUseCase {
        return AddHabitUseCase(habitRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideGetHabitsUseCase(
        habitRepository: HabitRepository
    ): GetHabitsUseCase {
        return GetHabitsUseCase(habitRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideToggleHabitCompletionUseCase(
        habitRepository: HabitRepository
    ): ToggleHabitCompletionUseCase {
        return ToggleHabitCompletionUseCase(habitRepository)
    }
}

