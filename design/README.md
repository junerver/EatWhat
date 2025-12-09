# 吃点啥 - 家庭烹饪助手设计文档

## 产品定位

**家庭烹饪决策助手** - 帮助家庭用户快速决定今天做什么菜，基于个人菜谱库进行智能搭配。

## 核心场景

每天面对"今天做什么菜"的选择困难：
- 家里有很多会做的菜，但每次都想不起来
- 想要荤素搭配合理，但懒得思考
- 希望有点随机性和惊喜感

## 核心功能

### 1. Roll点（主页）
**核心交互** - 配置 + 一键随机

**配置选项**：
- **几个菜**：2菜、3菜、4菜、5菜
- **荤素搭配**：1荤2素、2荤1素、全荤、全素
- **汤品**：不要汤、1个汤

**Roll点逻辑**：
- 根据配置从菜谱库中随机抽取
- 确保荤素比例符合要求
- 避免重复（可配置）

### 2. 菜谱管理（核心功能）
**菜谱是整个 App 的基础**

**菜谱列表**：
- 分类筛选：全部、荤菜、素菜、汤、主食
- 显示：菜名、类型、难度
- 点击查看详情

**菜谱详情**：
- 基本信息：菜名、类型、难度、时间
- 食材清单：名称 + 用量
- 制作步骤：分步骤说明

**添加菜谱**：
- 菜名
- 类型：荤菜、素菜、汤、主食
- 难度：简单、中等、困难
- 食材列表
- 制作步骤

### 3. Roll点结果
**展示今天的菜单**：
- 汇总信息：3菜1汤 · 1荤2素
- 菜品卡片：显示每道菜的基本信息
- 操作：
  - "重新Roll"：不满意，重新随机
  - "就这些了"：确认并保存到历史

### 4. 历史记录（待开发）
- 记录每次 Roll 的结果
- 查看过往菜单搭配
- 可以快速复用

## 信息架构

```
底部导航（3个Tab）
├── Roll点（主页）
│   ├── 配置区域
│   │   ├── 几个菜
│   │   ├── 荤素搭配
│   │   └── 汤品
│   └── Roll按钮 → 结果页
│       ├── 重新Roll
│       └── 确认保存
├── 菜谱
│   ├── 菜谱列表
│   │   ├── 分类筛选
│   │   ├── 添加按钮 → 添加菜谱页
│   │   └── 菜谱项 → 菜谱详情页
│   ├── 菜谱详情
│   │   ├── 基本信息
│   │   ├── 食材清单
│   │   └── 制作步骤
│   └── 添加菜谱
│       └── 表单填写
└── 历史（待开发）
```

## 设计原则

### Material Design 3 应用

1. **颜色系统**
   - Primary: #6750A4（紫色）
   - Surface: #FFFFFF
   - Background: #F5F5F5

2. **圆角设计**
   - 卡片：16px
   - 按钮：12px
   - 标签：6px

3. **交互反馈**
   - 按钮点击：缩放效果
   - 列表项：背景色变化
   - 页面切换：平滑过渡

### 极简主义

1. **信息层级清晰**
   - Roll点页：突出核心功能
   - 菜谱页：清晰的列表和分类

2. **减少认知负担**
   - 配置选项简单明了
   - 一键完成核心任务

3. **流畅的用户体验**
   - 底部导航快速切换
   - 最少点击完成任务

## 数据模型

### Recipe（菜谱）
```kotlin
data class Recipe(
    val id: String,
    val name: String,              // 菜名
    val type: RecipeType,          // 类型：荤菜、素菜、汤、主食
    val difficulty: Difficulty,    // 难度：简单、中等、困难
    val time: Int,                 // 制作时间（分钟）
    val ingredients: List<Ingredient>, // 食材列表
    val steps: List<String>,       // 制作步骤
    val icon: String,              // 图标emoji
    val createdAt: Long,           // 创建时间
    val updatedAt: Long            // 更新时间
)

enum class RecipeType { MEAT, VEGETABLE, SOUP, STAPLE }
enum class Difficulty { EASY, MEDIUM, HARD }

data class Ingredient(
    val name: String,              // 食材名称
    val amount: String             // 用量
)
```

### MealConfig（配置）
```kotlin
data class MealConfig(
    val dishCount: Int,            // 几个菜：2-5
    val meatCount: Int,            // 几个荤菜：0-dishCount
    val soupCount: Int             // 几个汤：0-1
)
```

### MealResult（结果）
```kotlin
data class MealResult(
    val id: String,
    val config: MealConfig,        // 配置
    val recipes: List<Recipe>,     // 选中的菜谱
    val createdAt: Long            // 创建时间
)
```

## Compose 实现要点

### 页面结构
```kotlin
@Composable
fun MainScreen() {
    Scaffold(
        bottomBar = { BottomNavigation() }
    ) { paddingValues ->
        NavHost(...) {
            composable("roll") { RollScreen() }
            composable("recipes") { RecipesScreen() }
            composable("history") { HistoryScreen() }
        }
    }
}
```

### 状态管理（ComposeHooks）
```kotlin
@Composable
fun RollScreen() {
    val (config, setConfig) = useState(MealConfig(3, 1, 1))
    val (result, setResult) = useState<MealResult?>(null)
    val recipes = useRecipes() // 从数据库加载菜谱

    fun rollMeal() {
        val selected = selectRecipes(recipes, config)
        setResult(MealResult(config, selected))
    }
}
```

### 关键组件
- `RollConfigCard` - 配置卡片
- `RollButton` - Roll按钮
- `RecipeCard` - 菜谱卡片
- `RecipeList` - 菜谱列表
- `RecipeDetailScreen` - 菜谱详情
- `AddRecipeForm` - 添加菜谱表单

## 技术栈

- **UI**: Jetpack Compose + Material 3
- **状态管理**: ComposeHooks
- **数据存储**: Room Database
- **导航**: Compose Navigation
- **依赖注入**: Hilt（可选）

## 下一步

1. ✅ 完成高保真原型
2. 设计数据库 Schema
3. 实现菜谱 CRUD 功能
4. 实现 Roll 点算法
5. 实现历史记录功能
6. 添加偏好设置（过滤重复、饮食偏好等）
