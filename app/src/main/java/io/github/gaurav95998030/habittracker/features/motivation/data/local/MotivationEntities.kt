package io.github.gaurav95998030.habittracker.features.motivation.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Why a habit was missed on [date] ("yyyy-MM-dd"). One reason per habit per day. */
@Entity(tableName = "miss_logs", primaryKeys = ["habitId", "date"])
data class MissLogEntity(
    val habitId: String,
    val date: String,
    val reason: String,
    val loggedAt: Long,
)

/** One energy rating (1 = drained … 5 = charged) per day. */
@Entity(tableName = "energy_checkins")
data class EnergyCheckinEntity(
    @PrimaryKey val date: String,
    val level: Int,
)

/** A named chain of habits done in order ("Morning stack"). */
@Entity(tableName = "habit_stacks")
data class HabitStackEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdDate: String,
)

/**
 * A habit's place in a stack. Rows are not cascaded when a habit is deleted (so undo
 * can restore it); readers drop steps whose habit no longer exists.
 */
@Entity(tableName = "stack_steps", primaryKeys = ["stackId", "habitId"])
data class StackStepEntity(
    val stackId: String,
    val habitId: String,
    val position: Int,
    /** Optional "~N min" estimate shown on the step. */
    val minutes: Int?,
)
