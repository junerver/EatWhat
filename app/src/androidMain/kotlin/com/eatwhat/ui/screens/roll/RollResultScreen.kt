package com.eatwhat.ui.screens.roll

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.eatwhat.EatWhatApplication
import com.eatwhat.domain.model.Recipe
import com.eatwhat.domain.model.RollConfig
import com.eatwhat.domain.model.RollResult
import com.eatwhat.domain.usecase.InsufficientRecipesException
import com.eatwhat.domain.usecase.RollRecipesUseCase
import com.eatwhat.navigation.Destinations
import com.eatwhat.ui.theme.MeatRed
import com.eatwhat.ui.theme.PrimaryOrange
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks._useState
import xyz.junerver.compose.hooks.getValue
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.compose.palette.components.button.ButtonColors
import xyz.junerver.compose.palette.components.button.PButton
import xyz.junerver.compose.palette.components.loading.PLoading
import xyz.junerver.compose.palette.components.message.MessageType
import xyz.junerver.compose.palette.components.message.rememberMessageState
import xyz.junerver.compose.palette.components.scaffold.PScaffold
import xyz.junerver.compose.palette.components.scaffold.ScaffoldDefaults
import xyz.junerver.compose.palette.components.text.PText

@Composable
fun RollResultScreen(
    navController: NavController,
    meatCount: Int,
    vegCount: Int,
    soupCount: Int,
    stapleCount: Int,
    randomCount: Int = 0
) {
    val context = LocalContext.current
    val app = context.applicationContext as EatWhatApplication
  val useCase by useCreation { RollRecipesUseCase(app.rollRepository) }
    val scope = rememberCoroutineScope()
    val messageState = rememberMessageState()

  var rollResult by _useState<RollResult?>(null)
  val (isLoading, setIsLoading) = useGetState(default = true)
  var error by _useState<String?>(null)

    val config = remember {
        RollConfig(
            meatCount = meatCount,
            vegCount = vegCount,
            soupCount = soupCount,
          stapleCount = stapleCount,
          randomCount = randomCount
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

    PScaffold(
        colors = ScaffoldDefaults.colors(
            containerColor = MaterialTheme.colorScheme.background
        )
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
                        PText(
                            text = "🎲",
                            fontSize = 64.sp
                        )
                        PText(
                            text = "正在为你挑选...",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                        PLoading(
                            size = 48.dp,
                            color = PrimaryOrange,
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
                        PText(
                            text = "😅",
                            fontSize = 64.sp
                        )
                        PText(
                          text = errorMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MeatRed
                        )
                        PButton(
                            text = "返回添加菜谱",
                            colors = ButtonColors(
                                containerColor = PrimaryOrange,
                                contentColor = Color.White
                            ),
                            onClick = { navController.popBackStack() }
                        )
                    }
                }
                rollResult != null -> {
                  val currentRollResult = rollResult ?: return@PScaffold
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
                                        messageState.show("该类型没有更多菜品了", MessageType.Warning)
                                    }
                                } catch (e: Exception) {
                                    messageState.show("重新Roll失败: ${e.message}", MessageType.Error)
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
