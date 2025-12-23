package com.eatwhat.ui.screens.recipe

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.eatwhat.ui.components.RecipeCard
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.*

// 定义主题色
private val PrimaryOrange = Color(0xFFFF6B35)
private val PageBackground = Color(0xFFF5F5F5)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RecipeListScreen(navController: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as EatWhatApplication
    val repository = remember { app.recipeRepository }
    val scope = rememberCoroutineScope()

    val (searchQuery, setSearchQuery) = useState("")
    val (isSearching, setIsSearching) = useState(false)

    // Tab配置 - 使用 emoji 和颜色
    val tabs = listOf(
        null to TabInfo("全部", "📋", Color(0xFF6750A4)),
        RecipeType.MEAT to TabInfo("荤菜", "🍗", Color(0xFFE57373)),
        RecipeType.VEG to TabInfo("素菜", "🥬", Color(0xFF81C784)),
        RecipeType.SOUP to TabInfo("汤", "🍲", Color(0xFF64B5F6)),
        RecipeType.STAPLE to TabInfo("主食", "🍚", Color(0xFFFFB74D))
    )

    val pagerState = rememberPagerState(pageCount = { tabs.size })

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { 
                        Text(
                            "我的菜谱",
                            fontWeight = FontWeight.Bold
                        ) 
                    },
                    actions = {
                        // 搜索按钮
                        IconButton(onClick = { setIsSearching(!isSearching) }) {
                            Icon(
                                if (isSearching) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "搜索",
                                tint = PrimaryOrange
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )
                
                // 搜索栏
                AnimatedVisibility(
                    visible = isSearching,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8F8F8)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = setSearchQuery,
                                modifier = Modifier.weight(1f),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                "搜索菜谱或标签",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = Color.Gray
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { setSearchQuery("") },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "清除",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("recipe/add") },
                containerColor = PrimaryOrange,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.shadow(8.dp, CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加菜谱")
            }
        },
        containerColor = PageBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 类型选择器 - 卡片式设计
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White
            ) {
                ScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    modifier = Modifier.fillMaxWidth(),
                    edgePadding = 16.dp,
                    containerColor = Color.White,
                    contentColor = PrimaryOrange,
                    indicator = { tabPositions ->
                        if (pagerState.currentPage < tabPositions.size) {
                            TabRowDefaults.Indicator(
                                Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                                color = tabs[pagerState.currentPage].second.color
                            )
                        }
                    },
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, (_, tabInfo) ->
                        val selected = pagerState.currentPage == index
                        Tab(
                            selected = selected,
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    tabInfo.emoji,
                                    fontSize = 16.sp
                                )
                                Text(
                                    tabInfo.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) tabInfo.color else Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

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

private data class TabInfo(
    val title: String,
    val emoji: String,
    val color: Color
)

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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🍽️",
                    fontSize = 48.sp
                )
                Text(
                    text = if (searchQuery.isNotEmpty()) "未找到相关菜谱" else "暂无菜谱",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (searchQuery.isEmpty()) {
                    Text(
                        text = "点击右下角 + 添加你的第一道菜谱",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
