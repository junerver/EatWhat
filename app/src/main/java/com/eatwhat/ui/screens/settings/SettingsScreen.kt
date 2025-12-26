package com.eatwhat.ui.screens.settings

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.eatwhat.data.database.EatWhatDatabase
import com.eatwhat.data.repository.ExportRepositoryImpl
import com.eatwhat.data.sync.ConflictStrategy
import com.eatwhat.data.sync.ExportData
import com.eatwhat.data.sync.ImportPreview
import com.eatwhat.domain.usecase.ExportDataUseCase
import com.eatwhat.domain.usecase.ImportDataUseCase
import com.eatwhat.domain.usecase.ImportPreviewResult
import com.eatwhat.navigation.Destinations
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * 导出类型
 */
enum class ExportType {
    ALL, RECIPES, HISTORY
}

/**
 * 设置主页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 创建 Repository 和 UseCase
    val database = remember { EatWhatDatabase.getInstance(context) }
    val exportRepository = remember { ExportRepositoryImpl(context, database) }
    val exportUseCase = remember { ExportDataUseCase(context, exportRepository) }
    val importUseCase = remember { ImportDataUseCase(context, exportRepository) }

    // 导出状态
    var showExportDialog by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    var dataCount by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var pendingExportType by remember { mutableStateOf<ExportType?>(null) }

    // 导入状态
    var isImporting by remember { mutableStateOf(false) }
    var importPreviewResult by remember { mutableStateOf<ImportPreviewResult?>(null) }
    var showImportPreviewDialog by remember { mutableStateOf(false) }
    var loadingMessage by remember { mutableStateOf("") }

    // 加载数据统计
    LaunchedEffect(Unit) {
        dataCount = exportUseCase.getDataCount()
    }

    // SAF 导出文件选择器
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            val exportType = pendingExportType ?: ExportType.ALL
            pendingExportType = null
            isExporting = true
            loadingMessage = "正在导出..."

            scope.launch {
                val result = when (exportType) {
                    ExportType.ALL -> exportUseCase.exportAll(uri)
                    ExportType.RECIPES -> exportUseCase.exportRecipes(uri)
                    ExportType.HISTORY -> exportUseCase.exportHistory(uri)
                }

                isExporting = false
                loadingMessage = ""

                result.fold(
                    onSuccess = { exportResult ->
                        Toast.makeText(
                            context,
                            "导出成功！菜谱: ${exportResult.recipeCount}，历史: ${exportResult.historyCount}",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    onFailure = { error ->
                        Toast.makeText(
                            context,
                            "导出失败: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
        }
    }

    // SAF 导入文件选择器
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            isImporting = true
            loadingMessage = "正在读取文件..."

            scope.launch {
                val result = importUseCase.previewImport(uri)

                isImporting = false
                loadingMessage = ""

                result.fold(
                    onSuccess = { previewResult ->
                        importPreviewResult = previewResult
                        showImportPreviewDialog = true
                    },
                    onFailure = { error ->
                        Toast.makeText(
                            context,
                            "读取失败: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
        }
    }

    // 生成导出文件名
    fun generateFileName(type: ExportType): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val timestamp = dateFormat.format(Date())
        val suffix = when (type) {
            ExportType.ALL -> "all"
            ExportType.RECIPES -> "recipes"
            ExportType.HISTORY -> "history"
        }
        return "eatwhat_${suffix}_$timestamp.json"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "设置",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 数据管理卡片
                SettingsCard(
                    title = "数据管理",
                    icon = Icons.Default.Storage,
                    iconColor = MaterialTheme.colorScheme.primary
                ) {
                    SettingsItem(
                        icon = Icons.Default.FileUpload,
                        title = "导出数据",
                        subtitle = dataCount?.let { "菜谱: ${it.first}，历史: ${it.second}" }
                            ?: "备份菜谱和历史记录",
                        onClick = { showExportDialog = true }
                    )
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsItem(
                        icon = Icons.Default.FileDownload,
                        title = "导入数据",
                        subtitle = "从备份文件恢复数据",
                        onClick = { importLauncher.launch(arrayOf("application/json")) }
                    )
                }

                // 云同步卡片
                SettingsCard(
                    title = "云同步",
                    icon = Icons.Default.Cloud,
                    iconColor = MaterialTheme.colorScheme.tertiary
                ) {
                    SettingsItem(
                        icon = Icons.Default.Settings,
                        title = "WebDAV 配置",
                        subtitle = "配置云端存储服务器",
                        onClick = { navController.navigate(Destinations.WebDAVConfig.route) }
                    )
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsItem(
                        icon = Icons.Default.Sync,
                        title = "同步数据",
                        subtitle = "上传或下载云端备份",
                        onClick = { navController.navigate(Destinations.Sync.route) }
                    )
                }

                // 关于卡片
                SettingsCard(
                    title = "关于",
                    icon = Icons.Default.Info,
                    iconColor = MaterialTheme.colorScheme.secondary
                ) {
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "应用版本",
                        subtitle = "1.0.0",
                        onClick = { }
                    )
                }
            }

            // 加载指示器
            if (isExporting || isImporting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = loadingMessage,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }

    // 导出对话框
    if (showExportDialog) {
        ExportOptionsDialog(
            dataCount = dataCount,
            onDismiss = { showExportDialog = false },
            onExport = { type ->
                showExportDialog = false
                pendingExportType = type
                exportLauncher.launch(generateFileName(type))
            }
        )
    }

    // 导入预览对话框
    if (showImportPreviewDialog && importPreviewResult != null) {
        ImportPreviewDialog(
            previewResult = importPreviewResult!!,
            onDismiss = {
                showImportPreviewDialog = false
                importPreviewResult = null
            },
            onImport = { strategy ->
                showImportPreviewDialog = false
                isImporting = true
                loadingMessage = "正在导入..."

                scope.launch {
                    val result = importUseCase.executeImport(
                        importPreviewResult!!.data,
                        strategy
                    )

                    isImporting = false
                    loadingMessage = ""
                    importPreviewResult = null

                    result.fold(
                        onSuccess = { importResult ->
                            // 刷新数据统计
                            dataCount = exportUseCase.getDataCount()

                            Toast.makeText(
                                context,
                                "导入成功！新增: ${importResult.recipesImported + importResult.historyImported}，更新: ${importResult.recipesUpdated + importResult.historyUpdated}",
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        onFailure = { error ->
                            Toast.makeText(
                                context,
                                "导入失败: ${error.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                }
            }
        )
    }
}

/**
 * 导出选项对话框
 */
@Composable
private fun ExportOptionsDialog(
    dataCount: Pair<Int, Int>?,
    onDismiss: () -> Unit,
    onExport: (ExportType) -> Unit
) {
    val recipeCount = dataCount?.first ?: 0
    val historyCount = dataCount?.second ?: 0
    val hasNoData = recipeCount == 0 && historyCount == 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "选择导出内容",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 无数据警告
                if (hasNoData) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "没有数据可导出，请先添加菜谱或历史记录",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                ExportOptionItem(
                    icon = Icons.Default.SelectAll,
                    title = "全部数据",
                    subtitle = dataCount?.let { "菜谱: ${it.first}，历史: ${it.second}" } ?: "菜谱和历史记录",
                    enabled = !hasNoData,
                    onClick = { onExport(ExportType.ALL) }
                )
                ExportOptionItem(
                    icon = Icons.Default.Restaurant,
                    title = "仅菜谱",
                    subtitle = dataCount?.let { "${it.first} 个菜谱" } ?: "所有菜谱",
                    enabled = recipeCount > 0,
                    onClick = { onExport(ExportType.RECIPES) }
                )
                ExportOptionItem(
                    icon = Icons.Default.History,
                    title = "仅历史记录",
                    subtitle = dataCount?.let { "${it.second} 条记录" } ?: "所有历史",
                    enabled = historyCount > 0,
                    onClick = { onExport(ExportType.HISTORY) }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 导入预览对话框
 */
@Composable
private fun ImportPreviewDialog(
    previewResult: ImportPreviewResult,
    onDismiss: () -> Unit,
    onImport: (ConflictStrategy) -> Unit
) {
    val preview = previewResult.preview
    var selectedStrategy by remember { mutableStateOf(ConflictStrategy.UPDATE_IF_NEWER) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "导入预览",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 数据统计
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "文件内容",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("菜谱", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    "${preview.recipeCount} 个",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("历史记录", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    "${preview.historyCount} 条",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "${preview.newRecipes + preview.newHistory}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text("新增", style = MaterialTheme.typography.bodySmall)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "${preview.updatedRecipes + preview.updatedHistory}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                                Text("更新", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                // 冲突处理策略选择
                if (preview.updatedRecipes + preview.updatedHistory > 0) {
                    Text(
                        text = "冲突处理",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    ConflictStrategyOption(
                        title = "仅更新较新的",
                        description = "只更新修改时间更新的数据",
                        selected = selectedStrategy == ConflictStrategy.UPDATE_IF_NEWER,
                        onClick = { selectedStrategy = ConflictStrategy.UPDATE_IF_NEWER }
                    )

                    ConflictStrategyOption(
                        title = "全部更新",
                        description = "强制更新所有冲突的数据",
                        selected = selectedStrategy == ConflictStrategy.UPDATE,
                        onClick = { selectedStrategy = ConflictStrategy.UPDATE }
                    )

                    ConflictStrategyOption(
                        title = "跳过冲突",
                        description = "只导入新数据，跳过已存在的",
                        selected = selectedStrategy == ConflictStrategy.SKIP,
                        onClick = { selectedStrategy = ConflictStrategy.SKIP }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onImport(selectedStrategy) }
            ) {
                Text("开始导入")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 冲突策略选项
 */
@Composable
private fun ConflictStrategyOption(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (selected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else
            Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 导出选项项
 */
@Composable
private fun ExportOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val alpha = if (enabled) 1f else 0.5f

    Surface(
        onClick = { if (enabled) onClick() },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f * alpha)
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
                tint = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
                )
            }
            if (enabled) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * 设置卡片容器
 */
@Composable
private fun SettingsCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 卡片标题
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
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 卡片内容
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = Color.Black.copy(alpha = 0.08f)
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                content()
            }
        }
    }
}

/**
 * 设置项
 */
@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
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
            // 图标
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

            // 文字
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 箭头
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
