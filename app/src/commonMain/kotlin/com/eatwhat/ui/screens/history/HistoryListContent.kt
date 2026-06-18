package com.eatwhat.ui.screens.history

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eatwhat.domain.model.HistoryRecord
import com.eatwhat.ui.components.AppToolbar
import com.eatwhat.ui.components.PaletteConfirmDialog
import com.eatwhat.ui.theme.ErrorRed
import com.eatwhat.ui.theme.PrimaryOrange
import com.eatwhat.ui.theme.SoftPurple
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.palette.components.button.ButtonType
import xyz.junerver.compose.palette.components.card.CardColors
import xyz.junerver.compose.palette.components.card.CardVariant
import xyz.junerver.compose.palette.components.card.PCard
import xyz.junerver.compose.palette.components.swipe.PSwipeToDismissBox
import xyz.junerver.compose.palette.components.swipe.SwipeDismissDirection
import xyz.junerver.compose.palette.components.tag.PTag
import xyz.junerver.compose.palette.components.tag.TagColors
import xyz.junerver.compose.palette.components.tag.TagSize
import xyz.junerver.compose.palette.components.text.PText

@Composable
fun HistoryListContent(
    historyList: List<HistoryRecord>,
    highlightedHistoryId: Long?,
    formatTimestamp: (Long) -> String,
    onSettingsClick: () -> Unit,
    onHistoryClick: (HistoryRecord) -> Unit,
    onLockToggle: (HistoryRecord, Boolean) -> Unit,
    onDeleteHistory: (HistoryRecord) -> Unit,
    onDeleteAllUnlocked: () -> Unit
) {
    var showClearDialog by useState(false)
    val unlockedCount = historyList.count { !it.isLocked }
    val zebraLight = MaterialTheme.colorScheme.surface
    val zebraDark = MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppToolbar(
            title = "历史记录",
            actions = {
                if (unlockedCount > 0) {
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "清除未锁定记录",
                            tint = ErrorRed
                        )
                    }
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "设置",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )

        if (historyList.isEmpty()) {
            EmptyHistoryState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 88.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(historyList, key = { _, history -> history.id }) { index, history ->
                    val isHighlighted = highlightedHistoryId == history.id
                    val zebraBackground = if (index % 2 == 0) zebraLight else zebraDark

                    if (history.isLocked) {
                        HistoryCard(
                            history = history,
                            isHighlighted = isHighlighted,
                            zebraBackground = zebraBackground,
                            formatTimestamp = formatTimestamp,
                            onClick = { onHistoryClick(history) },
                            onLockToggle = { onLockToggle(history, false) }
                        )
                    } else {
                        SwipeToDeleteItem(
                            onDelete = { onDeleteHistory(history) }
                        ) {
                            HistoryCard(
                                history = history,
                                isHighlighted = isHighlighted,
                                zebraBackground = zebraBackground,
                                formatTimestamp = formatTimestamp,
                                onClick = { onHistoryClick(history) },
                                onLockToggle = { onLockToggle(history, true) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        PaletteConfirmDialog(
            title = "确认删除",
            message = "确定要删除全部 $unlockedCount 条未锁定的历史记录吗？\n\n此操作无法撤销，已锁定的记录将保留。",
            confirmText = "删除",
            confirmType = ButtonType.DANGER,
            icon = Icons.Default.DeleteSweep,
            iconTint = ErrorRed,
            onDismiss = { showClearDialog = false },
            onConfirm = {
                showClearDialog = false
                onDeleteAllUnlocked()
            }
        )
    }
}

@Composable
private fun EmptyHistoryState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PText(
                text = "📋",
                fontSize = 64.sp
            )
            PText(
                text = "暂无历史记录",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            PText(
                text = "Roll 一些菜谱后这里会显示记录",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun SwipeToDeleteItem(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    PSwipeToDismissBox(
        onDismiss = { dismissDirection ->
            if (dismissDirection == SwipeDismissDirection.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        },
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

@Composable
private fun HistoryCard(
    history: HistoryRecord,
    isHighlighted: Boolean,
    zebraBackground: Color,
    formatTimestamp: (Long) -> String,
    onClick: () -> Unit,
    onLockToggle: () -> Unit
) {
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

    val borderColor = when {
        isHighlighted -> PrimaryOrange.copy(alpha = highlightAlpha)
        history.isLocked -> SoftPurple
        else -> Color.Transparent
    }
    val borderWidth = if (isHighlighted) 3.dp else if (history.isLocked) 2.dp else 0.dp
    val cardShape = RoundedCornerShape(12.dp)
    val cardModifier = if (borderWidth > 0.dp) {
        Modifier
            .fillMaxWidth()
            .border(BorderStroke(borderWidth, borderColor), cardShape)
    } else {
        Modifier.fillMaxWidth()
    }

    PCard(
        modifier = cardModifier,
        variant = CardVariant.Elevated,
        onClick = onClick,
        colors = CardColors(
            containerColor = zebraBackground,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryOrange.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                PText(
                    text = "🍽️",
                    fontSize = 24.sp
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PText(
                    text = history.customName.ifEmpty {
                        history.summary.ifEmpty { "${history.totalCount}个菜" }
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (history.customName.isNotEmpty()) {
                    PText(
                        text = history.summary.ifEmpty { "${history.totalCount}个菜" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (history.recipes.isNotEmpty()) {
                    PText(
                        text = history.recipes.joinToString("、") { it.name },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
                PTag(
                    text = formatTimestamp(history.timestamp),
                    size = TagSize.Small,
                    colors = TagColors(
                        containerColor = PrimaryOrange.copy(alpha = 0.1f),
                        contentColor = PrimaryOrange,
                        borderColor = Color.Transparent
                    )
                )
            }

            IconButton(
                onClick = onLockToggle,
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (history.isLocked) {
                                SoftPurple.copy(alpha = 0.15f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (history.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = if (history.isLocked) "解除锁定" else "锁定",
                        tint = if (history.isLocked) SoftPurple else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
