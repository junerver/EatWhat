# Research: 深色模式适配

**Branch**: `002-dark-mode-adapt` | **Date**: 2025-12-24

## 研究摘要

本研究分析了 EatWhat 项目现有的主题系统和需要适配深色模式的页面组件。

## 1. 现有主题系统分析

### 1.1 主题文件状态

| 文件 | 状态 | 说明 |
|------|------|------|
| `ui/theme/Color.kt` | ✅ 完整 | 已定义浅色和深色模式颜色 |
| `ui/theme/Theme.kt` | ✅ 完整 | 已支持系统深色模式自动切换 |
| `ui/theme/Type.kt` | ✅ 完整 | 排版系统完整定义 |

**Decision**: 主题系统基础设施已完善，问题在于页面组件未正确使用 `MaterialTheme.colorScheme`

**Rationale**: Color.kt 已包含完整的深色模式颜色定义（DarkPrimary, DarkBackground, DarkSurface, DarkOnSurface等），Theme.kt 使用 `isSystemInDarkTheme()` 自动切换主题

**Alternatives Considered**: 无需考虑，基础设施已就绪

### 1.2 颜色系统映射

**浅色模式颜色（在 Color.kt 中定义）**:
- Background: `0xFFFFFBFE`
- Surface: `0xFFFFFBFE`
- OnSurface: `0xFF1C1B1F`
- Primary: `0xFF6750A4`

**深色模式颜色（在 Color.kt 中定义）**:
- DarkBackground: `0xFF1C1B1F`
- DarkSurface: `0xFF1C1B1F`
- DarkOnSurface: `0xFFE6E1E5`
- DarkPrimary: `0xFFD0BCFF`

**应用自定义色（需保留）**:
- PrimaryOrange: `0xFFFF6B35`
- PrimaryOrangeLight: `0xFFFF8C5A`
- PrimaryOrangeDark: `0xFFE55A2B`
- PageBackground: `0xFFF5F5F5`（问题：硬编码，需改用 colorScheme.background）

## 2. 需要适配的页面分析

### 2.1 RollScreen.kt (Roll 主页面) - P1

**当前问题**:
- 硬编码橙色渐变背景 `PrimaryOrange` → `PrimaryOrangeLight`
- 对话框使用 `Color.White` 硬编码背景
- 文字颜色硬编码 `Color.White`、`Color(0xFF1C1B1F)`

**Decision**: 保留橙色渐变作为品牌色，但对话框和内容区域使用 `MaterialTheme.colorScheme`

**Rationale**: 橙色渐变是应用的视觉标识，在深色模式下同样应该保持。但对话框、卡片等容器应适配深色主题。

### 2.2 RollResultScreen.kt (Roll 结果页面) - P1

**当前问题**:
- `Scaffold.containerColor` 使用 `PageBackground` 硬编码
- 卡片使用 `Color.White` 硬编码
- 文字颜色硬编码 `Color(0xFF1C1B1F)`

**Decision**: 全部改用 `MaterialTheme.colorScheme` 对应色值

**Rationale**: 结果页面需要完全适配深色模式以保证可读性

### 2.3 RecipeListScreen.kt (菜谱列表页面) - P2

**当前问题**:
- TopAppBar 背景使用 `Color.White` 硬编码
- 页面背景使用 `PageBackground` 硬编码
- 空状态提示文字颜色硬编码

**Decision**: 改用 `MaterialTheme.colorScheme.surface`（TopAppBar）和 `.background`（页面）

**Rationale**: 列表页是主要浏览页面，必须提供良好的深色模式体验

### 2.4 HistoryListScreen.kt (历史记录页面) - P3

**当前问题**:
- 页面背景使用 `PageBackground` 硬编码
- TopAppBar 使用 `Color.White` 硬编码
- 斑马纹背景色 `ZebraLight`/`ZebraDark` 都是亮色

**Decision**: 
- 页面/TopAppBar 使用 `MaterialTheme.colorScheme`
- 斑马纹改用 `surface`/`surfaceVariant` 或透明度变化

**Rationale**: 斑马纹在深色模式下需要使用深色系颜色来保持视觉区分

### 2.5 详情页面 (RecipeDetailScreen.kt, HistoryDetailScreen.kt) - P4

**当前问题**:
- 卡片背景硬编码 `Color.White`
- 标题/描述文字颜色硬编码
- 分隔线颜色硬编码

**Decision**: 全部改用主题色系统

**Rationale**: 详情页包含大量文字内容，深色模式下必须保证可读性

### 2.6 PrepScreen.kt (食材准备页) - P4

**当前问题**:
- 与其他页面相同的硬编码颜色问题

**Decision**: 遵循统一的适配方案

## 3. 已适配组件参考

### 3.1 RecipeCard.kt 适配模式

```kotlin
// ✅ 正确的做法
Card(
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    // 不指定 colors，使用默认的 MaterialTheme.colorScheme.surface
) {
    Text(
        style = MaterialTheme.typography.titleMedium
        // 不指定 color，使用默认的 onSurface
    )
    Text(
        color = MaterialTheme.colorScheme.onSurfaceVariant  // 次要文字
    )
}
```

**Key Takeaway**: 
1. 不指定容器颜色，让组件使用默认主题色
2. 文字使用 `onSurface`（主要）或 `onSurfaceVariant`（次要）

## 4. 适配策略总结

### 4.1 颜色替换规则

| 硬编码颜色 | 替换为 |
|-----------|--------|
| `Color.White` (背景) | `MaterialTheme.colorScheme.surface` |
| `PageBackground` | `MaterialTheme.colorScheme.background` |
| `Color(0xFF1C1B1F)` (文字) | `MaterialTheme.colorScheme.onSurface` |
| `CardBackground` | `MaterialTheme.colorScheme.surfaceContainerLow` |
| `ZebraLight`/`ZebraDark` | `surface`/`surfaceVariant` 或使用透明度 |

### 4.2 保留的颜色

以下颜色作为品牌色保留，不做深色模式变体：
- `PrimaryOrange` 系列（橙色主题色）
- `SoftGreen`、`SoftBlue` 等分类色

### 4.3 需要新增的深色模式颜色

**在 Color.kt 中新增**:
```kotlin
// 斑马纹深色模式颜色
val ZebraLightDark = Color(0xFF2C2C2C)  // 深色模式下的亮条
val ZebraDarkDark = Color(0xFF242424)   // 深色模式下的暗条
```

## 5. Constitution 合规性检查

| 原则 | 状态 | 说明 |
|------|------|------|
| Compose First | ✅ | 不涉及 View 系统 |
| State Management | ✅ | 不涉及状态管理变更 |
| Material Design 3 | ✅ | 使用 MD3 colorScheme |
| User-Centric Simplicity | ✅ | 提升用户体验 |
| Code Quality | ✅ | 统一使用主题系统，提高可维护性 |

## 6. 风险评估

| 风险 | 影响 | 缓解措施 |
|------|------|---------|
| 浅色模式布局变化 | 中 | 严格使用 colorScheme，不改变布局代码 |
| 对比度不足 | 低 | 使用 MD3 标准色，已通过 WCAG 验证 |
| 遗漏硬编码颜色 | 低 | 全局搜索 `Color(` 和 `Color.` 确保完整覆盖 |

## 7. 文件修改清单

**需要修改的文件（按优先级）**:

1. **P1 - Roll 页面**:
   - `ui/screens/roll/RollScreen.kt`
   - `ui/screens/roll/RollResultScreen.kt`

2. **P2 - 菜谱列表**:
   - `ui/screens/recipe/RecipeListScreen.kt`

3. **P3 - 历史记录**:
   - `ui/screens/history/HistoryListScreen.kt`

4. **P4 - 详情页面**:
   - `ui/screens/recipe/RecipeDetailScreen.kt`
   - `ui/screens/history/HistoryDetailScreen.kt`
   - `ui/screens/prep/PrepScreen.kt`

5. **主题支持（可选）**:
   - `ui/theme/Color.kt` - 添加斑马纹深色颜色
