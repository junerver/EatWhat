package com.eatwhat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eatwhat.domain.model.CookingStep
import com.eatwhat.domain.model.Difficulty
import com.eatwhat.domain.model.Ingredient
import com.eatwhat.domain.model.Recipe
import com.eatwhat.domain.model.RecipeType
import com.eatwhat.domain.model.RollConfig
import com.eatwhat.domain.model.Unit as IngredientUnit
import com.eatwhat.domain.usecase.GeneratePrepListUseCase
import com.eatwhat.domain.usecase.PrepListItem
import com.eatwhat.ui.screens.cooking.CookingContent
import com.eatwhat.ui.screens.prep.PrepContent
import com.eatwhat.ui.screens.roll.RollPlannerContent
import com.eatwhat.ui.screens.roll.RollResultContent
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.palette.components.alert.AlertType
import xyz.junerver.compose.palette.components.alert.PAlert
import xyz.junerver.compose.palette.components.button.ButtonSize
import xyz.junerver.compose.palette.components.button.ButtonType
import xyz.junerver.compose.palette.components.button.PButton
import xyz.junerver.compose.palette.components.card.CardVariant
import xyz.junerver.compose.palette.components.card.PCard
import xyz.junerver.compose.palette.components.progress.PProgress
import xyz.junerver.compose.palette.components.segmented.PSegmented
import xyz.junerver.compose.palette.components.segmented.SegmentedOption
import xyz.junerver.compose.palette.components.statistic.PStatistic
import xyz.junerver.compose.palette.components.statistic.TrendType
import xyz.junerver.compose.palette.components.steps.PSteps
import xyz.junerver.compose.palette.components.steps.StepItem
import xyz.junerver.compose.palette.components.tag.PEditableTagGroup
import xyz.junerver.compose.palette.components.tag.PTag
import xyz.junerver.compose.palette.components.tag.TagDefaults
import xyz.junerver.compose.palette.components.tag.TagSize
import xyz.junerver.compose.palette.components.text.PText
import xyz.junerver.compose.palette.core.theme.PaletteTheme

private const val VIEW_DASHBOARD = "dashboard"
private const val VIEW_ROLL_PLANNER = "roll_planner"
private const val VIEW_ROLL_RESULT = "roll_result"
private const val VIEW_PREP = "prep"
private const val VIEW_COOKING = "cooking"

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EatWhatDesktopApp() {
  PaletteTheme {
    MaterialTheme {
      val (mode, setMode) = useState("balanced")
      val (pantryTags, setPantryTags) = useState(listOf("鸡蛋", "番茄", "青菜", "米饭"))
      val (activeView, setActiveView) = useState(VIEW_DASHBOARD)
      val (lastRollSummary, setLastRollSummary) = useState("尚未 Roll")
      val (lastConfig, setLastConfig) = useState(RollConfig(meatCount = 1, vegCount = 1, soupCount = 1))
      val (selectedRecipes, setSelectedRecipes) = useState(sampleRecipes().take(3))
      val (prepItems, setPrepItems) = useState(emptyList<PrepListItem>())
      val progress = when (mode) {
        "quick" -> 75f
        "balanced" -> 55f
        else -> 35f
      }

      when (activeView) {
        VIEW_ROLL_PLANNER -> {
          RollPlannerContent(
            onRoll = { config ->
              val recipes = selectSampleRecipes(config)
              setLastConfig(config)
              setSelectedRecipes(recipes)
              setLastRollSummary(formatRollConfig(config))
              setActiveView(VIEW_ROLL_RESULT)
            }
          )
        }

        VIEW_ROLL_RESULT -> {
          RollResultContent(
            recipes = selectedRecipes,
            config = lastConfig,
            onRecipeClick = {},
            onReRoll = {
              val recipes = selectSampleRecipes(lastConfig)
              setSelectedRecipes(recipes)
            },
            onReRollSingle = { recipe ->
              setSelectedRecipes(
                selectedRecipes.map { selected ->
                  if (selected.id == recipe.id) {
                    replacementRecipeFor(recipe, selectedRecipes)
                  } else {
                    selected
                  }
                }
              )
            },
            onConfirm = {
              setPrepItems(GeneratePrepListUseCase()(selectedRecipes))
              setActiveView(VIEW_PREP)
            },
            onBack = { setActiveView(VIEW_ROLL_PLANNER) }
          )
        }

        VIEW_PREP -> {
          PrepContent(
            prepList = prepItems,
            isStartDisabled = prepItems.isEmpty(),
            onNavigateUp = { setActiveView(VIEW_ROLL_RESULT) },
            onCheckedChange = { index, _, checked ->
              setPrepItems(
                prepItems.mapIndexed { itemIndex, item ->
                  if (itemIndex == index) item.copy(isChecked = checked) else item
                }
              )
            },
            onStartCooking = { setActiveView(VIEW_COOKING) }
          )
        }

        VIEW_COOKING -> {
          CookingContent(
            recipes = selectedRecipes,
            onNavigateUp = { setActiveView(VIEW_PREP) },
            onFinish = { setActiveView(VIEW_DASHBOARD) }
          )
        }

        else -> {
          DesktopDashboard(
            mode = mode,
            pantryTags = pantryTags,
            lastRollSummary = lastRollSummary,
            progress = progress,
            onModeChange = setMode,
            onPantryTagsChange = setPantryTags,
            onStartRoll = { setActiveView(VIEW_ROLL_PLANNER) },
            onClear = {
              setLastRollSummary("尚未 Roll")
              setSelectedRecipes(sampleRecipes().take(3))
              setPrepItems(emptyList())
            }
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DesktopDashboard(
  mode: String,
  pantryTags: List<String>,
  lastRollSummary: String,
  progress: Float,
  onModeChange: (String) -> Unit,
  onPantryTagsChange: (List<String>) -> Unit,
  onStartRoll: () -> Unit,
  onClear: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(32.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
      PAlert(
        message = "EatWhat 工作台",
        description = "冰箱、菜谱与备菜流程已同步，今天可以直接从这里开始安排。",
        type = AlertType.Success
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.Top
      ) {
        PCard(
          modifier = Modifier.weight(1.3f),
          variant = CardVariant.Elevated
        ) {
          PText(
            text = "今天吃什么",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(8.dp))
          PText(
            text = "用同一套 common UI 在 Android 与 Desktop 展示菜谱、备菜、筛选和进度。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(20.dp))

          PSegmented(
            options = listOf(
              SegmentedOption("quick", "快手"),
              SegmentedOption("balanced", "均衡"),
              SegmentedOption("fresh", "清爽")
            ),
            value = mode,
            onValueChange = onModeChange
          )

          Spacer(modifier = Modifier.height(20.dp))
          PProgress(
            percent = progress,
            modifier = Modifier.fillMaxWidth(),
            formatter = { "${it.toInt()}%" }
          )

          Spacer(modifier = Modifier.height(20.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PButton(
              text = "随机一餐",
              size = ButtonSize.MEDIUM,
              type = ButtonType.PRIMARY,
              onClick = onStartRoll
            )
            PButton(
              text = "清空",
              size = ButtonSize.MEDIUM,
              type = ButtonType.PLAIN,
              onClick = onClear
            )
          }
        }

        PCard(
          modifier = Modifier.weight(1f),
          variant = CardVariant.Outlined
        ) {
          PText(
            text = "样例指标",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(8.dp))
          PText(
            text = lastRollSummary,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(16.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            PStatistic(
              title = "候选菜谱",
              value = "18",
              suffix = "道",
              trend = TrendType.Up
            )
            PStatistic(
              title = "预计耗时",
              value = if (mode == "quick") "15" else "35",
              suffix = "分钟"
            )
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.Top
      ) {
        PCard(
          modifier = Modifier.weight(1f),
          variant = CardVariant.Filled
        ) {
          PText(
            text = "冰箱标签",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(12.dp))
          PEditableTagGroup(
            tags = pantryTags,
            onTagsChange = onPantryTagsChange,
            placeholder = "添加食材",
            size = TagSize.Medium,
            tagColors = { TagDefaults.pastelColors(it) }
          )
          Spacer(modifier = Modifier.height(16.dp))
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            PTag("荤菜", size = TagSize.Small, colors = TagDefaults.errorColors())
            PTag("素菜", size = TagSize.Small, colors = TagDefaults.successColors())
            PTag("汤", size = TagSize.Small, colors = TagDefaults.infoColors())
            PTag("主食", size = TagSize.Small, colors = TagDefaults.warningColors())
          }
        }

        PCard(
          modifier = Modifier.weight(1f),
          variant = CardVariant.Elevated
        ) {
          PText(
            text = "备菜流程",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(12.dp))
          PSteps(
            currentStep = when (mode) {
              "quick" -> 2
              "balanced" -> 1
              else -> 0
            },
            items = listOf(
              StepItem("选择口味", "根据模式过滤菜谱"),
              StepItem("汇总食材", "合并相同单位的食材"),
              StepItem("开始做菜", "进入步骤式烹饪指导")
            )
          )
        }
      }

      PCard(variant = CardVariant.Outlined) {
        PText(
          text = "今日模块",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          SampleSwatch("餐单", Color(0xFFFF6B35))
          SampleSwatch("食材", Color(0xFF4CAF50))
          SampleSwatch("进度", Color(0xFF2196F3))
          SampleSwatch("筛选", Color(0xFF9C27B0))
          SampleSwatch("流程", Color(0xFFFFC107))
        }
      }
    }
  }
}

private fun formatRollConfig(config: RollConfig): String {
  val parts = buildList {
    if (config.meatCount > 0) add("${config.meatCount}荤")
    if (config.vegCount > 0) add("${config.vegCount}素")
    if (config.soupCount > 0) add("${config.soupCount}汤")
    if (config.stapleCount > 0) add("${config.stapleCount}主食")
    if (config.randomCount > 0) add("${config.randomCount}随机")
  }
  return if (parts.isEmpty()) {
    "随机一餐"
  } else {
    "最近一次：${parts.joinToString(" + ")}"
  }
}

private fun selectSampleRecipes(config: RollConfig): List<Recipe> {
  val normalizedConfig = if (config.isValid()) {
    config.withAutoBalance()
  } else {
    RollConfig(meatCount = 1, vegCount = 1, soupCount = 1)
  }
  val recipes = sampleRecipes()
  val selected = mutableListOf<Recipe>()

  fun addByType(type: RecipeType, count: Int) {
    selected += recipes
      .filter { it.type == type }
      .filterNot { recipe -> selected.any { it.id == recipe.id } }
      .take(count)
  }

  addByType(RecipeType.MEAT, normalizedConfig.meatCount)
  addByType(RecipeType.VEG, normalizedConfig.vegCount)
  addByType(RecipeType.SOUP, normalizedConfig.soupCount)
  addByType(RecipeType.STAPLE, normalizedConfig.stapleCount)

  selected += recipes
    .filterNot { recipe -> selected.any { it.id == recipe.id } }
    .take(normalizedConfig.randomCount)

  return selected.ifEmpty { recipes.take(3) }
}

private fun replacementRecipeFor(recipe: Recipe, selectedRecipes: List<Recipe>): Recipe {
  return sampleRecipes()
    .firstOrNull { candidate ->
      candidate.type == recipe.type &&
        candidate.id != recipe.id &&
        selectedRecipes.none { it.id == candidate.id }
    }
    ?: recipe
}

private fun sampleRecipes(): List<Recipe> {
  return listOf(
    Recipe(
      id = 1,
      syncId = "desktop-recipe-1",
      name = "番茄炒蛋",
      type = RecipeType.VEG,
      icon = "🍅",
      difficulty = Difficulty.EASY,
      estimatedTime = 12,
      ingredients = listOf(
        Ingredient(name = "番茄", amount = "2", unit = IngredientUnit.PIECE),
        Ingredient(name = "鸡蛋", amount = "3", unit = IngredientUnit.PIECE),
        Ingredient(name = "葱花", amount = "1", unit = IngredientUnit.SPOON)
      ),
      steps = listOf(
        CookingStep(stepNumber = 1, description = "番茄切块，鸡蛋打散并加少量盐。"),
        CookingStep(stepNumber = 2, description = "热锅下油，先把鸡蛋炒至凝固后盛出。"),
        CookingStep(stepNumber = 3, description = "番茄炒出汁后倒回鸡蛋，翻匀调味。")
      )
    ),
    Recipe(
      id = 2,
      syncId = "desktop-recipe-2",
      name = "青椒牛肉",
      type = RecipeType.MEAT,
      icon = "🥩",
      difficulty = Difficulty.MEDIUM,
      estimatedTime = 25,
      ingredients = listOf(
        Ingredient(name = "牛肉", amount = "250", unit = IngredientUnit.G),
        Ingredient(name = "青椒", amount = "2", unit = IngredientUnit.PIECE),
        Ingredient(name = "生抽", amount = "1", unit = IngredientUnit.SPOON)
      ),
      steps = listOf(
        CookingStep(stepNumber = 1, description = "牛肉切片，用生抽和少量淀粉抓匀。"),
        CookingStep(stepNumber = 2, description = "青椒切块，热锅快炒至断生。"),
        CookingStep(stepNumber = 3, description = "牛肉大火滑炒，回锅青椒后快速调味出锅。")
      )
    ),
    Recipe(
      id = 3,
      syncId = "desktop-recipe-3",
      name = "紫菜蛋花汤",
      type = RecipeType.SOUP,
      icon = "🥣",
      difficulty = Difficulty.EASY,
      estimatedTime = 10,
      ingredients = listOf(
        Ingredient(name = "紫菜", amount = "8", unit = IngredientUnit.G),
        Ingredient(name = "鸡蛋", amount = "1", unit = IngredientUnit.PIECE),
        Ingredient(name = "香油", amount = "1", unit = IngredientUnit.SPOON)
      ),
      steps = listOf(
        CookingStep(stepNumber = 1, description = "锅中加水煮开，放入紫菜。"),
        CookingStep(stepNumber = 2, description = "淋入蛋液形成蛋花，加入盐和香油。")
      )
    ),
    Recipe(
      id = 4,
      syncId = "desktop-recipe-4",
      name = "葱油拌面",
      type = RecipeType.STAPLE,
      icon = "🍜",
      difficulty = Difficulty.EASY,
      estimatedTime = 18,
      ingredients = listOf(
        Ingredient(name = "面条", amount = "200", unit = IngredientUnit.G),
        Ingredient(name = "小葱", amount = "4", unit = IngredientUnit.PIECE),
        Ingredient(name = "酱油", amount = "2", unit = IngredientUnit.SPOON)
      ),
      steps = listOf(
        CookingStep(stepNumber = 1, description = "小葱切段，小火炸出葱香。"),
        CookingStep(stepNumber = 2, description = "加入酱油和少量糖熬成葱油汁。"),
        CookingStep(stepNumber = 3, description = "面条煮熟沥干，与葱油汁拌匀。")
      )
    ),
    Recipe(
      id = 5,
      syncId = "desktop-recipe-5",
      name = "蒜蓉西兰花",
      type = RecipeType.VEG,
      icon = "🥦",
      difficulty = Difficulty.EASY,
      estimatedTime = 15,
      ingredients = listOf(
        Ingredient(name = "西兰花", amount = "300", unit = IngredientUnit.G),
        Ingredient(name = "蒜", amount = "4", unit = IngredientUnit.PIECE)
      ),
      steps = listOf(
        CookingStep(stepNumber = 1, description = "西兰花掰小朵，焯水后沥干。"),
        CookingStep(stepNumber = 2, description = "蒜末爆香，倒入西兰花快炒调味。")
      )
    ),
    Recipe(
      id = 6,
      syncId = "desktop-recipe-6",
      name = "香煎鸡腿排",
      type = RecipeType.MEAT,
      icon = "🍗",
      difficulty = Difficulty.MEDIUM,
      estimatedTime = 30,
      ingredients = listOf(
        Ingredient(name = "鸡腿肉", amount = "300", unit = IngredientUnit.G),
        Ingredient(name = "黑胡椒", amount = "1", unit = IngredientUnit.SPOON),
        Ingredient(name = "盐", amount = "1", unit = IngredientUnit.MODERATE)
      ),
      steps = listOf(
        CookingStep(stepNumber = 1, description = "鸡腿肉用盐和黑胡椒腌制。"),
        CookingStep(stepNumber = 2, description = "皮面朝下小火煎至金黄，再翻面煎熟。"),
        CookingStep(stepNumber = 3, description = "静置后切块，保留肉汁。")
      )
    )
  )
}

@Composable
private fun SampleSwatch(
  label: String,
  color: Color
) {
  Row(
    modifier = Modifier.padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Box(
      modifier = Modifier
        .width(18.dp)
        .height(18.dp)
        .background(color)
    )
    PText(
      text = label,
      fontSize = 13.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}
