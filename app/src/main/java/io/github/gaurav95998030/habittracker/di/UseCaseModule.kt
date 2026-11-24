package io.github.gaurav95998030.habittracker.di

import io.github.gaurav95998030.habittracker.features.habits.domain.repository.HabitRepository
import io.github.gaurav95998030.habittracker.features.habits.domain.usecases.AddHabitUseCase
import io.github.gaurav95998030.habittracker.features.habits.domain.usecases.DeleteHabitUseCase
import io.github.gaurav95998030.habittracker.features.habits.domain.usecases.GetHabitsUseCase
import io.github.gaurav95998030.habittracker.features.habits.domain.usecases.ToggleHabitCompletionUseCase
import io.github.gaurav95998030.habittracker.features.habits.domain.usecases.UpdateHabitUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

import io.github.gaurav95998030.habittracker.features.motivation.domain.repository.MotivationRepository
import io.github.gaurav95998030.habittracker.features.motivation.domain.usecases.ObserveMissLogsUseCase
import io.github.gaurav95998030.habittracker.features.motivation.domain.usecases.LogMissUseCase
import io.github.gaurav95998030.habittracker.features.motivation.domain.usecases.ObserveEnergyUseCase
import io.github.gaurav95998030.habittracker.features.motivation.domain.usecases.SetEnergyUseCase
import io.github.gaurav95998030.habittracker.features.motivation.domain.usecases.ObserveStacksUseCase
import io.github.gaurav95998030.habittracker.features.motivation.domain.usecases.SaveStackUseCase
import io.github.gaurav95998030.habittracker.features.motivation.domain.usecases.DeleteStackUseCase

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

    @Provides
    @ViewModelScoped
    fun provideUpdateHabitUseCase(
        habitRepository: HabitRepository
    ): UpdateHabitUseCase {
        return UpdateHabitUseCase(habitRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideDeleteHabitUseCase(
        habitRepository: HabitRepository
    ): DeleteHabitUseCase {
        return DeleteHabitUseCase(habitRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideObserveMissLogsUseCase(repo: MotivationRepository): ObserveMissLogsUseCase = ObserveMissLogsUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideLogMissUseCase(repo: MotivationRepository): LogMissUseCase = LogMissUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideObserveEnergyUseCase(repo: MotivationRepository): ObserveEnergyUseCase = ObserveEnergyUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideSetEnergyUseCase(repo: MotivationRepository): SetEnergyUseCase = SetEnergyUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideObserveStacksUseCase(repo: MotivationRepository): ObserveStacksUseCase = ObserveStacksUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideSaveStackUseCase(repo: MotivationRepository): SaveStackUseCase = SaveStackUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideDeleteStackUseCase(repo: MotivationRepository): DeleteStackUseCase = DeleteStackUseCase(repo)
}
