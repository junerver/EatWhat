# 数据模型设计：设置页面与数据同步导出

**Feature Branch**: `003-settings-sync-export`
**Created**: 2025-12-25

---

## 1. 导出数据结构

### ExportData（导出文件根结构）

| 字段 | 类型 | 说明 |
|------|------|------|
| version | String | 数据格式版本（语义化版本号），当前 "1.0.0" |
| exportTime | Long | 导出时间戳（毫秒） |
| appVersion | String | 应用版本号 |
| deviceId | String | 设备唯一标识（可选，用于同步冲突识别） |
| encrypted | Boolean | 是否加密 |
| recipes | List\<RecipeExport\> | 菜谱列表（含食材、步骤、标签） |
| historyRecords | List\<HistoryExport\> | 历史记录列表（含关联菜谱快照） |

### RecipeExport（菜谱导出结构）

| 字段 | 类型 | 说明 |
|------|------|------|
| syncId | String | UUID，用于跨设备同步识别 |
| name | String | 菜谱名称 |
| type | String | 类型（MEAT/VEG/SOUP/STAPLE） |
| icon | String | Emoji 图标 |
| imageBase64 | String? | Base64 编码的 WebP 图片（可选） |
| difficulty | String | 难度（EASY/MEDIUM/HARD） |
| estimatedTime | Int | 预计时间（分钟） |
| ingredients | List\<IngredientExport\> | 食材列表 |
| cookingSteps | List\<CookingStepExport\> | 烹饪步骤列表 |
| tags | List\<String\> | 标签名称列表 |
| createdAt | Long | 创建时间戳 |
| lastModified | Long | 最后修改时间戳 |

### IngredientExport（食材导出结构）

| 字段 | 类型 | 说明 |
|------|------|------|
| name | String | 食材名称 |
| amount | String | 数量 |
| unit | String | 单位（G/ML/PIECE/SPOON/MODERATE） |
| orderIndex | Int | 排序索引 |

### CookingStepExport（烹饪步骤导出结构）

| 字段 | 类型 | 说明 |
|------|------|------|
| stepNumber | Int | 步骤编号 |
| description | String | 步骤描述 |

### HistoryExport（历史记录导出结构）

| 字段 | 类型 | 说明 |
|------|------|------|
| syncId | String | UUID，用于跨设备同步识别 |
| timestamp | Long | 创建时间戳 |
| totalCount | Int | 总菜品数量 |
| meatCount | Int | 荤菜数量 |
| vegCount | Int | 素菜数量 |
| soupCount | Int | 汤数量 |
| summary | String | 摘要（如 "1荤2素1汤"） |
| customName | String | 自定义名称 |
| isLocked | Boolean | 是否锁定 |
| recipes | List\<HistoryRecipeSnapshot\> | 关联菜谱快照 |
| lastModified | Long | 最后修改时间戳 |

### HistoryRecipeSnapshot（历史菜谱快照）

| 字段 | 类型 | 说明 |
|------|------|------|
| recipeId | Long | 原菜谱 ID（仅供参考） |
| name | String | 菜谱名称 |
| type | String | 类型 |
| icon | String | Emoji 图标 |
| imageBase64 | String? | 图片快照（可选） |
| difficulty | String | 难度 |
| estimatedTime | Int | 预计时间 |

---

## 2. WebDAV 配置结构

### WebDAVConfig

| 字段 | 类型 | 说明 |
|------|------|------|
| serverUrl | String | WebDAV 服务器地址（含路径） |
| username | String | 用户名 |
| password | String | 密码（加密存储） |
| remotePath | String | 远程备份目录路径，默认 "/EatWhat/" |
| encryptionEnabled | Boolean | 是否启用数据加密 |
| encryptionPassword | String? | 数据加密密码（加密存储） |
| lastSyncTime | Long? | 上次同步时间戳 |
| lastSyncStatus | String? | 上次同步状态（SUCCESS/FAILED） |

**存储方式**: EncryptedSharedPreferences

---

## 3. 同步元数据

### SyncMetadata（云端元数据文件）

| 字段 | 类型 | 说明 |
|------|------|------|
| syncId | String | 同步会话 ID |
| deviceId | String | 设备标识 |
| uploadTime | Long | 上传时间戳 |
| dataHash | String | 数据 SHA-256 哈希值 |
| appVersion | String | 应用版本 |
| encrypted | Boolean | 是否加密 |

**云端文件结构**:
```
/EatWhat/
├── metadata.json      # 同步元数据
└── backup.json        # 或 backup.enc（加密时）
```

---

## 4. 导入导出状态

### ImportExportState（UI 状态）

| 字段 | 类型 | 说明 |
|------|------|------|
| isLoading | Boolean | 是否加载中 |
| progress | Float | 进度（0.0 - 1.0） |
| status | String | 状态描述 |
| error | String? | 错误信息 |

### ImportPreview（导入预览）

| 字段 | 类型 | 说明 |
|------|------|------|
| recipeCount | Int | 将导入的菜谱数量 |
| historyCount | Int | 将导入的历史数量 |
| newRecipes | Int | 新增菜谱数量 |
| updatedRecipes | Int | 更新菜谱数量 |
| newHistory | Int | 新增历史数量 |
| updatedHistory | Int | 更新历史数量 |

---

## 5. 实体关系图

```
ExportData
├── RecipeExport (1:N)
│   ├── IngredientExport (1:N)
│   ├── CookingStepExport (1:N)
│   └── tags: List<String>
└── HistoryExport (1:N)
    └── HistoryRecipeSnapshot (1:N)

WebDAVConfig (单例)
└── SyncMetadata (云端)
```

---

## 6. 验证规则

### ExportData
- version: 必须符合语义化版本格式 (X.Y.Z)
- exportTime: 必须为正数
- recipes 和 historyRecords: 至少一个非空

### RecipeExport
- syncId: 必须为有效 UUID
- name: 非空，最大 100 字符
- type: 必须为 MEAT/VEG/SOUP/STAPLE 之一
- difficulty: 必须为 EASY/MEDIUM/HARD 之一
- estimatedTime: 必须 >= 0

### WebDAVConfig
- serverUrl: 必须为有效 URL，以 http:// 或 https:// 开头
- username: 非空
- password: 非空
- remotePath: 必须以 "/" 开头和结尾

---

## 7. 与现有实体映射

| 导出结构 | Room 实体 | 映射说明 |
|----------|-----------|----------|
| RecipeExport | RecipeEntity | syncId, 基本字段一一对应 |
| IngredientExport | IngredientEntity | 去除 id, recipeId |
| CookingStepExport | CookingStepEntity | 去除 id, recipeId |
| tags | TagEntity + RecipeTagCrossRef | 仅导出标签名称 |
| HistoryExport | HistoryRecordEntity | syncId, 基本字段对应 |
| HistoryRecipeSnapshot | HistoryRecipeCrossRef | 去除 historyId |
