package com.eatwhat.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.eatwhat.data.database.EatWhatDatabase
import com.eatwhat.data.repository.ExportRepositoryImpl
import com.eatwhat.data.repository.SyncRepositoryImpl
import com.eatwhat.data.sync.ConnectionResult
import com.eatwhat.data.sync.SyncWorker
import com.eatwhat.data.sync.WebDAVConfig
import kotlinx.coroutines.launch

/**
 * WebDAV 配置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebDAVConfigScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isDark = isSystemInDarkTheme()

    // 创建 Repository
    val database = remember { EatWhatDatabase.getInstance(context) }
    val exportRepository = remember { ExportRepositoryImpl(context, database) }
    val syncRepository = remember { SyncRepositoryImpl(context, exportRepository) }

    // 加载现有配置
    val existingConfig = remember { syncRepository.getConfig() }

    // 表单状态
    var serverUrl by remember { mutableStateOf(existingConfig?.serverUrl ?: "") }
    var username by remember { mutableStateOf(existingConfig?.username ?: "") }
    var password by remember { mutableStateOf(existingConfig?.password ?: "") }
    var remotePath by remember { mutableStateOf(existingConfig?.remotePath ?: "/EatWhat/") }
    var encryptionEnabled by remember { mutableStateOf(existingConfig?.encryptionEnabled ?: false) }
    var encryptionPassword by remember { mutableStateOf(existingConfig?.encryptionPassword ?: "") }
    var autoSyncEnabled by remember { mutableStateOf(existingConfig?.autoSyncEnabled ?: false) }
    var syncIntervalMinutes by remember { mutableStateOf(existingConfig?.syncIntervalMinutes ?: 60) }

    // UI 状态
    var showPassword by remember { mutableStateOf(false) }
    var showEncryptionPassword by remember { mutableStateOf(false) }
    var isTesting by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // 验证状态
    val isFormValid = serverUrl.isNotBlank() && username.isNotBlank() && password.isNotBlank()
    val isEncryptionValid = !encryptionEnabled || encryptionPassword.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "WebDAV 配置",
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
                actions = {
                    // 保存按钮
                    TextButton(
                        onClick = {
                            if (isFormValid && isEncryptionValid) {
                                isSaving = true
                                val config = WebDAVConfig(
                                    serverUrl = serverUrl.trim(),
                                    username = username.trim(),
                                    password = password,
                                    remotePath = remotePath.trim().ifBlank { "/EatWhat/" },
                                    encryptionEnabled = encryptionEnabled,
                                    encryptionPassword = if (encryptionEnabled) encryptionPassword else null,
                                    lastSyncTime = existingConfig?.lastSyncTime,
                                    lastSyncStatus = existingConfig?.lastSyncStatus,
                                    autoSyncEnabled = autoSyncEnabled,
                                    syncIntervalMinutes = syncIntervalMinutes
                                )
                                syncRepository.saveConfig(config)

                                // 根据自动同步设置调度或取消 WorkManager
                                if (autoSyncEnabled) {
                                    SyncWorker.schedule(context, syncIntervalMinutes)
                                } else {
                                    SyncWorker.cancel(context)
                                }

                                isSaving = false
                                Toast.makeText(context, "配置已保存", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                        },
                        enabled = isFormValid && isEncryptionValid && !isSaving
                    ) {
                        Text("保存")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 服务器配置卡片
            ConfigCard(
                title = "服务器设置",
                isDark = isDark
            ) {
                // 服务器地址
                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { serverUrl = it },
                    label = { Text("服务器地址") },
                    placeholder = { Text("https://example.com/dav") },
                    leadingIcon = {
                        Icon(Icons.Default.Cloud, contentDescription = null)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 用户名
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("用户名") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 密码
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("密码") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null)
                    },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showPassword) "隐藏密码" else "显示密码"
                            )
                        }
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 远程路径
                OutlinedTextField(
                    value = remotePath,
                    onValueChange = { remotePath = it },
                    label = { Text("远程目录") },
                    placeholder = { Text("/EatWhat/") },
                    leadingIcon = {
                        Icon(Icons.Default.Folder, contentDescription = null)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 测试连接按钮
                Button(
                    onClick = {
                        if (isFormValid) {
                            isTesting = true
                            scope.launch {
                                val testConfig = WebDAVConfig(
                                    serverUrl = serverUrl.trim(),
                                    username = username.trim(),
                                    password = password,
                                    remotePath = remotePath.trim().ifBlank { "/EatWhat/" }
                                )
                                val result = syncRepository.testConnection(testConfig)
                                isTesting = false

                                when (result) {
                                    is ConnectionResult.Success -> {
                                        Toast.makeText(context, "连接成功！", Toast.LENGTH_SHORT).show()
                                    }
                                    is ConnectionResult.Error -> {
                                        Toast.makeText(context, "连接失败: ${result.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                    },
                    enabled = isFormValid && !isTesting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (isTesting) "正在测试..." else "测试连接")
                }
            }

            // 加密设置卡片
            ConfigCard(
                title = "数据加密",
                isDark = isDark
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "启用加密",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "使用密码加密云端数据",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = encryptionEnabled,
                        onCheckedChange = { encryptionEnabled = it }
                    )
                }

                if (encryptionEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = encryptionPassword,
                        onValueChange = { encryptionPassword = it },
                        label = { Text("加密密码") },
                        leadingIcon = {
                            Icon(Icons.Default.Key, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(onClick = { showEncryptionPassword = !showEncryptionPassword }) {
                                Icon(
                                    imageVector = if (showEncryptionPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showEncryptionPassword) "隐藏密码" else "显示密码"
                                )
                            }
                        },
                        visualTransformation = if (showEncryptionPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        isError = encryptionEnabled && encryptionPassword.isBlank(),
                        supportingText = if (encryptionEnabled && encryptionPassword.isBlank()) {
                            { Text("请输入加密密码") }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 警告提示
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "请牢记加密密码！忘记密码将无法恢复云端数据。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // 自动同步设置卡片
            ConfigCard(
                title = "自动同步",
                isDark = isDark
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "启用自动同步",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "定期自动同步数据到云端",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = autoSyncEnabled,
                        onCheckedChange = { autoSyncEnabled = it }
                    )
                }

                if (autoSyncEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "同步间隔",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 同步间隔选项 - 使用两行显示
                    val intervalOptions = listOf(
                        15 to "15分钟",
                        30 to "30分钟",
                        60 to "1小时",
                        120 to "2小时",
                        360 to "6小时",
                        720 to "12小时"
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            intervalOptions.take(3).forEach { (interval, label) ->
                                FilterChip(
                                    selected = syncIntervalMinutes == interval,
                                    onClick = { syncIntervalMinutes = interval },
                                    label = { Text(label, style = MaterialTheme.typography.bodySmall) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            intervalOptions.drop(3).forEach { (interval, label) ->
                                FilterChip(
                                    selected = syncIntervalMinutes == interval,
                                    onClick = { syncIntervalMinutes = interval },
                                    label = { Text(label, style = MaterialTheme.typography.bodySmall) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 智能合并说明
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "自动同步采用智能合并策略，根据时间戳自动合并本地与云端数据。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // 危险操作区域
            if (existingConfig != null) {
                ConfigCard(
                    title = "危险操作",
                    isDark = isDark
                ) {
                    OutlinedButton(
                        onClick = {
                            syncRepository.clearConfig()
                            SyncWorker.cancel(context)
                            Toast.makeText(context, "配置已清除", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("清除配置")
                    }
                }
            }
        }
    }
}

/**
 * 配置卡片容器
 */
@Composable
private fun ConfigCard(
    title: String,
    isDark: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

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
                    .padding(16.dp)
            ) {
                content()
            }
        }
    }
}
