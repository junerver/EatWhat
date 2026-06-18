package com.eatwhat.ui.screens.prep

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.eatwhat.domain.usecase.GeneratePrepListUseCase
import com.eatwhat.domain.usecase.PrepListItem
import com.eatwhat.ui.theme.DarkProgressTrack
import com.eatwhat.ui.theme.LightBorder
import com.eatwhat.ui.theme.LocalDarkTheme
import com.eatwhat.ui.theme.PrimaryOrange
import com.eatwhat.ui.theme.SoftGreen
import com.eatwhat.ui.theme.UnselectedBackground
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks._useState
import xyz.junerver.compose.hooks.getValue
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.palette.components.button.ButtonColors
import xyz.junerver.compose.palette.components.button.PButton
import xyz.junerver.compose.palette.components.card.CardColors
import xyz.junerver.compose.palette.components.card.CardVariant
import xyz.junerver.compose.palette.components.card.PCard
import xyz.junerver.compose.palette.components.progress.PProgress
import xyz.junerver.compose.palette.components.text.PText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrepScreen(
    navController: NavController
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as com.eatwhat.EatWhatApplication
    val rollResult = app.currentRollResult
    val scope = rememberCoroutineScope()

    // If no roll result, navigate back
    if (rollResult == null) {
        LaunchedEffect(Unit) {
            navController.navigateUp()
        }
        return
    }

  val useCase by useCreation { GeneratePrepListUseCase() }
  val initialPrepList by useCreation { useCase(rollResult.recipes) }

    // Save history when entering PrepScreen and store the historyId
  val saveHistoryUseCase by useCreation { com.eatwhat.domain.usecase.SaveHistoryUseCase(app.historyRepository) }
  val historyRepository by useCreation { app.historyRepository }
  var historyId by _useState<Long?>(null)
    
    LaunchedEffect(Unit) {
        try {
            val id = saveHistoryUseCase(rollResult, initialPrepList)
            historyId = id
        } catch (e: Exception) {
            // Handle error if needed
        }
    }
    
    // Read prep items from database - only when historyId is available
    val prepItemsFromDb by historyRepository.getPrepItemsByHistoryId(historyId ?: 0L)
        .collectAsState(initial = emptyList())

    // Convert database entities to PrepListItem for display
    val prepList = remember(prepItemsFromDb) {
        prepItemsFromDb.map { entity ->
            PrepListItem(
                name = entity.ingredientName,
                amount = "",
                unit = "",
                isChecked = entity.isChecked
            )
        }
    }

    // Calculate progress
    val checkedCount = prepList.count { it.isChecked }
    val totalCount = prepList.size
    val progress = if (totalCount > 0) checkedCount.toFloat() / totalCount else 0f

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column {
                    TopAppBar(
                        title = {
                            PText(
                                "准备食材",
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { navController.navigateUp() }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "返回"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        windowInsets = WindowInsets.statusBars
                    )
                    
                    // Progress bar
                    Column(
                        modifier = Modifier
                          .fillMaxWidth()
                          .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PText(
                                text = "备菜进度",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            PText(
                                text = "$checkedCount / $totalCount",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (checkedCount == totalCount) SoftGreen else PrimaryOrange
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                      val isDarkForProgress = LocalDarkTheme.current
                        val progressTrackColor = if (isDarkForProgress) DarkProgressTrack else LightBorder
                        PProgress(
                            percent = progress * 100f,
                            modifier = Modifier
                                .fillMaxWidth(),
                            progressColor = if (checkedCount == totalCount) SoftGreen else PrimaryOrange,
                            trackColor = progressTrackColor,
                            formatter = null
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(paddingValues)
        ) {
            // 食材清单
            LazyColumn(
                modifier = Modifier
                  .weight(1f)
                  .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header card
                item {
                    PCard(
                        modifier = Modifier
                          .fillMaxWidth(),
                        variant = CardVariant.Elevated,
                        colors = CardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                              .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                  .size(40.dp)
                                  .clip(RoundedCornerShape(12.dp))
                                  .background(SoftGreen.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.ShoppingCart,
                                    contentDescription = null,
                                    tint = SoftGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                PText(
                                    text = "食材清单",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                PText(
                                    text = "共${totalCount}种食材",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                // Ingredient items
                itemsIndexed(prepList, key = { _, item -> "${item.name}_${item.unit}" }) { index, item ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        IngredientCheckCard(
                            index = index + 1,
                            item = item,
                            onCheckedChange = { checked ->
                                // Update database directly
                                if (prepItemsFromDb.isNotEmpty() && index < prepItemsFromDb.size) {
                                    scope.launch {
                                        historyRepository.updatePrepItemChecked(prepItemsFromDb[index].id, checked)
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // 底部按钮
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                PButton(
                    text = if (checkedCount == totalCount) "✓ 开始做菜" else "开始做菜",
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(16.dp),
                    disabled = historyId == null,
                    colors = ButtonColors(
                        containerColor = PrimaryOrange,
                        contentColor = Color.White
                    ),
                    onClick = {
                        // 跳转到历史详情页面
                        historyId?.let { id ->
                            // 设置全局高亮 ID
                            app.highlightHistoryId = id
                            
                            // 先导航到 History 列表（清除当前栈）
                            navController.navigate(com.eatwhat.navigation.Destinations.History.routeWithoutArgs) {
                                // 清除所有返回栈，回到起始页面
                                popUpTo(0) {
                                    inclusive = true
                                }
                            }
                            // 然后导航到 HistoryDetail
                            navController.navigate(com.eatwhat.navigation.Destinations.HistoryDetail.createRoute(id))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun IngredientCheckCard(
    index: Int,
    item: PrepListItem,
    onCheckedChange: (Boolean) -> Unit
) {
    val isDark = LocalDarkTheme.current
    val uncheckedBackground = if (isDark) MaterialTheme.colorScheme.surface else Color.White
    val uncheckedCheckboxColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant else UnselectedBackground

    val backgroundColor = if (item.isChecked) SoftGreen.copy(alpha = 0.1f) else uncheckedBackground
    val textDecoration = if (item.isChecked) TextDecoration.LineThrough else null
    val textOpacity = if (item.isChecked) 0.6f else 1f

    PCard(
        modifier = Modifier
          .fillMaxWidth(),
        variant = if (item.isChecked) CardVariant.Filled else CardVariant.Elevated,
        colors = CardColors(
            containerColor = backgroundColor,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        onClick = { onCheckedChange(!item.isChecked) }
    ) {
        Row(
            modifier = Modifier
              .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Surface(
                shape = CircleShape,
                color = if (item.isChecked) SoftGreen else uncheckedCheckboxColor,
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (item.isChecked) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        PText(
                            "$index",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Ingredient name
            PText(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = textOpacity),
                textDecoration = textDecoration,
                modifier = Modifier.weight(1f)
            )

            // Amount
            PText(
                text = "${item.amount}${
                    when (item.unit) {
                        "G" -> "克"
                        "ML" -> "毫升"
                        "PIECE" -> "个"
                        "SPOON" -> "勺"
                        "MODERATE" -> ""
                        else -> item.unit
                    }
                }",
                style = MaterialTheme.typography.bodyMedium,
                color = if (item.isChecked) SoftGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                textDecoration = textDecoration
            )
        }
    }
}
