# Implementation Plan: 深色模式适配

**Branch**: `002-dark-mode-adapt` | **Date**: 2025-12-24 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/002-dark-mode-adapt/spec.md`

## Summary

适配应用的深色模式，修复多个页面组件未正确使用 Material Theme 颜色系统的问题。主要工作是将硬编码的颜色值替换为 `MaterialTheme.colorScheme` 中的对应颜色，确保所有页面在深色模式下正确显示，同时不破坏现有浅色模式的布局和效果。

## Technical Context

**Language/Version**: Kotlin 1.9.21
**Primary Dependencies**: Jetpack Compose (Material 3), ComposeHooks
**Storage**: N/A（不涉及数据存储变更）
**Testing**: 手动测试深色/浅色模式切换
**Target Platform**: Android (minSdk 24, targetSdk 34)
**Project Type**: Mobile Android 应用
**Performance Goals**: 主题切换响应时间 < 500ms，无视觉闪烁
**Constraints**: 不能破坏现有浅色模式的布局和视觉效果
**Scale/Scope**: 7个页面文件 + 1个主题文件（可选）

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| 原则 | Pre-Design | Post-Design | 说明 |
|------|------------|-------------|------|
| I. Compose First | ✅ PASS | ✅ PASS | 仅修改 Compose 代码，不涉及 View 系统 |
| II. State Management | ✅ PASS | ✅ PASS | 不涉及状态管理变更，主题由系统自动管理 |
| III. Material Design 3 | ✅ PASS | ✅ PASS | 使用 MD3 colorScheme，符合设计规范 |
| IV. User-Centric Simplicity | ✅ PASS | ✅ PASS | 提升深色环境下的用户体验 |
| V. Code Quality | ✅ PASS | ✅ PASS | 统一使用主题系统，提高代码可维护性 |

**Gate Status**: ✅ ALL PASSED

## Project Structure

### Documentation (this feature)

```text
specs/002-dark-mode-adapt/
├── spec.md              # 功能规范
├── plan.md              # 本文件 - 实现计划
├── research.md          # 研究报告
├── quickstart.md        # 快速开始指南
├── checklists/
│   └── requirements.md  # 需求检查清单
└── tasks.md             # 任务列表（由 /speckit.tasks 生成）
```

### Source Code (需要修改的文件)

```text
app/src/main/java/com/eatwhat/ui/
├── theme/
│   └── Color.kt                          # 可选：添加深色模式斑马纹颜色
├── screens/
│   ├── roll/
│   │   ├── RollScreen.kt                 # P1 - Roll 主页面
│   │   └── RollResultScreen.kt           # P1 - Roll 结果页面
│   ├── recipe/
│   │   ├── RecipeListScreen.kt           # P2 - 菜谱列表页
│   │   └── RecipeDetailScreen.kt         # P4 - 菜谱详情页
│   ├── history/
│   │   ├── HistoryListScreen.kt          # P3 - 历史记录列表
│   │   └── HistoryDetailScreen.kt        # P4 - 历史记录详情
│   └── prep/
│       └── PrepScreen.kt                 # P4 - 食材准备页
└── components/
    └── RecipeCard.kt                     # ✅ 已适配 - 参考实现
```

**Structure Decision**: 仅修改现有文件，不新增文件。修改集中在 screens 目录下的页面文件。

## Implementation Phases

### Phase 1: Roll 页面适配 (P1)

**文件**: `RollScreen.kt`, `RollResultScreen.kt`

**修改内容**:

1. **RollScreen.kt**:
   - 对话框 `Surface.color`: `Color.White` → `MaterialTheme.colorScheme.surface`
   - 对话框文字颜色: 硬编码 → `MaterialTheme.colorScheme.onSurface`
   - 保留橙色渐变背景（品牌色）

2. **RollResultScreen.kt**:
   - `Scaffold.containerColor`: `PageBackground` → `MaterialTheme.colorScheme.background`
   - 卡片颜色: `Color.White` → 移除（使用默认）
   - 文字颜色: `Color(0xFF1C1B1F)` → `MaterialTheme.colorScheme.onSurface`

### Phase 2: 菜谱列表页适配 (P2)

**文件**: `RecipeListScreen.kt`

**修改内容**:
- TopAppBar 背景: `Color.White` → `MaterialTheme.colorScheme.surface`
- 页面背景: `PageBackground` → `MaterialTheme.colorScheme.background`
- 空状态文字: 硬编码 → `MaterialTheme.colorScheme.onSurfaceVariant`

### Phase 3: 历史记录页适配 (P3)

**文件**: `HistoryListScreen.kt`

**修改内容**:
- TopAppBar 背景: `Color.White` → `MaterialTheme.colorScheme.surface`
- 页面背景: `PageBackground` → `MaterialTheme.colorScheme.background`
- 斑马纹颜色: 使用 `surface`/`surfaceVariant` 或条件判断 `isSystemInDarkTheme()`

### Phase 4: 详情页面适配 (P4)

**文件**: `RecipeDetailScreen.kt`, `HistoryDetailScreen.kt`, `PrepScreen.kt`

**修改内容**:
- 所有卡片背景: `Color.White` → 移除或使用默认
- 所有文字颜色: 硬编码 → `MaterialTheme.colorScheme.onSurface`
- 分隔线颜色: 硬编码 → `MaterialTheme.colorScheme.outlineVariant`

### Phase 5: 主题文件更新（可选）

**文件**: `Color.kt`

**修改内容**:
- 添加深色模式斑马纹颜色定义（如果需要）

## 颜色替换速查表

| 原硬编码颜色 | 替换为 | 用途 |
|-------------|--------|------|
| `Color.White` (背景) | `MaterialTheme.colorScheme.surface` | 卡片、TopAppBar 背景 |
| `PageBackground` | `MaterialTheme.colorScheme.background` | 页面背景 |
| `Color(0xFF1C1B1F)` | `MaterialTheme.colorScheme.onSurface` | 主要文字 |
| `Color(0xFF666666)` | `MaterialTheme.colorScheme.onSurfaceVariant` | 次要文字 |
| `CardBackground` | `MaterialTheme.colorScheme.surfaceContainerLow` | 卡片背景变体 |
| `ZebraLight`/`ZebraDark` | `surface`/`surfaceVariant` | 斑马纹 |

## 测试验证

### 验证步骤

1. **浅色模式验证**:
   - 确认所有页面布局未发生变化
   - 确认颜色视觉效果与修改前一致

2. **深色模式验证**:
   - 系统设置切换到深色模式
   - 逐一检查每个页面的背景、文字、卡片颜色
   - 确认对比度足够，文字清晰可读

3. **切换测试**:
   - 在应用内实时切换系统主题
   - 确认页面即时响应，无闪烁

### 验收标准

- [ ] 所有7个页面在深色模式下正确显示
- [ ] 浅色模式布局和效果未被破坏
- [ ] 主题切换响应时间 < 500ms
- [ ] 文字与背景对比度达到 WCAG 2.1 AA 标准（4.5:1）

## Complexity Tracking

> 无 Constitution 违规，无需记录

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| N/A | N/A | N/A |

## References

- [研究报告](./research.md) - 详细的代码分析和问题清单
- [已适配组件参考](../../app/src/main/java/com/eatwhat/ui/components/RecipeCard.kt) - RecipeCard 实现
- [Material 3 颜色系统](https://m3.material.io/styles/color/the-color-system)
