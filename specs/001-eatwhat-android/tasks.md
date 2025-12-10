# Tasks: 吃点啥 Android 应用

**Input**: Design documents from `/specs/001-eatwhat-android/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/database-schema.sql

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3, US4)
- Include exact file paths in descriptions

## Path Conventions

- Android project: `app/src/main/java/com/eatwhat/`
- Test files: `app/src/test/java/com/eatwhat/`
- Resources: `app/src/main/res/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [X] T001 Create Android project with Kotlin and Jetpack Compose support
- [X] T002 Configure root `build.gradle.kts` with plugin versions (Android 8.2.0, Kotlin 1.9.21, KSP 1.9.21-1.0.15)
- [X] T003 Configure app `build.gradle.kts` with all dependencies (Compose BOM, ComposeHooks, Room, Navigation)
- [X] T004 [P] Create project directory structure per plan.md (data/, domain/, ui/, navigation/)
- [X] T005 [P] Configure ProGuard rules for Room and Compose in `proguard-rules.pro`
- [X] T006 [P] Create `strings.xml` with all app strings
- [X] T007 Create `EatWhatApplication.kt` in `app/src/main/java/com/eatwhat/`
- [X] T008 Configure `AndroidManifest.xml` with application class and main activity

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

### Database Foundation

- [X] T009 Create `RecipeEntity.kt` in `app/src/main/java/com/eatwhat/data/database/entities/`
- [X] T010 [P] Create `IngredientEntity.kt` in `app/src/main/java/com/eatwhat/data/database/entities/`
- [X] T011 [P] Create `CookingStepEntity.kt` in `app/src/main/java/com/eatwhat/data/database/entities/`
- [X] T012 [P] Create `TagEntity.kt` in `app/src/main/java/com/eatwhat/data/database/entities/`
- [X] T013 [P] Create `RecipeTagCrossRef.kt` in `app/src/main/java/com/eatwhat/data/database/entities/`
- [X] T014 [P] Create `HistoryRecordEntity.kt` in `app/src/main/java/com/eatwhat/data/database/entities/`
- [X] T015 [P] Create `HistoryRecipeCrossRef.kt` in `app/src/main/java/com/eatwhat/data/database/entities/`
- [X] T016 [P] Create `PrepItemEntity.kt` in `app/src/main/java/com/eatwhat/data/database/entities/`
- [X] T017 Create `RecipeWithDetails.kt` relation in `app/src/main/java/com/eatwhat/data/database/relations/`
- [X] T018 [P] Create `HistoryWithDetails.kt` relation in `app/src/main/java/com/eatwhat/data/database/relations/`
- [X] T019 Create `RecipeDao.kt` in `app/src/main/java/com/eatwhat/data/database/dao/`
- [X] T020 [P] Create `HistoryDao.kt` in `app/src/main/java/com/eatwhat/data/database/dao/`
- [X] T021 [P] Create `TagDao.kt` in `app/src/main/java/com/eatwhat/data/database/dao/`
- [X] T022 Create `EatWhatDatabase.kt` in `app/src/main/java/com/eatwhat/data/database/` with all entities and DAOs
- [X] T023 Initialize database in `EatWhatApplication.kt` with Room.databaseBuilder
- [X] T024 [P] Add database callback to populate sample data from contracts/database-schema.sql

### Domain Models

- [X] T025 [P] Create `Recipe.kt` domain model in `app/src/main/java/com/eatwhat/domain/model/`
- [X] T026 [P] Create `Ingredient.kt` domain model in `app/src/main/java/com/eatwhat/domain/model/`
- [X] T027 [P] Create `CookingStep.kt` domain model in `app/src/main/java/com/eatwhat/domain/model/`
- [X] T028 [P] Create `Tag.kt` domain model in `app/src/main/java/com/eatwhat/domain/model/`
- [X] T029 [P] Create `RollConfig.kt` domain model in `app/src/main/java/com/eatwhat/domain/model/`
- [X] T030 [P] Create `RollResult.kt` domain model in `app/src/main/java/com/eatwhat/domain/model/`
- [X] T031 [P] Create `HistoryRecord.kt` domain model in `app/src/main/java/com/eatwhat/domain/model/`
- [X] T032 [P] Create `PrepItem.kt` domain model in `app/src/main/java/com/eatwhat/domain/model/`

### UI Foundation

- [X] T033 Create `Theme.kt` in `app/src/main/java/com/eatwhat/ui/theme/` with Material 3 theme
- [X] T034 [P] Create `Color.kt` in `app/src/main/java/com/eatwhat/ui/theme/` with color scheme
- [X] T035 [P] Create `Type.kt` in `app/src/main/java/com/eatwhat/ui/theme/` with typography
- [X] T036 Create `Destinations.kt` in `app/src/main/java/com/eatwhat/navigation/` with sealed class for routes
- [X] T037 Create `NavGraph.kt` in `app/src/main/java/com/eatwhat/navigation/` with NavHost setup
- [X] T038 Create `BottomNavBar.kt` in `app/src/main/java/com/eatwhat/ui/components/` with 3 tabs (Roll点, 菜谱, 历史)
- [X] T039 Create `MainActivity.kt` in `app/src/main/java/com/eatwhat/` with Compose setContent and EatWhatApp

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Roll点 (Priority: P1) 🎯 MVP

**Goal**: 用户可以配置荤素搭配规则,随机选择今天要做的菜,并查看结果

**Independent Test**:
1. 打开应用进入Roll点页面
2. 配置荤菜1道、素菜2道
3. 点击"Roll点"按钮
4. 验证显示3道随机菜谱(1荤2素)
5. 验证可以重新Roll或确认结果

### Implementation for User Story 1

- [X] T040 [P] [US1] Create `RecipeRepository.kt` in `app/src/main/java/com/eatwhat/data/repository/` with basic CRUD operations
- [X] T041 [US1] Create `RollRepository.kt` in `app/src/main/java/com/eatwhat/data/repository/` with random recipe selection logic
- [X] T042 [US1] Create `RollRecipesUseCase.kt` in `app/src/main/java/com/eatwhat/domain/usecase/` with Roll algorithm
- [X] T043 [US1] Implement Roll algorithm: validate config, query recipes by type, random selection, avoid duplicates
- [X] T044 [US1] Create `RollScreen.kt` in `app/src/main/java/com/eatwhat/ui/screens/roll/` with ComposeHooks state management
- [X] T045 [US1] Implement Roll configuration UI: counters for meat/veg/soup/staple with +/- buttons
- [X] T046 [US1] Implement "荤素搭配" toggle in Roll configuration (auto-balance meat and veg)
- [X] T047 [US1] Implement Roll button with loading state using useRequest hook
- [X] T048 [US1] Create `RecipeCard.kt` component in `app/src/main/java/com/eatwhat/ui/components/` for displaying recipe in result
- [X] T049 [US1] Implement Roll result display with recipe cards (name, icon, type, difficulty, time)
- [X] T050 [US1] Implement "重新Roll" button to regenerate results
- [X] T051 [US1] Implement "确认" button to proceed to prep list (navigate to PrepScreen)
- [X] T052 [US1] Add error handling for insufficient recipes (show toast/snackbar)
- [X] T053 [US1] Add validation: at least one recipe type must be selected
- [X] T054 [US1] Initialize RecipeRepository and RollRepository in EatWhatApplication

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently

---

## Phase 4: User Story 2 - 菜谱管理 (Priority: P1)

**Goal**: 用户可以添加、查看、编辑、删除菜谱,包括食材和烹饪步骤

**Independent Test**:
1. 进入菜谱列表页面
2. 点击"+"添加新菜谱
3. 填写菜谱信息(名称、类型、难度、时间、食材、步骤、标签)
4. 保存后在列表中查看
5. 点击菜谱查看详情
6. 编辑和删除菜谱

### Implementation for User Story 2

- [X] T055 [US2] Extend `RecipeRepository.kt` with full CRUD operations (insert, update, delete, getAll, getById, search)
- [X] T056 [P] [US2] Create `TagRepository.kt` in `app/src/main/java/com/eatwhat/data/repository/` for tag management
- [X] T057 [US2] Create `RecipeListScreen.kt` in `app/src/main/java/com/eatwhat/ui/screens/recipe/` with LazyColumn
- [X] T058 [US2] Implement recipe list with type filter tabs (全部, 荤菜, 素菜, 汤, 主食)
- [X] T059 [US2] Implement search bar in recipe list (search by name or tag)
- [X] T060 [US2] Implement FAB "+" button to navigate to AddRecipeScreen
- [X] T061 [US2] Create `RecipeDetailScreen.kt` in `app/src/main/java/com/eatwhat/ui/screens/recipe/` with recipe details display
- [X] T062 [US2] Implement recipe detail UI: name, icon, type, difficulty, time, ingredients list, cooking steps
- [X] T063 [US2] Add "编辑" and "删除" buttons in recipe detail screen
- [X] T064 [US2] Create `AddRecipeScreen.kt` in `app/src/main/java/com/eatwhat/ui/screens/recipe/` for adding/editing recipes
- [X] T065 [US2] Implement recipe form: name input, type selector, icon picker (emoji), difficulty selector
- [X] T066 [US2] Implement estimated time input with number picker
- [X] T067 [US2] Implement dynamic ingredient list with add/remove buttons (name, amount, unit)
- [X] T068 [US2] Implement dynamic cooking steps list with add/remove buttons
- [X] T069 [US2] Implement tag management: display existing tags, add new tags with "+" button
- [X] T070 [US2] Implement "从食材快速添加" feature: suggest tags from ingredient names
- [X] T071 [US2] Add form validation: required fields, valid time range (1-300 minutes)
- [X] T072 [US2] Implement save button with loading state
- [X] T073 [US2] Implement soft delete for recipes (set isDeleted = true)
- [X] T074 [US2] Add navigation from RecipeListScreen to RecipeDetailScreen and AddRecipeScreen
- [X] T075 [US2] Add proper back navigation handling

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently

---

## Phase 5: User Story 3 - 备菜清单 (Priority: P2)

**Goal**: Roll点确认后生成备菜清单,用户可以勾选已准备的食材

**Independent Test**:
1. 完成Roll点并点击"确认"
2. 进入备菜清单页面
3. 查看所有需要准备的食材(合并重复项)
4. 勾选已准备的食材
5. 点击"开始做菜"保存到历史记录

### Implementation for User Story 3

- [X] T076 [US3] Create `GeneratePrepListUseCase.kt` in `app/src/main/java/com/eatwhat/domain/usecase/` to aggregate ingredients
- [X] T077 [US3] Implement ingredient aggregation logic: merge same ingredients, sum amounts, handle different units
- [X] T078 [US3] Create `PrepScreen.kt` in `app/src/main/java/com/eatwhat/ui/screens/prep/` with checklist UI
- [X] T079 [US3] Implement prep list display with checkboxes using ComposeHooks useState
- [X] T080 [US3] Create `IngredientCheckItem.kt` component in `app/src/main/java/com/eatwhat/ui/components/` for checklist items
- [X] T081 [US3] Implement checkbox toggle with visual feedback (strikethrough when checked)
- [X] T082 [US3] Add progress indicator showing checked/total items
- [X] T083 [US3] Implement "开始做菜" button to save to history and navigate to HistoryDetailScreen
- [X] T084 [US3] Pass Roll result data from RollScreen to PrepScreen via navigation arguments
- [X] T085 [US3] Add back button to return to RollScreen

**Checkpoint**: User Story 3 should work independently after completing a Roll

---

## Phase 6: User Story 4 - 历史记录 (Priority: P2)

**Goal**: 用户可以查看历史Roll记录,包括菜谱快照和备菜进度

**Independent Test**:
1. 进入历史记录页面
2. 查看历史记录列表(按时间倒序)
3. 点击某条记录查看详情
4. 查看该次Roll的菜谱快照
5. 查看备菜清单和完成进度
6. 点击菜谱查看详情后返回历史详情页

### Implementation for User Story 4

- [X] T086 [US4] Create `HistoryRepository.kt` in `app/src/main/java/com/eatwhat/data/repository/` with history CRUD operations
- [X] T087 [US4] Create `SaveHistoryUseCase.kt` in `app/src/main/java/com/eatwhat/domain/usecase/` to save Roll result as history
- [X] T088 [US4] Implement history saving logic: create HistoryRecordEntity, save recipe snapshots, save prep items
- [X] T089 [US4] Create `HistoryListScreen.kt` in `app/src/main/java/com/eatwhat/ui/screens/history/` with LazyColumn
- [X] T090 [US4] Implement history list display: timestamp, summary text (e.g., "1荤2素"), recipe count
- [X] T091 [US4] Implement swipe-to-delete for history records (soft delete)
- [X] T092 [US4] Create `HistoryDetailScreen.kt` in `app/src/main/java/com/eatwhat/ui/screens/history/` with history details
- [X] T093 [US4] Implement history detail UI: timestamp, recipe snapshots with cards, prep checklist
- [X] T094 [US4] Display recipe snapshots using RecipeCard component (show snapshot data, not current recipe)
- [X] T095 [US4] Display prep checklist with checkboxes (read-only or editable based on requirements)
- [X] T096 [US4] Implement navigation from recipe card to RecipeDetailScreen (if recipe still exists)
- [X] T097 [US4] Handle case where recipe was deleted: show snapshot data only, disable navigation
- [X] T098 [US4] Add proper back navigation: from HistoryDetailScreen to HistoryListScreen
- [X] T099 [US4] Integrate SaveHistoryUseCase in PrepScreen "开始做菜" button
- [X] T100 [US4] Navigate from PrepScreen to HistoryDetailScreen after saving history

**Checkpoint**: All user stories should now be independently functional

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] T101 [P] Add loading states and error handling across all screens
- [ ] T102 [P] Implement consistent empty states (empty recipe list, empty history, no results)
- [ ] T103 [P] Add confirmation dialogs for destructive actions (delete recipe, delete history)
- [ ] T104 [P] Implement toast/snackbar messages for user feedback (save success, delete success, errors)
- [ ] T105 [P] Add animations and transitions (screen transitions, list item animations)
- [ ] T106 [P] Optimize database queries with proper indexing (verify indexes from schema)
- [ ] T107 [P] Add database query logging for debugging (Room QueryCallback)
- [ ] T108 [P] Implement proper error handling for database operations
- [ ] T109 [P] Add input validation across all forms
- [ ] T110 [P] Implement accessibility features (content descriptions, semantic properties)
- [ ] T111 [P] Test on different screen sizes and orientations
- [ ] T112 [P] Optimize Compose recomposition (use remember, derivedStateOf appropriately)
- [ ] T113 [P] Add unit tests for repositories in `app/src/test/java/com/eatwhat/data/repository/`
- [ ] T114 [P] Add unit tests for use cases in `app/src/test/java/com/eatwhat/domain/usecase/`
- [ ] T115 [P] Add Compose UI tests for critical flows in `app/src/androidTest/java/com/eatwhat/ui/`
- [ ] T116 Code cleanup and refactoring (remove unused code, improve naming)
- [ ] T117 Update quickstart.md with any changes from implementation
- [ ] T118 Run quickstart.md validation (verify setup instructions work)
- [ ] T119 Generate APK and test on physical device
- [ ] T120 Final constitution compliance check (verify Compose-only, ComposeHooks usage, Material 3)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-6)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2)
- **Polish (Phase 7)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1) - Roll点**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P1) - 菜谱管理**: Can start after Foundational (Phase 2) - No dependencies on other stories (but Roll needs recipes to work)
- **User Story 3 (P2) - 备菜清单**: Depends on US1 completion (needs Roll result data)
- **User Story 4 (P2) - 历史记录**: Depends on US1 and US3 completion (needs Roll result and prep list to save)

### Within Each User Story

- Repository before Use Case
- Use Case before Screen
- Components before Screens that use them
- UI implementation before navigation integration

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All entity creation tasks (T009-T016) can run in parallel
- All relation creation tasks (T017-T018) can run in parallel
- All DAO creation tasks (T019-T021) can run in parallel
- All domain model creation tasks (T025-T032) can run in parallel
- All theme files (T033-T035) can run in parallel
- US1 and US2 can be developed in parallel after Foundational phase
- All Polish tasks marked [P] can run in parallel

---

## Implementation Strategy

### MVP First (User Stories 1 & 2 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1 (Roll点)
4. Complete Phase 4: User Story 2 (菜谱管理)
5. **STOP and VALIDATE**: Test US1 and US2 independently
6. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 + User Story 2 → Test independently → Deploy/Demo (MVP!)
3. Add User Story 3 → Test independently → Deploy/Demo
4. Add User Story 4 → Test independently → Deploy/Demo
5. Add Polish → Final release

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1 (Roll点)
   - Developer B: User Story 2 (菜谱管理)
3. After US1 and US2 complete:
   - Developer A: User Story 3 (备菜清单)
   - Developer B: User Story 4 (历史记录)
4. Team completes Polish together

---

## Notes

- [P] tasks = different files, no dependencies, can run in parallel
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- All paths follow the structure defined in plan.md
- Use ComposeHooks (useState, useEffect, useRequest) for all state management
- Follow Material Design 3 guidelines for all UI components
- Ensure all database operations are async (use Kotlin Coroutines)
- Test on Android 7.0+ devices (API 24+)
