package com.eatwhat.ui.screens.recipe

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import com.eatwhat.EatWhatApplication
import com.eatwhat.domain.model.RecipeType
import com.eatwhat.ui.components.AppToolbar
import com.eatwhat.ui.theme.InputBackground
import com.eatwhat.ui.theme.LocalDarkTheme
import com.eatwhat.ui.theme.PrimaryOrange
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.getValue
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.compose.palette.components.container.PContainer
import xyz.junerver.compose.palette.components.floatbutton.FloatButtonDefaults
import xyz.junerver.compose.palette.components.floatbutton.PFloatButton
import xyz.junerver.compose.palette.components.text.PText

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RecipeListScreen(navController: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as EatWhatApplication
  val repository by useCreation { app.recipeRepository }
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
        null to TabInfo("全部", "📋"),
        RecipeType.MEAT to TabInfo("荤菜", "🍗"),
        RecipeType.VEG to TabInfo("素菜", "🥬"),
        RecipeType.SOUP to TabInfo("汤", "🍲"),
      RecipeType.STAPLE to TabInfo("主食", "🍚"),
      RecipeType.OTHER to TabInfo("其他", "🥣")
    )

    val pagerState = rememberPagerState(pageCount = { tabs.size })

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
              .fillMaxSize()
              .background(MaterialTheme.colorScheme.background)
              .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            // Toolbar
            Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(MaterialTheme.colorScheme.surface)
            ) {
                AppToolbar(
                    title = "我的菜谱",
                    actions = {
                      IconButton(onClick = { setIsSearching(!isSearching.value) }) {
                            Icon(
                              if (isSearching.value) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "搜索",
                                tint = PrimaryOrange
                            )
                        }
                    }
                )
                     
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
                        PContainer(
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
                                                PText(
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
            
            // 类型选择器 - 卡片式设计
            PContainer(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tabs.forEachIndexed { index, (_, tabInfo) ->
                        RecipeTypeFilterChip(
                            tabInfo = tabInfo,
                            selected = index == pagerState.currentPage,
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                        )
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
        
        PFloatButton(
            onClick = { navController.navigate("recipe/add") },
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .padding(end = 16.dp, bottom = 104.dp)
              .windowInsetsPadding(WindowInsets.navigationBars),
            icon = {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "添加菜谱",
                    tint = FloatButtonDefaults.iconColor(),
                    modifier = Modifier.size(FloatButtonDefaults.iconSize())
                )
            }
        )
    }
}

private data class TabInfo(
    val title: String,
    val emoji: String
)

@Composable
private fun RecipeTypeFilterChip(
    tabInfo: TabInfo,
    selected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (selected) {
        PrimaryOrange.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    }
    val contentColor = if (selected) {
        PrimaryOrange
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val borderColor = if (selected) {
        PrimaryOrange.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    }

    PContainer(
        onClick = onClick,
        modifier = Modifier
            .height(44.dp)
            .widthIn(min = 72.dp),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PText(
                    text = tabInfo.emoji,
                    fontSize = 16.sp
                )
                PText(
                    text = tabInfo.title,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                    ),
                    color = contentColor
                )
            }
        }
    }
}
