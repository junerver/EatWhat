package com.eatwhat.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.eatwhat.data.database.dao.AIProviderDao
import com.eatwhat.data.database.dao.HistoryDao
import com.eatwhat.data.database.dao.RecipeDao
import com.eatwhat.data.database.dao.TagDao
import com.eatwhat.data.database.entities.AIProviderEntity
import com.eatwhat.data.database.entities.CookingStepEntity
import com.eatwhat.data.database.entities.HistoryRecipeCrossRef
import com.eatwhat.data.database.entities.HistoryRecordEntity
import com.eatwhat.data.database.entities.IngredientEntity
import com.eatwhat.data.database.entities.PrepItemEntity
import com.eatwhat.data.database.entities.RecipeEntity
import com.eatwhat.data.database.entities.RecipeTagCrossRef
import com.eatwhat.data.database.entities.TagEntity

/**
 * Room database for EatWhat app
 * Version 1: Initial schema with all entities
 * Version 2: Add is_locked column to history_records
 * Version 3: Add custom_name column to history_records
 * Version 4: Add image_base64 column to recipes for storing WebP images as Base64
 * Version 5: Add recipe_image_base64 column to history_recipe_cross_ref for storing image snapshots
 * Version 6: Add ai_providers table for multi-provider support
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
      PrepItemEntity::class,
      AIProviderEntity::class
    ],
  version = 6,
    exportSchema = true
)
abstract class EatWhatDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun historyDao(): HistoryDao
    abstract fun tagDao(): TagDao
  abstract fun aiProviderDao(): AIProviderDao

    companion object {
        @Volatile
        private var INSTANCE: EatWhatDatabase? = null

        fun getInstance(context: android.content.Context): EatWhatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EatWhatDatabase::class.java,
                    "eatwhat.db"
                )
                  .addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_4_5,
                    MIGRATION_5_6
                  )
                    .build()
                INSTANCE = instance
                instance
            }
        }

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

      /**
       * Migration from version 5 to 6
       * Adds ai_providers table
       */
      val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
          db.execSQL(
            """
                    CREATE TABLE IF NOT EXISTS `ai_providers` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `sync_id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `base_url` TEXT NOT NULL,
                        `api_key` TEXT NOT NULL,
                        `model` TEXT NOT NULL,
                        `is_active` INTEGER NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `last_modified` INTEGER NOT NULL,
                        `is_deleted` INTEGER NOT NULL
                    )
                """.trimIndent()
          )
          db.execSQL("CREATE INDEX IF NOT EXISTS `index_ai_providers_is_active` ON `ai_providers` (`is_active`)")
          db.execSQL("CREATE INDEX IF NOT EXISTS `index_ai_providers_is_deleted` ON `ai_providers` (`is_deleted`)")
        }
      }
    }
}
