package com.eatwhat.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Recipe entity for Room database
 * Represents a recipe with all its metadata
 */
@Entity(
    tableName = "recipes",
    indices = [
        Index(value = ["type"]),
        Index(value = ["sync_id"], unique = true),
        Index(value = ["is_deleted"])
    ]
)
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "sync_id")
    val syncId: String = UUID.randomUUID().toString(),

    val name: String,

    val type: String, // RecipeType: MEAT, VEG, SOUP, STAPLE

    val icon: String, // Emoji icon

    val difficulty: String, // Difficulty: EASY, MEDIUM, HARD

    @ColumnInfo(name = "estimated_time")
    val estimatedTime: Int, // Minutes

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "last_modified")
    val lastModified: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false
)

/**
 * Recipe type enum
 */
enum class RecipeType {
    MEAT,   // 荤菜
    VEG,    // 素菜
    SOUP,   // 汤
    STAPLE  // 主食
}

/**
 * Recipe difficulty enum
 */
enum class Difficulty {
    EASY,   // 简单
    MEDIUM, // 中等
    HARD    // 困难
}
