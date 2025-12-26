package com.eatwhat.ui.screens.history

import android.app.Activity
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import com.eatwhat.EatWhatApplication
import com.eatwhat.data.repository.HistoryRepository
import com.eatwhat.domain.model.HistoryRecord
import com.eatwhat.navigation.Destinations
import com.eatwhat.ui.theme.ErrorRed
import com.eatwhat.ui.theme.PrimaryOrange
import com.eatwhat.ui.theme.SoftPurple
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryListScreen(
    navController: NavController,
    highlightId: Long? = null
) {
    val context = LocalContext.current
    val app = context.applicationContext as EatWhatApplication
    val repository = remember { HistoryRepository(app.database) }
    val scope = rememberCoroutineScope()

    val historyList by repository.getAllHistory().collectAsState(initial = emptyList())

    // 确认清除对话框状态
    var showClearDialog by remember { mutableStateOf(false) }

    // 高亮状态：存储需要闪烁的 historyId
    var currentHighlightId by remember { mutableStateOf<Long?>(null) }

    // 设置透明状态栏
    val view = LocalView.current
    val darkTheme = isSystemInDarkTheme()
    SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
    }

    // 斑马纹颜色：根据深色模式自适应
    val zebraLight = MaterialTheme.colorScheme.surface
    val zebraDark = MaterialTheme.colorScheme.surfaceVariant
    
    // 从全局状态读取高亮 ID 并启动闪烁效果
    LaunchedEffect(Unit) {
        val globalHighlightId = app.highlightHistoryId
        Log.d("HistoryListScreen", "=== HistoryListScreen Loaded ===")
        Log.d("HistoryListScreen", "Global highlightHistoryId: $globalHighlightId")
        
        if (globalHighlightId != null && globalHighlightId > 0) {
            Log.d("HistoryListScreen", "✓ Valid global highlightId, starting animation for: $globalHighlightId")
            currentHighlightId = globalHighlightId
            
            // 2秒后清除高亮
            delay(2000)
            Log.d("HistoryListScreen", "✓ Clearing highlight after 2 seconds")
            currentHighlightId = null
            // 清除全局状态，避免下次进入时再次触发
            app.highlightHistoryId = null
        } else {
            Log.d("HistoryListScreen", "✗ No valid global highlightId, skipping animation")
        }
    }

    // 计算未锁定记录数量
    val unlockedCount = historyList.count { !it.isLocked }

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "历史记录",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                // 一键清除按钮（仅当有未锁定记录时显示）
                if (unlockedCount > 0) {
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "清除未锁定记录",
                            tint = ErrorRed
                        )
                    }
                }
                // 设置按钮
                IconButton(onClick = { navController.navigate(Destinations.Settings.route) }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "设置",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        // Content
        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "📋",
                        fontSize = 64.sp
                    )
                    Text(
                        text = "暂无历史记录",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Roll 一些菜谱后这里会显示记录",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(historyList, key = { _, history -> history.id }) { index, history ->
                    val isHighlighted = currentHighlightId == history.id
                    // 斑马纹效果：使用索引决定背景色
                    val zebraBackground = if (index % 2 == 0) zebraLight else zebraDark
                    
                    if (history.isLocked) {
                        // 锁定的条目不能滑动删除
                        HistoryCard(
                            history = history,
                            isHighlighted = isHighlighted,
                            zebraBackground = zebraBackground,
                            onClick = {
                                navController.navigate("history/${history.id}")
                            },
                            onLongClick = {
                                scope.launch {
                                    repository.toggleHistoryLocked(history.id, false)
                                }
                            }
                        )
                    } else {
                        // 未锁定的条目可以滑动删除
                        SwipeToDeleteItem(
                            onDelete = {
                                scope.launch {
                                    repository.deleteHistory(history.id)
                                }
                            }
                        ) {
                            HistoryCard(
                                history = history,
                                isHighlighted = isHighlighted,
                                zebraBackground = zebraBackground,
                                onClick = {
                                    navController.navigate("history/${history.id}")
                                },
                                onLongClick = {
                                    scope.launch {
                                        repository.toggleHistoryLocked(history.id, true)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // 确认清除对话框
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = null,
                    tint = ErrorRed
                )
            },
            title = {
                Text("清除历史记录")
            },
            text = {
                Text("确定要删除全部 $unlockedCount 条未锁定的历史记录吗？\n\n此操作不可恢复，已锁定的记录将保留。")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            repository.deleteAllUnlockedHistory()
                        }
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = ErrorRed
                    )
                ) {
                    Text("确认删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteItem(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ErrorRed)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = Color.White
                )
            }
        },
        content = { content() },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryCard(
    history: HistoryRecord,
    isHighlighted: Boolean = false,
    zebraBackground: Color = Color.White,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    // 闪烁动画：在2秒内从 1f 到 0f 到 1f 重复
    val infiniteTransition = rememberInfiniteTransition(label = "highlight")
    val highlightAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "highlightAlpha"
    )
    
    // 计算边框颜色和宽度
    val borderColor = when {
        isHighlighted -> PrimaryOrange.copy(alpha = highlightAlpha)
        history.isLocked -> SoftPurple
        else -> Color.Transparent
    }
    
    val borderWidth = if (isHighlighted) 3.dp else if (history.isLocked) 2.dp else 0.dp
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isHighlighted) 8.dp else 2.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.08f)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = zebraBackground
        ),
        border = if (borderWidth > 0.dp) {
            androidx.compose.foundation.BorderStroke(borderWidth, borderColor)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧图标
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryOrange.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🍽️",
                    fontSize = 24.sp
                )
            }

            // 中间内容
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 主标题：优先显示自定义名称，否则显示 summary
                Text(
                    text = history.customName.ifEmpty {
                        history.summary.ifEmpty { "${history.totalCount}个菜" }
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // 第二行：如果有自定义名称，显示配置摘要；否则显示菜名列表
                if (history.customName.isNotEmpty()) {
                    Text(
                        text = history.summary.ifEmpty { "${history.totalCount}个菜" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (history.recipes.isNotEmpty()) {
                    Text(
                        text = history.recipes.joinToString("、") { it.name },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // 时间标签
                Spacer(modifier = Modifier.height(2.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PrimaryOrange.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = formatTimestamp(history.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = PrimaryOrange,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // 右侧锁定图标
            if (history.isLocked) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SoftPurple.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "已锁定",
                        tint = SoftPurple,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("M月d日 HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
