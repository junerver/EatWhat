# Tasks: 设置页面与数据同步导出

**Input**: Design documents from `/specs/003-settings-sync-export/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Tests**: 未明确要求，本任务列表不包含测试任务。

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Android 项目**: `app/src/main/java/com/eatwhat/`
- 遵循现有项目结构

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: 添加新依赖和基础设施

- [x] T001 添加 dav4jvm、kotlinx-serialization、security-crypto 依赖到 gradle/libs.versions.toml
- [x] T002 添加 kotlin-serialization 插件到 app/build.gradle.kts
- [x] T003 [P] 添加 JitPack 仓库到 settings.gradle.kts
- [x] T004 [P] 创建 sync 目录结构 app/src/main/java/com/eatwhat/data/sync/
- [x] T005 [P] 创建 settings 屏幕目录 app/src/main/java/com/eatwhat/ui/screens/settings/

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: 核心数据模型和工具类，所有用户故事都依赖

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T006 创建导出数据模型 ExportData.kt 在 app/src/main/java/com/eatwhat/data/sync/ExportData.kt
- [x] T007 [P] 创建 CryptoManager.kt AES 加解密工具 在 app/src/main/java/com/eatwhat/data/sync/CryptoManager.kt
- [x] T008 [P] 创建 FileHelper.kt 文件操作工具 在 app/src/main/java/com/eatwhat/data/sync/FileHelper.kt
- [x] T009 添加 Settings、WebDAVConfig、Sync 路由到 app/src/main/java/com/eatwhat/navigation/Destinations.kt
- [x] T010 更新 NavGraph.kt 添加新路由 在 app/src/main/java/com/eatwhat/navigation/NavGraph.kt

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - 进入设置页面 (Priority: P1) 🎯 MVP

**Goal**: 用户能通过历史页面进入设置页面

**Independent Test**: 点击历史页面顶部栏设置图标，导航到设置页面

### Implementation for User Story 1

- [x] T011 [US1] 修改 HistoryListScreen.kt 添加设置按钮到 TopAppBar 在 app/src/main/java/com/eatwhat/ui/screens/history/HistoryListScreen.kt
- [x] T012 [US1] 创建 SettingsScreen.kt 设置主页面框架 在 app/src/main/java/com/eatwhat/ui/screens/settings/SettingsScreen.kt
- [x] T013 [US1] 实现设置页面 UI（导出/导入/WebDAV 选项卡片） 在 app/src/main/java/com/eatwhat/ui/screens/settings/SettingsScreen.kt

**Checkpoint**: 设置入口和页面框架完成，可独立测试导航功能

---

## Phase 4: User Story 2 - 本地文件导出 (Priority: P1)

**Goal**: 用户能将数据导出为 JSON 文件

**Independent Test**: 在设置页面选择导出，生成 JSON 文件到设备存储

### Implementation for User Story 2

- [x] T014 [P] [US2] 创建 ExportRepository.kt 导出逻辑接口 在 app/src/main/java/com/eatwhat/data/repository/ExportRepository.kt
- [x] T015 [US2] 实现 ExportRepositoryImpl.kt 从 Room 查询并组装导出数据 在 app/src/main/java/com/eatwhat/data/repository/ExportRepositoryImpl.kt
- [x] T016 [US2] 创建 ExportDataUseCase.kt 导出用例 在 app/src/main/java/com/eatwhat/domain/usecase/ExportDataUseCase.kt
- [x] T017 [US2] 在 SettingsScreen.kt 添加导出功能 UI 和 SAF 文件选择器集成
- [x] T018 [US2] 实现导出选项对话框（菜谱/历史/全部）和进度指示

**Checkpoint**: 导出功能完成，可独立测试导出到文件

---

## Phase 5: User Story 3 - 本地文件导入 (Priority: P1)

**Goal**: 用户能从 JSON 文件导入数据

**Independent Test**: 选择导出文件，预览并确认导入，数据成功恢复

### Implementation for User Story 3

- [x] T019 [US3] 在 ExportRepository.kt 添加 importData 和 previewImport 方法
- [x] T020 [US3] 实现 ImportDataUseCase.kt 导入用例 在 app/src/main/java/com/eatwhat/domain/usecase/ImportDataUseCase.kt
- [x] T021 [US3] 在 SettingsScreen.kt 添加导入功能 UI 和 SAF 文件选择器集成
- [x] T022 [US3] 实现导入预览对话框（显示将导入的数据条数）
- [x] T023 [US3] 实现导入冲突处理（基于 syncId 判断新增或更新）

**Checkpoint**: 导入功能完成，可独立测试完整的备份恢复流程

---

## Phase 6: User Story 4 - WebDAV服务配置 (Priority: P2)

**Goal**: 用户能配置 WebDAV 服务器信息

**Independent Test**: 填写 WebDAV 配置，测试连接成功

### Implementation for User Story 4

- [x] T024 [P] [US4] 创建 WebDAVClient.kt WebDAV 操作封装 在 app/src/main/java/com/eatwhat/data/sync/WebDAVClient.kt
- [x] T025 [P] [US4] 创建 SyncRepository.kt 同步仓库接口 在 app/src/main/java/com/eatwhat/data/repository/SyncRepository.kt
- [x] T026 [US4] 实现 SyncRepositoryImpl.kt 使用 EncryptedSharedPreferences 保存配置 在 app/src/main/java/com/eatwhat/data/repository/SyncRepositoryImpl.kt
- [x] T027 [US4] 创建 WebDAVConfigScreen.kt 配置页面 在 app/src/main/java/com/eatwhat/ui/screens/settings/WebDAVConfigScreen.kt
- [x] T028 [US4] 实现配置表单（URL/用户名/密码输入）和测试连接按钮
- [x] T029 [US4] 实现密码脱敏显示和配置持久化

**Checkpoint**: WebDAV 配置完成，可独立测试连接

---

## Phase 7: User Story 5 - WebDAV数据同步 (Priority: P2)

**Goal**: 用户能上传数据到云端或从云端恢复

**Independent Test**: 配置 WebDAV 后，上传成功并能从云端恢复

### Implementation for User Story 5

- [x] T030 [US5] 在 SyncRepository.kt 添加 uploadToCloud 和 downloadFromCloud 方法
- [x] T031 [US5] 创建 SyncDataUseCase.kt 同步用例 在 app/src/main/java/com/eatwhat/domain/usecase/SyncDataUseCase.kt
- [x] T032 [US5] 创建 SyncScreen.kt 同步操作页面 在 app/src/main/java/com/eatwhat/ui/screens/settings/SyncScreen.kt
- [x] T033 [US5] 实现上传到云端功能和进度指示
- [x] T034 [US5] 实现从云端恢复功能和恢复预览
- [x] T035 [US5] 实现同步时间记录和状态显示

**Checkpoint**: 云同步功能完成，可独立测试上传和恢复

---

## Phase 8: User Story 6 - 数据加密设置 (Priority: P2)

**Goal**: 用户能设置加密密码保护云端数据

**Independent Test**: 设置加密密码，上传后数据确实被加密

### Implementation for User Story 6

- [x] T036 [US6] 在 WebDAVConfigScreen.kt 添加加密开关和密码设置 UI
- [x] T037 [US6] 在 SyncRepositoryImpl.kt 集成 CryptoManager 实现加密上传
- [x] T038 [US6] 实现解密下载和密码错误处理
- [x] T039 [US6] 添加加密状态指示和密码遗忘警告提示

**Checkpoint**: 加密功能完成，完整的云同步+加密流程可用

---

## Phase 9: Polish & Cross-Cutting Concerns

**Purpose**: 完善和优化

- [x] T040 [P] 添加边界情况处理（无数据导出提示、文件损坏提示）
- [x] T041 [P] 统一错误处理和用户提示样式
- [x] T042 优化大数据量导入导出性能
- [x] T043 [P] 添加 strings.xml 中的本地化字符串
- [x] T044 深色模式适配检查（所有新增页面）

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-8)**: All depend on Foundational phase completion
- **Polish (Phase 9)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational - 设置入口
- **User Story 2 (P1)**: Can start after Foundational - 导出功能
- **User Story 3 (P1)**: Can start after US2 (需要相同的数据模型) - 导入功能
- **User Story 4 (P2)**: Can start after Foundational - WebDAV 配置
- **User Story 5 (P2)**: Depends on US4 (需要 WebDAV 配置) - 云同步
- **User Story 6 (P2)**: Depends on US5 (加密应用于同步) - 加密

### Parallel Opportunities

- T003, T004, T005 可并行执行
- T007, T008 可并行执行（不同文件）
- T014, T024, T025 可并行执行（不同模块）
- US1, US2, US4 可并行开始（Foundational 完成后）

---

## Parallel Example: Setup Phase

```bash
# Launch these tasks together:
Task: "添加 JitPack 仓库到 settings.gradle.kts"
Task: "创建 sync 目录结构"
Task: "创建 settings 屏幕目录"
```

## Parallel Example: Foundational Phase

```bash
# Launch these tasks together:
Task: "创建 CryptoManager.kt AES 加解密工具"
Task: "创建 FileHelper.kt 文件操作工具"
```

---

## Implementation Strategy

### MVP First (User Stories 1-3)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational
3. Complete Phase 3: US1 设置入口
4. Complete Phase 4: US2 导出功能
5. Complete Phase 5: US3 导入功能
6. **STOP and VALIDATE**: 完整的本地备份恢复功能可用
7. Deploy/demo if ready

### Incremental Delivery

1. Setup + Foundational → 基础就绪
2. Add US1 → 设置入口可用 (MVP-1)
3. Add US2 + US3 → 本地导入导出可用 (MVP-2)
4. Add US4 → WebDAV 配置可用
5. Add US5 + US6 → 完整云同步+加密可用

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- 遵循项目 Constitution：Compose First、ComposeHooks、Material 3
- 所有新页面需适配深色模式
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently

---

## Summary

| 统计项 | 数量 |
|--------|------|
| 总任务数 | 44 |
| Phase 1 Setup | 5 |
| Phase 2 Foundational | 5 |
| User Story 1 | 3 |
| User Story 2 | 5 |
| User Story 3 | 5 |
| User Story 4 | 6 |
| User Story 5 | 6 |
| User Story 6 | 4 |
| Polish | 5 |
| 可并行任务 | 14 |

**MVP Scope**: User Stories 1-3（设置入口 + 本地导入导出）
