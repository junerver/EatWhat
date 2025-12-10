package com.eatwhat.domain.model

/**
 * Domain model for History Record
 */
data class HistoryRecord(
    val id: Long = 0,
    val syncId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val totalCount: Int,
    val meatCount: Int,
    val vegCount: Int,
    val soupCount: Int,
    val summary: String,
    val recipes: List<RecipeSnapshot> = emptyList(),
    val prepItems: List<PrepItem> = emptyList(),
    val lastModified: Long = System.currentTimeMillis()
)

/**
 * Recipe snapshot in history
 * Preserves recipe data even if original is deleted
 */
data class RecipeSnapshot(
    val recipeId: Long,
    val name: String,
    val type: RecipeType,
    val icon: String,
    val difficulty: Difficulty,
    val estimatedTime: Int
)
