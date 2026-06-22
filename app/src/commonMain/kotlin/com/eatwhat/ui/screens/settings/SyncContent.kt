package com.eatwhat.ui.screens.settings

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eatwhat.data.sync.SyncMetadata
import com.eatwhat.data.sync.SyncResult
import com.eatwhat.data.sync.WebDAVConfig
import com.eatwhat.ui.components.AppToolbar
import com.eatwhat.ui.components.PaletteDialogActionDivider
import com.eatwhat.ui.components.PaletteDialogCancelAction
import com.eatwhat.ui.components.PaletteDialogConfirmAction
import com.eatwhat.ui.components.StyledTextField
import com.eatwhat.ui.theme.LocalDarkTheme
import com.eatwhat.ui.theme.PrimaryOrange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks._useState
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.palette.components.button.ButtonColors
import xyz.junerver.compose.palette.components.button.ButtonSize
import xyz.junerver.compose.palette.components.button.ButtonType
import xyz.junerver.compose.palette.components.button.PButton
import xyz.junerver.compose.palette.components.card.CardColors
import xyz.junerver.compose.palette.components.card.CardVariant
import xyz.junerver.compose.palette.components.card.PCard
import xyz.junerver.compose.palette.components.dialog.PDialog
import xyz.junerver.compose.palette.components.loading.PLoading
import xyz.junerver.compose.palette.components.scaffold.PScaffold
import xyz.junerver.compose.palette.components.scaffold.ScaffoldDefaults
import xyz.junerver.compose.palette.components.text.PText

@Composable
fun SyncContent(
    config: WebDAVConfig?,
    formatTimestamp: (Long) -> String,
    onNavigateUp: () -> Unit,
    onConfigureClick: () -> Unit,
    onLoadCloudMetadata: suspend () -> SyncMetadata?,
    onUploadToCloud: suspend (String?) -> SyncResult,
    onDownloadFromCloud: suspend (String?) -> SyncResult,
    onSyncComplete: (success: Boolean, message: String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val isDark = LocalDarkTheme.current

    var isSyncing by useState(false)
    var syncMessage by useState("")
    var cloudMetadata by _useState<SyncMetadata?>(null)
    var showPasswordDialog by useState(false)
    var pendingAction by _useState<SyncAction?>(null)

    val pageBackground = if (isDark) MaterialTheme.colorScheme.background else Color(0xFFF5F5F5)
    val cardBackground = if (isDark) MaterialTheme.colorScheme.surface else Color.White
    val textColor = if (isDark) MaterialTheme.colorScheme.onSurface else Color.Black
    val subTextColor = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color.Gray

    LaunchedEffect(config) {
        if (config != null) {
            cloudMetadata = onLoadCloudMetadata()
        }
    }

    PScaffold(
        topBar = {
            AppToolbar(
                title = "数据同步",
                containerColor = Color.Transparent,
                onNavigateUp = onNavigateUp
            )
        },
        colors = ScaffoldDefaults.colors(
            containerColor = pageBackground
        )
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
                if (config == null) {
                    NotConfiguredCard(
                        textColor = textColor,
                        subTextColor = subTextColor,
                        onConfigureClick = onConfigureClick
                    )
                } else {
                    SyncStatusCard(
                        config = config,
                        cloudMetadata = cloudMetadata,
                        formatTimestamp = formatTimestamp,
                        textColor = textColor,
                        subTextColor = subTextColor
                    )

                    SyncActionsCard(
                        cloudMetadata = cloudMetadata,
                        encryptionEnabled = config.encryptionEnabled,
                        textColor = textColor,
                        subTextColor = subTextColor,
                        onUpload = {
                            performSync(
                                scope = scope,
                                action = SyncAction.UPLOAD,
                                password = if (config.encryptionEnabled) config.encryptionPassword else null,
                                runSync = onUploadToCloud,
                                onStart = {
                                    isSyncing = true
                                    syncMessage = "正在上传..."
                                },
                                onComplete = { success, message ->
                                    isSyncing = false
                                    syncMessage = ""
                                    onSyncComplete(success, message)
                                    if (success) {
                                        scope.launch {
                                            cloudMetadata = onLoadCloudMetadata()
                                        }
                                    }
                                }
                            )
                        },
                        onDownload = {
                            if (cloudMetadata?.encrypted == true) {
                                if (config.encryptionEnabled && config.encryptionPassword != null) {
                                    performSync(
                                        scope = scope,
                                        action = SyncAction.DOWNLOAD,
                                        password = config.encryptionPassword,
                                        runSync = onDownloadFromCloud,
                                        onStart = {
                                            isSyncing = true
                                            syncMessage = "正在下载..."
                                        },
                                        onComplete = { success, message ->
                                            isSyncing = false
                                            syncMessage = ""
                                            onSyncComplete(success, message)
                                        }
                                    )
                                } else {
                                    pendingAction = SyncAction.DOWNLOAD
                                    showPasswordDialog = true
                                }
                            } else {
                                performSync(
                                    scope = scope,
                                    action = SyncAction.DOWNLOAD,
                                    password = null,
                                    runSync = onDownloadFromCloud,
                                    onStart = {
                                        isSyncing = true
                                        syncMessage = "正在下载..."
                                    },
                                    onComplete = { success, message ->
                                        isSyncing = false
                                        syncMessage = ""
                                        onSyncComplete(success, message)
                                    }
                                )
                            }
                        }
                    )

                    ConfigEntryCard(
                        textColor = textColor,
                        subTextColor = subTextColor,
                        onClick = onConfigureClick
                    )
                }
            }

            if (isSyncing) {
                SyncLoadingOverlay(
                    syncMessage = syncMessage,
                    cardBackground = cardBackground,
                    textColor = textColor
                )
            }
        }
    }

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
                        action = action,
                        password = password,
                        runSync = when (action) {
                            SyncAction.UPLOAD -> onUploadToCloud
                            SyncAction.DOWNLOAD -> onDownloadFromCloud
                        },
                        onStart = {
                            isSyncing = true
                            syncMessage = if (action == SyncAction.UPLOAD) "正在上传..." else "正在下载..."
                        },
                        onComplete = { success, message ->
                            isSyncing = false
                            syncMessage = ""
                            onSyncComplete(success, message)
                            if (success && action == SyncAction.UPLOAD) {
                                scope.launch {
                                    cloudMetadata = onLoadCloudMetadata()
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
    scope: CoroutineScope,
    action: SyncAction,
    password: String?,
    runSync: suspend (String?) -> SyncResult,
    onStart: () -> Unit,
    onComplete: (success: Boolean, message: String) -> Unit
) {
    onStart()
    scope.launch {
        when (val result = runSync(password)) {
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

@Composable
private fun NotConfiguredCard(
    textColor: Color,
    subTextColor: Color,
    onConfigureClick: () -> Unit
) {
    PCard(
        modifier = Modifier.fillMaxWidth(),
        variant = CardVariant.Elevated
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
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
                    containerColor = PrimaryOrange,
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

@Composable
private fun SyncStatusCard(
    config: WebDAVConfig,
    cloudMetadata: SyncMetadata?,
    formatTimestamp: (Long) -> String,
    textColor: Color,
    subTextColor: Color
) {
    PCard(
        modifier = Modifier.fillMaxWidth(),
        variant = CardVariant.Elevated
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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

            StatusRow(
                label = "上次同步",
                value = config.lastSyncTime?.let(formatTimestamp) ?: "从未同步",
                textColor = textColor,
                subTextColor = subTextColor
            )

            if (config.lastSyncStatus != null) {
                SyncResultRow(
                    lastSyncStatus = config.lastSyncStatus,
                    textColor = textColor,
                    subTextColor = subTextColor
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(subTextColor.copy(alpha = 0.1f))
            )

            PText(
                text = "云端备份",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )

            if (cloudMetadata != null) {
                StatusRow(
                    label = "上传时间",
                    value = formatTimestamp(cloudMetadata.uploadTime),
                    textColor = textColor,
                    subTextColor = subTextColor
                )
                CloudEncryptionRow(
                    encrypted = cloudMetadata.encrypted,
                    textColor = textColor,
                    subTextColor = subTextColor
                )
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

@Composable
private fun SyncResultRow(
    lastSyncStatus: String,
    textColor: Color,
    subTextColor: Color
) {
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
            val isSuccess = lastSyncStatus == "SUCCESS"
            Icon(
                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (isSuccess) Color(0xFF4CAF50) else Color(0xFFFF5252)
            )
            PText(
                text = if (isSuccess) "成功" else "失败",
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CloudEncryptionRow(
    encrypted: Boolean,
    textColor: Color,
    subTextColor: Color
) {
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
                imageVector = if (encrypted) Icons.Default.Lock else Icons.Default.LockOpen,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (encrypted) Color(0xFF9C27B0) else subTextColor
            )
            PText(
                text = if (encrypted) "已加密" else "未加密",
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SyncActionsCard(
    cloudMetadata: SyncMetadata?,
    encryptionEnabled: Boolean,
    textColor: Color,
    subTextColor: Color,
    onUpload: () -> Unit,
    onDownload: () -> Unit
) {
    PCard(
        modifier = Modifier.fillMaxWidth(),
        variant = CardVariant.Elevated
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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

            PButton(
                text = "上传到云端",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonColors(
                    containerColor = PrimaryOrange,
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

            PButton(
                text = "从云端恢复",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                type = ButtonType.OUTLINED,
                disabled = cloudMetadata == null,
                colors = ButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = PrimaryOrange,
                    borderColor = PrimaryOrange
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

@Composable
private fun ConfigEntryCard(
    textColor: Color,
    subTextColor: Color,
    onClick: () -> Unit
) {
    PCard(
        modifier = Modifier.fillMaxWidth(),
        variant = CardVariant.Elevated,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                        .background(PrimaryOrange.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = PrimaryOrange,
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

@Composable
private fun SyncLoadingOverlay(
    syncMessage: String,
    cardBackground: Color,
    textColor: Color
) {
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
                    color = PrimaryOrange
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

@Composable
private fun PasswordInputDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by useState("")
    var showPassword by useState(false)

    PDialog(
        onDismiss = onDismiss,
        title = {
            PText(
                "输入加密密码",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 24.dp, end = 24.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        content = {
            StyledTextField(
                value = password,
                onValueChange = { password = it },
                label = "密码",
                isPassword = !showPassword,
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword)
                                Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
        },
        actions = {
            PaletteDialogCancelAction(onClick = onDismiss)
            PaletteDialogActionDivider()
            PaletteDialogConfirmAction(
                color = if (password.isBlank()) {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                } else {
                    null
                },
                onClick = {
                    if (password.isNotBlank()) {
                        onConfirm(password)
                    }
                }
            )
        }
    )
}
