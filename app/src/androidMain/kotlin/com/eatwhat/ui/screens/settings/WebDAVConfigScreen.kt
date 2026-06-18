package com.eatwhat.ui.screens.settings

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.eatwhat.EatWhatApplication
import com.eatwhat.data.database.EatWhatDatabase
import com.eatwhat.data.repository.SyncRepositoryImpl
import com.eatwhat.data.sync.ConnectionResult
import com.eatwhat.data.sync.SyncWorker
import com.eatwhat.data.sync.WebDAVConfig
import com.eatwhat.ui.components.StyledTextField
import com.eatwhat.ui.theme.LocalDarkTheme
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.getValue
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.palette.components.alert.AlertType
import xyz.junerver.compose.palette.components.alert.PAlert
import xyz.junerver.compose.palette.components.button.ButtonColors
import xyz.junerver.compose.palette.components.button.ButtonType
import xyz.junerver.compose.palette.components.button.PButton
import xyz.junerver.compose.palette.components.card.CardVariant
import xyz.junerver.compose.palette.components.card.PCard
import xyz.junerver.compose.palette.components.segmented.PSegmented
import xyz.junerver.compose.palette.components.segmented.SegmentedOption
import xyz.junerver.compose.palette.components.switch.PSwitch
import xyz.junerver.compose.palette.components.text.PText

/**
 * WebDAV 配置页面 - 美化版本
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebDAVConfigScreen(navController: NavController) {
  val TAG = "WebDAVConfigScreen"
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val isDark = LocalDarkTheme.current

  // 创建 Repository
  val database by useCreation { EatWhatDatabase.getInstance(context) }
  val app = context.applicationContext as EatWhatApplication
  val exportRepository by useCreation { app.exportRepository }
  val syncRepository by useCreation { SyncRepositoryImpl(context, exportRepository) }

  // 加载现有配置
  val existingConfig by useCreation { syncRepository.getConfig() }

  // 表单状态
  var serverUrl by useState(existingConfig?.serverUrl ?: "")
  var username by useState(existingConfig?.username ?: "")
  var password by useState(existingConfig?.password ?: "")
  var remotePath by useState(existingConfig?.remotePath ?: "/EatWhat/")
  var encryptionEnabled by useState(existingConfig?.encryptionEnabled ?: false)
  var encryptionPassword by useState(existingConfig?.encryptionPassword ?: "")
  var autoSyncEnabled by useState(existingConfig?.autoSyncEnabled ?: false)
  var syncIntervalMinutes by useState(existingConfig?.syncIntervalMinutes ?: 60)

  // UI 状态
  var showPassword by useState(false)
  var showEncryptionPassword by useState(false)
  var isTesting by useState(false)
  var isSaving by useState(false)
  var testSuccess by useState(false)
  var testMessage by useState("") 

  // 验证状态
  val isFormValid = serverUrl.isNotBlank() && username.isNotBlank() && password.isNotBlank()
  val isEncryptionValid = !encryptionEnabled || encryptionPassword.isNotBlank()

  // 主题颜色
  val pageBackground = if (isDark) MaterialTheme.colorScheme.background else Color(0xFFF5F5F5)
  val inputBackground = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF8F8F8)
  val textColor = if (isDark) MaterialTheme.colorScheme.onSurface else Color.Black
  val subTextColor = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color.Gray
  val primaryColor = Color(0xFFFF6B35)

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          PText(
            text = "WebDAV 配置",
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
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // 服务器配置卡片
      SectionCard(
        title = "服务器设置",
        icon = Icons.Default.Cloud,
        iconColor = Color(0xFF2196F3),
        textColor = textColor
      ) {
        // 服务器地址
        StyledTextField(
          value = serverUrl,
          onValueChange = { serverUrl = it },
          label = "服务器地址",
          placeholder = "https://example.com/dav",
          backgroundColor = inputBackground,
          textColor = textColor,
          placeholderColor = subTextColor
        )

        // 用户名
        StyledTextField(
          value = username,
          onValueChange = { username = it },
          label = "用户名",
          placeholder = "your_username",
          backgroundColor = inputBackground,
          textColor = textColor,
          placeholderColor = subTextColor
        )

        // 密码
        StyledTextField(
          value = password,
          onValueChange = { password = it },
          label = "密码",
          placeholder = "your_password",
          isPassword = !showPassword,
          backgroundColor = inputBackground,
          textColor = textColor,
          placeholderColor = subTextColor,
          trailingIcon = {
            Icon(
              imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
              contentDescription = if (showPassword) "隐藏密码" else "显示密码",
              tint = subTextColor,
              modifier = Modifier
                .size(24.dp)
                .clickable { showPassword = !showPassword }
            )
          }
        )

        // 远程路径
        StyledTextField(
          value = remotePath,
          onValueChange = { remotePath = it },
          label = "远程目录",
          placeholder = "/EatWhat/",
          backgroundColor = inputBackground,
          textColor = textColor,
          placeholderColor = subTextColor
        )

        // 测试连接按钮
        PButton(
          text = if (isTesting) "正在测试..." else "测试连接",
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
          disabled = !isFormValid,
          loading = isTesting,
          colors = ButtonColors(
            containerColor = primaryColor,
            contentColor = Color.White
          ),
          leadingIcon = {
            Icon(
              Icons.Default.CheckCircle,
              contentDescription = null,
              modifier = Modifier.size(20.dp)
            )
          },
          onClick = {
            if (isFormValid) {
              isTesting = true
              testSuccess = false
              testMessage = ""
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
                    testSuccess = true
                    testMessage = "连接成功！"
                  }
                  is ConnectionResult.Error -> {
                    testSuccess = false
                    testMessage = result.message
                    Log.d(TAG, result.message)
                  }
                }
              }
            }
          }
        )

        // 测试结果显示
        if (testMessage.isNotBlank()) {
          Spacer(modifier = Modifier.height(12.dp))
          PAlert(
            message = if (testSuccess) "连接成功" else "连接失败",
            description = testMessage,
            type = if (testSuccess) AlertType.Success else AlertType.Error
          )
        }
      }

      // 加密设置卡片
      SectionCard(
        title = "数据加密",
        icon = Icons.Default.Security,
        iconColor = Color(0xFF9C27B0),
        textColor = textColor
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            PText(
              text = "启用加密",
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = FontWeight.Medium,
              color = textColor
            )
            PText(
              text = "使用密码加密云端数据",
              style = MaterialTheme.typography.bodySmall,
              color = subTextColor
            )
          }
          PSwitch(
            checked = encryptionEnabled,
            onChange = { encryptionEnabled = it }
          )
        }

        if (encryptionEnabled) {

          StyledTextField(
            value = encryptionPassword,
            onValueChange = { encryptionPassword = it },
            label = "加密密码",
            placeholder = "请输入加密密码",
            isPassword = !showEncryptionPassword,
            backgroundColor = inputBackground,
            textColor = textColor,
            placeholderColor = subTextColor,
            trailingIcon = {
              Icon(
                imageVector = if (showEncryptionPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = if (showEncryptionPassword) "隐藏密码" else "显示密码",
                tint = subTextColor,
                modifier = Modifier
                  .size(24.dp)
                  .clickable { showEncryptionPassword = !showEncryptionPassword }
              )
            }
          )

          if (encryptionPassword.isBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            PText(
              text = "请输入加密密码",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.error
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // 警告提示
          PAlert(
            message = "请牢记加密密码",
            description = "忘记密码将无法恢复云端数据。",
            type = AlertType.Warning
          )
        }
      }

      // 自动同步设置卡片
      SectionCard(
        title = "自动同步",
        icon = Icons.Default.Sync,
        iconColor = Color(0xFF4CAF50),
        textColor = textColor
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            PText(
              text = "启用自动同步",
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = FontWeight.Medium,
              color = textColor
            )
            PText(
              text = "定期自动同步数据到云端",
              style = MaterialTheme.typography.bodySmall,
              color = subTextColor
            )
          }
          PSwitch(
            checked = autoSyncEnabled,
            onChange = { autoSyncEnabled = it }
          )
        }

        if (autoSyncEnabled) {
          Spacer(modifier = Modifier.height(12.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              Icons.Default.Schedule,
              contentDescription = null,
              tint = subTextColor,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            PText(
              text = "同步间隔",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Medium,
              color = textColor
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // 同步间隔选项
          val intervalOptions = listOf(
            15 to "15分钟",
            30 to "30分钟",
            60 to "1小时",
            120 to "2小时",
            360 to "6小时",
            720 to "12小时"
          )

          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PSegmented(
              options = intervalOptions.take(3).map { (interval, label) ->
                SegmentedOption(interval.toString(), label)
              },
              value = syncIntervalMinutes.toString(),
              onValueChange = { syncIntervalMinutes = it.toInt() },
              modifier = Modifier.fillMaxWidth(),
            )
            PSegmented(
              options = intervalOptions.drop(3).map { (interval, label) ->
                SegmentedOption(interval.toString(), label)
              },
              value = syncIntervalMinutes.toString(),
              onValueChange = { syncIntervalMinutes = it.toInt() },
              modifier = Modifier.fillMaxWidth(),
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // 智能合并说明
          PAlert(
            message = "智能合并",
            description = "自动同步会根据时间戳合并本地与云端数据。",
            type = AlertType.Info
          )
        }
      }

      // 危险操作区域
      if (existingConfig != null) {
        SectionCard(
          title = "危险操作",
          icon = Icons.Default.Delete,
          iconColor = Color(0xFFFF5252),
          textColor = textColor
        ) {
          PButton(
            text = "清除配置",
            modifier = Modifier.fillMaxWidth(),
            type = ButtonType.OUTLINED,
            colors = ButtonColors(
              containerColor = Color.Transparent,
              contentColor = Color(0xFFFF5252),
              borderColor = Color(0xFFFF5252)
            ),
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
            },
            onClick = {
              syncRepository.clearConfig()
              SyncWorker.cancel(context)
              Toast.makeText(context, "配置已清除", Toast.LENGTH_SHORT).show()
              navController.popBackStack()
            }
          )
        }
      }

      Spacer(modifier = Modifier.weight(1f))

      // 保存按钮
      PButton(
        text = if (isSaving) "保存中..." else "保存配置",
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp),
        disabled = !isFormValid || !isEncryptionValid,
        loading = isSaving,
        colors = ButtonColors(
          containerColor = primaryColor,
          contentColor = Color.White
        ),
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
        }
      )
    }
  }
}

/**
 * 统一的分区卡片组件
 */
@Composable
private fun SectionCard(
  title: String,
  icon: ImageVector,
  iconColor: Color,
  textColor: Color,
  content: @Composable () -> Unit
) {
  PCard(
    modifier = Modifier
      .fillMaxWidth(),
    variant = CardVariant.Elevated
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Header
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .background(iconColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        PText(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = textColor
        )
      }

      content()
    }
  }
}
