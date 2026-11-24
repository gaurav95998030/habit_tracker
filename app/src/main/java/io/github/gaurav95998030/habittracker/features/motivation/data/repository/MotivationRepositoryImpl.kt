package io.github.gaurav95998030.habittracker.features.motivation.data.repository

import io.github.gaurav95998030.habittracker.features.motivation.data.local.EnergyCheckinEntity
import io.github.gaurav95998030.habittracker.features.motivation.data.local.HabitStackEntity
import io.github.gaurav95998030.habittracker.features.motivation.data.local.MissLogEntity
import io.github.gaurav95998030.habittracker.features.motivation.data.local.MotivationDao
import io.github.gaurav95998030.habittracker.features.motivation.data.local.StackStepEntity
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.EnergyCheckin
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.EnergyLevel
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.HabitStack
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.MissLog
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.MissReason
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.StackStep
import io.github.gaurav95998030.habittracker.features.motivation.domain.repository.MotivationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class MotivationRepositoryImpl(private val dao: MotivationDao) : MotivationRepository {

    private fun MissLogEntity.toDomain() = MissReason.fromStorage(reason)?.let { MissLog(habitId, date, it) }

    override fun observeMissLogs(since: String): Flow<List<MissLog>> =
        dao.observeMissLogs(since).map { rows -> rows.mapNotNull { it.toDomain() } }

    override suspend fun missLogsOn(date: String): List<MissLog> = dao.missLogsOn(date).mapNotNull { it.toDomain() }

    override suspend fun logMiss(habitId: String, date: String, reason: MissReason) =
        dao.upsertMissLog(MissLogEntity(habitId, date, reason.name, System.currentTimeMillis()))

    override suspend fun clearMiss(habitId: String, date: String) = dao.deleteMissLog(habitId, date)

    override fun observeEnergy(since: String): Flow<List<EnergyCheckin>> =
        dao.observeEnergy(since).map { rows -> rows.mapNotNull { r -> EnergyLevel.of(r.level)?.let { EnergyCheckin(r.date, it) } } }

    override suspend fun setEnergy(date: String, level: EnergyLevel?) {
        if (level == null) dao.deleteEnergy(date) else dao.upsertEnergy(EnergyCheckinEntity(date, level.level))
    }

    override fun observeStacks(): Flow<List<HabitStack>> =
        combine(dao.observeStacks(), dao.observeSteps()) { stacks, steps ->
            val byStack = steps.groupBy { it.stackId }
            stacks.map { s ->
                HabitStack(
                    id = s.id, name = s.name, createdDate = s.createdDate,
                    steps = byStack[s.id].orEmpty().sortedBy { it.position }.map { StackStep(it.habitId, it.position, it.minutes) },
                )
            }
        }

    override suspend fun saveStack(id: String, name: String, createdDate: String, steps: List<StackStep>) {
        dao.upsertStack(HabitStackEntity(id, name, createdDate))
        dao.replaceSteps(id, steps.mapIndexed { i, s -> StackStepEntity(id, s.habitId, i, s.minutes) })
    }

    override suspend fun deleteStack(id: String) = dao.deleteStack(id)
}
