package com.eatwhat.data.sync

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 文件操作工具类
 * 使用 Storage Access Framework (SAF) 进行文件读写
 */
object FileHelper {

    /**
     * 文件信息
     */
    data class FileInfo(
        val name: String,
        val size: Long
    )

    /**
     * 写入数据到 URI
     * @param context 上下文
     * @param uri 文件 URI
     * @param data 要写入的数据
     */
    suspend fun writeToUri(context: Context, uri: Uri, data: ByteArray) = withContext(Dispatchers.IO) {
        context.contentResolver.openOutputStream(uri)?.use { output ->
            output.write(data)
        } ?: throw IllegalStateException("无法打开输出流")
    }

    /**
     * 从 URI 读取数据
     * @param context 上下文
     * @param uri 文件 URI
     * @return 读取的数据
     */
    suspend fun readFromUri(context: Context, uri: Uri): ByteArray = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.use { input ->
            input.readBytes()
        } ?: throw IllegalStateException("无法打开输入流")
    }

    /**
     * 获取文件信息
     * @param context 上下文
     * @param uri 文件 URI
     * @return 文件信息，如果无法获取则返回 null
     */
    fun getFileInfo(context: Context, uri: Uri): FileInfo? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)

                val name = if (nameIndex >= 0) cursor.getString(nameIndex) else "unknown"
                val size = if (sizeIndex >= 0) cursor.getLong(sizeIndex) else 0L

                FileInfo(name, size)
            } else {
                null
            }
        }
    }

    /**
     * 检查 URI 是否可读
     */
    fun isReadable(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { true } ?: false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 生成导出文件名
     * @param prefix 文件名前缀
     * @param extension 文件扩展名
     */
    fun generateExportFileName(prefix: String = "eatwhat_backup", extension: String = "json"): String {
        val timestamp = System.currentTimeMillis()
        return "${prefix}_$timestamp.$extension"
    }
}
