package com.eatwhat.data.sync

import kotlinx.serialization.Serializable

/**
 * 导出数据根结构
 */
@Serializable
data class ExportData(
    val version: String = "1.0.0",
    val exportTime: Long = System.currentTimeMillis(),
    val appVersion: String = "",
    val deviceId: String = "",
    val encrypted: Boolean = false,
    val recipes: List<RecipeExport> = emptyList(),
    val historyRecords: List<HistoryExport> = emptyList(),
    val aiConfig: AIConfigExport? = null,
    val aiProviders: List<AIProviderExport> = emptyList()
)

/**
 * AI配置导出结构 (Deprecated: use aiProviders instead)
 */
@Serializable
data class AIConfigExport(
  val baseUrl: String,
  val apiKey: String,
  val model: String
)

/**
 * AI供应商导出结构
 */
@Serializable
data class AIProviderExport(
  val syncId: String,
  val name: String,
  val baseUrl: String,
  val apiKey: String,
  val model: String,
  val isActive: Boolean,
  val lastModified: Long
)

/**
 * 菜谱导出结构
 */
@Serializable
data class RecipeExport(
    val syncId: String,
    val name: String,
    val type: String,
    val icon: String,
    val imageBase64: String? = null,
    val difficulty: String,
    val estimatedTime: Int,
    val ingredients: List<IngredientExport> = emptyList(),
    val cookingSteps: List<CookingStepExport> = emptyList(),
    val tags: List<String> = emptyList(),
    val createdAt: Long,
    val lastModified: Long
)

/**
 * 食材导出结构
 */
@Serializable
data class IngredientExport(
    val name: String,
    val amount: String,
    val unit: String,
    val orderIndex: Int
)

/**
 * 烹饪步骤导出结构
 */
@Serializable
data class CookingStepExport(
    val stepNumber: Int,
    val description: String
)

/**
 * 历史记录导出结构
 */
@Serializable
data class HistoryExport(
    val syncId: String,
    val timestamp: Long,
    val totalCount: Int,
    val meatCount: Int,
    val vegCount: Int,
    val soupCount: Int,
    val summary: String,
    val customName: String = "",
    val isLocked: Boolean = false,
    val recipes: List<HistoryRecipeSnapshot> = emptyList(),
    val lastModified: Long
)

/**
 * 历史菜谱快照
 */
@Serializable
data class HistoryRecipeSnapshot(
    val recipeId: Long,
    val name: String,
    val type: String,
    val icon: String,
    val imageBase64: String? = null,
    val difficulty: String,
    val estimatedTime: Int
)

/**
 * WebDAV 配置
 */
@Serializable
data class WebDAVConfig(
    val serverUrl: String = "",
    val username: String = "",
    val password: String = "",
    val remotePath: String = "/EatWhat/",
    val encryptionEnabled: Boolean = false,
    val encryptionPassword: String? = null,
    val lastSyncTime: Long? = null,
    val lastSyncStatus: String? = null,
    // 自动同步配置
    val autoSyncEnabled: Boolean = false,
    val syncIntervalMinutes: Int = 60  // 默认60分钟
)

/**
 * 同步元数据（云端）
 */
@Serializable
data class SyncMetadata(
    val syncId: String,
    val deviceId: String,
    val uploadTime: Long,
    val dataHash: String,
    val appVersion: String,
    val encrypted: Boolean
)

/**
 * 导入预览
 */
data class ImportPreview(
    val recipeCount: Int = 0,
    val historyCount: Int = 0,
    val aiProviderCount: Int = 0,
    val newRecipes: Int = 0,
    val updatedRecipes: Int = 0,
    val newHistory: Int = 0,
    val updatedHistory: Int = 0,
    val newAIProviders: Int = 0,
    val updatedAIProviders: Int = 0
)

/**
 * 导入结果
 */
data class ImportResult(
    val success: Boolean,
    val recipesImported: Int = 0,
    val recipesUpdated: Int = 0,
    val recipesSkipped: Int = 0,
    val historyImported: Int = 0,
    val historyUpdated: Int = 0,
    val historySkipped: Int = 0,
    val aiProvidersImported: Int = 0,
    val aiProvidersUpdated: Int = 0,
    val aiProvidersSkipped: Int = 0,
    val errors: List<String> = emptyList()
)

/**
 * 冲突处理策略
 */
enum class ConflictStrategy {
    SKIP,           // 跳过已存在的数据
    UPDATE,         // 更新已存在的数据
    UPDATE_IF_NEWER // 仅当导入数据更新时更新
}

/**
 * 连接结果
 */
sealed class ConnectionResult {
    data object Success : ConnectionResult()
    data class Error(val message: String, val code: Int? = null) : ConnectionResult()
}

/**
 * 同步结果
 */
sealed class SyncResult {
    data class Success(val syncTime: Long) : SyncResult()
    data class Error(val message: String) : SyncResult()
}
