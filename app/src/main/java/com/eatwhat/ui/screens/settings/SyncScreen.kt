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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.eatwhat.EatWhatApplication
import com.eatwhat.data.database.EatWhatDatabase
import com.eatwhat.data.repository.SyncRepositoryImpl
import com.eatwhat.data.sync.SyncMetadata
import com.eatwhat.data.sync.SyncResult
import com.eatwhat.navigation.Destinations
import com.eatwhat.ui.theme.LocalDarkTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 同步页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
  val isDark = LocalDarkTheme.current

    // 创建 Repository
    val database = remember { EatWhatDatabase.getInstance(context) }
  val app = context.applicationContext as EatWhatApplication
  val exportRepository = remember { app.exportRepository }
    val syncRepository = remember { SyncRepositoryImpl(context, exportRepository) }

    // 配置状态
    val config = remember { syncRepository.getConfig() }
    val isConfigured = config != null

    // UI 状态
    var isSyncing by remember { mutableStateOf(false) }
    var syncMessage by remember { mutableStateOf("") }
    var cloudMetadata by remember { mutableStateOf<SyncMetadata?>(null) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<SyncAction?>(null) }

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
                    Text(
                        text = "数据同步",
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
                  .verticalScroll(rememberScrollState())
                  .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!isConfigured) {
                    // 未配置提示
                    NotConfiguredCard(
                        isDark = isDark,
                        onConfigureClick = {
                            navController.navigate(Destinations.WebDAVConfig.route)
                        }
                    )
                } else {
                    // 同步状态卡片
                    SyncStatusCard(
                        config = config!!,
                        cloudMetadata = cloudMetadata,
                        isDark = isDark
                    )

                    // 同步操作卡片
                    SyncActionsCard(
                        isDark = isDark,
                        cloudMetadata = cloudMetadata,
                        encryptionEnabled = config.encryptionEnabled,
                        onUpload = {
                            // 直接使用配置中保存的加密密码
                            performSync(
                                scope = scope,
                                syncRepository = syncRepository,
                                action = SyncAction.UPLOAD,
                                password = if (config.encryptionEnabled) config.encryptionPassword else null,
                                onStart = {
                                    isSyncing = true
                                    syncMessage = "正在上传..."
                                },
                                onComplete = { success, message ->
                                    isSyncing = false
                                    syncMessage = ""
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    if (success) {
                                        // 刷新元数据
                                        scope.launch {
                                            cloudMetadata = syncRepository.getCloudMetadata()
                                        }
                                    }
                                }
                            )
                        },
                        onDownload = {
                            if (cloudMetadata?.encrypted == true) {
                                // 下载加密数据时，如果本地也配置了加密且密码一致，直接使用
                                if (config.encryptionEnabled && config.encryptionPassword != null) {
                                    performSync(
                                        scope = scope,
                                        syncRepository = syncRepository,
                                        action = SyncAction.DOWNLOAD,
                                        password = config.encryptionPassword,
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
                                    // 云端加密但本地未配置密码，需要输入
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

                    // 配置入口
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
                        ),
                        onClick = { navController.navigate(Destinations.WebDAVConfig.route) }
                    ) {
                        Row(
                            modifier = Modifier
                              .fillMaxWidth()
                              .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "修改配置",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            // 同步加载指示器
            if (isSyncing) {
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
                                text = syncMessage,
                                style = MaterialTheme.typography.bodyMedium
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
    isDark: Boolean,
    onConfigureClick: () -> Unit
) {
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
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = "未配置 WebDAV",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "请先配置 WebDAV 服务器信息才能使用云同步功能",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(onClick = onConfigureClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("配置 WebDAV")
            }
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
    isDark: Boolean
) {
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
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "同步状态",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            // 本地同步状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "上次同步",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = config.lastSyncTime?.let { formatTime(it) } ?: "从未同步",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (config.lastSyncStatus != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "同步状态",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (config.lastSyncStatus == "SUCCESS")
                                Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (config.lastSyncStatus == "SUCCESS")
                                MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = if (config.lastSyncStatus == "SUCCESS") "成功" else "失败",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Divider()

            // 云端状态
            Text(
                text = "云端备份",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            if (cloudMetadata != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "上传时间",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatTime(cloudMetadata.uploadTime),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "加密状态",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (cloudMetadata.encrypted)
                                Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (cloudMetadata.encrypted) "已加密" else "未加密",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                Text(
                    text = "云端暂无备份",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
    isDark: Boolean,
    cloudMetadata: SyncMetadata?,
    encryptionEnabled: Boolean,
    onUpload: () -> Unit,
    onDownload: () -> Unit
) {
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
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "同步操作",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            // 上传按钮
            Button(
                onClick = onUpload,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("上传到云端")
                if (encryptionEnabled) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "加密",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // 下载按钮
            OutlinedButton(
                onClick = onDownload,
                enabled = cloudMetadata != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("从云端恢复")
                if (cloudMetadata?.encrypted == true) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "需要密码",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (cloudMetadata == null) {
                Text(
                    text = "云端暂无备份，无法恢复",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("输入加密密码") },
        text = {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("密码") },
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
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(password) },
                enabled = password.isNotBlank()
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

private fun formatTime(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}
