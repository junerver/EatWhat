package com.eatwhat.ui.screens.recipe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.eatwhat.EatWhatApplication
import com.eatwhat.domain.model.Recipe
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    navController: NavController,
    recipeId: Long
) {
    val context = LocalContext.current
    val app = context.applicationContext as EatWhatApplication
    val repository = remember { app.recipeRepository }
    val scope = rememberCoroutineScope()

    val recipe by repository.getRecipeById(recipeId).collectAsState(initial = null)
    val (showDeleteDialog, setShowDeleteDialog) = useState(false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("菜谱详情") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("recipe/edit/$recipeId")
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑")
                    }
                    IconButton(onClick = { setShowDeleteDialog(true) }) {
                        Icon(Icons.Default.Delete, contentDescription = "删除")
                    }
                }
            )
        }
    ) { paddingValues ->
        recipe?.let { recipeData ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = recipeData.icon,
                                style = MaterialTheme.typography.displayLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = recipeData.name,
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Chip(
                                    label = when (recipeData.type.name) {
                                        "MEAT" -> "荤菜"
                                        "VEG" -> "素菜"
                                        "SOUP" -> "汤"
                                        "STAPLE" -> "主食"
                                        else -> recipeData.type.name
                                    }
                                )
                                Chip(
                                    label = when (recipeData.difficulty.name) {
                                        "EASY" -> "简单"
                                        "MEDIUM" -> "中等"
                                        "HARD" -> "困难"
                                        else -> recipeData.difficulty.name
                                    }
                                )
                                Chip(label = "${recipeData.estimatedTime}分钟")
                            }
                        }
                    }
                }

                // Ingredients
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "食材",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Divider()
                            recipeData.ingredients.forEach { ingredient ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(ingredient.name)
                                    Text(
                                        text = "${ingredient.amount} ${
                                            when (ingredient.unit.name) {
                                                "G" -> "g"
                                                "ML" -> "ml"
                                                "PIECE" -> "个"
                                                "SPOON" -> "勺"
                                                "MODERATE" -> ""
                                                else -> ingredient.unit.name
                                            }
                                        }",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Cooking Steps
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "烹饪步骤",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Divider()
                            recipeData.steps.forEach { step ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "${step.stepNumber}.",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = step.description,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Tags
                if (recipeData.tags.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "标签",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Divider()
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    recipeData.tags.forEach { tag ->
                                        Chip(label = tag.name)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } ?: Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "加载中...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { setShowDeleteDialog(false) },
            title = { Text("删除菜谱") },
            text = { Text("确定要删除这个菜谱吗？此操作无法撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            repository.deleteRecipe(recipeId)
                            setShowDeleteDialog(false)
                            navController.navigateUp()
                        }
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { setShowDeleteDialog(false) }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun Chip(label: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}
