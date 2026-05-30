package io.github.gaurav95998030.habittracker.features.habits.data.local

import androidx.room.*

@Dao
interface HabitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity)

    @Query("SELECT * FROM habits")
    suspend fun getHabits(): List<HabitEntity>

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("""
    UPDATE habits
    SET name = :name,
        description = :description,
        color = :color,
        category = :category,
        updatedDate = :updatedDate
    WHERE id = :id
""")
    suspend fun updateHabitFields(
        id: String,
        name: String,
        description: String,
        color: String,
        category: String,
        updatedDate: String
    )

    @Query("""
    UPDATE habits
    SET completedDates = :completed
    WHERE id = :id
""")
    suspend fun updateHabitCompletion(
        id: String,
        completed: List<String>
    )

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabit(id: String)
}
