# Quickstart: 深色模式适配

## 概述

本功能将修复 EatWhat 应用中多个页面在深色模式下显示不正确的问题。

## 核心变更

将硬编码的颜色值替换为 `MaterialTheme.colorScheme` 中的动态颜色。

## 快速参考

### 颜色替换规则

```kotlin
// ❌ 错误 - 硬编码颜色
Surface(color = Color.White)
Text(color = Color(0xFF1C1B1F))
Scaffold(containerColor = PageBackground)

// ✅ 正确 - 使用主题颜色
Surface(color = MaterialTheme.colorScheme.surface)
Text(color = MaterialTheme.colorScheme.onSurface)
Scaffold(containerColor = MaterialTheme.colorScheme.background)
```

### 常用颜色映射

| 用途 | 使用的颜色 |
|------|-----------|
| 页面背景 | `MaterialTheme.colorScheme.background` |
| 卡片/容器背景 | `MaterialTheme.colorScheme.surface` |
| 主要文字 | `MaterialTheme.colorScheme.onSurface` |
| 次要文字 | `MaterialTheme.colorScheme.onSurfaceVariant` |
| 分隔线 | `MaterialTheme.colorScheme.outlineVariant` |

## 修改文件清单

按优先级排序：

1. **P1**: `RollScreen.kt`, `RollResultScreen.kt`
2. **P2**: `RecipeListScreen.kt`
3. **P3**: `HistoryListScreen.kt`
4. **P4**: `RecipeDetailScreen.kt`, `HistoryDetailScreen.kt`, `PrepScreen.kt`

## 参考实现

查看 `RecipeCard.kt` 了解正确的主题颜色使用方式。

## 测试方法

1. 在 Android 设置中切换到深色模式
2. 打开应用，检查每个页面的显示效果
3. 确认文字清晰可读，背景颜色正确

## 注意事项

- **不要修改布局代码**，只替换颜色值
- **保留品牌色**（PrimaryOrange 系列），这些在深色模式下也使用相同颜色
- **优先使用默认值**，如果组件有合适的默认主题色，不需要显式指定
