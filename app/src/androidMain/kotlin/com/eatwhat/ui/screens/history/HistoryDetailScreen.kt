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
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.eatwhat.ui.components.StyledTextField
import com.eatwhat.ui.theme.DarkBorder
import com.eatwhat.ui.theme.DarkCheckboxBorder
import com.eatwhat.ui.theme.DarkProgressTrack
import com.eatwhat.ui.theme.InputBackground
import com.eatwhat.ui.theme.LightBorder
import com.eatwhat.ui.theme.LocalDarkTheme
import com.eatwhat.ui.theme.MeatRed
import com.eatwhat.ui.theme.PrimaryOrange
import com.eatwhat.ui.theme.SoftBlue
import com.eatwhat.ui.theme.SoftGreen
import com.eatwhat.ui.theme.WarmYellow
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.getValue
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.palette.components.button.ButtonSize
import xyz.junerver.compose.palette.components.button.ButtonType
import xyz.junerver.compose.palette.components.button.PButton
import xyz.junerver.compose.palette.components.card.CardColors
import xyz.junerver.compose.palette.components.card.CardVariant
import xyz.junerver.compose.palette.components.card.PCard
import xyz.junerver.compose.palette.components.dialog.PDialog
import xyz.junerver.compose.palette.components.loading.PLoading
import xyz.junerver.compose.palette.components.progress.PProgress
import xyz.junerver.compose.palette.components.tag.PTag
import xyz.junerver.compose.palette.components.tag.TagColors
import xyz.junerver.compose.palette.components.tag.TagSize
import xyz.junerver.compose.palette.components.text.PText
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailScreen(
    navController: NavController,
    historyId: Long
) {
    val context = LocalContext.current
    val app = context.applicationContext as EatWhatApplication
  val repository by useCreation { HistoryRepository(app.database) }
    val scope = rememberCoroutineScope()

    val historyWithRecipes by repository.getHistoryById(historyId)
        .collectAsState(initial = null)

    // 编辑名称对话框状态
  var showEditNameDialog by useState(false)
  var editingName by useState("")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    PText(
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
                    PCard(
                        modifier = Modifier
                          .fillMaxWidth(),
                        variant = CardVariant.Elevated,
                        colors = CardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                              .fillMaxWidth()
                              .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            PText(
                                text = "🍽️",
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            PText(
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
                                PText(
                                    text = data.history.summary.ifEmpty { "${data.history.totalCount}个菜" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // 时间标签
                            PTag(
                                text = formatTimestamp(data.history.timestamp),
                                size = TagSize.Large,
                                colors = TagColors(
                                    containerColor = PrimaryOrange.copy(alpha = 0.1f),
                                    contentColor = PrimaryOrange,
                                    borderColor = Color.Transparent
                                )
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
                    PCard(
                        modifier = Modifier
                          .fillMaxWidth(),
                        variant = CardVariant.Elevated,
                        colors = CardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(4.dp)
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
                                        Icons.AutoMirrored.Outlined.MenuBook,
                                        contentDescription = null,
                                        tint = SoftBlue,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                PText(
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
                PLoading(
                    size = 48.dp,
                    color = PrimaryOrange,
                )
                PText(
                    text = "加载中...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 编辑名称对话框
        if (showEditNameDialog) {
            PDialog(
                onDismiss = { showEditNameDialog = false },
                title = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp, start = 24.dp, end = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = PrimaryOrange
                        )
                        PText(
                            text = "编辑名称",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                content = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PText(
                            text = "为这个菜肴搭配起个名字，方便管理收藏",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        StyledTextField(
                            value = editingName,
                            onValueChange = { editingName = it },
                            label = "自定义名称",
                            placeholder = "例如：周末家宴、快手晚餐",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                actions = {
                    PButton(
                        text = "取消",
                        modifier = Modifier.weight(1f),
                        size = ButtonSize.SMALL,
                        type = ButtonType.PLAIN,
                        onClick = { showEditNameDialog = false }
                    )
                    PButton(
                        text = "保存",
                        modifier = Modifier.weight(1f),
                        size = ButtonSize.SMALL,
                        onClick = {
                            scope.launch {
                                repository.updateHistoryCustomName(historyId, editingName.trim())
                            }
                            showEditNameDialog = false
                        }
                    )
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
    PCard(
        modifier = Modifier
          .fillMaxWidth(),
        variant = CardVariant.Elevated,
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(4.dp)
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
                        PText(
                            title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        PText(
                            subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (progress >= 1f) SoftGreen else MaterialTheme.colorScheme.onSurfaceVariant
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
          val isDark = LocalDarkTheme.current
            val trackColor = if (isDark) DarkProgressTrack else LightBorder
            PProgress(
                percent = progress * 100f,
                modifier = Modifier
                    .fillMaxWidth(),
                progressColor = if (progress >= 1f) SoftGreen else PrimaryOrange,
                trackColor = trackColor,
                formatter = null
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
  val isDark = LocalDarkTheme.current
    val uncheckedBackground = if (isDark) MaterialTheme.colorScheme.surfaceVariant else InputBackground
    val uncheckedBorderColor = if (isDark) DarkBorder else LightBorder
    val uncheckedCheckboxColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White
    val uncheckedCheckboxBorderColor = if (isDark) DarkCheckboxBorder else LightBorder

    val backgroundColor = if (item.isChecked) SoftGreen.copy(alpha = 0.1f) else uncheckedBackground
    val borderColor = if (item.isChecked) SoftGreen.copy(alpha = 0.3f) else uncheckedBorderColor
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
                color = if (item.isChecked) SoftGreen else uncheckedCheckboxColor,
                border = if (!item.isChecked) androidx.compose.foundation.BorderStroke(2.dp, uncheckedCheckboxBorderColor) else null,
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

            PText(
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
  val isDark = LocalDarkTheme.current
    val cardBackground = if (isDark) MaterialTheme.colorScheme.surfaceVariant else InputBackground

    PCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        variant = CardVariant.Filled,
        colors = CardColors(
            containerColor = cardBackground,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
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
                PText(
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
                        "MEAT" -> "荤菜" to MeatRed
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
    PTag(
        text = text,
        size = TagSize.Small,
        colors = TagColors(
            containerColor = color.copy(alpha = 0.1f),
            contentColor = color,
            borderColor = Color.Transparent
        )
    )
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
