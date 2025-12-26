# 内部契约：导入导出服务

**Feature Branch**: `003-settings-sync-export`
**Created**: 2025-12-25

---

## 1. ExportRepository 接口

```kotlin
interface ExportRepository {
    /**
     * 导出所有数据
     * @return ExportData 导出数据对象
     */
    suspend fun exportAll(): ExportData

    /**
     * 仅导出菜谱数据
     * @return ExportData 仅包含菜谱的导出数据
     */
    suspend fun exportRecipesOnly(): ExportData

    /**
     * 仅导出历史记录
     * @return ExportData 仅包含历史的导出数据
     */
    suspend fun exportHistoryOnly(): ExportData

    /**
     * 导入数据
     * @param data 导入的数据
     * @param strategy 冲突处理策略
     * @return ImportResult 导入结果
     */
    suspend fun importData(data: ExportData, strategy: ConflictStrategy): ImportResult

    /**
     * 预览导入
     * @param data 待导入的数据
     * @return ImportPreview 导入预览
     */
    suspend fun previewImport(data: ExportData): ImportPreview
}
```

---

## 2. SyncRepository 接口

```kotlin
interface SyncRepository {
    /**
     * 获取 WebDAV 配置
     */
    fun getConfig(): Flow<WebDAVConfig?>

    /**
     * 保存 WebDAV 配置
     */
    suspend fun saveConfig(config: WebDAVConfig)

    /**
     * 清除 WebDAV 配置
     */
    suspend fun clearConfig()

    /**
     * 测试 WebDAV 连接
     * @return ConnectionResult 连接结果
     */
    suspend fun testConnection(config: WebDAVConfig): ConnectionResult

    /**
     * 上传数据到云端
     * @param encryptionPassword 加密密码（可选）
     * @return SyncResult 同步结果
     */
    suspend fun uploadToCloud(encryptionPassword: String?): SyncResult

    /**
     * 从云端下载数据
     * @param encryptionPassword 解密密码（可选）
     * @return ExportData 下载的数据
     */
    suspend fun downloadFromCloud(encryptionPassword: String?): ExportData

    /**
     * 获取云端元数据
     * @return SyncMetadata? 元数据或 null（无数据）
     */
    suspend fun getCloudMetadata(): SyncMetadata?
}
```

---

## 3. CryptoManager 接口

```kotlin
interface CryptoManager {
    /**
     * 使用密码加密数据
     * @param data 原始数据
     * @param password 加密密码
     * @return EncryptedData 加密后的数据（含 IV 和 Salt）
     */
    fun encrypt(data: ByteArray, password: String): EncryptedData

    /**
     * 使用密码解密数据
     * @param encryptedData 加密数据
     * @param password 解密密码
     * @return ByteArray 解密后的数据
     * @throws DecryptionException 密码错误或数据损坏
     */
    fun decrypt(encryptedData: EncryptedData, password: String): ByteArray
}
```

---

## 4. 数据类型定义

### ConflictStrategy

```kotlin
enum class ConflictStrategy {
    SKIP,           // 跳过已存在的数据
    UPDATE,         // 更新已存在的数据
    UPDATE_IF_NEWER // 仅当导入数据更新时更新
}
```

### ImportResult

```kotlin
data class ImportResult(
    val success: Boolean,
    val recipesImported: Int,
    val recipesUpdated: Int,
    val recipesSkipped: Int,
    val historyImported: Int,
    val historyUpdated: Int,
    val historySkipped: Int,
    val errors: List<String>
)
```

### ConnectionResult

```kotlin
sealed class ConnectionResult {
    object Success : ConnectionResult()
    data class Error(val message: String, val code: Int?) : ConnectionResult()
}
```

### SyncResult

```kotlin
sealed class SyncResult {
    data class Success(val syncTime: Long) : SyncResult()
    data class Error(val message: String) : SyncResult()
}
```

### EncryptedData

```kotlin
data class EncryptedData(
    val ciphertext: ByteArray,
    val iv: ByteArray,
    val salt: ByteArray
)
```

---

## 5. 文件操作契约

### FileHelper 接口

```kotlin
interface FileHelper {
    /**
     * 写入数据到 URI
     */
    suspend fun writeToUri(uri: Uri, data: ByteArray)

    /**
     * 从 URI 读取数据
     */
    suspend fun readFromUri(uri: Uri): ByteArray

    /**
     * 获取文件信息
     */
    fun getFileInfo(uri: Uri): FileInfo?
}
```

---

## 6. 导航契约

### 新增路由

| 路由 | 参数 | 说明 |
|------|------|------|
| settings | 无 | 设置主页面 |
| settings/webdav | 无 | WebDAV 配置页面 |
| settings/sync | 无 | 同步操作页面 |

### 导航方法

```kotlin
sealed class Destinations {
    // 现有路由...

    object Settings : Destinations("settings")
    object WebDAVConfig : Destinations("settings/webdav")
    object Sync : Destinations("settings/sync")
}
```
