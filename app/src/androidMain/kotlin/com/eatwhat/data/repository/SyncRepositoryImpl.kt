package com.eatwhat.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.eatwhat.data.sync.ConnectionResult
import com.eatwhat.data.sync.CryptoManager
import com.eatwhat.data.sync.ExportData
import com.eatwhat.data.sync.SyncMetadata
import com.eatwhat.data.sync.SyncResult
import com.eatwhat.data.sync.WebDAVClient
import com.eatwhat.data.sync.WebDAVConfig
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.MessageDigest
import java.util.UUID

/**
 * 同步仓库实现
 * 使用 EncryptedSharedPreferences 保存 WebDAV 配置
 */
class SyncRepositoryImpl(
    private val context: Context,
    private val exportRepository: ExportRepository
) : SyncRepository {

    private val json = Json {
        prettyPrint = false
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    private val prefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // 回退到普通 SharedPreferences（不推荐，仅用于兼容性）
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    // ========== 配置管理 ==========

    override fun getConfig(): WebDAVConfig? {
        val serverUrl = prefs.getString(KEY_SERVER_URL, null) ?: return null
        return WebDAVConfig(
            serverUrl = serverUrl,
            username = prefs.getString(KEY_USERNAME, "") ?: "",
            password = prefs.getString(KEY_PASSWORD, "") ?: "",
            remotePath = prefs.getString(KEY_REMOTE_PATH, "/EatWhat/") ?: "/EatWhat/",
            encryptionEnabled = prefs.getBoolean(KEY_ENCRYPTION_ENABLED, false),
            encryptionPassword = prefs.getString(KEY_ENCRYPTION_PASSWORD, null),
            lastSyncTime = prefs.getLong(KEY_LAST_SYNC_TIME, 0).takeIf { it > 0 },
            lastSyncStatus = prefs.getString(KEY_LAST_SYNC_STATUS, null),
            autoSyncEnabled = prefs.getBoolean(KEY_AUTO_SYNC_ENABLED, false),
            syncIntervalMinutes = prefs.getInt(KEY_SYNC_INTERVAL_MINUTES, 60)
        )
    }

    override fun saveConfig(config: WebDAVConfig) {
        prefs.edit().apply {
            putString(KEY_SERVER_URL, config.serverUrl)
            putString(KEY_USERNAME, config.username)
            putString(KEY_PASSWORD, config.password)
            putString(KEY_REMOTE_PATH, config.remotePath)
            putBoolean(KEY_ENCRYPTION_ENABLED, config.encryptionEnabled)
            putString(KEY_ENCRYPTION_PASSWORD, config.encryptionPassword)
            config.lastSyncTime?.let { putLong(KEY_LAST_SYNC_TIME, it) }
            config.lastSyncStatus?.let { putString(KEY_LAST_SYNC_STATUS, it) }
            putBoolean(KEY_AUTO_SYNC_ENABLED, config.autoSyncEnabled)
            putInt(KEY_SYNC_INTERVAL_MINUTES, config.syncIntervalMinutes)
            apply()
        }
    }

    override fun clearConfig() {
        prefs.edit().clear().apply()
    }

    override fun isConfigured(): Boolean {
        return prefs.getString(KEY_SERVER_URL, null)?.isNotBlank() == true
    }

    // ========== 连接测试 ==========

    override suspend fun testConnection(config: WebDAVConfig): ConnectionResult {
        val client = WebDAVClient(config.serverUrl, config.username, config.password)
        return client.testConnection()
    }

    // ========== 云端同步 ==========

    override suspend fun uploadToCloud(encryptionPassword: String?): SyncResult {
        val config = getConfig() ?: return SyncResult.Error("未配置 WebDAV")

        return try {
            val client = WebDAVClient(config.serverUrl, config.username, config.password)

            // 确保目录存在
            if (!client.ensureDirectory(config.remotePath)) {
                return SyncResult.Error("无法创建远程目录")
            }

            // 导出数据
            val exportData = exportRepository.exportAll()
            val jsonData = json.encodeToString(exportData)

            // 计算哈希
            val dataHash = calculateHash(jsonData.toByteArray())

            // 加密（如果需要）
            val uploadData = if (config.encryptionEnabled && encryptionPassword != null) {
                val encrypted = CryptoManager.encrypt(jsonData.toByteArray(), encryptionPassword)
                encrypted.toBytes()
            } else {
                jsonData.toByteArray()
            }

            // 上传数据
            val dataPath = config.remotePath + if (config.encryptionEnabled) BACKUP_FILE_ENCRYPTED else BACKUP_FILE
            client.upload(dataPath, uploadData).getOrElse {
                return SyncResult.Error("上传失败: ${it.message}")
            }

            // 上传元数据
            val metadata = SyncMetadata(
                syncId = UUID.randomUUID().toString(),
                deviceId = getDeviceId(),
                uploadTime = System.currentTimeMillis(),
                dataHash = dataHash,
                appVersion = getAppVersion(),
                encrypted = config.encryptionEnabled
            )
            val metadataJson = json.encodeToString(metadata)
            client.upload(config.remotePath + METADATA_FILE, metadataJson.toByteArray()).getOrElse {
                return SyncResult.Error("上传元数据失败: ${it.message}")
            }

            // 更新状态
            val syncTime = System.currentTimeMillis()
            updateSyncStatus(true, syncTime)

            SyncResult.Success(syncTime)
        } catch (e: Exception) {
            updateSyncStatus(false)
            SyncResult.Error("同步失败: ${e.message}")
        }
    }

    override suspend fun downloadFromCloud(encryptionPassword: String?): SyncResult {
        val config = getConfig() ?: return SyncResult.Error("未配置 WebDAV")

        return try {
            val client = WebDAVClient(config.serverUrl, config.username, config.password)

            // 获取元数据
            val metadata = getCloudMetadata() ?: return SyncResult.Error("云端没有备份数据")

            // 下载数据
            val dataPath = config.remotePath + if (metadata.encrypted) BACKUP_FILE_ENCRYPTED else BACKUP_FILE
            val downloadedData = client.download(dataPath).getOrElse {
                return SyncResult.Error("下载失败: ${it.message}")
            }

            // 解密（如果需要）
            val jsonData = if (metadata.encrypted) {
                if (encryptionPassword == null) {
                    return SyncResult.Error("数据已加密，需要密码")
                }
                try {
                    val encryptedData = CryptoManager.EncryptedData.fromBytes(downloadedData)
                    CryptoManager.decrypt(encryptedData, encryptionPassword)
                } catch (e: Exception) {
                    return SyncResult.Error("解密失败：密码错误或数据损坏")
                }
            } else {
                downloadedData
            }

            // 解析数据
            val exportData = try {
                json.decodeFromString<ExportData>(String(jsonData, Charsets.UTF_8))
            } catch (e: Exception) {
                return SyncResult.Error("数据格式错误: ${e.message}")
            }

            // 导入数据
            val importResult = exportRepository.importData(
                exportData,
                com.eatwhat.data.sync.ConflictStrategy.UPDATE_IF_NEWER
            )

            if (!importResult.success) {
                return SyncResult.Error("导入失败: ${importResult.errors.joinToString()}")
            }

            // 更新状态
            val syncTime = System.currentTimeMillis()
            updateSyncStatus(true, syncTime)

            SyncResult.Success(syncTime)
        } catch (e: Exception) {
            updateSyncStatus(false)
            SyncResult.Error("同步失败: ${e.message}")
        }
    }

    override suspend fun getCloudMetadata(): SyncMetadata? {
        val config = getConfig() ?: return null

        return try {
            val client = WebDAVClient(config.serverUrl, config.username, config.password)
            val metadataBytes = client.download(config.remotePath + METADATA_FILE).getOrNull()
                ?: return null

            json.decodeFromString<SyncMetadata>(String(metadataBytes, Charsets.UTF_8))
        } catch (e: Exception) {
            null
        }
    }

    override fun updateSyncStatus(success: Boolean, timestamp: Long) {
        prefs.edit().apply {
            putLong(KEY_LAST_SYNC_TIME, timestamp)
            putString(KEY_LAST_SYNC_STATUS, if (success) "SUCCESS" else "FAILED")
            apply()
        }
    }

    // ========== 辅助方法 ==========

    private fun calculateHash(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(data)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    private fun getDeviceId(): String {
        return Build.MODEL + "_" + Build.BRAND
    }

    private fun getAppVersion(): String {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.PackageInfoFlags.of(0)
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

    companion object {
        private const val PREFS_NAME = "eatwhat_sync_config"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_REMOTE_PATH = "remote_path"
        private const val KEY_ENCRYPTION_ENABLED = "encryption_enabled"
        private const val KEY_ENCRYPTION_PASSWORD = "encryption_password"
        private const val KEY_LAST_SYNC_TIME = "last_sync_time"
        private const val KEY_LAST_SYNC_STATUS = "last_sync_status"
        private const val KEY_AUTO_SYNC_ENABLED = "auto_sync_enabled"
        private const val KEY_SYNC_INTERVAL_MINUTES = "sync_interval_minutes"

        private const val BACKUP_FILE = "backup.json"
        private const val BACKUP_FILE_ENCRYPTED = "backup.enc"
        private const val METADATA_FILE = "metadata.json"
    }
}
