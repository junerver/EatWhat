package com.eatwhat.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.eatwhat.data.database.EatWhatDatabase
import com.eatwhat.data.database.entities.CookingStepEntity
import com.eatwhat.data.database.entities.HistoryRecipeCrossRef
import com.eatwhat.data.database.entities.HistoryRecordEntity
import com.eatwhat.data.database.entities.IngredientEntity
import com.eatwhat.data.database.entities.RecipeEntity
import com.eatwhat.data.database.entities.RecipeTagCrossRef
import com.eatwhat.data.database.relations.HistoryWithDetails
import com.eatwhat.data.database.relations.RecipeWithDetails
import com.eatwhat.data.preferences.AIConfig
import com.eatwhat.data.preferences.AIPreferences
import com.eatwhat.data.sync.AIConfigExport
import com.eatwhat.data.sync.ConflictStrategy
import com.eatwhat.data.sync.CookingStepExport
import com.eatwhat.data.sync.ExportData
import com.eatwhat.data.sync.HistoryExport
import com.eatwhat.data.sync.HistoryRecipeSnapshot
import com.eatwhat.data.sync.ImportPreview
import com.eatwhat.data.sync.ImportResult
import com.eatwhat.data.sync.IngredientExport
import com.eatwhat.data.sync.RecipeExport
import kotlinx.coroutines.flow.first

/**
 * 导入导出仓库实现
 */
class ExportRepositoryImpl(
    private val context: Context,
    private val database: EatWhatDatabase,
    private val aiPreferences: AIPreferences
) : ExportRepository {

    private val recipeDao = database.recipeDao()
    private val historyDao = database.historyDao()
    private val tagDao = database.tagDao()

    override suspend fun exportAll(): ExportData {
        val recipes = exportRecipeEntities()
        val history = exportHistoryEntities()
      val aiConfig = aiPreferences.aiConfigFlow.first()
      return createExportData(recipes, history, aiConfig)
    }

    override suspend fun exportRecipes(): ExportData {
        val recipes = exportRecipeEntities()
        return createExportData(recipes, emptyList())
    }

    override suspend fun exportHistory(): ExportData {
        val history = exportHistoryEntities()
        return createExportData(emptyList(), history)
    }

    override suspend fun previewImport(data: ExportData): ImportPreview {
        var newRecipes = 0
        var updatedRecipes = 0
        var newHistory = 0
        var updatedHistory = 0

        // 检查菜谱
        data.recipes.forEach { recipe ->
            val existing = recipeDao.getRecipeBySyncId(recipe.syncId)
            if (existing != null) {
                updatedRecipes++
            } else {
                newRecipes++
            }
        }

        // 检查历史记录
        data.historyRecords.forEach { history ->
            val existing = historyDao.getHistoryBySyncId(history.syncId)
            if (existing != null) {
                updatedHistory++
            } else {
                newHistory++
            }
        }

        return ImportPreview(
            recipeCount = data.recipes.size,
            historyCount = data.historyRecords.size,
            newRecipes = newRecipes,
            updatedRecipes = updatedRecipes,
            newHistory = newHistory,
            updatedHistory = updatedHistory
        )
    }

    override suspend fun importData(data: ExportData, strategy: ConflictStrategy): ImportResult {
        var recipesImported = 0
        var recipesUpdated = 0
        var recipesSkipped = 0
        var historyImported = 0
        var historyUpdated = 0
        var historySkipped = 0
        val errors = mutableListOf<String>()

        // 导入菜谱
        data.recipes.forEach { recipe ->
            try {
                val result = importRecipe(recipe, strategy)
                when (result) {
                    ImportAction.INSERTED -> recipesImported++
                    ImportAction.UPDATED -> recipesUpdated++
                    ImportAction.SKIPPED -> recipesSkipped++
                }
            } catch (e: Exception) {
                errors.add("菜谱 ${recipe.name}: ${e.message}")
            }
        }

        // 导入历史记录
        data.historyRecords.forEach { history ->
            try {
                val result = importHistory(history, strategy)
                when (result) {
                    ImportAction.INSERTED -> historyImported++
                    ImportAction.UPDATED -> historyUpdated++
                    ImportAction.SKIPPED -> historySkipped++
                }
            } catch (e: Exception) {
                errors.add("历史记录 ${history.syncId}: ${e.message}")
            }
        }

      // 导入 AI 配置
      data.aiConfig?.let { aiConfigExport ->
        try {
          val aiConfig = aiConfigExport.toAIConfig()
          aiPreferences.saveConfig(aiConfig)
        } catch (e: Exception) {
          errors.add("AI配置: ${e.message}")
        }
      }

        return ImportResult(
            success = errors.isEmpty(),
            recipesImported = recipesImported,
            recipesUpdated = recipesUpdated,
            recipesSkipped = recipesSkipped,
            historyImported = historyImported,
            historyUpdated = historyUpdated,
            historySkipped = historySkipped,
            errors = errors
        )
    }

    override suspend fun getDataCount(): Pair<Int, Int> {
        val recipeCount = recipeDao.getRecipeCount()
        val historyCount = historyDao.getHistoryCount()
        return Pair(recipeCount, historyCount)
    }

    // ========== Private Helper Methods ==========

    private suspend fun exportRecipeEntities(): List<RecipeExport> {
        return recipeDao.getAllRecipesWithDetailsSync().map { it.toExport() }
    }

    private suspend fun exportHistoryEntities(): List<HistoryExport> {
        return historyDao.getAllHistoryWithDetailsSync().map { it.toExport() }
    }

    private fun createExportData(
        recipes: List<RecipeExport>,
        history: List<HistoryExport>,
        aiConfig: AIConfig? = null
    ): ExportData {
        return ExportData(
            version = "1.0.0",
            exportTime = System.currentTimeMillis(),
            appVersion = getAppVersion(),
            deviceId = getDeviceId(),
            encrypted = false,
            recipes = recipes,
          historyRecords = history,
          aiConfig = aiConfig?.toExport()
        )
    }

    private fun getAppVersion(): String {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    private fun getDeviceId(): String {
        return Build.MODEL + "_" + Build.BRAND
    }

    private suspend fun importRecipe(recipe: RecipeExport, strategy: ConflictStrategy): ImportAction {
        val existing = recipeDao.getRecipeBySyncId(recipe.syncId)

        return when {
            existing == null -> {
                // 新增菜谱
                insertRecipeFromExport(recipe)
                ImportAction.INSERTED
            }
            strategy == ConflictStrategy.SKIP -> {
                ImportAction.SKIPPED
            }
            strategy == ConflictStrategy.UPDATE_IF_NEWER && recipe.lastModified <= existing.lastModified -> {
                ImportAction.SKIPPED
            }
            else -> {
                // 更新菜谱
                updateRecipeFromExport(existing.id, recipe)
                ImportAction.UPDATED
            }
        }
    }

    private suspend fun insertRecipeFromExport(recipe: RecipeExport) {
        val entity = RecipeEntity(
            syncId = recipe.syncId,
            name = recipe.name,
            type = recipe.type,
            icon = recipe.icon,
            imageBase64 = recipe.imageBase64,
            difficulty = recipe.difficulty,
            estimatedTime = recipe.estimatedTime,
            createdAt = recipe.createdAt,
            lastModified = recipe.lastModified
        )
        val recipeId = recipeDao.insertRecipe(entity)

        // 插入食材
        val ingredients = recipe.ingredients.map {
            IngredientEntity(
                recipeId = recipeId,
                name = it.name,
                amount = it.amount,
                unit = it.unit,
                orderIndex = it.orderIndex
            )
        }
        recipeDao.insertIngredients(ingredients)

        // 插入步骤
        val steps = recipe.cookingSteps.map {
            CookingStepEntity(
                recipeId = recipeId,
                stepNumber = it.stepNumber,
                description = it.description
            )
        }
        recipeDao.insertCookingSteps(steps)

        // 插入标签
        recipe.tags.forEach { tagName ->
            val existingTag = tagDao.getTagByName(tagName)
            val tagId = existingTag?.id ?: tagDao.insertTag(
                com.eatwhat.data.database.entities.TagEntity(name = tagName)
            )
            recipeDao.insertRecipeTagCrossRef(RecipeTagCrossRef(recipeId, tagId))
        }
    }

    private suspend fun updateRecipeFromExport(recipeId: Long, recipe: RecipeExport) {
        val entity = RecipeEntity(
            id = recipeId,
            syncId = recipe.syncId,
            name = recipe.name,
            type = recipe.type,
            icon = recipe.icon,
            imageBase64 = recipe.imageBase64,
            difficulty = recipe.difficulty,
            estimatedTime = recipe.estimatedTime,
            createdAt = recipe.createdAt,
            lastModified = recipe.lastModified
        )
        recipeDao.updateRecipe(entity)

        // 更新食材
        recipeDao.deleteIngredientsByRecipeId(recipeId)
        val ingredients = recipe.ingredients.map {
            IngredientEntity(
                recipeId = recipeId,
                name = it.name,
                amount = it.amount,
                unit = it.unit,
                orderIndex = it.orderIndex
            )
        }
        recipeDao.insertIngredients(ingredients)

        // 更新步骤
        recipeDao.deleteCookingStepsByRecipeId(recipeId)
        val steps = recipe.cookingSteps.map {
            CookingStepEntity(
                recipeId = recipeId,
                stepNumber = it.stepNumber,
                description = it.description
            )
        }
        recipeDao.insertCookingSteps(steps)

        // 更新标签
        recipeDao.deleteRecipeTagsByRecipeId(recipeId)
        recipe.tags.forEach { tagName ->
            val existingTag = tagDao.getTagByName(tagName)
            val tagId = existingTag?.id ?: tagDao.insertTag(
                com.eatwhat.data.database.entities.TagEntity(name = tagName)
            )
            recipeDao.insertRecipeTagCrossRef(RecipeTagCrossRef(recipeId, tagId))
        }
    }

    private suspend fun importHistory(history: HistoryExport, strategy: ConflictStrategy): ImportAction {
        val existing = historyDao.getHistoryBySyncId(history.syncId)

        return when {
            existing == null -> {
                insertHistoryFromExport(history)
                ImportAction.INSERTED
            }
            strategy == ConflictStrategy.SKIP -> {
                ImportAction.SKIPPED
            }
            strategy == ConflictStrategy.UPDATE_IF_NEWER && history.lastModified <= existing.lastModified -> {
                ImportAction.SKIPPED
            }
            else -> {
                updateHistoryFromExport(existing.id, history)
                ImportAction.UPDATED
            }
        }
    }

    private suspend fun insertHistoryFromExport(history: HistoryExport) {
        val entity = HistoryRecordEntity(
            syncId = history.syncId,
            timestamp = history.timestamp,
            totalCount = history.totalCount,
            meatCount = history.meatCount,
            vegCount = history.vegCount,
            soupCount = history.soupCount,
            summary = history.summary,
            customName = history.customName,
            isLocked = history.isLocked,
            lastModified = history.lastModified
        )
        val historyId = historyDao.insertHistoryRecord(entity)

        // 插入菜谱快照
        val snapshots = history.recipes.map {
            HistoryRecipeCrossRef(
                historyId = historyId,
                recipeId = it.recipeId,
                recipeName = it.name,
                recipeType = it.type,
                recipeIcon = it.icon,
                recipeImageBase64 = it.imageBase64,
                recipeDifficulty = it.difficulty,
                recipeTime = it.estimatedTime
            )
        }
        historyDao.insertHistoryRecipeCrossRefs(snapshots)
    }

    private suspend fun updateHistoryFromExport(historyId: Long, history: HistoryExport) {
        val entity = HistoryRecordEntity(
            id = historyId,
            syncId = history.syncId,
            timestamp = history.timestamp,
            totalCount = history.totalCount,
            meatCount = history.meatCount,
            vegCount = history.vegCount,
            soupCount = history.soupCount,
            summary = history.summary,
            customName = history.customName,
            isLocked = history.isLocked,
            lastModified = history.lastModified
        )
        historyDao.updateHistoryRecord(entity)

        // 注意：历史记录的菜谱快照不更新，保留原始快照
    }

    // ========== Extension Functions ==========

    private fun RecipeWithDetails.toExport(): RecipeExport {
        return RecipeExport(
            syncId = recipe.syncId,
            name = recipe.name,
            type = recipe.type,
            icon = recipe.icon,
            imageBase64 = recipe.imageBase64,
            difficulty = recipe.difficulty,
            estimatedTime = recipe.estimatedTime,
            ingredients = ingredients.map { it.toExport() },
            cookingSteps = steps.map { it.toExport() },
            tags = tags.map { it.name },
            createdAt = recipe.createdAt,
            lastModified = recipe.lastModified
        )
    }

    private fun IngredientEntity.toExport(): IngredientExport {
        return IngredientExport(
            name = name,
            amount = amount,
            unit = unit,
            orderIndex = orderIndex
        )
    }

    private fun CookingStepEntity.toExport(): CookingStepExport {
        return CookingStepExport(
            stepNumber = stepNumber,
            description = description
        )
    }

    private fun HistoryWithDetails.toExport(): HistoryExport {
        return HistoryExport(
            syncId = history.syncId,
            timestamp = history.timestamp,
            totalCount = history.totalCount,
            meatCount = history.meatCount,
            vegCount = history.vegCount,
            soupCount = history.soupCount,
            summary = history.summary,
            customName = history.customName,
            isLocked = history.isLocked,
            recipes = recipeSnapshots.map { it.toExport() },
            lastModified = history.lastModified
        )
    }

    private fun HistoryRecipeCrossRef.toExport(): HistoryRecipeSnapshot {
        return HistoryRecipeSnapshot(
            recipeId = recipeId,
            name = recipeName,
            type = recipeType,
            icon = recipeIcon,
            imageBase64 = recipeImageBase64,
            difficulty = recipeDifficulty,
            estimatedTime = recipeTime
        )
    }

  private fun AIConfig.toExport(): AIConfigExport {
    return AIConfigExport(
      baseUrl = baseUrl,
      apiKey = apiKey,
      model = model
    )
  }

  private fun AIConfigExport.toAIConfig(): AIConfig {
    return AIConfig(
      baseUrl = baseUrl,
      apiKey = apiKey,
      model = model
    )
  }

    private enum class ImportAction {
        INSERTED, UPDATED, SKIPPED
    }
}
