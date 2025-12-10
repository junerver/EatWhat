# Quick Start Guide

**Feature**: 吃点啥 Android 应用
**Date**: 2025-12-10

## Prerequisites

### Required Software
- **Android Studio**: Hedgehog (2023.1.1) or later
- **JDK**: 17 or later
- **Kotlin**: 1.9+ (bundled with Android Studio)
- **Gradle**: 8.0+ (bundled with Android Studio)
- **Android SDK**: API 24+ (Android 7.0) minimum, API 34+ target

### Recommended Tools
- **Git**: For version control
- **Android Emulator**: Pixel 6 or similar (API 34)
- **Physical Device**: Android 7.0+ for testing

## Project Setup

### 1. Clone Repository

```bash
git clone <repository-url>
cd EatWhat
git checkout 001-eatwhat-android
```

### 2. Open in Android Studio

1. Launch Android Studio
2. Select "Open an Existing Project"
3. Navigate to the project directory
4. Wait for Gradle sync to complete

### 3. Configure Build

**Root `build.gradle.kts`**:
```kotlin
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.21" apply false
    id("com.google.devtools.ksp") version "1.9.21-1.0.15" apply false
}
```

**App `build.gradle.kts`**:
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.eatwhat"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.eatwhat"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }
}

dependencies {
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Compose Hooks
    implementation("xyz.junerver.compose:hooks:1.0.0") // Check latest version

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Core
    implementation("androidx.core:core-ktx:1.12.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.room:room-testing:2.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.01.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
```

### 4. Sync Gradle

Click "Sync Now" in Android Studio or run:
```bash
./gradlew build
```

## Project Structure

```
app/src/main/java/com/eatwhat/
├── EatWhatApplication.kt       # Application class
├── MainActivity.kt              # Main activity
├── navigation/                  # Navigation setup
├── data/                        # Data layer (Room, Repository)
├── domain/                      # Business logic (Use Cases, Models)
└── ui/                          # UI layer (Compose screens)
```

## Running the App

### On Emulator

1. Create AVD in Android Studio (Tools > Device Manager)
2. Select Pixel 6, API 34
3. Click "Run" (Shift+F10) or use:
```bash
./gradlew installDebug
```

### On Physical Device

1. Enable Developer Options on device
2. Enable USB Debugging
3. Connect device via USB
4. Click "Run" and select device

## Development Workflow

### 1. Create Feature Branch

```bash
git checkout -b feature/your-feature-name
```

### 2. Write Code

Follow the architecture:
- **Data Layer**: Create entities, DAOs, repositories
- **Domain Layer**: Create models, use cases
- **UI Layer**: Create screens with Compose + ComposeHooks

### 3. Run Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

### 4. Code Style

Format code with ktlint:
```bash
./gradlew ktlintFormat
```

### 5. Build APK

```bash
# Debug APK
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease
```

## Key Files to Start With

### 1. Application Class

**`EatWhatApplication.kt`**:
```kotlin
class EatWhatApplication : Application() {
    lateinit var database: EatWhatDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            EatWhatDatabase::class.java,
            "eatwhat.db"
        ).build()
    }
}
```

### 2. Main Activity

**`MainActivity.kt`**:
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EatWhatTheme {
                EatWhatApp()
            }
        }
    }
}
```

### 3. Navigation Setup

**`navigation/NavGraph.kt`**:
```kotlin
@Composable
fun EatWhatApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "roll",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("roll") { RollScreen(navController) }
            composable("recipes") { RecipeListScreen(navController) }
            composable("history") { HistoryListScreen(navController) }
            // Add more destinations...
        }
    }
}
```

## Database Initialization

### Create Database Class

**`data/database/EatWhatDatabase.kt`**:
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

### Populate Sample Data (Optional)

Add callback to database builder:
```kotlin
.addCallback(object : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        // Insert sample data from contracts/database-schema.sql
    }
})
```

## Using ComposeHooks

### Example: Roll Screen State

```kotlin
@Composable
fun RollScreen(navController: NavController) {
    val app = LocalContext.current.applicationContext as EatWhatApplication
    val repository = remember { RollRepository(app.database) }

    // State management with ComposeHooks
    val (config, setConfig) = useState(RollConfig())
    val (result, setResult) = useState<RollResult?>(null)
    val (isLoading, setIsLoading) = useState(false)

    // Async operation with useRequest
    val rollRequest = useRequest(
        requestFn = { repository.rollRecipes(config) },
        manual = true
    )

    useEffect(rollRequest.data) {
        rollRequest.data?.let { setResult(it) }
    }

    // UI implementation...
}
```

## Debugging

### Enable Room Query Logging

In `EatWhatDatabase` builder:
```kotlin
.setQueryCallback({ sqlQuery, bindArgs ->
    Log.d("RoomQuery", "SQL: $sqlQuery, Args: $bindArgs")
}, Executors.newSingleThreadExecutor())
```

### Inspect Database

Use Android Studio Database Inspector:
1. Run app on emulator/device
2. View > Tool Windows > App Inspection
3. Select "Database Inspector" tab

### Compose Layout Inspector

View > Tool Windows > Layout Inspector

## Common Issues

### Issue: Gradle Sync Failed

**Solution**:
- Check internet connection
- Invalidate Caches (File > Invalidate Caches)
- Update Gradle wrapper: `./gradlew wrapper --gradle-version=8.2`

### Issue: Room Schema Export Error

**Solution**: Create `schemas` directory in app module:
```bash
mkdir -p app/schemas
```

### Issue: ComposeHooks Not Found

**Solution**: Check Maven repository availability and version:
```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}
```

### Issue: Compose Preview Not Working

**Solution**:
- Rebuild project
- Ensure @Preview annotation is present
- Check Compose version compatibility

## Next Steps

1. **Implement Core Features**: Start with Roll点 screen (P1)
2. **Add Recipe Management**: Implement recipe CRUD (P1)
3. **Build Prep List**: Add prep checklist feature (P2)
4. **Add History**: Implement history tracking (P2)
5. **Write Tests**: Add unit and UI tests
6. **Optimize Performance**: Profile and optimize

## Resources

### Documentation
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [ComposeHooks](https://github.com/junerver/ComposeHooks)
- [Material Design 3](https://m3.material.io/)

### Code Style
- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- [Android Kotlin Style Guide](https://developer.android.com/kotlin/style-guide)

### Project Constitution
- See [.specify/memory/constitution.md](../../../.specify/memory/constitution.md)

## Support

For questions or issues:
1. Check project documentation in `specs/001-eatwhat-android/`
2. Review constitution for architectural guidance
3. Consult research.md for technology decisions
