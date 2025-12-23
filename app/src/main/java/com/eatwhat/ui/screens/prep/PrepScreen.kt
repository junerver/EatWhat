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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.eatwhat.domain.usecase.GeneratePrepListUseCase
import com.eatwhat.domain.usecase.PrepListItem
import com.eatwhat.ui.theme.PageBackground
import com.eatwhat.ui.theme.PrimaryOrange
import com.eatwhat.ui.theme.SoftGreen
import kotlinx.coroutines.launch

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

    val useCase = remember { GeneratePrepListUseCase() }
    val initialPrepList = remember { useCase(rollResult.recipes) }

    // Save history when entering PrepScreen and store the historyId
    val saveHistoryUseCase = remember { com.eatwhat.domain.usecase.SaveHistoryUseCase(app.historyRepository) }
    val historyRepository = remember { app.historyRepository }
    var historyId by remember { mutableStateOf<Long?>(null) }
    
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
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column {
                    TopAppBar(
                        title = {
                            Text(
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
                            containerColor = Color.White
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
                            Text(
                                text = "备菜进度",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray
                            )
                            Text(
                                text = "$checkedCount / $totalCount",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (checkedCount == totalCount) SoftGreen else PrimaryOrange
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (checkedCount == totalCount) SoftGreen else PrimaryOrange,
                            trackColor = Color(0xFFE0E0E0)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        },
        containerColor = PageBackground
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
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(20.dp),
                                spotColor = Color.Black.copy(alpha = 0.1f)
                            ),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
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
                                Text(
                                    text = "食材清单",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "共${totalCount}种食材",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
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
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Button(
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
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = historyId != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryOrange
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (checkedCount == totalCount) "✓ 开始做菜" else "开始做菜",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
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
    val backgroundColor = if (item.isChecked) SoftGreen.copy(alpha = 0.1f) else Color.White
    val borderColor = if (item.isChecked) SoftGreen.copy(alpha = 0.3f) else Color(0xFFE0E0E0)
    val textDecoration = if (item.isChecked) TextDecoration.LineThrough else null
    val textOpacity = if (item.isChecked) 0.6f else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (item.isChecked) 0.dp else 2.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = Color.Black.copy(alpha = 0.05f)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        onClick = { onCheckedChange(!item.isChecked) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Surface(
                shape = CircleShape,
                color = if (item.isChecked) SoftGreen else Color(0xFFF5F5F5),
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
                        Text(
                            "$index",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Ingredient name
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1C1B1F).copy(alpha = textOpacity),
                textDecoration = textDecoration,
                modifier = Modifier.weight(1f)
            )

            // Amount
            Text(
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
                color = if (item.isChecked) SoftGreen else Color.Gray,
                textDecoration = textDecoration
            )
        }
    }
}
