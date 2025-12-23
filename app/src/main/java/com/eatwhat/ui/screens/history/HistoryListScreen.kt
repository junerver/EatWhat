package com.eatwhat.ui.screens.history

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.eatwhat.EatWhatApplication
import com.eatwhat.data.repository.HistoryRepository
import com.eatwhat.domain.model.HistoryRecord
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// 定义主题色
private val PrimaryOrange = Color(0xFFFF6B35)
private val SoftPurple = Color(0xFF9C27B0)
private val PageBackground = Color(0xFFF5F5F5)

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "历史记录",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                actions = {
                    // 一键清除按钮（仅当有未锁定记录时显示）
                    if (unlockedCount > 0) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "清除未锁定记录",
                                tint = Color(0xFFE57373)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = PageBackground
    ) { paddingValues ->
        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
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
                        color = Color.Gray
                    )
                    Text(
                        text = "Roll 一些菜谱后这里会显示记录",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(historyList, key = { it.id }) { history ->
                    val isHighlighted = currentHighlightId == history.id
                    
                    if (history.isLocked) {
                        // 锁定的条目不能滑动删除
                        HistoryCard(
                            history = history,
                            isHighlighted = isHighlighted,
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
                    tint = Color(0xFFE57373)
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
                        contentColor = Color(0xFFE57373)
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
    val dismissState = rememberDismissState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == DismissValue.DismissedToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismiss(
        state = dismissState,
        background = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE57373))
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
        dismissContent = { content() },
        directions = setOf(DismissDirection.EndToStart)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryCard(
    history: HistoryRecord,
    isHighlighted: Boolean = false,
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
        history.isLocked -> SoftPurple.copy(alpha = 0.3f)
        else -> Color.Transparent
    }
    
    val borderWidth = if (isHighlighted) 3.dp else if (history.isLocked) 1.dp else 0.dp
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isHighlighted) 8.dp else 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.1f)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (history.isLocked) {
                SoftPurple.copy(alpha = 0.05f)
            } else {
                Color.White
            }
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
                    color = Color(0xFF1C1B1F)
                )

                // 第二行：如果有自定义名称，显示配置摘要；否则显示菜名列表
                if (history.customName.isNotEmpty()) {
                    Text(
                        text = history.summary.ifEmpty { "${history.totalCount}个菜" },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                } else if (history.recipes.isNotEmpty()) {
                    Text(
                        text = history.recipes.joinToString("、") { it.name },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
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
                Surface(
                    shape = CircleShape,
                    color = SoftPurple.copy(alpha = 0.1f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "已锁定",
                            tint = SoftPurple,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("M月d日 HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
