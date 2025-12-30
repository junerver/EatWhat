package com.eatwhat.ui.screens.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.eatwhat.EatWhatApplication
import com.eatwhat.R
import com.eatwhat.data.database.EatWhatDatabase
import com.eatwhat.data.preferences.ThemeMode
import com.eatwhat.data.preferences.ThemePreferences
import com.eatwhat.data.sync.ConflictStrategy
import com.eatwhat.domain.usecase.ExportDataUseCase
import com.eatwhat.domain.usecase.ImportDataUseCase
import com.eatwhat.domain.usecase.ImportPreviewResult
import com.eatwhat.navigation.Destinations
import com.eatwhat.ui.theme.LocalDarkTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 导出类型
 */
enum class ExportType {
  ALL, RECIPES, AI_PROVIDERS
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
  val app = context.applicationContext as EatWhatApplication
  val exportRepository = remember { app.exportRepository }
    val exportUseCase = remember { ExportDataUseCase(context, exportRepository) }
    val importUseCase = remember { ImportDataUseCase(context, exportRepository) }

  // 主题偏好设置
  val themePreferences = remember { ThemePreferences(context) }
  val currentThemeMode by themePreferences.themeModeFlow.collectAsState(initial = ThemeMode.SYSTEM)

    // 导出状态
    var showExportDialog by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
  var dataCount by remember { mutableStateOf<Triple<Int, Int, Int>?>(null) }
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
                  ExportType.AI_PROVIDERS -> exportUseCase.exportAIProviders(uri)
                }

                isExporting = false
                loadingMessage = ""

                result.fold(
                    onSuccess = { exportResult ->
                      val message = buildString {
                        append("导出成功！")
                        if (exportResult.recipeCount > 0) append(" 菜谱: ${exportResult.recipeCount}")
                        if (exportResult.historyCount > 0) append(" 历史: ${exportResult.historyCount}")
                        if (exportResult.aiProviderCount > 0) append(" AI供应商: ${exportResult.aiProviderCount}")
                      }
                      Toast.makeText(context, message, Toast.LENGTH_LONG).show()
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
          ExportType.AI_PROVIDERS -> "ai_providers"
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
              // 外观设置卡片
              SettingsCard(
                title = "外观",
                icon = Icons.Default.Palette,
                iconColor = MaterialTheme.colorScheme.tertiary
              ) {
                ThemeSettingItem(
                  currentThemeMode = currentThemeMode,
                  onThemeModeChange = { mode ->
                    scope.launch {
                      themePreferences.setThemeMode(mode)
                    }
                  }
                )
              }

                // 数据管理卡片
                SettingsCard(
                    title = "数据管理",
                    icon = Icons.Default.Storage,
                    iconColor = MaterialTheme.colorScheme.primary
                ) {
                    SettingsItem(
                        icon = Icons.Default.FileUpload,
                        title = "导出数据",
                      subtitle = dataCount?.let {
                        val parts = mutableListOf<String>()
                        if (it.first > 0) parts.add("菜谱: ${it.first}")
                        if (it.second > 0) parts.add("历史: ${it.second}")
                        if (it.third > 0) parts.add("AI供应商: ${it.third}")
                        if (parts.isEmpty()) "无数据" else parts.joinToString("，")
                      } ?: "备份菜谱、历史和配置",
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

              // AI 助手卡片
              SettingsCard(
                title = "AI 助手",
                iconDrawableRes = R.drawable.ic_ai_sparkles,
                iconColor = MaterialTheme.colorScheme.secondary
              ) {
                SettingsItem(
                  icon = Icons.Default.Settings,
                  title = "模型配置",
                  subtitle = "配置 OpenAI 接口参数",
                  onClick = { navController.navigate(Destinations.AIConfig.route) }
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
  dataCount: Triple<Int, Int, Int>?,
    onDismiss: () -> Unit,
    onExport: (ExportType) -> Unit
) {
    val recipeCount = dataCount?.first ?: 0
    val historyCount = dataCount?.second ?: 0
  val providerCount = dataCount?.third ?: 0
  val hasNoData = recipeCount == 0 && historyCount == 0 && providerCount == 0

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
                  subtitle = dataCount?.let {
                    val parts = mutableListOf<String>()
                    if (it.first > 0) parts.add("菜谱: ${it.first}")
                    if (it.second > 0) parts.add("历史: ${it.second}")
                    if (it.third > 0) parts.add("AI供应商: ${it.third}")
                    if (parts.isEmpty()) "无数据" else parts.joinToString("，")
                  } ?: "菜谱、历史和配置",
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
                  iconDrawableRes = R.drawable.ic_ai_sparkles,
                  title = "仅AI供应商",
                  subtitle = dataCount?.let { "${it.third} 个配置" } ?: "所有AI供应商配置",
                  enabled = providerCount > 0,
                  onClick = { onExport(ExportType.AI_PROVIDERS) }
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
  icon: ImageVector? = null,
  iconDrawableRes: Int? = null,
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
          if (icon != null) {
            Icon(
              imageVector = icon,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
              modifier = Modifier.size(24.dp)
            )
          } else if (iconDrawableRes != null) {
            Icon(
              painter = painterResource(id = iconDrawableRes),
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
              modifier = Modifier.size(24.dp)
            )
          }
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
    icon: ImageVector? = null,
    iconDrawableRes: Int? = null,
    iconColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
  val isDark = LocalDarkTheme.current

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
          when {
            icon != null -> Icon(
              imageVector = icon,
              contentDescription = null,
              tint = iconColor,
              modifier = Modifier.size(20.dp)
            )

            iconDrawableRes != null -> Icon(
              painter = painterResource(id = iconDrawableRes),
              contentDescription = null,
              tint = iconColor,
              modifier = Modifier.size(20.dp)
            )
          }
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

/**
 * 主题设置项
 */
@Composable
private fun ThemeSettingItem(
  currentThemeMode: ThemeMode,
  onThemeModeChange: (ThemeMode) -> Unit
) {
  var showThemeDialog by remember { mutableStateOf(false) }

  Surface(
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
          imageVector = Icons.Default.DarkMode,
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
          text = "主题模式",
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = when (currentThemeMode) {
            ThemeMode.SYSTEM -> "跟随系统"
            ThemeMode.LIGHT -> "浅色"
            ThemeMode.DARK -> "深色"
          },
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

  // 主题选择对话框
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

/**
 * 主题选择对话框
 */
@Composable
private fun ThemeSelectionDialog(
  currentThemeMode: ThemeMode,
  onDismiss: () -> Unit,
  onThemeModeSelect: (ThemeMode) -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "选择主题模式",
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
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
    confirmButton = {},
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("取消")
      }
    }
  )
}

/**
 * 主题模式选项
 */
@Composable
private fun ThemeModeOption(
  icon: ImageVector,
  title: String,
  description: String,
  selected: Boolean,
  onClick: () -> Unit
) {
  Surface(
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
        Text(
          text = title,
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
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

