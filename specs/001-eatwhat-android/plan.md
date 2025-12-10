# Implementation Plan: 吃点啥 Android 应用

**Branch**: `001-eatwhat-android` | **Date**: 2025-12-10 | **Spec**: [spec.md](./spec.md)

## Summary

开发一个基于 Jetpack Compose 的 Android 应用，核心功能是通过"Roll点"系统帮助用户随机选择今天要做的菜。应用包含菜谱管理、食材管理、��菜清单和历史记录功能。采用纯 Compose UI、ComposeHooks 状态管理、Room 数据库持久化，遵循 Material Design 3 设计规范。

## Technical Context

**Language/Version**: Kotlin 1.9+ (latest stable)
**Primary Dependencies**:
- Jetpack Compose (latest stable BOM)
- ComposeHooks (https://github.com/junerver/ComposeHooks)
- Room Database (latest stable)
- Navigation Compose
- Material 3 (androidx.compose.material3)

**Storage**: Room Database (SQLite) for local persistence
**Testing**: JUnit 4/5, Compose UI Testing, Room Testing, MockK
**Target Platform**: Android 7.0+ (API 24+), Target SDK: Latest stable
**Project Type**: Mobile (Android single module)
**Performance Goals**:
- App launch < 2s on mid-range devices
- UI interactions at 60 FPS
- Roll calculation < 500ms

**Constraints**:
- Pure Compose (no XML layouts)
- Offline-first (no network required)
- Material Design 3 compliance
- ComposeHooks for state management

**Scale/Scope**:
- ~10-100 recipes per user
- 3 main screens + detail screens
- Local-only data (future: cloud sync)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### ✅ Principle I: Compose First
- **Status**: PASS
- **Verification**: Project will use 100% Jetpack Compose, no XML layouts, no View classes

### ✅ Principle II: State Management Excellence
- **Status**: PASS
- **Verification**: ComposeHooks library specified for all state management

### ✅ Principle III: Material Design Consistency
- **Status**: PASS
- **Verification**: Material 3 components exclusively, following MD3 guidelines

### ✅ Principle IV: User-Centric Simplicity
- **Status**: PASS
- **Verification**: Spec prioritizes core Roll点 feature, clear user flows, minimal steps

### ✅ Principle V: Code Quality & Maintainability
- **Status**: PASS
- **Verification**: Feature-first organization, testable architecture, clear documentation

**Overall**: ✅ ALL GATES PASS - Proceed to Phase 0

## Project Structure

### Documentation (this feature)

```text
specs/001-eatwhat-android/
├── spec.md              # Feature specification
├── plan.md              # This file
├── research.md          # Phase 0: Technology research
├── data-model.md        # Phase 1: Database schema & entities
├── quickstart.md        # Phase 1: Setup & development guide
├── contracts/           # Phase 1: API contracts (future)
│   └── database-schema.sql
└── tasks.md             # Phase 2: Implementation tasks (via /speckit.tasks)
```

### Source Code (repository root)

```text
app/
├── src/
│   ├── main/
│   │   ├── java/com/eatwhat/
│   │   │   ├── EatWhatApplication.kt
│   │   │   ├── MainActivity.kt
│   │   │   ├── navigation/
│   │   │   │   ├── NavGraph.kt
│   │   │   │   └── Destinations.kt
│   │   │   ├── data/
│   │   │   │   ├── database/
│   │   │   │   │   ├── EatWhatDatabase.kt
│   │   │   │   │   ├── entities/
│   │   │   │   │   │   ├── RecipeEntity.kt
│   │   │   │   │   │   ├── IngredientEntity.kt
│   │   │   │   │   │   ├── CookingStepEntity.kt
│   │   │   │   │   │   ├── TagEntity.kt
│   │   │   │   │   │   ├── HistoryRecordEntity.kt
│   │   │   │   │   │   └── PrepItemEntity.kt
│   │   │   │   │   ├── dao/
│   │   │   │   │   │   ├── RecipeDao.kt
│   │   │   │   │   │   ├── HistoryDao.kt
│   │   │   │   │   │   └── TagDao.kt
│   │   │   │   │   └── relations/
│   │   │   │   │       ├── RecipeWithDetails.kt
│   │   │   │   │       └── HistoryWithDetails.kt
│   │   │   │   └── repository/
│   │   │   │       ├── RecipeRepository.kt
│   │   │   │       ├── HistoryRepository.kt
│   │   │   │       └── RollRepository.kt
│   │   │   ├── domain/
│   │   │   │   ├── model/
│   │   │   │   │   ├── Recipe.kt
│   │   │   │   │   ├── Ingredient.kt
│   │   │   │   │   ├── RollConfig.kt
│   │   │   │   │   └── RollResult.kt
│   │   │   │   └── usecase/
│   │   │   │       ├── RollRecipesUseCase.kt
│   │   │   │       ├── GeneratePrepListUseCase.kt
│   │   │   │       └── SaveHistoryUseCase.kt
│   │   │   └── ui/
│   │   │       ├── theme/
│   │   │       │   ├── Theme.kt
│   │   │       │   ├── Color.kt
│   │   │       │   └── Type.kt
│   │   │       ├── components/
│   │   │       │   ├── BottomNavBar.kt
│   │   │       │   ├── RecipeCard.kt
│   │   │       │   └── IngredientCheckItem.kt
│   │   │       └── screens/
│   │   │           ├── roll/
│   │   │           │   ├── RollScreen.kt
│   │   │           │   ├── RollViewModel.kt
│   │   │           │   └── components/
│   │   │           ├── recipe/
│   │   │           │   ├── RecipeListScreen.kt
│   │   │           │   ├── RecipeDetailScreen.kt
│   │   │           │   ├── AddRecipeScreen.kt
│   │   │           │   └── RecipeViewModel.kt
│   │   │           ├── prep/
│   │   │           │   ├── PrepScreen.kt
│   │   │           │   └── PrepViewModel.kt
│   │   │           └── history/
│   │   │               ├── HistoryListScreen.kt
│   │   │               ├── HistoryDetailScreen.kt
│   │   │               └── HistoryViewModel.kt
│   │   └── res/
│   │       └── values/
│   │           └── strings.xml
│   └── test/
│       └── java/com/eatwhat/
│           ├── data/
│           │   └── repository/
│           ├── domain/
│           │   └── usecase/
│           └── ui/
│               └── screens/
└── build.gradle.kts

build.gradle.kts (root)
settings.gradle.kts
gradle.properties
```

**Structure Decision**: 采用标准 Android 单模块结构，按功能特性组织代码（feature-first）。数据层使用 Room + Repository 模式，UI 层使用 Compose + ComposeHooks，遵循清晰的分层架构（data/domain/ui）。

## Complexity Tracking

> 本项目完全符合宪法要求，无需记录违规项。

## Phase 0: Research & Technology Decisions

详见 [research.md](./research.md)

## Phase 1: Data Model & Contracts

详见 [data-model.md](./data-model.md) 和 [contracts/](./contracts/)

## Phase 2: Implementation Tasks

将通过 `/speckit.tasks` 命令生成，详见 [tasks.md](./tasks.md)
