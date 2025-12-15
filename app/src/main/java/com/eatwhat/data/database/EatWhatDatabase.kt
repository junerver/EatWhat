package com.eatwhat.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.eatwhat.data.database.dao.HistoryDao
import com.eatwhat.data.database.dao.RecipeDao
import com.eatwhat.data.database.dao.TagDao
import com.eatwhat.data.database.entities.*

/**
 * Room database for EatWhat app
 * Version 1: Initial schema with all entities
 * Version 2: Add is_locked column to history_records
 * Version 3: Add custom_name column to history_records
 */
@Database(
    entities = [
        RecipeEntity::class,
        IngredientEntity::class,
        CookingStepEntity::class,
        TagEntity::class,
        RecipeTagCrossRef::class,
        HistoryRecordEntity::class,
        HistoryRecipeCrossRef::class,
        PrepItemEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class EatWhatDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun historyDao(): HistoryDao
    abstract fun tagDao(): TagDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add is_locked column with default value 0 (false)
                db.execSQL("ALTER TABLE history_records ADD COLUMN is_locked INTEGER NOT NULL DEFAULT 0")
                // Create index for is_locked
                db.execSQL("CREATE INDEX IF NOT EXISTS index_history_records_is_locked ON history_records(is_locked)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add custom_name column with default empty string
                db.execSQL("ALTER TABLE history_records ADD COLUMN custom_name TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
