# 快速入门：设置页面与数据同步导出

**Feature Branch**: `003-settings-sync-export`

---

## 概述

本功能为 EatWhat 应用添加设置页面和数据导入导出能力：

1. **设置入口**：历史页面顶部栏增加设置按钮
2. **本地导入导出**：JSON 格式文件备份恢复
3. **WebDAV 云同步**：配置、上传、下载
4. **数据加密**：AES-GCM 加密保护云端数据

---

## 快速开始

### 1. 添加依赖

在 `gradle/libs.versions.toml` 中添加：

```toml
[versions]
dav4jvm = "2.2.1"
okhttp = "4.12.0"
kotlinxSerialization = "1.6.2"
documentfile = "1.0.1"
securityCrypto = "1.1.0-alpha06"

[libraries]
dav4jvm = { group = "com.github.bitfireAT", name = "dav4jvm", version.ref = "dav4jvm" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinxSerialization" }
androidx-documentfile = { group = "androidx.documentfile", name = "documentfile", version.ref = "documentfile" }
androidx-security-crypto = { group = "androidx.security", name = "security-crypto", version.ref = "securityCrypto" }

[plugins]
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

在 `app/build.gradle.kts` 中添加：

```kotlin
plugins {
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.dav4jvm)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.documentfile)
    implementation(libs.androidx.security.crypto)
}
```

### 2. 添加 JitPack 仓库

在 `settings.gradle.kts` 中：

```kotlin
dependencyResolutionManagement {
    repositories {
        // 现有仓库...
        maven { url = uri("https://jitpack.io") }
    }
}
```

---

## 核心文件结构

```
app/src/main/java/com/eatwhat/
├── data/
│   ├── repository/
│   │   ├── ExportRepository.kt   # 导入导出逻辑
│   │   └── SyncRepository.kt     # WebDAV 同步逻辑
│   └── sync/
│       ├── ExportData.kt         # 导出数据模型
│       ├── WebDAVClient.kt       # WebDAV 操作封装
│       └── CryptoManager.kt      # AES 加解密
└── ui/screens/settings/
    ├── SettingsScreen.kt         # 设置主页面
    ├── WebDAVConfigScreen.kt     # WebDAV 配置页面
    └── SyncScreen.kt             # 同步操作页面
```

---

## 关键实现模式

### 导出数据

```kotlin
// 使用 SAF 创建文件
val exportLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.CreateDocument("application/json")
) { uri ->
    uri?.let {
        scope.launch {
            val data = exportRepository.exportAll()
            val json = Json.encodeToString(data)
            fileHelper.writeToUri(uri, json.toByteArray())
        }
    }
}

// 触发导出
Button(onClick = {
    exportLauncher.launch("eatwhat_backup_${System.currentTimeMillis()}.json")
}) {
    Text("导出数据")
}
```

### WebDAV 同步

```kotlin
// 上传到云端
val syncResult = syncRepository.uploadToCloud(encryptionPassword)
when (syncResult) {
    is SyncResult.Success -> showSuccess("同步完成")
    is SyncResult.Error -> showError(syncResult.message)
}
```

### 数据加密

```kotlin
// 加密数据
val encrypted = cryptoManager.encrypt(jsonData.toByteArray(), password)

// 解密数据
val decrypted = cryptoManager.decrypt(encrypted, password)
```

---

## 测试要点

1. **本地导出**：验证 JSON 格式正确，包含所有关联数据
2. **本地导入**：验证 syncId 冲突处理
3. **WebDAV 连接**：验证认证和错误处理
4. **加密解密**：验证密码正确/错误时的行为
5. **大数据量**：验证 1000 条数据的性能

---

## 注意事项

- 使用 `kotlinx.serialization` 序列化，需要 `@Serializable` 注解
- WebDAV 密码使用 `EncryptedSharedPreferences` 存储
- 云端加密使用用户设置的密码 + PBKDF2 派生密钥
- SAF 不需要存储权限请求
