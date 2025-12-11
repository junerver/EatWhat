package com.eatwhat.ui.screens.roll

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
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
import com.eatwhat.domain.model.RollResult
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

    val (rollResult, setRollResult) = useState<RollResult?>(null)
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
                    onSuccess = { setRollResult(it) },
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
        topBar = {
            TopAppBar(
                title = { Text("今日菜单") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { executeRoll() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "重新Roll")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
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
                        result = rollResult,
                        onRecipeClick = { recipe ->
                            navController.navigate("recipe/${recipe.id}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RollResultContent(
    result: RollResult,
    onRecipeClick: (Recipe) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "🎉 今天就吃这些！",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = result.getSummary(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        if (result.meatRecipes.isNotEmpty()) {
            item {
                CategoryHeader(title = "🍗 荤菜", count = result.meatRecipes.size)
            }
            items(result.meatRecipes, key = { it.id }) { recipe ->
                ResultRecipeCard(recipe = recipe, onClick = { onRecipeClick(recipe) })
            }
        }

        if (result.vegRecipes.isNotEmpty()) {
            item {
                CategoryHeader(title = "🥬 素菜", count = result.vegRecipes.size)
            }
            items(result.vegRecipes, key = { it.id }) { recipe ->
                ResultRecipeCard(recipe = recipe, onClick = { onRecipeClick(recipe) })
            }
        }

        if (result.soupRecipes.isNotEmpty()) {
            item {
                CategoryHeader(title = "🍲 汤", count = result.soupRecipes.size)
            }
            items(result.soupRecipes, key = { it.id }) { recipe ->
                ResultRecipeCard(recipe = recipe, onClick = { onRecipeClick(recipe) })
            }
        }

        if (result.stapleRecipes.isNotEmpty()) {
            item {
                CategoryHeader(title = "🍚 主食", count = result.stapleRecipes.size)
            }
            items(result.stapleRecipes, key = { it.id }) { recipe ->
                ResultRecipeCard(recipe = recipe, onClick = { onRecipeClick(recipe) })
            }
        }
    }
}

@Composable
private fun CategoryHeader(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun ResultRecipeCard(
    recipe: Recipe,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = when (recipe.type) {
                            RecipeType.MEAT -> Color(0xFFFFE0E0)
                            RecipeType.VEG -> Color(0xFFE0FFE0)
                            RecipeType.SOUP -> Color(0xFFE0F0FF)
                            RecipeType.STAPLE -> Color(0xFFFFF5E0)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (recipe.type) {
                        RecipeType.MEAT -> "🍗"
                        RecipeType.VEG -> "🥬"
                        RecipeType.SOUP -> "🍲"
                        RecipeType.STAPLE -> "🍚"
                    },
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                if (recipe.estimatedTime > 0) {
                    Text(
                        text = "${recipe.estimatedTime}分钟",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
