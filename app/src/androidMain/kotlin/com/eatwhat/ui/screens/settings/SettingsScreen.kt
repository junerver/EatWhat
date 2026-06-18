package com.eatwhat.ui.screens.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.eatwhat.EatWhatApplication
import com.eatwhat.R
import com.eatwhat.data.preferences.ThemeMode
import com.eatwhat.data.preferences.ThemePreferences
import com.eatwhat.data.sync.ConflictStrategy
import com.eatwhat.domain.usecase.ExportDataUseCase
import com.eatwhat.domain.usecase.ImportDataUseCase
import com.eatwhat.domain.usecase.ImportPreviewResult
import com.eatwhat.navigation.Destinations
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks._useState
import xyz.junerver.compose.hooks.getValue
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.palette.components.button.ButtonSize
import xyz.junerver.compose.palette.components.button.ButtonType
import xyz.junerver.compose.palette.components.button.PButton
import xyz.junerver.compose.palette.components.card.CardColors
import xyz.junerver.compose.palette.components.card.CardVariant
import xyz.junerver.compose.palette.components.card.PCard
import xyz.junerver.compose.palette.components.container.PContainer
import xyz.junerver.compose.palette.components.dialog.PDialog
import xyz.junerver.compose.palette.components.radio.PRadio
import xyz.junerver.compose.palette.components.text.PText
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
@Composable
fun SettingsScreen(navController: NavController) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  // 创建 Repository 和 UseCase
  val app = context.applicationContext as EatWhatApplication
  val exportRepository by useCreation { app.exportRepository }
  val exportUseCase by useCreation { ExportDataUseCase(context, exportRepository) }
  val importUseCase by useCreation { ImportDataUseCase(context, exportRepository) }

  // 主题偏好设置
  val themePreferences by useCreation { ThemePreferences(context) }
  val currentThemeMode by themePreferences.themeModeFlow.collectAsState(initial = ThemeMode.SYSTEM)

  // 导出状态
  var showExportDialog by useState(false)
  var isExporting by useState(false)
  var dataCount by _useState<Triple<Int, Int, Int>?>(null)
  var pendingExportType by _useState<ExportType?>(null)

  // 导入状态
  var isImporting by useState(false)
  var importPreviewResult by _useState<ImportPreviewResult?>(null)
  var showImportPreviewDialog by useState(false)
  var loadingMessage by useState("")

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

  SettingsContent(
    currentThemeMode = currentThemeMode,
    dataCount = dataCount?.let { (recipeCount, historyCount, providerCount) ->
      SettingsDataCount(
        recipeCount = recipeCount,
        historyCount = historyCount,
        aiProviderCount = providerCount
      )
    },
    isLoading = isExporting || isImporting,
    loadingMessage = loadingMessage,
    onNavigateUp = { navController.popBackStack() },
    onThemeModeChange = { mode ->
      scope.launch {
        themePreferences.setThemeMode(mode)
      }
    },
    onExportClick = { showExportDialog = true },
    onImportClick = { importLauncher.launch(arrayOf("application/json")) },
    onWebDavConfigClick = { navController.navigate(Destinations.WebDAVConfig.route) },
    onSyncClick = { navController.navigate(Destinations.Sync.route) },
    onAIConfigClick = { navController.navigate(Destinations.AIConfig.route) }
  )

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

  PDialog(
    onDismiss = onDismiss,
    title = {
      PText(
        text = "选择导出内容",
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
        // 无数据警告
        if (hasNoData) {
          PCard(
            variant = CardVariant.Filled,
            colors = CardColors(
              containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
              contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
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
              PText(
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
    actions = {
      PButton(
        text = "取消",
        modifier = Modifier.weight(1f),
        size = ButtonSize.SMALL,
        type = ButtonType.PLAIN,
        onClick = onDismiss
      )
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
  var selectedStrategy by useState(ConflictStrategy.UPDATE_IF_NEWER)

  PDialog(
    onDismiss = onDismiss,
    title = {
      PText(
        text = "导入预览",
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // 数据统计
        PCard(
          variant = CardVariant.Filled,
          colors = CardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            contentColor = MaterialTheme.colorScheme.onSurface
          )
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            PText(
              text = "文件内容",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.SemiBold
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                PText("菜谱", style = MaterialTheme.typography.bodySmall)
                PText(
                  "${preview.recipeCount} 个",
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.Medium
                )
              }
              Column(horizontalAlignment = Alignment.End) {
                PText("历史记录", style = MaterialTheme.typography.bodySmall)
                PText(
                  "${preview.historyCount} 条",
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.Medium
                )
              }
            }
            SettingsDivider(modifier = Modifier.padding(vertical = 4.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceEvenly
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                PText(
                  "${preview.newRecipes + preview.newHistory}",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
                PText("新增", style = MaterialTheme.typography.bodySmall)
              }
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                PText(
                  "${preview.updatedRecipes + preview.updatedHistory}",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.tertiary
                )
                PText("更新", style = MaterialTheme.typography.bodySmall)
              }
            }
          }
        }

        // 冲突处理策略选择
        if (preview.updatedRecipes + preview.updatedHistory > 0) {
          PText(
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
    actions = {
      PButton(
        text = "取消",
        modifier = Modifier.weight(1f),
        size = ButtonSize.SMALL,
        type = ButtonType.PLAIN,
        onClick = onDismiss
      )
      PButton(
        text = "开始导入",
        modifier = Modifier.weight(1f),
        size = ButtonSize.SMALL,
        onClick = { onImport(selectedStrategy) }
      )
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
  PContainer(
    shape = RoundedCornerShape(8.dp),
    color = if (selected)
      MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    else
      Color.Transparent
  ) {
    PRadio(
      label = title,
      description = description,
      checked = selected,
      onClick = onClick,
      modifier = Modifier.fillMaxWidth(),
      labelColor = MaterialTheme.colorScheme.onSurface,
      descriptionColor = MaterialTheme.colorScheme.onSurfaceVariant,
      checkedColor = MaterialTheme.colorScheme.primary
    )
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

  PContainer(
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
        PText(
          text = title,
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
        )
        PText(
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
