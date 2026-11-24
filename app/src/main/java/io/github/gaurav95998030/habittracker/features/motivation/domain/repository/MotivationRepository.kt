package io.github.gaurav95998030.habittracker.features.motivation.domain.repository

import io.github.gaurav95998030.habittracker.features.motivation.domain.model.EnergyCheckin
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.EnergyLevel
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.HabitStack
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.MissLog
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.MissReason
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.StackStep
import kotlinx.coroutines.flow.Flow

/** Miss reasons, energy check-ins and habit stacks. Dates are "yyyy-MM-dd". */
interface MotivationRepository {
    fun observeMissLogs(since: String): Flow<List<MissLog>>
    suspend fun missLogsOn(date: String): List<MissLog>
    suspend fun logMiss(habitId: String, date: String, reason: MissReason)
    suspend fun clearMiss(habitId: String, date: String)

    fun observeEnergy(since: String): Flow<List<EnergyCheckin>>
    suspend fun setEnergy(date: String, level: EnergyLevel?)

    fun observeStacks(): Flow<List<HabitStack>>
    suspend fun saveStack(id: String, name: String, createdDate: String, steps: List<StackStep>)
    suspend fun deleteStack(id: String)
}
