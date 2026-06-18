package com.eatwhat.ui.screens.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eatwhat.domain.model.Difficulty
import com.eatwhat.domain.model.HistoryRecord
import com.eatwhat.domain.model.PrepItem
import com.eatwhat.domain.model.RecipeSnapshot
import com.eatwhat.domain.model.RecipeType
import com.eatwhat.ui.components.AppToolbar
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
import com.eatwhat.ui.theme.OtherPurple
import com.eatwhat.ui.theme.PrimaryOrange
import com.eatwhat.ui.theme.SoftBlue
import com.eatwhat.ui.theme.SoftGreen
import com.eatwhat.ui.theme.WarmYellow
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.palette.components.button.ButtonSize
import xyz.junerver.compose.palette.components.button.ButtonType
import xyz.junerver.compose.palette.components.button.PButton
import xyz.junerver.compose.palette.components.card.CardColors
import xyz.junerver.compose.palette.components.card.CardVariant
import xyz.junerver.compose.palette.components.card.PCard
import xyz.junerver.compose.palette.components.container.PContainer
import xyz.junerver.compose.palette.components.dialog.PDialog
import xyz.junerver.compose.palette.components.loading.PLoading
import xyz.junerver.compose.palette.components.progress.PProgress
import xyz.junerver.compose.palette.components.scaffold.PScaffold
import xyz.junerver.compose.palette.components.scaffold.ScaffoldDefaults
import xyz.junerver.compose.palette.components.tag.PTag
import xyz.junerver.compose.palette.components.tag.TagColors
import xyz.junerver.compose.palette.components.tag.TagSize
import xyz.junerver.compose.palette.components.text.PText

@Composable
fun HistoryDetailContent(
    history: HistoryRecord?,
    formatTimestamp: (Long) -> String,
    onNavigateUp: () -> Unit,
    onRecipeClick: (Long) -> Unit,
    onPrepItemCheckedChange: (PrepItem, Boolean) -> Unit,
    onSaveCustomName: (String) -> Unit
) {
    var showEditNameDialog by useState(false)
    var editingName by useState("")

    PScaffold(
        topBar = {
            AppToolbar(
                title = "菜单详情",
                onNavigateUp = onNavigateUp,
                actions = {
                    IconButton(
                        onClick = {
                            history?.let { data ->
                                editingName = data.customName
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
                }
            )
        },
        colors = ScaffoldDefaults.colors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) { paddingValues ->
        history?.let { data ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    HistoryHeaderCard(
                        history = data,
                        formattedTimestamp = formatTimestamp(data.timestamp)
                    )
                }

                if (data.prepItems.isNotEmpty()) {
                    item {
                        PrepProgressSection(
                            prepItems = data.prepItems,
                            onPrepItemCheckedChange = onPrepItemCheckedChange
                        )
                    }
                }

                item {
                    RecipeListSection(
                        recipes = data.recipes,
                        onRecipeClick = onRecipeClick
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        } ?: LoadingState(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )

        if (showEditNameDialog) {
            EditNameDialog(
                editingName = editingName,
                onEditingNameChange = { editingName = it },
                onDismiss = { showEditNameDialog = false },
                onSave = {
                    onSaveCustomName(editingName.trim())
                    showEditNameDialog = false
                }
            )
        }
    }
}

@Composable
private fun HistoryHeaderCard(
    history: HistoryRecord,
    formattedTimestamp: String
) {
    PCard(
        modifier = Modifier.fillMaxWidth(),
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
                text = history.customName.ifEmpty {
                    history.summary.ifEmpty { "${history.totalCount}个菜" }
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (history.customName.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                PText(
                    text = history.summary.ifEmpty { "${history.totalCount}个菜" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            PTag(
                text = formattedTimestamp,
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

@Composable
private fun PrepProgressSection(
    prepItems: List<PrepItem>,
    onPrepItemCheckedChange: (PrepItem, Boolean) -> Unit
) {
    val checkedCount = prepItems.count { it.isChecked }
    val totalCount = prepItems.size
    val allChecked = checkedCount == totalCount
    val progress = if (totalCount > 0) checkedCount.toFloat() / totalCount else 0f
    var expanded by useState(!allChecked)

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
                prepItems.forEachIndexed { index, item ->
                    PrepItemCheckRow(
                        index = index + 1,
                        item = item,
                        onCheckedChange = { checked ->
                            onPrepItemCheckedChange(item, checked)
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

@Composable
private fun RecipeListSection(
    recipes: List<RecipeSnapshot>,
    onRecipeClick: (Long) -> Unit
) {
    PCard(
        modifier = Modifier.fillMaxWidth(),
        variant = CardVariant.Elevated,
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(4.dp)
        ) {
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

            recipes.forEachIndexed { index, snapshot ->
                RecipeSnapshotCard(
                    snapshot = snapshot,
                    onClick = { onRecipeClick(snapshot.recipeId) }
                )
                if (index < recipes.lastIndex) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PLoading(
                size = 48.dp,
                color = PrimaryOrange
            )
            PText(
                text = "加载中...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EditNameDialog(
    editingName: String,
    onEditingNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    PDialog(
        onDismiss = onDismiss,
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
                    onValueChange = onEditingNameChange,
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
                onClick = onDismiss
            )
            PButton(
                text = "保存",
                modifier = Modifier.weight(1f),
                size = ButtonSize.SMALL,
                onClick = onSave
            )
        }
    )
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
        modifier = Modifier.fillMaxWidth(),
        variant = CardVariant.Elevated,
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(4.dp)
        ) {
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

            Spacer(modifier = Modifier.height(12.dp))
            val isDark = LocalDarkTheme.current
            val trackColor = if (isDark) DarkProgressTrack else LightBorder
            PProgress(
                percent = progress * 100f,
                modifier = Modifier.fillMaxWidth(),
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
    item: PrepItem,
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

    PContainer(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        onClick = { onCheckedChange(!item.isChecked) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PContainer(
                shape = CircleShape,
                color = if (item.isChecked) SoftGreen else uncheckedCheckboxColor,
                border = if (!item.isChecked) BorderStroke(2.dp, uncheckedCheckboxBorderColor) else null,
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
                text = item.ingredientName,
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
                    val (typeText, typeColor) = when (snapshot.type) {
                        RecipeType.MEAT -> "荤菜" to MeatRed
                        RecipeType.VEG -> "素菜" to SoftGreen
                        RecipeType.SOUP -> "汤" to SoftBlue
                        RecipeType.STAPLE -> "主食" to WarmYellow
                        RecipeType.OTHER -> "其他" to OtherPurple
                    }
                    InfoTag(text = typeText, color = typeColor)

                    val difficultyText = when (snapshot.difficulty) {
                        Difficulty.EASY -> "简单"
                        Difficulty.MEDIUM -> "中等"
                        Difficulty.HARD -> "困难"
                    }
                    InfoTag(text = difficultyText, color = Color.Gray)

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
