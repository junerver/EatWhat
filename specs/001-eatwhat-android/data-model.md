# Data Model & Database Schema

**Feature**: 吃点啥 Android 应用
**Date**: 2025-12-10
**Database**: Room (SQLite)

## Overview

本文档定义了应用的数据模型和数据库架构。设计遵循以下原则:
- 支持当前功能需求
- 为未来扩展预留字段（云同步、数据导入导出）
- 保持数据完整性（外键约束）
- 优化查询性能（索引）

## Entity Relationship Diagram

```
┌─────────────┐       ┌──────────────┐       ┌─────────────────┐
│   Recipe    │───┬───│  Ingredient  │       │  CookingStep    │
│             │   │   │              │       │                 │
│ id (PK)     │   │   │ id (PK)      │       │ id (PK)         │
│ name        │   │   │ recipeId(FK) │       │ recipeId (FK)   │
│ type        │   │   │ name         │       │ stepNumber      │
│ difficulty  │   │   │ amount       │       │ description     │
│ ...         │   │   │ unit         │       │ ...             │
└─────────────┘   │   └──────────────┘       └─────────────────┘
       │          │
       │          │   ┌──────────────┐
       │          └───│     Tag      │
       │              │              │
       │              │ id (PK)      │
       │              │ name         │
       │              └──────────────┘
       │                     │
       │              ┌──────┴───────┐
       │              │ RecipeTagCrossRef │
       │              │ recipeId (FK)     │
       └──────────────│ tagId (FK)        │
                      └──────────────────┘

┌──────────────────┐       ┌─────────────────────┐
│  HistoryRecord   │───┬───│  HistoryRecipeCrossRef │
│                  │   │   │                     │
│ id (PK)          │   │   │ historyId (FK)      │
│ timestamp        │   │   │ recipeId (FK)       │
│ totalCount       │   │   └─────────────────────┘
│ meatCount        │   │
│ vegCount         │   │   ┌─────────────────┐
│ soupCount        │   └───│    PrepItem     │
│ ...              │       │                 │
└──────────────────┘       │ id (PK)         │
                           │ historyId (FK)  │
                           │ ingredientName  │
                           │ isChecked       │
                           │ ...             │
                           └─────────────────┘
```

## Entities

### 1. RecipeEntity (菜谱)

**Table Name**: `recipes`

**Purpose**: 存储用户创建的菜谱信息

**Fields**:

| Field | Type | Constraints | Description | Future Use |
|-------|------|-------------|-------------|------------|
| id | Long | PRIMARY KEY, AUTO_INCREMENT | 本地唯一标识 | - |
| syncId | String | UNIQUE, NOT NULL | UUID，用于云同步 | 云同步时的全局唯一标识 |
| name | String | NOT NULL | 菜名 | - |
| type | String | NOT NULL | 类型: meat/veg/soup/staple | - |
| icon | String | NOT NULL | Emoji 图标 | - |
| difficulty | String | NOT NULL | 难度: 简单/中等/困难 | - |
| estimatedTime | Int | NOT NULL | 预计时间（分钟） | - |
| createdAt | Long | NOT NULL | 创建时间戳 | - |
| lastModified | Long | NOT NULL | 最后修改时间戳 | 云同步冲突解决 |
| isDeleted | Boolean | NOT NULL, DEFAULT false | 软删除标记 | 云同步时保留删除记录 |

**Indexes**:
- `idx_recipe_type` on `type` (用于类型筛选)
- `idx_recipe_sync_id` on `syncId` (用于云同步查询)
- `idx_recipe_deleted` on `isDeleted` (用于过滤已删除记录)

**Kotlin Entity**:
```kotlin
@Entity(
    tableName = "recipes",
    indices = [
        Index(value = ["type"]),
        Index(value = ["syncId"], unique = true),
        Index(value = ["isDeleted"])
    ]
)
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "sync_id")
    val syncId: String = UUID.randomUUID().toString(),

    val name: String,
    val type: String, // RecipeType enum: MEAT, VEG, SOUP, STAPLE
    val icon: String,
    val difficulty: String, // Difficulty enum: EASY, MEDIUM, HARD

    @ColumnInfo(name = "estimated_time")
    val estimatedTime: Int,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "last_modified")
    val lastModified: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false
)
```

---

### 2. IngredientEntity (食材)

**Table Name**: `ingredients`

**Purpose**: 存储菜谱的食材列表

**Fields**:

| Field | Type | Constraints | Description | Future Use |
|-------|------|-------------|-------------|------------|
| id | Long | PRIMARY KEY, AUTO_INCREMENT | 本地唯一标识 | - |
| recipeId | Long | FOREIGN KEY → recipes(id), ON DELETE CASCADE | 所属菜谱 | - |
| name | String | NOT NULL | 食材名称 | - |
| amount | String | NOT NULL | 数量 | - |
| unit | String | NOT NULL | 单位: g/ml/个/勺/适量 | - |
| orderIndex | Int | NOT NULL | 显示顺序 | - |

**Indexes**:
- `idx_ingredient_recipe_id` on `recipeId` (用于查询菜谱的所有食材)

**Kotlin Entity**:
```kotlin
@Entity(
    tableName = "ingredients",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipe_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["recipe_id"])]
)
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "recipe_id")
    val recipeId: Long,

    val name: String,
    val amount: String,
    val unit: String, // Unit enum: G, ML, PIECE, SPOON, MODERATE

    @ColumnInfo(name = "order_index")
    val orderIndex: Int
)
```

---

### 3. CookingStepEntity (烹饪步骤)

**Table Name**: `cooking_steps`

**Purpose**: 存储菜谱的烹饪步骤

**Fields**:

| Field | Type | Constraints | Description | Future Use |
|-------|------|-------------|-------------|------------|
| id | Long | PRIMARY KEY, AUTO_INCREMENT | 本地唯一标识 | - |
| recipeId | Long | FOREIGN KEY → recipes(id), ON DELETE CASCADE | 所属菜谱 | - |
| stepNumber | Int | NOT NULL | 步骤序号 | - |
| description | String | NOT NULL | 步骤描述 | - |

**Indexes**:
- `idx_step_recipe_id` on `recipeId` (用于查询菜谱的所有步骤)

**Kotlin Entity**:
```kotlin
@Entity(
    tableName = "cooking_steps",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipe_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["recipe_id"])]
)
data class CookingStepEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "recipe_id")
    val recipeId: Long,

    @ColumnInfo(name = "step_number")
    val stepNumber: Int,

    val description: String
)
```

---

### 4. TagEntity (自定义标签)

**Table Name**: `tags`

**Purpose**: 存储用户自定义标签

**Fields**:

| Field | Type | Constraints | Description | Future Use |
|-------|------|-------------|-------------|------------|
| id | Long | PRIMARY KEY, AUTO_INCREMENT | 本地唯一标识 | - |
| name | String | UNIQUE, NOT NULL | 标签名称 | - |
| createdAt | Long | NOT NULL | 创建时间戳 | - |

**Indexes**:
- `idx_tag_name` on `name` (用于标签去重和查询)

**Kotlin Entity**:
```kotlin
@Entity(
    tableName = "tags",
    indices = [Index(value = ["name"], unique = true)]
)
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
```

---

### 5. RecipeTagCrossRef (菜谱-标签关联)

**Table Name**: `recipe_tag_cross_ref`

**Purpose**: 多对多关联表，连接菜谱和标签

**Fields**:

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| recipeId | Long | FOREIGN KEY → recipes(id), ON DELETE CASCADE | 菜谱ID |
| tagId | Long | FOREIGN KEY → tags(id), ON DELETE CASCADE | 标签ID |

**Primary Key**: (recipeId, tagId)

**Kotlin Entity**:
```kotlin
@Entity(
    tableName = "recipe_tag_cross_ref",
    primaryKeys = ["recipe_id", "tag_id"],
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipe_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tag_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["recipe_id"]),
        Index(value = ["tag_id"])
    ]
)
data class RecipeTagCrossRef(
    @ColumnInfo(name = "recipe_id")
    val recipeId: Long,

    @ColumnInfo(name = "tag_id")
    val tagId: Long
)
```

---

### 6. HistoryRecordEntity (历史记录)

**Table Name**: `history_records`

**Purpose**: 存储 Roll 点历史记录

**Fields**:

| Field | Type | Constraints | Description | Future Use |
|-------|------|-------------|-------------|------------|
| id | Long | PRIMARY KEY, AUTO_INCREMENT | 本地唯一标识 | - |
| syncId | String | UNIQUE, NOT NULL | UUID，用于云同步 | 云同步时的全局唯一标识 |
| timestamp | Long | NOT NULL | 记录时间戳 | - |
| totalCount | Int | NOT NULL | 总菜数 | - |
| meatCount | Int | NOT NULL | 荤菜数 | - |
| vegCount | Int | NOT NULL | 素菜数 | - |
| soupCount | Int | NOT NULL | 汤数 | - |
| summary | String | NOT NULL | 摘要文本 | - |
| lastModified | Long | NOT NULL | 最后修改时间戳 | 云同步冲突解决 |
| isDeleted | Boolean | NOT NULL, DEFAULT false | 软删除标记 | 云同步时保留删除记录 |

**Indexes**:
- `idx_history_timestamp` on `timestamp` (用于按时间排序)
- `idx_history_sync_id` on `syncId` (用于云同步查询)

**Kotlin Entity**:
```kotlin
@Entity(
    tableName = "history_records",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["syncId"], unique = true)
    ]
)
data class HistoryRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "sync_id")
    val syncId: String = UUID.randomUUID().toString(),

    val timestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "total_count")
    val totalCount: Int,

    @ColumnInfo(name = "meat_count")
    val meatCount: Int,

    @ColumnInfo(name = "veg_count")
    val vegCount: Int,

    @ColumnInfo(name = "soup_count")
    val soupCount: Int,

    val summary: String,

    @ColumnInfo(name = "last_modified")
    val lastModified: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false
)
```

---

### 7. HistoryRecipeCrossRef (历史-菜谱关联)

**Table Name**: `history_recipe_cross_ref`

**Purpose**: 多对多关联表，记录历史记录中包含的菜谱（快照）

**Fields**:

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| historyId | Long | FOREIGN KEY → history_records(id), ON DELETE CASCADE | 历史记录ID |
| recipeId | Long | NOT NULL | 菜谱ID（不使用外键，保留快照） |
| recipeName | String | NOT NULL | 菜谱名称快照 |
| recipeType | String | NOT NULL | 菜谱类型快照 |
| recipeIcon | String | NOT NULL | 菜谱图标快照 |
| recipeDifficulty | String | NOT NULL | 菜谱难度快照 |
| recipeTime | Int | NOT NULL | 预计时间快照 |

**Primary Key**: (historyId, recipeId)

**Note**: 不使用外键约束到 recipes 表，因为需要保留菜谱快照，即使原菜谱被删除

**Kotlin Entity**:
```kotlin
@Entity(
    tableName = "history_recipe_cross_ref",
    primaryKeys = ["history_id", "recipe_id"],
    foreignKeys = [
        ForeignKey(
            entity = HistoryRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["history_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["history_id"])]
)
data class HistoryRecipeCrossRef(
    @ColumnInfo(name = "history_id")
    val historyId: Long,

    @ColumnInfo(name = "recipe_id")
    val recipeId: Long,

    @ColumnInfo(name = "recipe_name")
    val recipeName: String,

    @ColumnInfo(name = "recipe_type")
    val recipeType: String,

    @ColumnInfo(name = "recipe_icon")
    val recipeIcon: String,

    @ColumnInfo(name = "recipe_difficulty")
    val recipeDifficulty: String,

    @ColumnInfo(name = "recipe_time")
    val recipeTime: Int
)
```

---

### 8. PrepItemEntity (备菜清单项)

**Table Name**: `prep_items`

**Purpose**: 存储历史记录的备菜清单和完成状态

**Fields**:

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | PRIMARY KEY, AUTO_INCREMENT | 本地唯一标识 |
| historyId | Long | FOREIGN KEY → history_records(id), ON DELETE CASCADE | 所属历史记录 |
| ingredientName | String | NOT NULL | 食材名称 |
| isChecked | Boolean | NOT NULL, DEFAULT false | 是否已勾选 |
| orderIndex | Int | NOT NULL | 显示顺序 |

**Indexes**:
- `idx_prep_history_id` on `historyId` (用于查询历史记录的备菜清单)

**Kotlin Entity**:
```kotlin
@Entity(
    tableName = "prep_items",
    foreignKeys = [
        ForeignKey(
            entity = HistoryRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["history_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["history_id"])]
)
data class PrepItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "history_id")
    val historyId: Long,

    @ColumnInfo(name = "ingredient_name")
    val ingredientName: String,

    @ColumnInfo(name = "is_checked")
    val isChecked: Boolean = false,

    @ColumnInfo(name = "order_index")
    val orderIndex: Int
)
```

---

## Relations & Data Classes

### RecipeWithDetails

**Purpose**: 查询菜谱及其关联的食材、步骤、标签

```kotlin
data class RecipeWithDetails(
    @Embedded val recipe: RecipeEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "recipe_id"
    )
    val ingredients: List<IngredientEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "recipe_id"
    )
    val steps: List<CookingStepEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "tag_id",
        associateBy = Junction(
            value = RecipeTagCrossRef::class,
            parentColumn = "recipe_id",
            entityColumn = "tag_id"
        )
    )
    val tags: List<TagEntity>
)
```

### HistoryWithDetails

**Purpose**: 查询历史记录及其关联的菜谱快照、备菜清单

```kotlin
data class HistoryWithDetails(
    @Embedded val history: HistoryRecordEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "history_id"
    )
    val recipeSnapshots: List<HistoryRecipeCrossRef>,

    @Relation(
        parentColumn = "id",
        entityColumn = "history_id"
    )
    val prepItems: List<PrepItemEntity>
)
```

---

## Database Class

```kotlin
@Database(
    entities = [
        RecipeEntity::class,
        IngredientEntity::class,
        CookingStepEntity::class,
        TagEntity::class,
        RecipeTagCrossRef::class,
        HistoryRecordEntity::class,
        HistoryRecipeCrossRef::class,
        PrepItemEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class EatWhatDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun historyDao(): HistoryDao
    abstract fun tagDao(): TagDao
}
```

---

## Migration Strategy

### Version 1 (Initial)
- 创建所有表和索引
- 插入示例数据（可选）

### Future Migrations
- Version 2: 添加云同步相关字段（如已预留）
- Version 3: 添加数据导入导出支持
- Version 4: 添加菜谱图片支持

**Migration Example**:
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 示例：添加新字段
        // database.execSQL("ALTER TABLE recipes ADD COLUMN image_url TEXT")
    }
}
```

---

## Data Validation Rules

### Recipe
- name: 非空，长度 1-50 字符
- type: 必须是 MEAT/VEG/SOUP/STAPLE 之一
- difficulty: 必须是 EASY/MEDIUM/HARD 之一
- estimatedTime: 正整数，范围 1-300 分钟

### Ingredient
- name: 非空，长度 1-30 字符
- amount: 非空，长度 1-10 字符
- unit: 必须是 G/ML/PIECE/SPOON/MODERATE 之一

### CookingStep
- stepNumber: 正整数，从 1 开始
- description: 非空，长度 1-200 字符

### Tag
- name: 非空，长度 1-20 字符，唯一

---

## Query Patterns

### Common Queries

1. **获取所有未删除的菜谱（按类型筛选）**
```kotlin
@Query("SELECT * FROM recipes WHERE is_deleted = 0 AND type = :type ORDER BY name ASC")
fun getRecipesByType(type: String): Flow<List<RecipeEntity>>
```

2. **获取菜谱完整信息（含食材、步骤、标签）**
```kotlin
@Transaction
@Query("SELECT * FROM recipes WHERE id = :recipeId AND is_deleted = 0")
fun getRecipeWithDetails(recipeId: Long): Flow<RecipeWithDetails?>
```

3. **随机选择指定类型的菜谱**
```kotlin
@Query("SELECT * FROM recipes WHERE is_deleted = 0 AND type = :type ORDER BY RANDOM() LIMIT :count")
suspend fun getRandomRecipesByType(type: String, count: Int): List<RecipeEntity>
```

4. **获取历史记录（按时间倒序）**
```kotlin
@Query("SELECT * FROM history_records WHERE is_deleted = 0 ORDER BY timestamp DESC")
fun getAllHistory(): Flow<List<HistoryRecordEntity>>
```

5. **获取历史记录完整信息（含菜谱快照、备菜清单）**
```kotlin
@Transaction
@Query("SELECT * FROM history_records WHERE id = :historyId AND is_deleted = 0")
fun getHistoryWithDetails(historyId: Long): Flow<HistoryWithDetails?>
```

6. **搜索菜谱（按名称或标签）**
```kotlin
@Query("""
    SELECT DISTINCT r.* FROM recipes r
    LEFT JOIN recipe_tag_cross_ref rt ON r.id = rt.recipe_id
    LEFT JOIN tags t ON rt.tag_id = t.id
    WHERE r.is_deleted = 0 AND (r.name LIKE :query OR t.name LIKE :query)
    ORDER BY r.name ASC
""")
fun searchRecipes(query: String): Flow<List<RecipeEntity>>
```

---

## Performance Considerations

1. **索引策略**: 为常用查询字段添加索引（type, timestamp, syncId）
2. **级联删除**: 使用外键级联删除，自动清理关联数据
3. **软删除**: 使用 isDeleted 标记而非物理删除，支持云同步
4. **批量操作**: 使用 @Transaction 确保数据一致性
5. **分页加载**: 对大量数据使用 Paging 3 库（未来优化）

---

## Data Export/Import Format

### JSON Schema (Future)

```json
{
  "version": "1.0",
  "exportDate": "2025-12-10T10:00:00Z",
  "recipes": [
    {
      "syncId": "uuid",
      "name": "宫保鸡丁",
      "type": "meat",
      "icon": "🍗",
      "difficulty": "medium",
      "estimatedTime": 30,
      "ingredients": [...],
      "steps": [...],
      "tags": [...]
    }
  ],
  "history": [...]
}
```

---

## Conclusion

数据模型设计完整支持当前功能需求，同时为未来扩展（云同步、数据导入导出）预留了必要字段。使用 Room 的关系型特性和索引优化确保了查询性能和数据完整性。
