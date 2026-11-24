package io.github.gaurav95998030.habittracker.features.habits.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.gaurav95998030.habittracker.features.motivation.data.local.EnergyCheckinEntity
import io.github.gaurav95998030.habittracker.features.motivation.data.local.HabitStackEntity
import io.github.gaurav95998030.habittracker.features.motivation.data.local.MissLogEntity
import io.github.gaurav95998030.habittracker.features.motivation.data.local.MotivationDao
import io.github.gaurav95998030.habittracker.features.motivation.data.local.StackStepEntity

@Database(
    entities = [
        HabitEntity::class,
        MissLogEntity::class,
        EnergyCheckinEntity::class,
        HabitStackEntity::class,
        StackStepEntity::class,
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(ListConverter::class)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun motivationDao(): MotivationDao

    companion object {
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE habits ADD COLUMN reminderEnabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE habits ADD COLUMN reminderTime TEXT")
            }
        }

        /** v3: per-habit Phosphor icon name (nullable → falls back to the category icon). */
        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE habits ADD COLUMN icon TEXT")
            }
        }

        /** v4: miss reasons, daily energy check-ins, habit stacks + their steps. */
        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `miss_logs` (`habitId` TEXT NOT NULL, `date` TEXT NOT NULL, `reason` TEXT NOT NULL, `loggedAt` INTEGER NOT NULL, PRIMARY KEY(`habitId`, `date`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `energy_checkins` (`date` TEXT NOT NULL, `level` INTEGER NOT NULL, PRIMARY KEY(`date`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `habit_stacks` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `createdDate` TEXT NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `stack_steps` (`stackId` TEXT NOT NULL, `habitId` TEXT NOT NULL, `position` INTEGER NOT NULL, `minutes` INTEGER, PRIMARY KEY(`stackId`, `habitId`))")
            }
        }
    }
}
