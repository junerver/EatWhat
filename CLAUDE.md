# EatWhat Development Guidelines

Auto-generated from all feature plans. Last updated: 2025-12-10

## Active Technologies

- **Language**: Kotlin 1.9.21 (001-eatwhat-android)
- **UI Framework**: Jetpack Compose (BOM 2024.01.00) - Pure Compose, no XML layouts
- **State Management**: ComposeHooks (https://github.com/junerver/ComposeHooks)
- **Database**: Room 2.6.1 (SQLite)
- **Navigation**: Navigation Compose 2.7.6
- **Design System**: Material Design 3 (Material You)
- **Build Tool**: Gradle 8.0+ with KSP 1.9.21-1.0.15

## Project Structure

```text
app/src/main/java/com/eatwhat/
├── EatWhatApplication.kt       # Application class with database initialization
├── MainActivity.kt              # Main activity with Compose setContent
├── navigation/                  # Navigation setup
│   ├── NavGraph.kt             # NavHost with all routes
│   └── Destinations.kt         # Sealed class for routes
├── data/                        # Data layer (Room, Repository)
│   ├── database/
│   │   ├── EatWhatDatabase.kt  # Room database
│   │   ├── entities/           # 8 entities (Recipe, Ingredient, etc.)
│   │   ├── dao/                # 3 DAOs (RecipeDao, HistoryDao, TagDao)
│   │   └── relations/          # Relations (RecipeWithDetails, etc.)
│   └── repository/             # Repository pattern implementations
├── domain/                      # Business logic (Use Cases, Models)
│   ├── model/                  # Domain models
│   └── usecase/                # Use cases (RollRecipesUseCase, etc.)
└── ui/                          # UI layer (Compose screens)
    ├── theme/                  # Material 3 theme
    ├── components/             # Reusable components
    └── screens/                # Feature screens (roll/, recipe/, prep/, history/)
```

## Commands

### Build & Run
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on device
./gradlew installDebug

# Run tests
./gradlew test
./gradlew connectedAndroidTest
```

### Code Quality
```bash
# Format code with ktlint
./gradlew ktlintFormat

# Check code style
./gradlew ktlintCheck
```

## Code Style

### Kotlin Conventions
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Follow [Android Kotlin Style Guide](https://developer.android.com/kotlin/style-guide)
- Use ktlint for automatic formatting

### Architecture Principles (from Constitution)
1. **Compose First**: 100% Jetpack Compose, no XML layouts, no View classes
2. **State Management Excellence**: Use ComposeHooks for all state management
3. **Material Design Consistency**: Material 3 components exclusively
4. **User-Centric Simplicity**: Prioritize core features, clear user flows
5. **Code Quality**: Feature-first organization, testable architecture

### ComposeHooks Usage
```kotlin
// State management
val (state, setState) = useState(initialValue)

// Side effects
useEffect(dependency) {
    // Effect code
}

// Async operations
val request = useRequest(
    requestFn = { repository.fetchData() },
    manual = true
)
```

### Database Patterns
- All entities include: `syncId` (UUID), `lastModified` (timestamp), `isDeleted` (soft delete)
- Use `@Transaction` for complex queries
- Return `Flow<T>` for reactive data
- All database operations must be async (Coroutines)

## Recent Changes

- 001-eatwhat-android: Created comprehensive specification with 4 user stories (2 P1, 2 P2)
- 001-eatwhat-android: Generated implementation plan with technical stack and architecture
- 001-eatwhat-android: Defined 8 database entities with Room schema
- 001-eatwhat-android: Generated 120 implementation tasks organized by user story

<!-- MANUAL ADDITIONS START -->
<!-- MANUAL ADDITIONS END -->
