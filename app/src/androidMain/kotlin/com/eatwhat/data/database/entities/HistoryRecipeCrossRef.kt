package com.eatwhat.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * History-Recipe cross-reference entity for Room database
 * Stores recipe snapshots in history records
 * Note: No foreign key to recipes table to preserve snapshots even if recipe is deleted
 *
 * @property recipeIcon Emoji icon for the recipe (fallback when imageBase64 is null)
 * @property recipeImageBase64 Base64 encoded WebP image snapshot (optional)
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

    /**
     * Base64 encoded WebP image snapshot
     * Captures the recipe image at the time of history creation
     */
    @ColumnInfo(name = "recipe_image_base64")
    val recipeImageBase64: String? = null,

    @ColumnInfo(name = "recipe_difficulty")
    val recipeDifficulty: String,

    @ColumnInfo(name = "recipe_time")
    val recipeTime: Int
)
