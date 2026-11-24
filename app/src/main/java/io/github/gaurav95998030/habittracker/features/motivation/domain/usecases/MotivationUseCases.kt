package io.github.gaurav95998030.habittracker.features.motivation.domain.usecases

import io.github.gaurav95998030.habittracker.features.motivation.domain.model.EnergyCheckin
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.EnergyLevel
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.HabitStack
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.MissLog
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.MissReason
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.StackStep
import io.github.gaurav95998030.habittracker.features.motivation.domain.repository.MotivationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// One class per action, registered in di/UseCaseModule (project convention).

class ObserveMissLogsUseCase @Inject constructor(private val repo: MotivationRepository) {
    operator fun invoke(since: String): Flow<List<MissLog>> = repo.observeMissLogs(since)
}

class LogMissUseCase @Inject constructor(private val repo: MotivationRepository) {
    suspend operator fun invoke(habitId: String, date: String, reason: MissReason?) {
        if (reason == null) repo.clearMiss(habitId, date) else repo.logMiss(habitId, date, reason)
    }
}

class ObserveEnergyUseCase @Inject constructor(private val repo: MotivationRepository) {
    operator fun invoke(since: String): Flow<List<EnergyCheckin>> = repo.observeEnergy(since)
}

class SetEnergyUseCase @Inject constructor(private val repo: MotivationRepository) {
    suspend operator fun invoke(date: String, level: EnergyLevel?) = repo.setEnergy(date, level)
}

class ObserveStacksUseCase @Inject constructor(private val repo: MotivationRepository) {
    operator fun invoke(): Flow<List<HabitStack>> = repo.observeStacks()
}

class SaveStackUseCase @Inject constructor(private val repo: MotivationRepository) {
    suspend operator fun invoke(id: String, name: String, createdDate: String, steps: List<StackStep>) =
        repo.saveStack(id, name, createdDate, steps)
}

class DeleteStackUseCase @Inject constructor(private val repo: MotivationRepository) {
    suspend operator fun invoke(id: String) = repo.deleteStack(id)
}
