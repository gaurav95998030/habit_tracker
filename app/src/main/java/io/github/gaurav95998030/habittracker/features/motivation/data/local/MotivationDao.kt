package io.github.gaurav95998030.habittracker.features.motivation.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MotivationDao {

    // ── Miss logs ──
    @Query("SELECT * FROM miss_logs WHERE date >= :since ORDER BY date DESC")
    fun observeMissLogs(since: String): Flow<List<MissLogEntity>>

    @Query("SELECT * FROM miss_logs WHERE date = :date")
    suspend fun missLogsOn(date: String): List<MissLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMissLog(log: MissLogEntity)

    @Query("DELETE FROM miss_logs WHERE habitId = :habitId AND date = :date")
    suspend fun deleteMissLog(habitId: String, date: String)

    // ── Energy ──
    @Query("SELECT * FROM energy_checkins WHERE date >= :since ORDER BY date")
    fun observeEnergy(since: String): Flow<List<EnergyCheckinEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEnergy(checkin: EnergyCheckinEntity)

    @Query("DELETE FROM energy_checkins WHERE date = :date")
    suspend fun deleteEnergy(date: String)

    // ── Stacks ──
    @Query("SELECT * FROM habit_stacks ORDER BY createdDate")
    fun observeStacks(): Flow<List<HabitStackEntity>>

    @Query("SELECT * FROM stack_steps ORDER BY stackId, position")
    fun observeSteps(): Flow<List<StackStepEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStack(stack: HabitStackEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSteps(steps: List<StackStepEntity>)

    @Query("DELETE FROM stack_steps WHERE stackId = :stackId")
    suspend fun deleteSteps(stackId: String)

    @Query("DELETE FROM habit_stacks WHERE id = :stackId")
    suspend fun deleteStackRow(stackId: String)

    @Transaction
    suspend fun deleteStack(stackId: String) {
        deleteSteps(stackId)
        deleteStackRow(stackId)
    }

    /** Replace a stack's steps wholesale (add / remove / reorder in one write). */
    @Transaction
    suspend fun replaceSteps(stackId: String, steps: List<StackStepEntity>) {
        deleteSteps(stackId)
        upsertSteps(steps)
    }
}
