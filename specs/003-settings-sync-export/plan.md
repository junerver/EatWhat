# Implementation Plan: 设置页面与数据同步导出

**Branch**: `003-settings-sync-export` | **Date**: 2025-12-25 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/003-settings-sync-export/spec.md`

## Summary

实现设置页面入口及数据导入导出功能。通过历史页面顶部栏增加设置入口，支持本地 JSON 文件导入导出和 WebDAV 云同步，使用 AES-GCM 加密保护云端数据。

## Technical Context

**Language/Version**: Kotlin 1.9.21
**Primary Dependencies**: Jetpack Compose, ComposeHooks 3.0.0, Room 2.6.1, dav4jvm 2.2.1, kotlinx.serialization 1.6.2
**Storage**: Room SQLite + EncryptedSharedPreferences (WebDAV 配置)
**Testing**: JUnit, Compose Testing
**Target Platform**: Android 7.0+ (API 24+)
**Project Type**: Mobile (Android)
**Performance Goals**: 导出 1000 条数据 < 5s，导入 < 10s
**Constraints**: 离线可用（本地导入导出），网络依赖（WebDAV 同步）
**Scale/Scope**: 单用户应用，数据量通常 < 1000 条

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| 原则 | 状态 | 说明 |
|------|------|------|
| I. Compose First | ✅ | 所有 UI 使用 Jetpack Compose |
| II. State Management Excellence | ✅ | 使用 ComposeHooks 管理状态 |
| III. Material Design Consistency | ✅ | Material 3 组件 |
| IV. User-Centric Simplicity | ✅ | 最少步骤完成导入导出和同步 |
| V. Code Quality & Maintainability | ✅ | 分层架构，可测试 |

**Gate Status**: ✅ PASSED

## Project Structure

### Documentation (this feature)

```text
specs/003-settings-sync-export/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output (internal contracts)
└── tasks.md             # Phase 2 output (/speckit.tasks)
```

### Source Code (repository root)

```text
app/src/main/java/com/eatwhat/
├── navigation/
│   └── Destinations.kt          # 新增 Settings, WebDAVConfig 路由
├── data/
│   ├── database/
│   │   └── entities/            # 现有实体（无新增）
│   ├── repository/
│   │   ├── ExportRepository.kt  # 新增：导入导出逻辑
│   │   └── SyncRepository.kt    # 新增：WebDAV 同步逻辑
│   └── sync/
│       ├── ExportData.kt        # 导出数据模型
│       ├── WebDAVClient.kt      # WebDAV 操作封装
│       └── CryptoManager.kt     # AES 加解密
├── domain/
│   └── usecase/
│       ├── ExportDataUseCase.kt # 导出用例
│       ├── ImportDataUseCase.kt # 导入用例
│       └── SyncDataUseCase.kt   # 同步用例
└── ui/
    └── screens/
        ├── history/
        │   └── HistoryListScreen.kt  # 修改：添加设置按钮
        └── settings/                  # 新增目录
            ├── SettingsScreen.kt      # 设置主页面
            ├── WebDAVConfigScreen.kt  # WebDAV 配置页面
            └── SyncScreen.kt          # 同步操作页面
```

**Structure Decision**: 遵循现有项目结构，新增 `settings` 屏幕目录和 `sync` 数据目录。

## Complexity Tracking

无需额外复杂性。设计符合所有 Constitution 原则。
