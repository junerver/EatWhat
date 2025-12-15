package com.eatwhat.ui.screens.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.eatwhat.EatWhatApplication
import com.eatwhat.data.repository.HistoryRepository
import com.eatwhat.data.repository.PrepItemRecord
import com.eatwhat.data.repository.RecipeSnapshot
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun HistoryDetailScreen(
    navController: NavController,
    historyId: Long
) {
    val context = LocalContext.current
    val app = context.applicationContext as EatWhatApplication
    val repository = remember { HistoryRepository(app.database) }
    val scope = rememberCoroutineScope()

    val historyWithRecipes by repository.getHistoryById(historyId)
        .collectAsState(initial = null)

    // 编辑名称对话框状态
    var showEditNameDialog by remember { mutableStateOf(false) }
    var editingName by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        historyWithRecipes?.let { data ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 24.dp, end = 24.dp, top = 60.dp, bottom = 100.dp)
            ) {
                // 返回按钮
                item {
                    TextButton(
                        onClick = { navController.navigateUp() },
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = "← 返回",
                            fontSize = 24.sp,
                            color = Color(0xFF6750A4)
                        )
                    }
                }

                // 标题 - 自定义名称或配置摘要
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = data.history.customName.ifEmpty {
                                    data.history.summary.ifEmpty { "${data.history.totalCount}个菜" }
                                },
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1B1F),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // 如果有自定义名称，显示配置摘要作为副标题
                            if (data.history.customName.isNotEmpty()) {
                                Text(
                                    text = data.history.summary.ifEmpty { "${data.history.totalCount}个菜" },
                                    fontSize = 14.sp,
                                    color = Color(0xFF79747E),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }

                            // 时间显示
                            Text(
                                text = formatTimestamp(data.history.timestamp),
                                fontSize = 14.sp,
                                color = Color(0xFF79747E),
                                modifier = Modifier.padding(bottom = 24.dp)
                            )
                        }

                        // 编辑按钮
                        IconButton(
                            onClick = {
                                editingName = data.history.customName
                                showEditNameDialog = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "编辑名称",
                                tint = Color(0xFF6750A4)
                            )
                        }
                    }
                }

                // 备菜进度区域（可折叠）
                if (data.prepItems.isNotEmpty()) {
                    item {
                        val checkedCount = data.prepItems.count { it.isChecked }
                        val totalCount = data.prepItems.size
                        val allChecked = checkedCount == totalCount

                        // 默认状态：全部完成时折叠，否则展开
                        var expanded by remember(allChecked) {
                            mutableStateOf(!allChecked)
                        }

                        // 标题行（可点击展开/折叠）
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = !expanded }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "备菜进度: $checkedCount/$totalCount",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1C1B1F)
                            )

                            Icon(
                                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (expanded) "收起" else "展开",
                                tint = Color(0xFF6750A4)
                            )
                        }

                        // 备菜清单（可折叠动画）
                        AnimatedVisibility(
                            visible = expanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column {
                                data.prepItems.forEach { item ->
                                    PrepItemCheckRow(
                                        item = item,
                                        onCheckedChange = { checked ->
                                            scope.launch {
                                                repository.updatePrepItemChecked(item.id, checked)
                                            }
                                            // 如果勾选了最后一个未完成项，自动折叠
                                            if (checked && checkedCount + 1 == totalCount) {
                                                expanded = false
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // 菜谱卡片列表
                data.recipes.forEach { snapshot ->
                    item {
                        RecipeSnapshotCard(
                            snapshot = snapshot,
                            onClick = {
                                navController.navigate("recipe/${snapshot.recipeId}")
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        } ?: Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "加载中...",
                fontSize = 16.sp,
                color = Color(0xFF79747E)
            )
        }

        // 编辑名称对话框
        if (showEditNameDialog) {
            AlertDialog(
                onDismissRequest = { showEditNameDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color(0xFF6750A4)
                    )
                },
                title = {
                    Text("编辑名称")
                },
                text = {
                    Column {
                        Text(
                            text = "为这个菜肴搭配起个名字，方便管理收藏",
                            fontSize = 14.sp,
                            color = Color(0xFF79747E),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        OutlinedTextField(
                            value = editingName,
                            onValueChange = { editingName = it },
                            label = { Text("自定义名称") },
                            placeholder = { Text("例如：周末家宴、快手晚餐") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                repository.updateHistoryCustomName(historyId, editingName.trim())
                            }
                            showEditNameDialog = false
                        }
                    ) {
                        Text("保存")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditNameDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}

@Composable
private fun PrepItemCheckRow(
    item: PrepItemRecord,
    onCheckedChange: (Boolean) -> Unit
) {
    val backgroundColor = if (item.isChecked) Color(0xFFE8DEF8) else Color(0xFFF5F5F5)
    val textDecoration = if (item.isChecked) TextDecoration.LineThrough else null
    val opacity = if (item.isChecked) 0.6f else 1f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .clickable { onCheckedChange(!item.isChecked) }
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isChecked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.size(20.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFF6750A4),
                uncheckedColor = Color(0xFF79747E)
            )
        )

        Text(
            text = item.name,
            fontSize = 16.sp,
            color = Color(0xFF1C1B1F).copy(alpha = opacity),
            textDecoration = textDecoration,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun RecipeSnapshotCard(
    snapshot: RecipeSnapshot,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = snapshot.icon,
                fontSize = 32.sp,
                modifier = Modifier.padding(end = 12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = snapshot.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1C1B1F)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // 类型标签
                    val typeText = when (snapshot.type) {
                        "MEAT" -> "荤菜"
                        "VEGETABLE" -> "素菜"
                        "SOUP" -> "汤"
                        "STAPLE" -> "主食"
                        else -> snapshot.type
                    }
                    Tag(text = typeText)

                    // 难度标签
                    val difficultyText = when (snapshot.difficulty) {
                        "EASY" -> "简单"
                        "MEDIUM" -> "中等"
                        "HARD" -> "困难"
                        else -> snapshot.difficulty
                    }
                    Tag(text = difficultyText)

                    // 时间标签
                    Tag(text = "${snapshot.estimatedTime}分钟")
                }
            }
        }
    }
}

@Composable
private fun Tag(text: String) {
    Box(
        modifier = Modifier
            .background(Color(0xFFE8DEF8), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color(0xFF6750A4)
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    val month = calendar.get(Calendar.MONTH) + 1
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    return "${month}月${day}日 ${hour}:${String.format("%02d", minute)}"
}
