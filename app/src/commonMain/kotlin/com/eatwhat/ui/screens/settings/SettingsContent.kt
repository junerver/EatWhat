package com.eatwhat.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eatwhat.data.preferences.ThemeMode
import com.eatwhat.ui.components.AppToolbar
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.palette.components.card.CardColors
import xyz.junerver.compose.palette.components.card.CardVariant
import xyz.junerver.compose.palette.components.card.PCard
import xyz.junerver.compose.palette.components.container.PContainer
import xyz.junerver.compose.palette.components.dialog.PDialog
import xyz.junerver.compose.palette.components.dialog.PDialogCancelAction
import xyz.junerver.compose.palette.components.loading.PLoading
import xyz.junerver.compose.palette.components.scaffold.PScaffold
import xyz.junerver.compose.palette.components.scaffold.ScaffoldDefaults
import xyz.junerver.compose.palette.components.text.PText

data class SettingsDataCount(
    val recipeCount: Int,
    val historyCount: Int,
    val aiProviderCount: Int
)

@Composable
fun SettingsContent(
    currentThemeMode: ThemeMode,
    dataCount: SettingsDataCount?,
    isLoading: Boolean,
    loadingMessage: String,
    onNavigateUp: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onWebDavConfigClick: () -> Unit,
    onSyncClick: () -> Unit,
    onAIConfigClick: () -> Unit
) {
    PScaffold(
        topBar = {
            AppToolbar(
                title = "设置",
                onNavigateUp = onNavigateUp
            )
        },
        colors = ScaffoldDefaults.colors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SettingsCard(
                    title = "外观",
                    icon = Icons.Default.Palette,
                    iconColor = MaterialTheme.colorScheme.tertiary
                ) {
                    ThemeSettingItem(
                        currentThemeMode = currentThemeMode,
                        onThemeModeChange = onThemeModeChange
                    )
                }

                SettingsCard(
                    title = "数据管理",
                    icon = Icons.Default.Storage,
                    iconColor = MaterialTheme.colorScheme.primary
                ) {
                    SettingsItem(
                        icon = Icons.Default.FileUpload,
                        title = "导出数据",
                        subtitle = dataCount?.toSummaryText() ?: "备份菜谱、历史和配置",
                        onClick = onExportClick
                    )
                    SettingsDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsItem(
                        icon = Icons.Default.FileDownload,
                        title = "导入数据",
                        subtitle = "从备份文件恢复数据",
                        onClick = onImportClick
                    )
                }

                SettingsCard(
                    title = "云同步",
                    icon = Icons.Default.Cloud,
                    iconColor = MaterialTheme.colorScheme.tertiary
                ) {
                    SettingsItem(
                        icon = Icons.Default.Settings,
                        title = "WebDAV 配置",
                        subtitle = "配置云端存储服务器",
                        onClick = onWebDavConfigClick
                    )
                    SettingsDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsItem(
                        icon = Icons.Default.Sync,
                        title = "同步数据",
                        subtitle = "上传或下载云端备份",
                        onClick = onSyncClick
                    )
                }

                SettingsCard(
                    title = "AI 助手",
                    icon = Icons.Default.AutoAwesome,
                    iconColor = MaterialTheme.colorScheme.secondary
                ) {
                    SettingsItem(
                        icon = Icons.Default.Settings,
                        title = "模型配置",
                        subtitle = "配置 OpenAI 接口参数",
                        onClick = onAIConfigClick
                    )
                }

                SettingsCard(
                    title = "关于",
                    icon = Icons.Default.Info,
                    iconColor = MaterialTheme.colorScheme.secondary
                ) {
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "应用版本",
                        subtitle = "1.0.0",
                        onClick = {}
                    )
                }
            }

            if (isLoading) {
                SettingsLoadingOverlay(loadingMessage = loadingMessage)
            }
        }
    }
}

private fun SettingsDataCount.toSummaryText(): String {
    val parts = buildList {
        if (recipeCount > 0) add("菜谱: $recipeCount")
        if (historyCount > 0) add("历史: $historyCount")
        if (aiProviderCount > 0) add("AI供应商: $aiProviderCount")
    }
    return if (parts.isEmpty()) "无数据" else parts.joinToString("，")
}

@Composable
private fun SettingsLoadingOverlay(loadingMessage: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        PCard(
            variant = CardVariant.Elevated,
            colors = CardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PLoading()
                PText(
                    text = loadingMessage,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
internal fun SettingsCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            PText(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        PCard(
            modifier = Modifier.fillMaxWidth(),
            variant = CardVariant.Elevated
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
internal fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    PContainer(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                PText(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                PText(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ThemeSettingItem(
    currentThemeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    var showThemeDialog by useState(false)

    PContainer(
        onClick = { showThemeDialog = true },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DarkMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                PText(
                    text = "主题模式",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                PText(
                    text = currentThemeMode.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentThemeMode = currentThemeMode,
            onDismiss = { showThemeDialog = false },
            onThemeModeSelect = { mode ->
                onThemeModeChange(mode)
                showThemeDialog = false
            }
        )
    }
}

private val ThemeMode.label: String
    get() = when (this) {
        ThemeMode.SYSTEM -> "跟随系统"
        ThemeMode.LIGHT -> "浅色"
        ThemeMode.DARK -> "深色"
    }

@Composable
private fun ThemeSelectionDialog(
    currentThemeMode: ThemeMode,
    onDismiss: () -> Unit,
    onThemeModeSelect: (ThemeMode) -> Unit
) {
    PDialog(
        onDismiss = onDismiss,
        title = {
            PText(
                text = "选择主题模式",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 24.dp, end = 24.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeModeOption(
                    icon = Icons.Default.SettingsBrightness,
                    title = "跟随系统",
                    description = "跟随系统设置自动切换",
                    selected = currentThemeMode == ThemeMode.SYSTEM,
                    onClick = { onThemeModeSelect(ThemeMode.SYSTEM) }
                )
                ThemeModeOption(
                    icon = Icons.Default.LightMode,
                    title = "浅色",
                    description = "始终使用浅色主题",
                    selected = currentThemeMode == ThemeMode.LIGHT,
                    onClick = { onThemeModeSelect(ThemeMode.LIGHT) }
                )
                ThemeModeOption(
                    icon = Icons.Default.DarkMode,
                    title = "深色",
                    description = "始终使用深色主题",
                    selected = currentThemeMode == ThemeMode.DARK,
                    onClick = { onThemeModeSelect(ThemeMode.DARK) }
                )
            }
        },
        actions = {
            PDialogCancelAction(onClick = onDismiss)
        }
    )
}

@Composable
private fun ThemeModeOption(
    icon: ImageVector,
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    PContainer(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                PText(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                PText(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
internal fun SettingsDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
    )
}
