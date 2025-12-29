package com.eatwhat.ui.screens.roll

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.eatwhat.EatWhatApplication
import com.eatwhat.domain.model.Recipe
import com.eatwhat.domain.model.RecipeType
import com.eatwhat.domain.model.RollConfig
import com.eatwhat.domain.model.RollResult
import com.eatwhat.domain.usecase.InsufficientRecipesException
import com.eatwhat.domain.usecase.RollRecipesUseCase
import com.eatwhat.navigation.Destinations
import com.eatwhat.ui.components.IconSize
import com.eatwhat.ui.components.RecipeIcon
import com.eatwhat.ui.components.SimpleCircularProgressIndicator
import com.eatwhat.ui.theme.MeatRed
import com.eatwhat.ui.theme.PrimaryOrange
import com.eatwhat.ui.theme.SoftBlue
import com.eatwhat.ui.theme.SoftGreen
import com.eatwhat.ui.theme.WarmYellow
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useGetState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RollResultScreen(
    navController: NavController,
    meatCount: Int,
    vegCount: Int,
    soupCount: Int,
    stapleCount: Int
) {
    val context = LocalContext.current
    val app = context.applicationContext as EatWhatApplication
    val useCase = remember { RollRecipesUseCase(app.rollRepository) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

  var rollResult by remember { mutableStateOf<RollResult?>(null) }
  val (isLoading, setIsLoading) = useGetState(default = true)
  var error by remember { mutableStateOf<String?>(null) }

    val config = remember {
        RollConfig(
            meatCount = meatCount,
            vegCount = vegCount,
            soupCount = soupCount,
            stapleCount = stapleCount
        )
    }

    // Store current roll result in Application for navigation
    LaunchedEffect(rollResult) {
        app.currentRollResult = rollResult
    }

    fun executeRoll() {
        scope.launch {
            setIsLoading(true)
          error = null
            try {
              Log.d("RollResultScreen", "开始执行 Roll，配置: $config")
                val result = useCase(config)
                result.fold(
                  onSuccess = {
                    Log.d("RollResultScreen", "Roll 成功，获得 ${it.recipes.size} 个菜谱")
                    rollResult = it
                  },
                    onFailure = { e ->
                      Log.e("RollResultScreen", "Roll 失败", e)
                      Log.e("RollResultScreen", "错误类型: ${e.javaClass.name}")
                      Log.e("RollResultScreen", "错误消息: ${e.message}")
                      Log.e("RollResultScreen", "堆栈跟踪: ${e.stackTraceToString()}")
                        when (e) {
                          is InsufficientRecipesException -> {
                            Log.e("RollResultScreen", "菜谱不足错误: ${e.errors}")
                            error = e.errors.joinToString("\n")
                          }
                          else -> error = "Roll失败: ${e.message}"
                        }
                    }
                )
            } catch (e: Exception) {
              Log.e("RollResultScreen", "executeRoll 捕获异常", e)
              error = "执行失败: ${e.message}"
            } finally {
                setIsLoading(false)
            }
        }
    }

    LaunchedEffect(Unit) {
        executeRoll()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(paddingValues)
        ) {
            when {
              isLoading.value -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "🎲",
                            fontSize = 64.sp
                        )
                        Text(
                            text = "正在为你挑选...",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                        SimpleCircularProgressIndicator(
                            color = PrimaryOrange,
                            strokeWidth = 4.dp
                        )
                    }
                }
                error != null -> {
                  val errorMessage = error ?: ""
                    Column(
                        modifier = Modifier
                          .align(Alignment.Center)
                          .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "😅",
                            fontSize = 64.sp
                        )
                        Text(
                          text = errorMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MeatRed
                        )
                        Button(
                            onClick = { navController.popBackStack() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryOrange
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("返回添加菜谱")
                        }
                    }
                }
                rollResult != null -> {
                  val currentRollResult = rollResult ?: return@Scaffold
                    RollResultContent(
                      recipes = currentRollResult.recipes,
                        config = config,
                        onRecipeClick = { recipe ->
                            navController.navigate("recipe/${recipe.id}")
                        },
                        onReRoll = { executeRoll() },
                        onReRollSingle = { recipe ->
                            // 重新Roll单个菜品
                            scope.launch {
                                try {
                                    // 获取该类型的所有菜品（最多10个）
                                    val allRecipes = app.recipeRepository.getRandomRecipesByType(
                                        recipe.type,
                                        10
                                    )

                                    // 排除当前菜品和已选中的其他菜品
                                    val currentIds = rollResult?.recipes?.map { it.id }?.toSet() ?: emptySet()
                                    val availableRecipes = allRecipes.filter { it.id !in currentIds }

                                    if (availableRecipes.isNotEmpty()) {
                                        // 随机选择一个新菜品
                                        val newRecipe = availableRecipes.random()
                                      val currentResult = rollResult
                                      if (currentResult != null) {
                                            val updatedList = currentResult.recipes.toMutableList()
                                            val index = updatedList.indexOfFirst { it.id == recipe.id }
                                            if (index != -1) {
                                                updatedList[index] = newRecipe
                                              rollResult = currentResult.copy(recipes = updatedList)
                                            }
                                        }
                                    } else {
                                        snackbarHostState.showSnackbar("该类型没有更多菜品了")
                                    }
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("重新Roll失败: ${e.message}")
                                }
                            }
                        },
                        onConfirm = {
                            // 跳转到食材准备页面
                            navController.navigate(Destinations.Prep.route)
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RollResultContent(
    recipes: List<Recipe>,
    config: RollConfig,
    onRecipeClick: (Recipe) -> Unit,
    onReRoll: () -> Unit,
    onReRollSingle: (Recipe) -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部返回栏
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "今日菜单",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // 内容区域
        LazyColumn(
            modifier = Modifier
              .weight(1f)
              .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 标题和摘要
            item {
                Card(
                    modifier = Modifier
                      .fillMaxWidth()
                      .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = Color.Black.copy(alpha = 0.1f)
                      ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                          .fillMaxWidth()
                          .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎉",
                            fontSize = 40.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "今天就做这些！",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = buildSummary(config),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 菜品卡片列表
            items(recipes, key = { it.id }) { recipe ->
                DishCard(
                    recipe = recipe,
                    onClick = { onRecipeClick(recipe) },
                    onReRoll = { onReRollSingle(recipe) }
                )
            }
        }

        // 底部操作按钮
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onReRoll,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryOrange
                    ),
                    border = androidx.compose.foundation.BorderStroke(2.dp, PrimaryOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "🎲 重新Roll",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryOrange
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "✓ 就这些了",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DishCard(
    recipe: Recipe,
    onClick: () -> Unit,
    onReRoll: () -> Unit
) {
    Card(
        modifier = Modifier
          .fillMaxWidth()
          .shadow(
            elevation = 4.dp,
            shape = RoundedCornerShape(16.dp),
            spotColor = Color.Black.copy(alpha = 0.1f)
          ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧图标 - 使用菜谱的图片或emoji
            Box(modifier = Modifier.clickable(onClick = onClick)) {
                RecipeIcon(
                    emoji = recipe.icon,
                    imageBase64 = recipe.imageBase64,
                    size = IconSize.MEDIUM
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 中间信息
            Column(
                modifier = Modifier
                  .weight(1f)
                  .clickable(onClick = onClick)
            ) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                // 标签行
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val (typeColor, typeName) = when (recipe.type) {
                        RecipeType.MEAT -> MeatRed to "荤菜"
                        RecipeType.VEG -> SoftGreen to "素菜"
                        RecipeType.SOUP -> SoftBlue to "汤"
                        RecipeType.STAPLE -> WarmYellow to "主食"
                    }
                    InfoTag(text = typeName, color = typeColor)
                    InfoTag(text = getDifficultyName(recipe.difficulty), color = Color.Gray)
                    if (recipe.estimatedTime > 0) {
                        InfoTag(text = "${recipe.estimatedTime}分钟", color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 右侧重新Roll按钮
            Surface(
                onClick = onReRoll,
                shape = CircleShape,
                color = PrimaryOrange.copy(alpha = 0.1f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "🎲",
                        fontSize = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoTag(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun buildSummary(config: RollConfig): String {
    val parts = mutableListOf<String>()
    if (config.meatCount > 0) parts.add("${config.meatCount}荤")
    if (config.vegCount > 0) parts.add("${config.vegCount}素")
    if (config.soupCount > 0) parts.add("${config.soupCount}汤")
    if (config.stapleCount > 0) parts.add("${config.stapleCount}主食")
    return if (parts.isEmpty()) "随机菜品" else parts.joinToString(" + ")
}

private fun getDifficultyName(difficulty: com.eatwhat.domain.model.Difficulty): String {
    return when (difficulty) {
        com.eatwhat.domain.model.Difficulty.EASY -> "简单"
        com.eatwhat.domain.model.Difficulty.MEDIUM -> "中等"
        com.eatwhat.domain.model.Difficulty.HARD -> "困难"
    }
}
