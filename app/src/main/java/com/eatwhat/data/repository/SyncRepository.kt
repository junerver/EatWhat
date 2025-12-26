package com.eatwhat.data.repository

import com.eatwhat.data.sync.ConnectionResult
import com.eatwhat.data.sync.SyncMetadata
import com.eatwhat.data.sync.SyncResult
import com.eatwhat.data.sync.WebDAVConfig

/**
 * 同步仓库接口
 * 负责 WebDAV 配置管理和云端数据同步
 */
interface SyncRepository {

    // ========== 配置管理 ==========

    /**
     * 获取 WebDAV 配置
     */
    fun getConfig(): WebDAVConfig?

    /**
     * 保存 WebDAV 配置
     */
    fun saveConfig(config: WebDAVConfig)

    /**
     * 清除 WebDAV 配置
     */
    fun clearConfig()

    /**
     * 检查是否已配置
     */
    fun isConfigured(): Boolean

    // ========== 连接测试 ==========

    /**
     * 测试 WebDAV 连接
     */
    suspend fun testConnection(config: WebDAVConfig): ConnectionResult

    // ========== 云端同步 ==========

    /**
     * 上传数据到云端
     * @param encryptionPassword 加密密码（如果启用加密）
     */
    suspend fun uploadToCloud(encryptionPassword: String? = null): SyncResult

    /**
     * 从云端下载数据
     * @param encryptionPassword 解密密码（如果数据已加密）
     */
    suspend fun downloadFromCloud(encryptionPassword: String? = null): SyncResult

    /**
     * 获取云端元数据
     */
    suspend fun getCloudMetadata(): SyncMetadata?

    /**
     * 更新同步状态
     */
    fun updateSyncStatus(success: Boolean, timestamp: Long = System.currentTimeMillis())
}
