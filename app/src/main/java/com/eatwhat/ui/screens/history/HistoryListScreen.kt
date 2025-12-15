package com.eatwhat.ui.screens.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.eatwhat.EatWhatApplication
import com.eatwhat.data.repository.HistoryRepository
import com.eatwhat.domain.model.HistoryRecord
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryListScreen(navController: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as EatWhatApplication
    val repository = remember { HistoryRepository(app.database) }
    val scope = rememberCoroutineScope()

    val historyList by repository.getAllHistory().collectAsState(initial = emptyList())

    // 确认清除对话框状态
    var showClearDialog by remember { mutableStateOf(false) }

    // 计算未锁定记录数量
    val unlockedCount = historyList.count { !it.isLocked }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("历史记录") },
                actions = {
                    // 一键清除按钮（仅当有未锁定记录时显示）
                    if (unlockedCount > 0) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "清除未锁定记录"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无历史记录",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(historyList, key = { it.id }) { history ->
                    if (history.isLocked) {
                        // 锁定的条目不能滑动删除
                        HistoryCard(
                            history = history,
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
                    tint = MaterialTheme.colorScheme.error
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
                        contentColor = MaterialTheme.colorScheme.error
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
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissContent = {
            content()
        },
        directions = setOf(DismissDirection.EndToStart)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryCard(
    history: HistoryRecord,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (history.isLocked) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧图标
            Text(
                text = "📋",
                style = MaterialTheme.typography.headlineMedium
            )

            // 中间内容
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 主标题：优先显示自定义名称，否则显示 summary
                Text(
                    text = history.customName.ifEmpty {
                        history.summary.ifEmpty { "${history.totalCount}菜" }
                    },
                    style = MaterialTheme.typography.titleMedium
                )

                // 第二行：如果有自定义名称，显示配置摘要；否则显示菜名列表
                if (history.customName.isNotEmpty()) {
                    Text(
                        text = history.summary.ifEmpty { "${history.totalCount}菜" },
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
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = formatTimestamp(history.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // 右侧锁定图标
            if (history.isLocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "已锁定",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("M月d日 HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
