# EatWhat Project - AI Agents Rules

**Single Source of Truth for all AI development tools**

Last updated: 2025-12-25

---

## 📋 Overview

This file serves as the **unified rule set** for all AI development tools working on the EatWhat Android project. All tool-specific configuration files should reference this file to ensure consistency.

## 🎯 Architecture Principles

1. **Compose First**: 100% Jetpack Compose, no XML layouts, no View classes
2. **State Management Excellence**: Use ComposeHooks for all state management
3. **Material Design Consistency**: Material 3 components exclusively
4. **User-Centric Simplicity**: Prioritize core features, clear user flows
5. **Code Quality**: Feature-first organization, testable architecture

## 🛠️ Technology Stack

### Core Technologies

- **Language**: Kotlin 1.9.21
- **UI Framework**: Jetpack Compose (BOM 2024.01.00) - Pure Compose, no XML layouts
- **State Management**: [ComposeHooks](https://github.com/junerver/ComposeHooks)
- **Database**: Room 2.6.1 (SQLite)
- **Navigation**: Navigation Compose 2.7.6
- **Design System**: Material Design 3 (Material You)
- **Build Tool**: Gradle 8.0+ with KSP 1.9.21-1.0.15

### Dependencies

```gradle
// See app/build.gradle.kts for complete dependency list
implementation(platform("androidx.compose:compose-bom:2024.01.00"))
implementation("androidx.room:room-runtime:2.6.1")
implementation("xyz.junerver.compose:hooks:3.0.0")
```

## 📁 Project Structure

```text
app/src/main/java/com/eatwhat/
├── EatWhatApplication.kt       # Application class with database initialization
├── MainActivity.kt              # Main activity with Compose setContent
├── navigation/                  # Navigation setup
│   ├── NavGraph.kt             # NavHost with all routes
│   └── Destinations.kt         # Sealed class for routes
├── data/                        # Data layer (Room, Repository)
│   ├── database/
│   │   ├── EatWhatDatabase.kt  # Room database (current version: 3)
│   │   ├── entities/           # 8 entities (Recipe, Ingredient, CookingStep, etc.)
│   │   ├── dao/                # 3 DAOs (RecipeDao, HistoryDao, TagDao)
│   │   └── relations/          # Relations (RecipeWithDetails, HistoryWithDetails)
│   └── repository/             # Repository pattern implementations
├── domain/                      # Business logic (Use Cases, Models)
│   ├── model/                  # Domain models (pure Kotlin, no Android deps)
│   └── usecase/                # Use cases (RollRecipesUseCase, etc.)
└── ui/                          # UI layer (Compose screens)
    ├── theme/                  # Material 3 theme
    ├── components/             # Reusable components
    └── screens/                # Feature screens (roll/, recipe/, prep/, history/)
```

## 💻 Code Style

### Kotlin Conventions

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Follow [Android Kotlin Style Guide](https://developer.android.com/kotlin/style-guide)
- Use ktlint for automatic formatting

### File Organization

- One class per file
- File name matches the class name
- Package structure follows feature modules

### Naming Conventions

- **Entities**: `*Entity.kt` (e.g., `RecipeEntity.kt`)
- **DAOs**: `*Dao.kt` (e.g., `RecipeDao.kt`)
- **Repositories**: `*Repository.kt` (e.g., `RecipeRepository.kt`)
- **Use Cases**: `*UseCase.kt` (e.g., `RollRecipesUseCase.kt`)
- **Screens**: `*Screen.kt` (e.g., `RollScreen.kt`)
- **Components**: Descriptive names (e.g., `RecipeCard.kt`, `BottomNavBar.kt`)
- **View Models**: `*ViewModel.kt` (if needed, though we prefer ComposeHooks)

## 🎨 UI Development

### Design System

#### Color Palette

项目使用温暖、现代的配色方案：

```kotlin
// 主色调 - 温暖橙色系
private val PrimaryOrange = Color(0xFFFF6B35)      // 主要操作、强调
private val PrimaryOrangeLight = Color(0xFFFF8C5A) // 浅色变体
private val PrimaryOrangeDark = Color(0xFFE55A2B)  // 深色变体

// 功能色
private val SoftGreen = Color(0xFF4CAF50)   // 食材相关、成功状态
private val SoftBlue = Color(0xFF2196F3)    // 步骤相关、信息状态
private val SoftPurple = Color(0xFF9C27B0)  // 特殊功能
private val WarmYellow = Color(0xFFFFC107)  // 中等难度、警告

// 背景色
private val CardBackground = Color(0xFFFFFBF8)  // 卡片背景
private val PageBackground = Color(0xFFF5F5F5)  // 页面背景
```

#### 深色模式适配规范

项目支持系统深色模式自动切换，所有 UI 组件必须正确适配深色模式。

**核心原则**：

1. 使用 `isSystemInDarkTheme()` 检测深色模式
2. 优先使用 `MaterialTheme.colorScheme` 语义化颜色
3. 硬编码颜色必须提供深色模式变体
4. 状态栏和导航栏需要同步适配

**标准适配模式**：

```kotlin
import androidx.compose.foundation.isSystemInDarkTheme

@Composable
fun AdaptiveComponent() {
    val isDark = isSystemInDarkTheme()

    // 方式1：使用 MaterialTheme 语义化颜色（推荐）
    val backgroundColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface

    // 方式2：条件颜色选择（用于自定义颜色）
    val cardBackground = if (isDark)
        MaterialTheme.colorScheme.surfaceVariant
    else
        Color(0xFFF8FBF8)
}
```

**常用颜色映射表**：

| 浅色模式 | 深色模式 | 用途 |
|---------|---------|-----|
| `Color.White` | `MaterialTheme.colorScheme.surface` | 卡片背景 |
| `Color(0xFFF8F8F8)` | `MaterialTheme.colorScheme.surfaceVariant` | 列表项背景 |
| `Color(0xFFF8FBF8)` | `MaterialTheme.colorScheme.surfaceVariant` | 食材卡片背景 |
| `Color(0xFFF5F9FF)` | `MaterialTheme.colorScheme.surfaceVariant` | 步骤卡片背景 |
| `Color(0xFFE0E0E0)` | `Color(0xFF3C3C3F)` | 进度条轨道 |
| `Color(0xFFE0E0E0)` | `Color(0xFF4A4A4A)` | 边框颜色 |
| `Color.Gray` | `MaterialTheme.colorScheme.onSurfaceVariant` | 次要文字 |

**进度条适配**：

```kotlin
val isDark = isSystemInDarkTheme()
val trackColor = if (isDark) Color(0xFF3C3C3F) else Color(0xFFE0E0E0)

LinearProgressIndicator(
    progress = progress,
    color = PrimaryOrange,
    trackColor = trackColor
)
```

**渐变背景适配**（如 RollScreen）：

```kotlin
val isDarkTheme = isSystemInDarkTheme()

val backgroundBrush = if (isDarkTheme) {
    Brush.linearGradient(
        colors = listOf(Color(0xFF1C1B1F), Color(0xFF2D2D30))
    )
} else {
    Brush.linearGradient(
        colors = listOf(PrimaryOrange, PrimaryOrangeLight)
    )
}
```

**状态栏适配**：

```kotlin
val view = LocalView.current
val darkTheme = isSystemInDarkTheme()

SideEffect {
    val window = (view.context as Activity).window
    // 深色模式使用透明状态栏，浅色模式可使用主题色
    window.statusBarColor = if (darkTheme) {
        android.graphics.Color.TRANSPARENT
    } else {
        PrimaryOrange.toArgb()
    }
    // 根据背景亮度设置状态栏图标颜色
    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
}
```

**复选框/选择项适配**：

```kotlin
val isDark = isSystemInDarkTheme()
val uncheckedBackground = if (isDark) MaterialTheme.colorScheme.surface else Color.White
val uncheckedBorderColor = if (isDark) Color(0xFF4A4A4A) else Color(0xFFE0E0E0)
val uncheckedCheckboxColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF5F5F5)

// 选中状态保持功能色不变
val checkedBackground = SoftGreen.copy(alpha = 0.1f)
val checkedBorderColor = SoftGreen.copy(alpha = 0.3f)
```

**底部导航栏与列表**：

当页面包含底部导航栏时，LazyColumn 需要预留底部内边距（约 88dp）：

```kotlin
LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(
        start = 16.dp,
        end = 16.dp,
        top = 16.dp,
        bottom = 88.dp  // 为底部导航栏预留空间
    )
)
```

**深色模式适配检查清单**：

- [ ] 所有硬编码背景色已提供深色变体
- [ ] 文字颜色使用 `onSurface` / `onSurfaceVariant`
- [ ] 边框颜色在深色模式下可见
- [ ] 进度条轨道颜色已适配
- [ ] 状态栏颜色和图标已适配
- [ ] 功能色（绿/蓝/橙）保持不变，仅调整透明度

#### 卡片设计规范

**SectionCard 组件模式**：

- 白色背景 (`Color.White`)
- 20dp 圆角 (`RoundedCornerShape(20.dp)`)
- 4dp 柔和阴影 (`shadow(elevation = 4.dp)`)
- 20dp 内边距
- 带图标标题区域（图标背景 40x40dp，12dp 圆角）

```kotlin
Card(
    modifier = Modifier
        .fillMaxWidth()
        .shadow(
            elevation = 4.dp,
            shape = RoundedCornerShape(20.dp),
            spotColor = Color.Black.copy(alpha = 0.1f)
        ),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White)
)
```

#### 输入框设计规范

**StyledTextField 模式**：

- 无边框设计
- 灰色背景 (`Color(0xFFF8F8F8)`)
- 12dp 圆角
- 支持 leading/trailing 图标
- 自定义 placeholder 样式

```kotlin
Surface(
    shape = RoundedCornerShape(12.dp),
    color = Color(0xFFF8F8F8)
) {
    BasicTextField(
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // leadingIcon
                innerTextField()
                // trailingIcon
            }
        }
    )
}
```

#### 选择器设计规范

**类型/难度选择器**：

- 卡片式设计，非传统 Chip
- Emoji + 文字组合
- 选中状态：彩色边框 + 浅色背景
- 未选中状态：灰色背景 (`Color(0xFFF5F5F5)`)

```kotlin
Surface(
    onClick = onClick,
    shape = RoundedCornerShape(12.dp),
    color = if (isSelected) color.copy(alpha = 0.15f) else Color(0xFFF5F5F5),
    border = if (isSelected) BorderStroke(2.dp, color) else null
) {
    Column(
        modifier = Modifier.padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 20.sp)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
```

#### 列表项设计规范

**食材输入卡片**：

- 浅绿色背景 (`Color(0xFFF8FBF8)`)
- 1dp 绿色边框 (`SoftGreen.copy(alpha = 0.2f)`)
- 16dp 圆角
- 圆形序号徽章（28dp，绿色背景）
- 紧凑型单位选择器

**烹饪步骤卡片**：

- 浅蓝色背景 (`Color(0xFFF5F9FF)`)
- 1dp 蓝色边框 (`SoftBlue.copy(alpha = 0.2f)`)
- 渐变圆形步骤编号（40dp）
- 时间线连接器（2dp 宽，16dp 高）

#### 标签设计规范

**TagsFlowRow**：

- 使用 `FlowRow` 布局
- 彩色粉彩背景（随机柔和色）
- 20dp 圆角胶囊形状
- 32dp 高度
- 添加按钮使用主色调浅色背景

#### 按钮设计规范

**主要操作按钮**：

- `FilledTonalButton` 配合主色调
- 图标 + 文字组合
- 加载状态显示 `CircularProgressIndicator`

**添加按钮**：

- 圆形设计（36dp）
- 浅色背景（10% 透明度）
- 图标使用对应功能色

#### 动画规范

- 列表项使用 `AnimatedVisibility`
- 进入动画：`fadeIn() + expandVertically()`
- 退出动画：`fadeOut() + shrinkVertically()`

### ComposeHooks Usage Pattern

```kotlin
@Composable
fun FeatureScreen(navController: NavController) {
    // State management with hooks
    val (state, setState) = useState(initialValue)
    val (loading, setLoading) = useState(false)

    // Side effects
    useEffect(Unit) {
        // Initial load
    }

    // Async operations with useRequest
    val request = useRequest(
        requestFn = { repository.fetchData() },
        manual = true,
        onSuccess = { data ->
            setState(data)
        }
    )

    // UI implementation
    Scaffold(
        containerColor = Color(0xFFF5F5F5) // 使用统一页面背景色
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 使用 SectionCard 组织内容
        }
    }
}
```

### Compose Best Practices

- Use Material 3 components exclusively
- Implement proper state hoisting
- Use ComposeHooks for state management (not ViewModel)
- Follow single source of truth principle
- Implement proper error states and loading states
- Use `remember` and `rememberSaveable` appropriately
- Avoid side effects in composition
- **使用统一的设计规范组件**（SectionCard, StyledTextField 等）
- **保持颜色系统一致性**（使用预定义的主题色）
- **遵循圆角规范**（卡片 20dp，输入框 12dp，标签 20dp）

### UI Testing

- Write UI tests for critical user flows
- Use Compose Testing APIs
- Test different screen sizes and orientations

## 🗄️ Database Guidelines

### Entity Design Principles

All entities MUST include:

```kotlin
@Entity(
    tableName = "table_name",
    indices = [/* appropriate indices */]
)
data class EntityName(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "sync_id")
    val syncId: String = UUID.randomUUID().toString(), // For future sync

    // Business fields

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "last_modified")
    val lastModified: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false // Soft delete
)
```

### Required Indices

- `sync_id`: Unique index for sync functionality
- `is_deleted`: Index for filtering deleted items
- Foreign keys: Index all foreign key columns
- Frequently queried fields: Add appropriate indices

### Migration Strategy

```kotlin
// In EatWhatDatabase companion object
val MIGRATION_X_Y = object : Migration(X, Y) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // SQL migration statements
        // Always add comments explaining the change
    }
}
```

**Migration Rules**:

1. Never break existing data
2. Always provide default values for new columns
3. Create indices in the same migration
4. Document the reason for migration in database class comments
5. Test migrations thoroughly
6. Export schema: `exportSchema = true` in `@Database`

### DAO Patterns

```kotlin
@Dao
interface EntityDao {
    // Queries MUST filter soft-deleted items
    @Query("SELECT * FROM table_name WHERE is_deleted = 0")
    fun getAll(): Flow<List<Entity>>

    // Use Flow for reactive queries
    @Query("SELECT * FROM table_name WHERE id = :id AND is_deleted = 0")
    fun getById(id: Long): Flow<Entity?>

    // Use suspend for write operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: Entity): Long

    @Update
    suspend fun update(entity: Entity)

    // Soft delete, not hard delete
    @Query("UPDATE table_name SET is_deleted = 1, last_modified = :timestamp WHERE id = :id")
    suspend fun softDelete(id: Long, timestamp: Long = System.currentTimeMillis())

    // Use @Transaction for complex queries
    @Transaction
    @Query("SELECT * FROM table_name WHERE id = :id AND is_deleted = 0")
    fun getWithDetails(id: Long): Flow<EntityWithDetails?>
}
```

### Repository Pattern

```kotlin
class EntityRepository(private val dao: EntityDao) {
    // Expose Flow for reactive data
    fun getAll(): Flow<List<DomainModel>> =
        dao.getAll().map { it.map { entity -> entity.toDomain() } }

    fun getById(id: Long): Flow<DomainModel?> =
        dao.getById(id).map { it?.toDomain() }

    // Suspend for write operations
    suspend fun insert(model: DomainModel): Long {
        return dao.insert(model.toEntity())
    }

    suspend fun update(model: DomainModel) {
        dao.update(model.toEntity())
    }

    suspend fun delete(id: Long) {
        dao.softDelete(id)
    }
}
```

### Domain Model Conversion

```kotlin
// Entity to Domain
fun EntityClass.toDomain(): DomainClass {
    return DomainClass(
        id = id,
        // map fields
    )
}

// Domain to Entity
fun DomainClass.toEntity(): EntityClass {
    return EntityClass(
        id = id,
        syncId = syncId ?: UUID.randomUUID().toString(),
        // map fields
        lastModified = System.currentTimeMillis()
    )
}
```

## 🔧 Build & Development

### Gradle Commands

```bash
# Build
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK
./gradlew installDebug           # Install on device

# Testing
./gradlew test                   # Run unit tests
./gradlew connectedAndroidTest   # Run instrumented tests

# Code Quality
./gradlew ktlintFormat           # Format code
./gradlew ktlintCheck            # Check style
./gradlew lint                   # Run Android lint
```

### Development Workflow

1. Create feature branch from `main`
2. Write tests first (TDD when applicable)
3. Implement feature following these rules
4. Run `ktlintFormat` before committing
5. Ensure all tests pass
6. Create PR with clear description

## 🧪 Testing Guidelines

### Unit Tests

- Test all use cases
- Test repository logic
- Test domain model transformations
- Use MockK for mocking
- Aim for >80% coverage on business logic

### Integration Tests

- Test DAO operations
- Test database migrations
- Test complex queries with @Transaction

### UI Tests

- Test critical user flows
- Test navigation
- Test state changes
- Use Compose Testing library

## 🚫 Anti-Patterns to Avoid

### UI Layer

- ❌ Using XML layouts
- ❌ Using Android View classes
- ❌ Using ViewModel (use ComposeHooks instead)
- ❌ Direct database access from UI
- ❌ Business logic in Composables

### Data Layer

- ❌ Hard deletes (always use soft delete)
- ❌ Blocking operations on main thread
- ❌ Returning LiveData (use Flow)
- ❌ Missing indices on foreign keys
- ❌ Missing migration strategy

### General

- ❌ Hardcoded strings (use strings.xml)
- ❌ Magic numbers (use constants)
- ❌ God objects/classes
- ❌ Circular dependencies

## 📝 Documentation

### Code Comments

- Add KDoc for public APIs
- Explain "why" not "what"
- Document complex algorithms
- Document migration reasons

### Entity Documentation

```kotlin
/**
 * Recipe entity for Room database
 * Represents a recipe with all its metadata
 *
 * @property id Auto-generated primary key
 * @property syncId UUID for sync functionality
 * @property name Recipe name
 * @property type Recipe type (MEAT, VEG, SOUP, STAPLE)
 * @property isDeleted Soft delete flag
 */
@Entity(tableName = "recipes")
data class RecipeEntity(...)
```

## 🔄 Change Log

### Database Schema Version History

- **v1**: Initial schema with all entities
- **v2**: Added `is_locked` column to `history_records` with index
- **v3**: Added `custom_name` column to `history_records`

### Recent Updates

- 2025-12-25: Added comprehensive dark mode adaptation guidelines
- 2025-12-15: Created unified AGENTS.md for all
