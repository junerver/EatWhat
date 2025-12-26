# 技术研究报告：设置页面与数据同步导出

**Feature Branch**: `003-settings-sync-export`
**Created**: 2025-12-25

---

## 1. WebDAV 客户端库

### Decision: dav4jvm

**版本**: 2.2.1

**依赖配置**:
```toml
dav4jvm = { group = "com.github.bitfireAT", name = "dav4jvm", version = "2.2.1" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version = "4.12.0" }
```

**Rationale**:
- Kotlin-first 设计，原生支持协程
- 由 DAVx⁵ 团队维护，活跃度高
- 完整支持 WebDAV 协议（PROPFIND, GET, PUT, DELETE, MKCOL）
- 基于 OkHttp，与现代 Android 网络栈兼容

**Alternatives considered**:
- sardine-android：API 更简单但协程支持不够好
- 手写 OkHttp：工作量大，容易出错

---

## 2. AES 加密实现

### Decision: Android Keystore + AES-256-GCM + PBKDF2

**模式选择**: AES-GCM（认证加密）

**Rationale**:
- GCM 提供 AEAD（认证加密），同时保证机密性和完整性
- 硬件加速支持，性能优于 CBC+HMAC
- Android Keystore 提供硬件级密钥保护

**密钥策略**:
- 本地加密：Android Keystore 自动生成密钥
- 云端加密：用户密码 + PBKDF2 派生密钥（100,000 迭代）

**Alternatives considered**:
- AES-CBC + HMAC：需要额外实现完整性校验，复杂度高
- ChaCha20-Poly1305：Android API 28+ 才支持

---

## 3. JSON 序列化

### Decision: kotlinx.serialization

**版本**: 1.6.2

**依赖配置**:
```toml
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version = "1.6.2" }
```

**Rationale**:
- Kotlin-first 设计，完整支持 Kotlin 特性
- 编译时代码生成，性能优于反射
- 官方支持，长期维护
- 与 Room TypeConverter 集成良好

**Alternatives considered**:
- Gson：反射性能差，不支持 Kotlin 特性
- Moshi：优秀但非官方，社区维护

---

## 4. 本地文件存储

### Decision: Storage Access Framework (SAF) + Activity Result API

**依赖配置**:
```toml
androidx-documentfile = { group = "androidx.documentfile", name = "documentfile", version = "1.0.1" }
```

**Rationale**:
- Android 10+ 强制要求 SAF
- 无需存储权限请求
- 用户主导的文件选择，安全可控
- Activity Result API 提供类型安全的回调

**关键 API**:
- `ActivityResultContracts.CreateDocument`：导出文件
- `ActivityResultContracts.OpenDocument`：导入文件
- `rememberLauncherForActivityResult`：Compose 集成

---

## 5. 数据格式设计

### Decision: JSON with versioned schema

**格式规范**:
```json
{
  "version": "1.0.0",
  "exportTime": 1703462400000,
  "appVersion": "1.0.0",
  "metadata": {
    "deviceId": "uuid",
    "encrypted": false
  },
  "recipes": [...],
  "historyRecords": [...],
  "ingredients": [...],
  "cookingSteps": [...]
}
```

**Rationale**:
- JSON 人类可读，便于调试
- 版本字段支持向后兼容
- 完整关联数据打包（菜谱+食材+步骤）

**Alternatives considered**:
- Protobuf：体积小但需要额外依赖，调试困难
- SQLite 文件直接拷贝：版本兼容性差

---

## 6. 密码存储方案

### Decision: EncryptedSharedPreferences

**Rationale**:
- Android Jetpack Security 库提供
- 自动使用 Android Keystore 加密
- API 简单，与 SharedPreferences 兼容

**依赖**:
```toml
androidx-security-crypto = { group = "androidx.security", name = "security-crypto", version = "1.1.0-alpha06" }
```

---

## 安全配置

### 网络安全
- 仅允许 HTTPS（WebDAV 连接）
- 支持用户证书（调试环境）

### ProGuard 规则
- kotlinx.serialization 保留序列化器
- OkHttp 保留类定义
- Crypto 相关类保留

---

## 依赖整合

**新增依赖列表**:

| 库 | 版本 | 用途 |
|---|---|---|
| dav4jvm | 2.2.1 | WebDAV 客户端 |
| okhttp | 4.12.0 | HTTP 客户端 |
| kotlinx-serialization-json | 1.6.2 | JSON 序列化 |
| androidx-documentfile | 1.0.1 | SAF 文件操作 |
| androidx-security-crypto | 1.1.0-alpha06 | 加密存储 |
