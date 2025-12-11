package com.eatwhat.ui.screens.recipe

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.eatwhat.EatWhatApplication
import com.eatwhat.domain.model.Recipe
import com.eatwhat.domain.model.RecipeType
import com.eatwhat.ui.components.RecipeCard
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RecipeListScreen(navController: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as EatWhatApplication
    val repository = remember { app.recipeRepository }
    val scope = rememberCoroutineScope()

    val (searchQuery, setSearchQuery) = useState("")
    val (isSearching, setIsSearching) = useState(false)

    // Tab配置
    val tabs = listOf(
        null to "全部",
        RecipeType.MEAT to "荤菜 🍗",
        RecipeType.VEG to "素菜 🥬",
        RecipeType.SOUP to "汤 🍲",
        RecipeType.STAPLE to "主食 🍚"
    )

    val pagerState = rememberPagerState(pageCount = { tabs.size })

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

            // Type filter tabs - 支持点击切换
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 16.dp
            ) {
                tabs.forEachIndexed { index, (_, title) ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(title) }
                    )
                }
            }

            // 支持左右滑动切换的内容区
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val selectedType = tabs[page].first

                // 根据当前tab获取菜谱
                val recipesFlow = remember(selectedType, searchQuery) {
                    when {
                        searchQuery.isNotEmpty() -> repository.searchRecipes(searchQuery)
                        selectedType != null -> repository.getRecipesByType(selectedType)
                        else -> repository.getAllRecipes()
                    }
                }

                val recipes by recipesFlow.collectAsState(initial = emptyList())

                RecipeListContent(
                    recipes = recipes,
                    searchQuery = searchQuery,
                    onRecipeClick = { recipe ->
                        navController.navigate("recipe/${recipe.id}")
                    }
                )
            }
        }
    }
}

@Composable
private fun RecipeListContent(
    recipes: List<Recipe>,
    searchQuery: String,
    onRecipeClick: (Recipe) -> Unit
) {
    if (recipes.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
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
                    onClick = { onRecipeClick(recipe) }
                )
            }
        }
    }
}
