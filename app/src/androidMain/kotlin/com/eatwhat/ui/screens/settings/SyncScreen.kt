package com.eatwhat.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.eatwhat.EatWhatApplication
import com.eatwhat.data.database.EatWhatDatabase
import com.eatwhat.data.repository.SyncRepositoryImpl
import com.eatwhat.data.sync.SyncMetadata
import com.eatwhat.data.sync.SyncResult
import com.eatwhat.navigation.Destinations
import com.eatwhat.ui.theme.LocalDarkTheme
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks._useState
import xyz.junerver.compose.hooks.getValue
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.palette.components.button.ButtonColors
import xyz.junerver.compose.palette.components.button.ButtonSize
import xyz.junerver.compose.palette.components.button.ButtonType
import xyz.junerver.compose.palette.components.button.PButton
import xyz.junerver.compose.palette.components.card.CardColors
import xyz.junerver.compose.palette.components.card.CardVariant
import xyz.junerver.compose.palette.components.card.PCard
import xyz.junerver.compose.palette.components.loading.PLoading
import xyz.junerver.compose.palette.components.text.PText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 同步页面 - 美化版本
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
  val isDark = LocalDarkTheme.current

    // 创建 Repository
  val database by useCreation { EatWhatDatabase.getInstance(context) }
  val app = context.applicationContext as EatWhatApplication
  val exportRepository by useCreation { app.exportRepository }
  val syncRepository by useCreation { SyncRepositoryImpl(context, exportRepository) }

    // 配置状态
  val config by useCreation { syncRepository.getConfig() }
    val isConfigured = config != null

    // UI 状态
  var isSyncing by useState(false)
  var syncMessage by useState("")
  var cloudMetadata by _useState<SyncMetadata?>(null)
  var showPasswordDialog by useState(false)
  var pendingAction by _useState<SyncAction?>(null)

  // 主题颜色
  val pageBackground = if (isDark) MaterialTheme.colorScheme.background else Color(0xFFF5F5F5)
  val cardBackground = if (isDark) MaterialTheme.colorScheme.surface else Color.White
  val textColor = if (isDark) MaterialTheme.colorScheme.onSurface else Color.Black
  val subTextColor = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color.Gray
  val primaryColor = Color(0xFFFF6B35)

    // 加载云端元数据
    LaunchedEffect(isConfigured) {
        if (isConfigured) {
            cloudMetadata = syncRepository.getCloudMetadata()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    PText(
                        text = "数据同步",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                  containerColor = Color.Transparent
                )
            )
        },
      containerColor = pageBackground
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(paddingValues)
                  .verticalScroll(rememberScrollState())
                  .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!isConfigured) {
                    // 未配置提示
                    NotConfiguredCard(
                      textColor = textColor,
                      subTextColor = subTextColor,
                      primaryColor = primaryColor,
                        onConfigureClick = {
                            navController.navigate(Destinations.WebDAVConfig.route)
                        }
                    )
                } else {
                    // 同步状态卡片
                    SyncStatusCard(
                        config = config!!,
                        cloudMetadata = cloudMetadata,
                      textColor = textColor,
                      subTextColor = subTextColor
                    )

                    // 同步操作卡片
                    SyncActionsCard(
                        cloudMetadata = cloudMetadata,
                      encryptionEnabled = config?.encryptionEnabled ?: false,
                      textColor = textColor,
                      subTextColor = subTextColor,
                      primaryColor = primaryColor,
                        onUpload = {
                            performSync(
                                scope = scope,
                                syncRepository = syncRepository,
                                action = SyncAction.UPLOAD,
                              password = if (config?.encryptionEnabled == true) config?.encryptionPassword else null,
                                onStart = {
                                    isSyncing = true
                                    syncMessage = "正在上传..."
                                },
                                onComplete = { success, message ->
                                    isSyncing = false
                                    syncMessage = ""
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    if (success) {
                                        scope.launch {
                                            cloudMetadata = syncRepository.getCloudMetadata()
                                        }
                                    }
                                }
                            )
                        },
                        onDownload = {
                            if (cloudMetadata?.encrypted == true) {
                              if (config?.encryptionEnabled == true && config?.encryptionPassword != null) {
                                    performSync(
                                        scope = scope,
                                        syncRepository = syncRepository,
                                        action = SyncAction.DOWNLOAD,
                                      password = config?.encryptionPassword,
                                        onStart = {
                                            isSyncing = true
                                            syncMessage = "正在下载..."
                                        },
                                        onComplete = { success, message ->
                                            isSyncing = false
                                            syncMessage = ""
                                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                } else {
                                    pendingAction = SyncAction.DOWNLOAD
                                    showPasswordDialog = true
                                }
                            } else {
                                performSync(
                                    scope = scope,
                                    syncRepository = syncRepository,
                                    action = SyncAction.DOWNLOAD,
                                    password = null,
                                    onStart = {
                                        isSyncing = true
                                        syncMessage = "正在下载..."
                                    },
                                    onComplete = { success, message ->
                                        isSyncing = false
                                        syncMessage = ""
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        }
                    )

                  // 配置入口卡片
                  ConfigEntryCard(
                    textColor = textColor,
                    subTextColor = subTextColor,
                    primaryColor = primaryColor,
                        onClick = { navController.navigate(Destinations.WebDAVConfig.route) }
                  )
                }
            }

            // 同步加载指示器
            if (isSyncing) {
                Box(
                    modifier = Modifier
                      .fillMaxSize()
                      .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    PCard(
                      variant = CardVariant.Elevated,
                      colors = CardColors(
                        containerColor = cardBackground,
                        contentColor = textColor
                      )
                    ) {
                        Column(
                          modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                          PLoading(
                            size = 32.dp,
                            color = primaryColor,
                          )
                            PText(
                                text = syncMessage,
                              style = MaterialTheme.typography.bodyLarge,
                              color = textColor,
                              fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

    // 密码输入对话框
    if (showPasswordDialog) {
        PasswordInputDialog(
            onDismiss = {
                showPasswordDialog = false
                pendingAction = null
            },
            onConfirm = { password ->
                showPasswordDialog = false
                pendingAction?.let { action ->
                    performSync(
                        scope = scope,
                        syncRepository = syncRepository,
                        action = action,
                        password = password,
                        onStart = {
                            isSyncing = true
                            syncMessage = if (action == SyncAction.UPLOAD) "正在上传..." else "正在下载..."
                        },
                        onComplete = { success, message ->
                            isSyncing = false
                            syncMessage = ""
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            if (success && action == SyncAction.UPLOAD) {
                                scope.launch {
                                    cloudMetadata = syncRepository.getCloudMetadata()
                                }
                            }
                        }
                    )
                }
                pendingAction = null
            }
        )
    }
}

private enum class SyncAction {
    UPLOAD, DOWNLOAD
}

private fun performSync(
    scope: kotlinx.coroutines.CoroutineScope,
    syncRepository: SyncRepositoryImpl,
    action: SyncAction,
    password: String?,
    onStart: () -> Unit,
    onComplete: (success: Boolean, message: String) -> Unit
) {
    onStart()
    scope.launch {
        val result = when (action) {
            SyncAction.UPLOAD -> syncRepository.uploadToCloud(password)
            SyncAction.DOWNLOAD -> syncRepository.downloadFromCloud(password)
        }

        when (result) {
            is SyncResult.Success -> {
                val actionText = if (action == SyncAction.UPLOAD) "上传" else "下载"
                onComplete(true, "${actionText}成功！")
            }
            is SyncResult.Error -> {
                onComplete(false, result.message)
            }
        }
    }
}

/**
 * 未配置提示卡片
 */
@Composable
private fun NotConfiguredCard(
  textColor: Color,
  subTextColor: Color,
  primaryColor: Color,
    onConfigureClick: () -> Unit
) {
    PCard(
        modifier = Modifier
          .fillMaxWidth(),
      variant = CardVariant.Elevated
    ) {
        Column(
            modifier = Modifier
              .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
          Box(
            modifier = Modifier
              .size(80.dp)
              .background(subTextColor.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.CloudOff,
              contentDescription = null,
              modifier = Modifier.size(48.dp),
              tint = subTextColor.copy(alpha = 0.5f)
            )
          }
            PText(
                text = "未配置 WebDAV",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = textColor
            )
            PText(
              text = "请先配置 WebDAV 服务器信息\n才能使用云同步功能",
                style = MaterialTheme.typography.bodyMedium,
              color = subTextColor,
              textAlign = TextAlign.Center,
              lineHeight = 20.sp
            )
          PButton(
            text = "配置 WebDAV",
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp),
            colors = ButtonColors(
              containerColor = primaryColor,
              contentColor = Color.White
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                  modifier = Modifier.size(20.dp)
                )
            },
            onClick = onConfigureClick
          )
        }
    }
}

/**
 * 同步状态卡片
 */
@Composable
private fun SyncStatusCard(
    config: com.eatwhat.data.sync.WebDAVConfig,
    cloudMetadata: SyncMetadata?,
    textColor: Color,
    subTextColor: Color
) {
    PCard(
        modifier = Modifier
          .fillMaxWidth(),
      variant = CardVariant.Elevated
    ) {
        Column(
            modifier = Modifier
              .fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          // Header
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .background(Color(0xFF2196F3).copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFF2196F3),
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            PText(
              text = "同步状态",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = textColor
            )
          }

            // 本地同步状态
          StatusRow(
            label = "上次同步",
            value = config.lastSyncTime?.let { formatTime(it) } ?: "从未同步",
            textColor = textColor,
            subTextColor = subTextColor
          )

            if (config.lastSyncStatus != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                    PText(
                      text = "同步结果",
                        style = MaterialTheme.typography.bodyMedium,
                      color = subTextColor
                    )
                    Row(
                      horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (config.lastSyncStatus == "SUCCESS")
                                Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                          modifier = Modifier.size(18.dp),
                            tint = if (config.lastSyncStatus == "SUCCESS")
                              Color(0xFF4CAF50) else Color(0xFFFF5252)
                        )
                        PText(
                            text = if (config.lastSyncStatus == "SUCCESS") "成功" else "失败",
                          style = MaterialTheme.typography.bodyMedium,
                          color = textColor,
                          fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

          // 分隔线
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(1.dp)
              .background(subTextColor.copy(alpha = 0.1f))
          )

          // 云端状态标题
            PText(
                text = "云端备份",
                style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.SemiBold,
              color = textColor
            )

            if (cloudMetadata != null) {
              StatusRow(
                label = "上传时间",
                value = formatTime(cloudMetadata.uploadTime),
                textColor = textColor,
                subTextColor = subTextColor
              )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                    PText(
                        text = "加密状态",
                        style = MaterialTheme.typography.bodyMedium,
                      color = subTextColor
                    )
                    Row(
                      horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (cloudMetadata.encrypted)
                                Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = null,
                          modifier = Modifier.size(18.dp),
                          tint = if (cloudMetadata.encrypted) Color(0xFF9C27B0) else subTextColor
                        )
                        PText(
                            text = if (cloudMetadata.encrypted) "已加密" else "未加密",
                          style = MaterialTheme.typography.bodyMedium,
                          color = textColor,
                          fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                PText(
                    text = "云端暂无备份",
                    style = MaterialTheme.typography.bodyMedium,
                  color = subTextColor,
                  modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

/**
 * 同步操作卡片
 */
@Composable
private fun SyncActionsCard(
    cloudMetadata: SyncMetadata?,
    encryptionEnabled: Boolean,
    textColor: Color,
    subTextColor: Color,
    primaryColor: Color,
    onUpload: () -> Unit,
    onDownload: () -> Unit
) {
    PCard(
        modifier = Modifier
          .fillMaxWidth(),
      variant = CardVariant.Elevated
    ) {
        Column(
            modifier = Modifier
              .fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          // Header
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .background(Color(0xFF4CAF50).copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.CloudSync,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            PText(
              text = "同步操作",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = textColor
            )
          }

            // 上传按钮
            PButton(
              text = "上传到云端",
              modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
              colors = ButtonColors(
                containerColor = primaryColor,
                contentColor = Color.White
              ),
              leadingIcon = {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                  modifier = Modifier.size(22.dp)
                )
              },
              trailingIcon = if (encryptionEnabled) {
                {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "加密",
                      modifier = Modifier.size(18.dp)
                    )
                }
              } else {
                null
              },
              onClick = onUpload
            )

            // 下载按钮
            PButton(
              text = "从云端恢复",
              modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
              type = ButtonType.OUTLINED,
              disabled = cloudMetadata == null,
              colors = ButtonColors(
                containerColor = Color.Transparent,
                contentColor = primaryColor,
                borderColor = primaryColor
              ),
              leadingIcon = {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = null,
                  modifier = Modifier.size(22.dp)
                )
              },
              trailingIcon = if (cloudMetadata?.encrypted == true) {
                {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "需要密码",
                      modifier = Modifier.size(18.dp)
                    )
                }
              } else {
                null
              },
              onClick = onDownload
            )

            if (cloudMetadata == null) {
                PText(
                    text = "云端暂无备份，无法恢复",
                    style = MaterialTheme.typography.bodySmall,
                  color = subTextColor,
                  modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

/**
 * 配置入口卡片
 */
@Composable
private fun ConfigEntryCard(
  textColor: Color,
  subTextColor: Color,
  primaryColor: Color,
  onClick: () -> Unit
) {
  PCard(
    modifier = Modifier
      .fillMaxWidth(),
    variant = CardVariant.Elevated,
    onClick = onClick
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .background(primaryColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = null,
            tint = primaryColor,
            modifier = Modifier.size(22.dp)
          )
        }
        PText(
          text = "修改配置",
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.Medium,
          color = textColor
        )
      }
      Icon(
        imageVector = Icons.Default.ChevronRight,
        contentDescription = null,
        tint = subTextColor.copy(alpha = 0.5f)
      )
    }
  }
}

/**
 * 状态行组件
 */
@Composable
private fun StatusRow(
  label: String,
  value: String,
  textColor: Color,
  subTextColor: Color
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    PText(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = subTextColor
    )
    PText(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
      color = textColor,
      fontWeight = FontWeight.Medium
    )
    }
}

/**
 * 密码输入对话框
 */
@Composable
private fun PasswordInputDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
  var password by useState("")
  var showPassword by useState(false)

    AlertDialog(
        onDismissRequest = onDismiss,
      title = {
        PText(
          "输入加密密码",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
      },
        text = {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { PText("密码") },
                singleLine = true,
                visualTransformation = if (showPassword)
                    VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword)
                                Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp)
            )
        },
        confirmButton = {
            PButton(
              text = "确定",
              size = ButtonSize.SMALL,
              disabled = password.isBlank(),
              onClick = { onConfirm(password) }
            )
        },
        dismissButton = {
          PButton(
            text = "取消",
            size = ButtonSize.SMALL,
            type = ButtonType.PLAIN,
            onClick = onDismiss
          )
        },
      shape = RoundedCornerShape(20.dp)
    )
}

private fun formatTime(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}
