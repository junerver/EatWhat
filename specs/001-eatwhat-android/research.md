# Research & Technology Decisions

**Feature**: 吃点啥 Android 应用
**Date**: 2025-12-10
**Status**: Complete

## Overview

本文档记录了项目技术栈选型、架构决策和最佳实践研究结果。所有决策均基于项目宪法要求和功能规格说明。

## Core Technology Stack

### 1. UI Framework: Jetpack Compose

**Decision**: 使用 Jetpack Compose 作为唯一 UI 框架

**Rationale**:
- 项目宪法明确要求纯 Compose 实现
- Compose 是 Android 现代 UI 开发的官方推荐方案
- 声明式 UI 范式提高开发效率和代码可维护性
- Material 3 组件完整支持
- 与 Kotlin 深度集成，类型安全

**Implementation Details**:
- 使用 Compose BOM (Bill of Materials) 管理版本
- 最低版本: Compose 1.5.0+
- 依赖项:
  ```kotlin
  implementation(platform("androidx.compose:compose-bom:2024.01.00"))
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.material3:material3")
  implementation("androidx.compose.ui:ui-tooling-preview")
  debugImplementation("androidx.compose.ui:ui-tooling")
  ```

**Alternatives Considered**:
- XML + View System: 被宪法明确禁止
- Jetpack Compose + XML 混合: 违反宪法"Compose First"原则

---

### 2. State Management: ComposeHooks

**Decision**: 使用 ComposeHooks 库进行状态管理

**Rationale**:
- 项目宪法要求使用 ComposeHooks
- 提供 React-like hooks API，降低学习曲线
- 统一状态管理模式，避免碎片化
- 支持复杂状态逻辑（useState, useEffect, useRequest 等）
- 提高代码可测试性和可复用性

**Implementation Details**:
- 库地址: https://github.com/junerver/ComposeHooks
- 依赖添加:
  ```kotlin
  implementation("xyz.junerver.compose:hooks:latest.version")
  ```
- 主要使用的 Hooks:
  - `useState`: 本地状态管理
  - `useEffect`: 副作用处理
  - `useRequest`: 异步数据请求
  - `useMemo`: 计算结果缓存
  - `useCallback`: 函数引用稳定

**Best Practices**:
- 状态提升: 将共享状态提升到最近的共同父组件
- 单一数据源: 每个状态只有一个来源
- 不可变数据: 使用 data class 和 copy() 更新状态
- 避免过度使用: 简单本地状态可直接使用 remember

**Alternatives Considered**:
- ViewModel + StateFlow: 传统方案，但 ComposeHooks 更符合 Compose 范式
- 直接使用 mutableStateOf: 缺乏统一模式，难以管理复杂状态

---

### 3. Database: Room

**Decision**: 使用 Room 作为本地数据库解决方案

**Rationale**:
- Jetpack 官方推荐的 SQLite 抽象层
- 编译时 SQL 验证，减少运行时错误
- 类型安全的数据库访问
- 支持 Flow/LiveData 响应式查询
- 与 Kotlin Coroutines 深度集成
- 支持数据库迁移和版本管理

**Implementation Details**:
- 依赖项:
  ```kotlin
  implementation("androidx.room:room-runtime:2.6.1")
  implementation("androidx.room:room-ktx:2.6.1")
  ksp("androidx.room:room-compiler:2.6.1")
  ```
- 数据库版本: 从 1 开始
- 导出 schema: 启用以支持迁移测试
  ```kotlin
  room {
      schemaDirectory("$projectDir/schemas")
  }
  ```

**Schema Design Principles**:
- 使用外键约束维护数据完整性
- 为常用查询字段添加索引
- 使用 @Embedded 和 @Relation 处理关联数据
- 设计支持未来扩展（如云同步）的字段:
  - `syncId`: UUID 用于云端标识
  - `lastModified`: 时间戳用于同步冲突解决
  - `isDeleted`: 软删除标记

**Alternatives Considered**:
- SQLite 直接使用: 缺乏类型安全和编译时验证
- Realm: 学习曲线陡峭，与 Jetpack 生态集成不如 Room
- DataStore: 仅适合简单键值对，不适合复杂关系数据

---

### 4. Navigation: Navigation Compose

**Decision**: 使用 Navigation Compose 进行页面导航

**Rationale**:
- Jetpack Navigation 的 Compose 版本
- 类型安全的导航参数传递
- 支持深度链接和返回栈管理
- 与 Compose 生命周期集成
- 支持底部导航栏等常见导航模式

**Implementation Details**:
- 依赖项:
  ```kotlin
  implementation("androidx.navigation:navigation-compose:2.7.6")
  ```
- 导航结构:
  - 底部导航: Roll点、菜谱、历史（3个主入口）
  - 详情页面: 菜谱详情、历史详情、添加菜谱、备菜清单
- 导航参数传递: 使用类型安全的参数（如 recipeId: Long）

**Best Practices**:
- 定义 sealed class 或 enum 管理所有路由
- 使用 NavBackStackEntry 传递复杂对象
- 正确处理返回栈（如从历史进入菜谱详情后返回历史）

**Alternatives Considered**:
- 手动管理导航: 容易出错，难以维护
- 第三方导航库: 不如官方方案成熟和稳定

---

### 5. Dependency Injection: Manual DI

**Decision**: 使用手动依赖注入，不引入 DI 框架

**Rationale**:
- 项目规模较小（单模块，~10个 Repository/UseCase）
- 手动 DI 更简单、更透明、更易调试
- 避免引入额外依赖和学习成本
- 符合宪法"User-Centric Simplicity"原则

**Implementation Details**:
- 在 Application 类中初始化单例:
  ```kotlin
  class EatWhatApplication : Application() {
      lateinit var database: EatWhatDatabase
      lateinit var recipeRepository: RecipeRepository
      // ...
  }
  ```
- 通过 LocalContext 在 Composable 中访问:
  ```kotlin
  val app = LocalContext.current.applicationContext as EatWhatApplication
  val repository = app.recipeRepository
  ```

**When to Reconsider**:
- 如果项目扩展到多模块架构
- 如果依赖关系变得复杂（>20个依赖）
- 如果需要更细粒度的作用域管理

**Alternatives Considered**:
- Hilt: 功能强大但对小项目过度设计
- Koin: 轻量但仍增加额外复杂度

---

## Architecture Patterns

### 1. Repository Pattern

**Decision**: 使用 Repository 模式作为数据访问层

**Rationale**:
- 分离数据源和业务逻辑
- 提供统一的数据访问接口
- 便于测试（可 mock Repository）
- 支持未来扩展（如添加网络数据源）

**Implementation**:
- Repository 封装 DAO 和数据转换逻辑
- 返回 Flow<T> 支持响应式数据流
- 处理数据库异常并转换为领域异常

---

### 2. Use Case Pattern

**Decision**: 对复杂业务逻辑使用 Use Case 模式

**Rationale**:
- 封装单一业务操作（如 RollRecipesUseCase）
- 提高代码可测试性和可复用性
- 清晰的业务逻辑边界

**When to Use**:
- 涉及多个 Repository 的操作
- 复杂的业务规则（如 Roll 算法）
- 需要在多处复用的逻辑

**When NOT to Use**:
- 简单的 CRUD 操作（直接调用 Repository）
- 仅涉及单个 Repository 的简单查询

---

### 3. MVVM with ComposeHooks

**Decision**: 使用 MVVM 架构，但用 ComposeHooks 替代传统 ViewModel

**Rationale**:
- ComposeHooks 提供类似 ViewModel 的状态管理能力
- 更符合 Compose 的声明式范式
- 减少样板代码
- 状态与 UI 更紧密结合

**Implementation**:
- 在 Composable 中使用 hooks 管理状态
- 复杂状态逻辑提取为自定义 hooks
- 使用 useRequest 处理异步操作

---

## Data Model Design

### Extensibility for Future Features

**Cloud Sync Support**:
- 每个实体添加 `syncId: String` (UUID)
- 添加 `lastModified: Long` (timestamp)
- 使用软删除 `isDeleted: Boolean`

**Data Export/Import**:
- 设计 JSON 序列化友好的数据结构
- 使用 kotlinx.serialization 支持序列化
- 预留版本字段用于格式兼容

**Performance Optimization**:
- 为常用查询字段添加索引
- 使用 @Relation 避免 N+1 查询
- 考虑分页加载大量数据

---

## Testing Strategy

### Unit Tests
- Repository 层: 使用 Room in-memory database
- Use Case 层: Mock Repository
- 工具: JUnit 5, MockK, Truth

### UI Tests
- Compose UI Testing: 测试用户交互和状态变化
- 工具: Compose Test, Espresso (minimal)

### Integration Tests
- 端到端流程测试（如完整的 Roll 流程）
- 使用真实 Room 数据库

---

## Performance Considerations

### App Launch Optimization
- 延迟初始化非关键组件
- 使用 App Startup library 管理初始化顺序
- 避免主线程阻塞操作

### UI Performance
- 使用 LazyColumn/LazyRow 处理长列表
- 避免不必要的 recomposition（使用 remember, derivedStateOf）
- 使用 Modifier.drawWithCache 优化绘制

### Database Performance
- 使用索引加速查询
- 批量操作使用事务
- 异步执行所有数据库操作（Coroutines）

---

## Security & Privacy

### Data Protection
- 所有数据本地存储，无网络传输
- 使用 Android Keystore 存储敏感配置（未来）
- 遵循 Android 数据保护最佳实践

### Permissions
- 无需特殊权限（纯本地应用）
- 未来如需导出数据，使用 Storage Access Framework

---

## Build Configuration

### Gradle Setup
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
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}
```

### ProGuard/R8
- 启用代码混淆和优化
- 保留 Room 和 Compose 相关规则

---

## Development Workflow

### Code Style
- 遵循 Kotlin 官方编码规范
- 使用 ktlint 自动格式化
- 配置 Android Studio 代码风格

### Version Control
- 功能分支开发
- PR review 必须检查宪法合规性
- Commit message 遵循 Conventional Commits

### CI/CD (Future)
- 自动运行测试
- 自动构建 APK
- 代码质量检查（ktlint, detekt）

---

## Conclusion

所有技术决策均符合项目宪法要求，优先考虑简单性、可维护性和用户体验。架构设计支持未来扩展（云同步、数据导入导出），同时保持当前实现的简洁性。
