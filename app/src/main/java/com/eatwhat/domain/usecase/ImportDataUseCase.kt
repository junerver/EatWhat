package com.eatwhat.domain.usecase

import android.content.Context
import android.net.Uri
import com.eatwhat.data.repository.ExportRepository
import com.eatwhat.data.sync.ConflictStrategy
import com.eatwhat.data.sync.ExportData
import com.eatwhat.data.sync.ImportPreview
import com.eatwhat.data.sync.ImportResult
import kotlinx.serialization.json.Json

/**
 * 导入数据用例
 * 封装数据导入逻辑，支持从 URI 导入
 */
class ImportDataUseCase(
    private val context: Context,
    private val exportRepository: ExportRepository
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * 从 URI 读取并预览导入数据
     */
    suspend fun previewImport(uri: Uri): Result<ImportPreviewResult> {
        return try {
            val data = readExportData(uri)
                ?: return Result.failure(ImportException("无法读取文件"))

            // 验证数据格式
            if (data.version.isBlank()) {
                return Result.failure(ImportException("无效的备份文件格式"))
            }

            val preview = exportRepository.previewImport(data)

            Result.success(
                ImportPreviewResult(
                    data = data,
                    preview = preview
                )
            )
        } catch (e: Exception) {
            Result.failure(ImportException("预览失败: ${e.message}", e))
        }
    }

    /**
     * 执行导入
     */
    suspend fun executeImport(
        data: ExportData,
        strategy: ConflictStrategy
    ): Result<ImportResult> {
        return try {
            val result = exportRepository.importData(data, strategy)

            if (result.success) {
                Result.success(result)
            } else {
                Result.failure(ImportException("导入过程中发生错误: ${result.errors.joinToString(", ")}"))
            }
        } catch (e: Exception) {
            Result.failure(ImportException("导入失败: ${e.message}", e))
        }
    }

    /**
     * 从 URI 读取并直接执行导入
     */
    suspend fun importFromUri(
        uri: Uri,
        strategy: ConflictStrategy
    ): Result<ImportResult> {
        return try {
            val data = readExportData(uri)
                ?: return Result.failure(ImportException("无法读取文件"))

            executeImport(data, strategy)
        } catch (e: Exception) {
            Result.failure(ImportException("导入失败: ${e.message}", e))
        }
    }

    private fun readExportData(uri: Uri): ExportData? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val jsonString = inputStream.bufferedReader().readText()
                json.decodeFromString<ExportData>(jsonString)
            }
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * 导入预览结果
 */
data class ImportPreviewResult(
    val data: ExportData,
    val preview: ImportPreview
)

/**
 * 导入异常
 */
class ImportException(message: String, cause: Throwable? = null) : Exception(message, cause)
