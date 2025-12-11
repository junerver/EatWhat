package com.eatwhat.data.repository

import com.eatwhat.data.database.EatWhatDatabase
import com.eatwhat.data.database.entities.*
import com.eatwhat.data.database.relations.HistoryWithDetails
import com.eatwhat.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for History operations
 */
class HistoryRepository(private val database: EatWhatDatabase) {

    private val historyDao = database.historyDao()

    fun getAllHistory(): Flow<List<HistoryRecord>> {
        return historyDao.getAllHistory().map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getHistoryById(historyId: Long): Flow<HistoryWithRecipes?> {
        return historyDao.getHistoryWithDetails(historyId).map { it?.toDomain() }
    }

    suspend fun insertHistory(
        config: RollConfig,
        recipes: List<Recipe>,
        prepItems: List<com.eatwhat.domain.usecase.PrepListItem>
    ): Long {
        val summary = buildSummary(config)

        val historyEntity = HistoryRecordEntity(
            totalCount = config.meatCount + config.vegCount + config.soupCount + config.stapleCount,
            meatCount = config.meatCount,
            vegCount = config.vegCount,
            soupCount = config.soupCount,
            summary = summary
        )

        val historyId = historyDao.insertHistoryRecord(historyEntity)

        // Save recipe snapshots
        val snapshots = recipes.map { recipe ->
            HistoryRecipeCrossRef(
                historyId = historyId,
                recipeId = recipe.id,
                recipeName = recipe.name,
                recipeType = recipe.type.name,
                recipeIcon = recipe.icon,
                recipeDifficulty = recipe.difficulty.name,
                recipeTime = recipe.estimatedTime
            )
        }
        historyDao.insertHistoryRecipeCrossRefs(snapshots)

        // Save prep items
        val prepEntities = prepItems.mapIndexed { index, item ->
            PrepItemEntity(
                historyId = historyId,
                ingredientName = item.name,
                isChecked = item.isChecked,
                orderIndex = index
            )
        }
        historyDao.insertPrepItems(prepEntities)

        return historyId
    }

    suspend fun deleteHistory(historyId: Long) {
        historyDao.softDeleteHistory(historyId)
    }

    private fun buildSummary(config: RollConfig): String {
        val parts = mutableListOf<String>()
        if (config.meatCount > 0) parts.add("${config.meatCount}荤")
        if (config.vegCount > 0) parts.add("${config.vegCount}素")
        if (config.soupCount > 0) parts.add("${config.soupCount}汤")
        if (config.stapleCount > 0) parts.add("${config.stapleCount}主食")
        return parts.joinToString("")
    }

    private fun HistoryRecordEntity.toDomain(): HistoryRecord {
        return HistoryRecord(
            id = id,
            syncId = syncId,
            timestamp = timestamp,
            totalCount = totalCount,
            meatCount = meatCount,
            vegCount = vegCount,
            soupCount = soupCount,
            summary = summary
        )
    }

    private fun HistoryWithDetails.toDomain(): HistoryWithRecipes {
        return HistoryWithRecipes(
            history = history.toDomain(),
            recipes = recipeSnapshots.map { it.toDomain() },
            prepItems = prepItems.map { it.toDomain() }
        )
    }

    private fun HistoryRecipeCrossRef.toDomain(): RecipeSnapshot {
        return RecipeSnapshot(
            recipeId = recipeId,
            name = recipeName,
            type = recipeType,
            icon = recipeIcon,
            difficulty = recipeDifficulty,
            estimatedTime = recipeTime
        )
    }

    private fun PrepItemEntity.toDomain(): PrepItemRecord {
        return PrepItemRecord(
            id = id,
            name = ingredientName,
            isChecked = isChecked
        )
    }

    /**
     * Update prep item checked status
     */
    suspend fun updatePrepItemChecked(prepItemId: Long, isChecked: Boolean) {
        historyDao.updatePrepItemChecked(prepItemId, isChecked)
    }
}

data class HistoryWithRecipes(
    val history: HistoryRecord,
    val recipes: List<RecipeSnapshot>,
    val prepItems: List<PrepItemRecord>
)

data class RecipeSnapshot(
    val recipeId: Long,
    val name: String,
    val type: String,
    val icon: String,
    val difficulty: String,
    val estimatedTime: Int
)

data class PrepItemRecord(
    val id: Long,
    val name: String,
    val isChecked: Boolean
)
