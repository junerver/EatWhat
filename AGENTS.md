# EatWhat Project - AI Agents Rules

**Single Source of Truth for all AI development tools**

Last updated: 2025-12-15

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
    Scaffold { paddingValues ->
        // Content
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

- 2025-12-15: Created unified AGENTS.md for all
