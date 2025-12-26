package com.eatwhat.domain.usecase

import android.content.Context
import android.net.Uri
import com.eatwhat.data.repository.ExportRepository
import com.eatwhat.data.sync.ExportData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 导出数据用例
 * 封装数据导出逻辑，支持导出到 URI
 */
class ExportDataUseCase(
    private val context: Context,
    private val exportRepository: ExportRepository
) {
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    /**
     * 导出所有数据到 URI
     */
    suspend fun exportAll(uri: Uri): Result<ExportResult> {
        return export(uri) { exportRepository.exportAll() }
    }

    /**
     * 仅导出菜谱到 URI
     */
    suspend fun exportRecipes(uri: Uri): Result<ExportResult> {
        return export(uri) { exportRepository.exportRecipes() }
    }

    /**
     * 仅导出历史到 URI
     */
    suspend fun exportHistory(uri: Uri): Result<ExportResult> {
        return export(uri) { exportRepository.exportHistory() }
    }

    /**
     * 获取当前数据统计
     */
    suspend fun getDataCount(): Pair<Int, Int> {
        return exportRepository.getDataCount()
    }

    private suspend fun export(
        uri: Uri,
        dataProvider: suspend () -> ExportData
    ): Result<ExportResult> {
        return try {
            val data = dataProvider()

            // 检查是否有数据
            if (data.recipes.isEmpty() && data.historyRecords.isEmpty()) {
                return Result.failure(ExportException("没有可导出的数据"))
            }

            val jsonString = json.encodeToString(data)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
            } ?: return Result.failure(ExportException("无法打开文件进行写入"))

            Result.success(
                ExportResult(
                    recipeCount = data.recipes.size,
                    historyCount = data.historyRecords.size,
                    fileSize = jsonString.length.toLong()
                )
            )
        } catch (e: Exception) {
            Result.failure(ExportException("导出失败: ${e.message}", e))
        }
    }
}

/**
 * 导出结果
 */
data class ExportResult(
    val recipeCount: Int,
    val historyCount: Int,
    val fileSize: Long
)

/**
 * 导出异常
 */
class ExportException(message: String, cause: Throwable? = null) : Exception(message, cause)
