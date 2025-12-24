# Tasks: 深色模式适配

**Input**: Design documents from `/specs/002-dark-mode-adapt/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md

**Tests**: 手动测试，无需自动化测试任务

**Organization**: 任务按用户故事分组，每个故事可独立实现和测试

## Format: `[ID] [P?] [Story] Description`

- **[P]**: 可并行执行（不同文件，无依赖）
- **[Story]**: 所属用户故事 (US1, US2, US3, US4)
- 描述中包含具体文件路径

## Path Conventions

- **Android 项目**: `app/src/main/java/com/eatwhat/ui/`
- **Screens**: `screens/[feature]/[Screen].kt`
- **Theme**: `theme/Color.kt`

---

## Phase 1: User Story 1 - Roll页面深色模式 (Priority: P1) 🎯 MVP

**Goal**: 适配主页Roll点功能页面的深色模式，包括Roll主页面和结果页面

**Independent Test**: 切换系统深色模式后，打开应用进入Roll页面，验证对话框、按钮、文字等UI元素正确显示深色主题样式

### Implementation for User Story 1

- [X] T001 [P] [US1] 适配 RollScreen.kt 对话框背景色: `Color.White` → `MaterialTheme.colorScheme.surface` in `app/src/main/java/com/eatwhat/ui/screens/roll/RollScreen.kt`
- [X] T002 [P] [US1] 适配 RollScreen.kt 对话框文字颜色: 硬编码 → `MaterialTheme.colorScheme.onSurface` in `app/src/main/java/com/eatwhat/ui/screens/roll/RollScreen.kt`
- [X] T003 [P] [US1] 适配 RollResultScreen.kt 页面背景: `PageBackground` → `MaterialTheme.colorScheme.background` in `app/src/main/java/com/eatwhat/ui/screens/roll/RollResultScreen.kt`
- [X] T004 [P] [US1] 适配 RollResultScreen.kt 卡片颜色: `Color.White` → 移除硬编码，使用默认主题色 in `app/src/main/java/com/eatwhat/ui/screens/roll/RollResultScreen.kt`
- [X] T005 [P] [US1] 适配 RollResultScreen.kt 文字颜色: `Color(0xFF1C1B1F)` → `MaterialTheme.colorScheme.onSurface` in `app/src/main/java/com/eatwhat/ui/screens/roll/RollResultScreen.kt`

**Checkpoint**: Roll页面在深色模式下正确显示，保留橙色品牌渐变背景

---

## Phase 2: User Story 2 - 菜谱列表页面深色模式 (Priority: P2)

**Goal**: 适配"我的菜谱"列表页面的容器组件深色模式（列表子项已适配）

**Independent Test**: 深色模式下进入我的菜谱列表页面，验证页面容器背景、TopAppBar、空状态提示正确显示

### Implementation for User Story 2

- [X] T006 [US2] 适配 RecipeListScreen.kt TopAppBar背景: `Color.White` → `MaterialTheme.colorScheme.surface` in `app/src/main/java/com/eatwhat/ui/screens/recipe/RecipeListScreen.kt`
- [X] T007 [US2] 适配 RecipeListScreen.kt 页面背景: `PageBackground` → `MaterialTheme.colorScheme.background` in `app/src/main/java/com/eatwhat/ui/screens/recipe/RecipeListScreen.kt`
- [X] T008 [US2] 适配 RecipeListScreen.kt 空状态文字: 硬编码颜色 → `MaterialTheme.colorScheme.onSurfaceVariant` in `app/src/main/java/com/eatwhat/ui/screens/recipe/RecipeListScreen.kt`

**Checkpoint**: 菜谱列表页面容器与已适配的列表子项颜色协调一致

---

## Phase 3: User Story 3 - 历史记录页面深色模式 (Priority: P3)

**Goal**: 适配历史记录列表页面深色模式，包括斑马纹背景色

**Independent Test**: 深色模式下进入历史记录页面，验证页面背景、列表项斑马纹、时间戳等正确显示

### Implementation for User Story 3

- [X] T009 [US3] 适配 HistoryListScreen.kt TopAppBar背景: `Color.White` → `MaterialTheme.colorScheme.surface` in `app/src/main/java/com/eatwhat/ui/screens/history/HistoryListScreen.kt`
- [X] T010 [US3] 适配 HistoryListScreen.kt 页面背景: `PageBackground` → `MaterialTheme.colorScheme.background` in `app/src/main/java/com/eatwhat/ui/screens/history/HistoryListScreen.kt`
- [X] T011 [US3] 适配 HistoryListScreen.kt 斑马纹颜色: `ZebraLight`/`ZebraDark` → 使用 `surface`/`surfaceVariant` 或添加深色模式条件判断 in `app/src/main/java/com/eatwhat/ui/screens/history/HistoryListScreen.kt`
- [X] T012 [US3] 适配 HistoryListScreen.kt 文字颜色: 硬编码 → `MaterialTheme.colorScheme.onSurface` / `onSurfaceVariant` in `app/src/main/java/com/eatwhat/ui/screens/history/HistoryListScreen.kt`

**Checkpoint**: 历史记录页面在深色模式下斑马纹清晰可辨，文字可读

---

## Phase 4: User Story 4 - 详情页面深色模式 (Priority: P4)

**Goal**: 适配菜谱详情、历史详情、食材准备等详情页面的深色模式

**Independent Test**: 深色模式下打开各详情页面，验证标题、内容、卡片等元素正确显示

### Implementation for User Story 4

- [X] T013 [P] [US4] 适配 RecipeDetailScreen.kt 页面背景和卡片: 硬编码 → `MaterialTheme.colorScheme` in `app/src/main/java/com/eatwhat/ui/screens/recipe/RecipeDetailScreen.kt`
- [X] T014 [P] [US4] 适配 RecipeDetailScreen.kt 所有文字颜色: 硬编码 → `onSurface` / `onSurfaceVariant` in `app/src/main/java/com/eatwhat/ui/screens/recipe/RecipeDetailScreen.kt`
- [X] T015 [P] [US4] 适配 HistoryDetailScreen.kt 页面背景和卡片: 硬编码 → `MaterialTheme.colorScheme` in `app/src/main/java/com/eatwhat/ui/screens/history/HistoryDetailScreen.kt`
- [X] T016 [P] [US4] 适配 HistoryDetailScreen.kt 所有文字颜色: 硬编码 → `onSurface` / `onSurfaceVariant` in `app/src/main/java/com/eatwhat/ui/screens/history/HistoryDetailScreen.kt`
- [X] T017 [P] [US4] 适配 PrepScreen.kt 页面背景和卡片: 硬编码 → `MaterialTheme.colorScheme` in `app/src/main/java/com/eatwhat/ui/screens/prep/PrepScreen.kt`
- [X] T018 [P] [US4] 适配 PrepScreen.kt 所有文字颜色: 硬编码 → `onSurface` / `onSurfaceVariant` in `app/src/main/java/com/eatwhat/ui/screens/prep/PrepScreen.kt`

**Checkpoint**: 所有详情页面在深色模式下内容清晰可读

---

## Phase 5: Polish & Validation

**Purpose**: 验证整体效果，确保浅色模式未被破坏

- [X] T019 验证浅色模式: 逐一检查所有修改的页面，确认布局和颜色与修改前一致
- [X] T020 验证深色模式: 逐一检查所有页面，确认背景、文字、卡片颜色正确
- [X] T021 验证主题切换: 在应用内实时切换系统主题，确认页面即时响应无闪烁
- [X] T022 对比度检查: 确认深色模式下文字与背景对比度达到 WCAG 2.1 AA 标准 (4.5:1)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (US1)**: 无依赖，可立即开始 - MVP
- **Phase 2 (US2)**: 无依赖，可与 Phase 1 并行
- **Phase 3 (US3)**: 无依赖，可与其他 Phase 并行
- **Phase 4 (US4)**: 无依赖，可与其他 Phase 并行
- **Phase 5 (Polish)**: 依赖所有用户故事完成

### Within Each User Story

- 同一文件内的任务顺序执行
- 不同文件的任务可并行（标记 [P]）

### Parallel Opportunities

**Phase 1 并行示例**:
```
T001 + T002 (RollScreen.kt 内部) - 顺序
T003 + T004 + T005 (RollResultScreen.kt 内部) - 顺序
RollScreen.kt 和 RollResultScreen.kt - 可并行
```

**Phase 4 并行示例**:
```
T013-T014 (RecipeDetailScreen.kt) - 同一文件顺序
T015-T016 (HistoryDetailScreen.kt) - 同一文件顺序
T017-T018 (PrepScreen.kt) - 同一文件顺序
三个文件之间 - 可并行
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. 完成 Phase 1: Roll页面适配
2. **验证**: 测试深色模式下 Roll 页面功能
3. 可以先部署/展示 MVP

### Incremental Delivery

1. Phase 1 → Roll页面可用 (MVP!)
2. Phase 2 → 菜谱列表可用
3. Phase 3 → 历史记录可用
4. Phase 4 → 所有详情页可用
5. Phase 5 → 完整验证

### 单人顺序执行

按优先级顺序：P1 → P2 → P3 → P4 → Polish

---

## Checklist Summary

| Phase | User Story | 任务数 | 可并行 |
|-------|-----------|--------|--------|
| 1 | US1 - Roll页面 | 5 | 2组文件并行 |
| 2 | US2 - 菜谱列表 | 3 | 单文件 |
| 3 | US3 - 历史记录 | 4 | 单文件 |
| 4 | US4 - 详情页面 | 6 | 3文件并行 |
| 5 | Polish | 4 | 部分并行 |
| **Total** | | **22** | |

---

## Notes

- 所有颜色替换遵循 `quickstart.md` 中的颜色映射规则
- 参考 `RecipeCard.kt` 作为正确的实现示例
- 保留品牌色（PrimaryOrange 系列）不变
- 优先使用组件默认主题色，避免显式指定
- 每个任务完成后立即在深色模式下测试验证
