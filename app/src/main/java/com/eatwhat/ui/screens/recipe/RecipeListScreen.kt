package com.eatwhat.ui.screens.recipe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.eatwhat.EatWhatApplication
import com.eatwhat.domain.model.Recipe
import com.eatwhat.domain.model.RecipeType
import com.eatwhat.ui.components.RecipeCard
import kotlinx.coroutines.flow.Flow
import xyz.junerver.compose.hooks.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(navController: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as EatWhatApplication
    val repository = remember { app.recipeRepository }

    val (selectedType, setSelectedType) = useState<RecipeType?>(null)
    val (searchQuery, setSearchQuery) = useState("")
    val (isSearching, setIsSearching) = useState(false)

    // Get recipes based on filter
    val recipesFlow: Flow<List<Recipe>> = remember(selectedType, searchQuery) {
        when {
            searchQuery.isNotEmpty() -> repository.searchRecipes(searchQuery)
            selectedType != null -> repository.getRecipesByType(selectedType)
            else -> repository.getAllRecipes()
        }
    }

    val recipes by recipesFlow.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("菜谱") },
                actions = {
                    IconButton(onClick = { setIsSearching(!isSearching) }) {
                        Icon(Icons.Default.Search, contentDescription = "搜索")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("recipe/add")
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加菜谱")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            if (isSearching) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = setSearchQuery,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("搜索菜谱或标签") },
                    singleLine = true
                )
            }

            // Type filter tabs
            ScrollableTabRow(
                selectedTabIndex = when (selectedType) {
                    null -> 0
                    RecipeType.MEAT -> 1
                    RecipeType.VEG -> 2
                    RecipeType.SOUP -> 3
                    RecipeType.STAPLE -> 4
                },
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 16.dp
            ) {
                Tab(
                    selected = selectedType == null,
                    onClick = { setSelectedType(null) },
                    text = { Text("全部") }
                )
                Tab(
                    selected = selectedType == RecipeType.MEAT,
                    onClick = { setSelectedType(RecipeType.MEAT) },
                    text = { Text("荤菜 🍗") }
                )
                Tab(
                    selected = selectedType == RecipeType.VEG,
                    onClick = { setSelectedType(RecipeType.VEG) },
                    text = { Text("素菜 🥬") }
                )
                Tab(
                    selected = selectedType == RecipeType.SOUP,
                    onClick = { setSelectedType(RecipeType.SOUP) },
                    text = { Text("汤 🍲") }
                )
                Tab(
                    selected = selectedType == RecipeType.STAPLE,
                    onClick = { setSelectedType(RecipeType.STAPLE) },
                    text = { Text("主食 🍚") }
                )
            }

            // Recipe list
            if (recipes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotEmpty()) "未找到相关菜谱" else "暂无菜谱",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recipes, key = { it.id }) { recipe ->
                        RecipeCard(
                            recipe = recipe,
                            onClick = {
                                navController.navigate("recipe/${recipe.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}
