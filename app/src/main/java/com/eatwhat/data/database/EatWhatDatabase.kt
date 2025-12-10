package com.eatwhat.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.eatwhat.data.database.dao.HistoryDao
import com.eatwhat.data.database.dao.RecipeDao
import com.eatwhat.data.database.dao.TagDao
import com.eatwhat.data.database.entities.*

/**
 * Room database for EatWhat app
 * Version 1: Initial schema with all entities
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
    version = 1,
    exportSchema = true
)
abstract class EatWhatDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun historyDao(): HistoryDao
    abstract fun tagDao(): TagDao
}
