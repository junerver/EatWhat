package com.eatwhat.ui.screens.roll

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.eatwhat.domain.usecase.InsufficientRecipesException
import com.eatwhat.domain.usecase.RollRecipesUseCase
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.*

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

    val (rollResult, setRollResult) = useState<List<Recipe>?>(null)
    val (isLoading, setIsLoading) = useState(true)
    val (error, setError) = useState<String?>(null)

    val config = remember {
        RollConfig(
            meatCount = meatCount,
            vegCount = vegCount,
            soupCount = soupCount,
            stapleCount = stapleCount
        )
    }

    fun executeRoll() {
        scope.launch {
            setIsLoading(true)
            setError(null)
            try {
                val result = useCase(config)
                result.fold(
                    onSuccess = { setRollResult(it.recipes) },
                    onFailure = { e ->
                        when (e) {
                            is InsufficientRecipesException -> setError(e.errors.joinToString("\n"))
                            else -> setError("Roll失败: ${e.message}")
                        }
                    }
                )
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
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎲",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "正在Roll...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "😅",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { navController.popBackStack() }) {
                            Text("返回添加菜谱")
                        }
                    }
                }
                rollResult != null -> {
                    RollResultContent(
                        recipes = rollResult,
                        config = config,
                        onRecipeClick = { recipe ->
                            navController.navigate("recipe/${recipe.id}")
                        },
                        onReRoll = { executeRoll() },
                        onReRollSingle = { recipe ->
                            // 重新Roll单个菜品
                            scope.launch {
                                try {
                                    val newRecipes = app.recipeRepository.getRandomRecipesByType(
                                        recipe.type,
                                        1
                                    )
                                    if (newRecipes.isNotEmpty()) {
                                        val updatedList = rollResult.toMutableList()
                                        val index = updatedList.indexOfFirst { it.id == recipe.id }
                                        if (index != -1) {
                                            updatedList[index] = newRecipes.first()
                                            setRollResult(updatedList)
                                        }
                                    }
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("重新Roll失败: ${e.message}")
                                }
                            }
                        },
                        onConfirm = {
                            // TODO: 保存到历史记录
                            navController.popBackStack()
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
        // 顶部返回按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text(
                    text = "← 返回",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF6750A4)
                )
            }
        }

        // 内容区域
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp)
        ) {
            // 标题和摘要
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "今天就做这些！",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF1C1B1F)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = buildSummary(config),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF79747E)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // 菜品卡片列表
            items(recipes, key = { it.id }) { recipe ->
                DishCard(
                    recipe = recipe,
                    onClick = { onRecipeClick(recipe) },
                    onReRoll = { onReRollSingle(recipe) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // 底部操作按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onReRoll,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE8DEF8),
                    contentColor = Color(0xFF6750A4)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "重新Roll",
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
                    containerColor = Color(0xFF6750A4)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "就这些了",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧图标
            Text(
                text = getRecipeEmoji(recipe.type),
                fontSize = 32.sp,
                modifier = Modifier.clickable(onClick = onClick)
            )

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
                    color = Color(0xFF1C1B1F)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 标签行
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    InfoTag(text = getRecipeTypeName(recipe.type))
                    InfoTag(text = getDifficultyName(recipe.difficulty))
                    if (recipe.estimatedTime > 0) {
                        InfoTag(text = "${recipe.estimatedTime}分钟")
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 右侧重新Roll按钮
            IconButton(
                onClick = onReRoll,
                modifier = Modifier.size(40.dp)
            ) {
                Text(
                    text = "🎲",
                    fontSize = 20.sp
                )
            }
        }
    }
}

@Composable
private fun InfoTag(text: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFFE8DEF8)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF6750A4),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun buildSummary(config: RollConfig): String {
    val total = config.meatCount + config.vegCount + config.soupCount + config.stapleCount
    return if (total == 1) "随机1个菜" else "随机${total}个菜"
}

private fun getRecipeEmoji(type: RecipeType): String {
    return when (type) {
        RecipeType.MEAT -> "🍗"
        RecipeType.VEG -> "🥦"
        RecipeType.SOUP -> "🍲"
        RecipeType.STAPLE -> "🍚"
    }
}

private fun getRecipeTypeName(type: RecipeType): String {
    return when (type) {
        RecipeType.MEAT -> "荤菜"
        RecipeType.VEG -> "素菜"
        RecipeType.SOUP -> "汤"
        RecipeType.STAPLE -> "主食"
    }
}

private fun getDifficultyName(difficulty: com.eatwhat.domain.model.Difficulty): String {
    return when (difficulty) {
        com.eatwhat.domain.model.Difficulty.EASY -> "简单"
        com.eatwhat.domain.model.Difficulty.MEDIUM -> "中等"
        com.eatwhat.domain.model.Difficulty.HARD -> "困难"
    }
}
