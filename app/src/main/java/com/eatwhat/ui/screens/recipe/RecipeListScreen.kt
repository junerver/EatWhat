package com.eatwhat.ui.screens.recipe

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import com.eatwhat.EatWhatApplication
import com.eatwhat.domain.model.Recipe
import com.eatwhat.domain.model.RecipeType
import com.eatwhat.ui.components.RecipeCard
import com.eatwhat.ui.theme.InputBackground
import com.eatwhat.ui.theme.LocalDarkTheme
import com.eatwhat.ui.theme.MeatRed
import com.eatwhat.ui.theme.OtherPurple
import com.eatwhat.ui.theme.Primary
import com.eatwhat.ui.theme.PrimaryOrange
import com.eatwhat.ui.theme.SoupBlue
import com.eatwhat.ui.theme.StapleOrange
import com.eatwhat.ui.theme.VegGreen
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useGetState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RecipeListScreen(navController: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as EatWhatApplication
    val repository = remember { app.recipeRepository }
    val scope = rememberCoroutineScope()

  val (searchQuery, setSearchQuery) = useGetState(default = "")
  val (isSearching, setIsSearching) = useGetState(default = false)

    // 设置透明状态栏
    val view = LocalView.current
  val darkTheme = LocalDarkTheme.current
    SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
    }

    // Tab配置 - 使用 emoji 和颜色
    val tabs = listOf(
        null to TabInfo("全部", "📋", Primary),
        RecipeType.MEAT to TabInfo("荤菜", "🍗", MeatRed),
        RecipeType.VEG to TabInfo("素菜", "🥬", VegGreen),
        RecipeType.SOUP to TabInfo("汤", "🍲", SoupBlue),
      RecipeType.STAPLE to TabInfo("主食", "🍚", StapleOrange),
      RecipeType.OTHER to TabInfo("其他", "🥣", OtherPurple)
    )

    val pagerState = rememberPagerState(pageCount = { tabs.size })

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
              .fillMaxSize()
              .background(MaterialTheme.colorScheme.background)
              .windowInsetsPadding(WindowInsets.statusBars)
              .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            // TopAppBar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column {
                    Row(
                        modifier = Modifier
                          .fillMaxWidth()
                          .height(64.dp)
                          .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "我的菜谱",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        // 搜索按钮
                      IconButton(onClick = { setIsSearching(!isSearching.value) }) {
                            Icon(
                              if (isSearching.value) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "搜索",
                                tint = PrimaryOrange
                            )
                        }
                    }
                    
                    // 搜索栏
                    AnimatedVisibility(
                      visible = isSearching.value,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                      val searchBackground =
                        if (darkTheme) MaterialTheme.colorScheme.surfaceVariant else InputBackground
                      val iconTint =
                        if (darkTheme) MaterialTheme.colorScheme.onSurfaceVariant else Color.Gray
                        Surface(
                            modifier = Modifier
                              .fillMaxWidth()
                              .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                          color = searchBackground
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
                                  tint = iconTint,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                BasicTextField(
                                  value = searchQuery.value,
                                  onValueChange = { setSearchQuery(it) },
                                    modifier = Modifier.weight(1f),
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    singleLine = true,
                                    decorationBox = { innerTextField ->
                                        Box {
                                          if (searchQuery.value.isEmpty()) {
                                                Text(
                                                    "搜索菜谱或标签",
                                                    style = MaterialTheme.typography.bodyLarge,
                                                  color = if (darkTheme) MaterialTheme.colorScheme.onSurfaceVariant else Color.Gray
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )
                              if (searchQuery.value.isNotEmpty()) {
                                    IconButton(
                                        onClick = { setSearchQuery("") },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "清除",
                                          tint = iconTint,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // 类型选择器 - 卡片式设计
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface
            ) {
                ScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    modifier = Modifier.fillMaxWidth(),
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
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
              val recipesFlow = remember(selectedType, searchQuery.value) {
                    when {
                      searchQuery.value.isNotEmpty() -> repository.searchRecipes(searchQuery.value)
                        selectedType != null -> repository.getRecipesByType(selectedType)
                        else -> repository.getAllRecipes()
                    }
                }

                val recipes by recipesFlow.collectAsState(initial = emptyList())

                RecipeListContent(
                    recipes = recipes,
                  searchQuery = searchQuery.value,
                    onRecipeClick = { recipe ->
                        navController.navigate("recipe/${recipe.id}")
                    }
                )
            }
        }
        
        // FloatingActionButton
        FloatingActionButton(
            onClick = { navController.navigate("recipe/add") },
            containerColor = PrimaryOrange,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .padding(end = 16.dp, bottom = 104.dp)
              .windowInsetsPadding(WindowInsets.navigationBars)
              .shadow(8.dp, CircleShape)
        ) {
            Icon(Icons.Default.Add, contentDescription = "添加菜谱")
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
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 88.dp  // 为底部导航栏预留空间
            ),
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
