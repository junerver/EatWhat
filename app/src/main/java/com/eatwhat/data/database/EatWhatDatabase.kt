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
 * Version 4: Add image_base64 column to recipes for storing WebP images as Base64
 * Version 5: Add recipe_image_base64 column to history_recipe_cross_ref for storing image snapshots
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
    version = 5,
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

        /**
         * Migration from version 3 to 4
         * Adds image_base64 column to recipes table for storing WebP images as Base64 strings
         * This supports custom dish images that take precedence over emoji icons
         * The column is nullable to maintain backward compatibility
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add image_base64 column with default null
                // This column stores WebP compressed images encoded as Base64 strings
                // When not null, it takes precedence over the icon emoji for display
                db.execSQL("ALTER TABLE recipes ADD COLUMN image_base64 TEXT DEFAULT NULL")
            }
        }

        /**
         * Migration from version 4 to 5
         * Adds recipe_image_base64 column to history_recipe_cross_ref table
         * This stores a snapshot of the recipe image at the time of history creation
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add recipe_image_base64 column with default null
                db.execSQL("ALTER TABLE history_recipe_cross_ref ADD COLUMN recipe_image_base64 TEXT DEFAULT NULL")
            }
        }
    }
}
