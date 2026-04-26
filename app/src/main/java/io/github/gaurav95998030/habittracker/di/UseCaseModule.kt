package io.github.gaurav95998030.habittracker.di

import io.github.gaurav95998030.habittracker.features.habits.domain.repository.HabitRepository
import io.github.gaurav95998030.habittracker.features.habits.domain.usecases.AddHabitUseCase
import io.github.gaurav95998030.habittracker.features.habits.domain.usecases.GetHabitsUseCase
import io.github.gaurav95998030.habittracker.features.habits.domain.usecases.ToggleHabitCompletionUseCase
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

