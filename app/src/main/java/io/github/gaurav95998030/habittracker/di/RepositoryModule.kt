package io.github.gaurav95998030.habittracker.di

import io.github.gaurav95998030.habittracker.features.habits.data.local.HabitDao
import io.github.gaurav95998030.habittracker.features.habits.data.repository.HabitRepositoryImpl
import io.github.gaurav95998030.habittracker.features.habits.domain.repository.HabitRepository
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

