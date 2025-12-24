package com.eatwhat.ui.screens.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.eatwhat.ui.components.IconSize
import com.eatwhat.ui.components.RecipeIcon
import com.eatwhat.ui.components.SimpleCircularProgressIndicator
import com.eatwhat.ui.theme.PrimaryOrange
import com.eatwhat.ui.theme.SoftBlue
import com.eatwhat.ui.theme.SoftGreen
import com.eatwhat.ui.theme.WarmYellow
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "菜单详情",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigateUp()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            historyWithRecipes?.let { data ->
                                editingName = data.history.customName
                                showEditNameDialog = true
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "编辑名称",
                            tint = PrimaryOrange
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                windowInsets = WindowInsets.statusBars
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        historyWithRecipes?.let { data ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Card
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🍽️",
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = data.history.customName.ifEmpty {
                                    data.history.summary.ifEmpty { "${data.history.totalCount}个菜" }
                                },
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // 如果有自定义名称，显示配置摘要作为副标题
                            if (data.history.customName.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = data.history.summary.ifEmpty { "${data.history.totalCount}个菜" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // 时间标签
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = PrimaryOrange.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = formatTimestamp(data.history.timestamp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = PrimaryOrange,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // 备菜进度区域（可折叠）
                if (data.prepItems.isNotEmpty()) {
                    item {
                        val checkedCount = data.prepItems.count { it.isChecked }
                        val totalCount = data.prepItems.size
                        val allChecked = checkedCount == totalCount
                        val progress = if (totalCount > 0) checkedCount.toFloat() / totalCount else 0f

                        // 默认状态：全部完成时折叠，否则展开
                        var expanded by remember(allChecked) {
                            mutableStateOf(!allChecked)
                        }

                        SectionCard(
                            title = "备菜进度",
                            subtitle = "$checkedCount / $totalCount",
                            icon = Icons.Outlined.ShoppingCart,
                            iconBackgroundColor = SoftGreen.copy(alpha = 0.1f),
                            iconTint = SoftGreen,
                            isExpanded = expanded,
                            onToggle = { expanded = !expanded },
                            progress = progress
                        ) {
                            AnimatedVisibility(
                                visible = expanded,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    data.prepItems.forEachIndexed { index, item ->
                                        PrepItemCheckRow(
                                            index = index + 1,
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
                        }
                    }
                }

                // 菜谱卡片列表
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            // Header
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SoftBlue.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Outlined.MenuBook,
                                        contentDescription = null,
                                        tint = SoftBlue,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Text(
                                    "菜谱列表",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            data.recipes.forEachIndexed { index, snapshot ->
                                RecipeSnapshotCard(
                                    snapshot = snapshot,
                                    onClick = {
                                        navController.navigate("recipe/${snapshot.recipeId}")
                                    }
                                )
                                if (index < data.recipes.lastIndex) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }

                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        } ?: Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SimpleCircularProgressIndicator(
                    color = PrimaryOrange,
                    strokeWidth = 4.dp
                )
                Text(
                    text = "加载中...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 编辑名称对话框
        if (showEditNameDialog) {
            AlertDialog(
                onDismissRequest = { showEditNameDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = PrimaryOrange
                    )
                },
                title = {
                    Text("编辑名称")
                },
                text = {
                    Column {
                        Text(
                            text = "为这个菜肴搭配起个名字，方便管理收藏",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        OutlinedTextField(
                            value = editingName,
                            onValueChange = { editingName = it },
                            label = { Text("自定义名称") },
                            placeholder = { Text("例如：周末家宴、快手晚餐") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryOrange,
                                cursorColor = PrimaryOrange
                            )
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
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = PrimaryOrange
                        )
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
private fun SectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBackgroundColor: Color,
    iconTint: Color,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    progress: Float,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color.Black.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header - clickable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(iconBackgroundColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (progress >= 1f) SoftGreen else Color.Gray
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "收起" else "展开",
                    tint = iconTint
                )
            }

            // Progress bar
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (progress >= 1f) SoftGreen else PrimaryOrange,
                trackColor = Color(0xFFE0E0E0)
            )

            Spacer(modifier = Modifier.height(16.dp))

            content()
        }
    }
}

@Composable
private fun PrepItemCheckRow(
    index: Int,
    item: PrepItemRecord,
    onCheckedChange: (Boolean) -> Unit
) {
    val backgroundColor = if (item.isChecked) SoftGreen.copy(alpha = 0.1f) else Color(0xFFF8F8F8)
    val borderColor = if (item.isChecked) SoftGreen.copy(alpha = 0.3f) else Color(0xFFE0E0E0)
    val textDecoration = if (item.isChecked) TextDecoration.LineThrough else null
    val textOpacity = if (item.isChecked) 0.6f else 1f

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
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
                color = if (item.isChecked) SoftGreen else Color.White,
                border = if (!item.isChecked) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFE0E0E0)) else null,
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (item.isChecked) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = textOpacity),
                textDecoration = textDecoration,
                modifier = Modifier.weight(1f)
            )
        }
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Use RecipeIcon to display image or emoji
            RecipeIcon(
                emoji = snapshot.icon,
                imageBase64 = snapshot.imageBase64,
                size = IconSize.MEDIUM,
                modifier = Modifier.padding(end = 12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = snapshot.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // 类型标签
                    val (typeText, typeColor) = when (snapshot.type) {
                        "MEAT" -> "荤菜" to Color(0xFFE57373)
                        "VEG" -> "素菜" to SoftGreen
                        "SOUP" -> "汤" to SoftBlue
                        "STAPLE" -> "主食" to WarmYellow
                        else -> snapshot.type to Color.Gray
                    }
                    InfoTag(text = typeText, color = typeColor)

                    // 难度标签
                    val difficultyText = when (snapshot.difficulty) {
                        "EASY" -> "简单"
                        "MEDIUM" -> "中等"
                        "HARD" -> "困难"
                        else -> snapshot.difficulty
                    }
                    InfoTag(text = difficultyText, color = Color.Gray)

                    // 时间标签
                    InfoTag(text = "${snapshot.estimatedTime}分钟", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun InfoTag(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
