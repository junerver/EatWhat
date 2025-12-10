package com.eatwhat.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * History-Recipe cross-reference entity for Room database
 * Stores recipe snapshots in history records
 * Note: No foreign key to recipes table to preserve snapshots even if recipe is deleted
 */
@Entity(
    tableName = "history_recipe_cross_ref",
    primaryKeys = ["history_id", "recipe_id"],
    foreignKeys = [
        ForeignKey(
            entity = HistoryRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["history_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["history_id"])]
)
data class HistoryRecipeCrossRef(
    @ColumnInfo(name = "history_id")
    val historyId: Long,

    @ColumnInfo(name = "recipe_id")
    val recipeId: Long,

    @ColumnInfo(name = "recipe_name")
    val recipeName: String,

    @ColumnInfo(name = "recipe_type")
    val recipeType: String,

    @ColumnInfo(name = "recipe_icon")
    val recipeIcon: String,

    @ColumnInfo(name = "recipe_difficulty")
    val recipeDifficulty: String,

    @ColumnInfo(name = "recipe_time")
    val recipeTime: Int
)
