package com.eatwhat.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.eatwhat.data.database.EatWhatDatabase
import com.eatwhat.data.database.entities.AIProviderEntity
import com.eatwhat.domain.model.AIConfig
import com.eatwhat.data.repository.AIProviderRepository
import com.eatwhat.domain.service.AIService
import com.eatwhat.domain.service.OpenAIService
import com.eatwhat.ui.components.AppToolbar
import com.eatwhat.ui.components.PaletteConfirmDialog
import com.eatwhat.ui.components.StyledTextField
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.getValue
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.compose.hooks.useTimeoutFn
import xyz.junerver.compose.palette.components.alert.AlertType
import xyz.junerver.compose.palette.components.alert.PAlert
import xyz.junerver.compose.palette.components.button.ButtonType
import xyz.junerver.compose.palette.components.card.CardVariant
import xyz.junerver.compose.palette.components.card.PCard
import xyz.junerver.compose.palette.components.loading.PLoading
import xyz.junerver.compose.palette.components.tag.PTag
import xyz.junerver.compose.palette.components.tag.TagColors
import xyz.junerver.compose.palette.components.tag.TagSize
import xyz.junerver.compose.palette.components.tag.TagVariant
import xyz.junerver.compose.palette.components.text.PText
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AIProviderEditScreen(navController: NavController, providerId: Long? = null) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val database by useCreation { EatWhatDatabase.getInstance(context) }
  val repository by useCreation { AIProviderRepository(database.aiProviderDao()) }
  val openAIService: AIService by useCreation { OpenAIService() }
  val focusManager = LocalFocusManager.current

  // Form state
  val (name, setName) = useGetState(default = "")
  val (baseUrl, setBaseUrl) = useGetState(default = "")
  val (apiKey, setApiKey) = useGetState(default = "")
  val (model, setModel) = useGetState(default = "")
  val (isActive, setIsActive) = useGetState(default = false)

  // Model fetch & test state
  val (availableModels, setAvailableModels) = useGetState(default = emptyList<String>())
  val (isFetchingModels, setIsFetchingModels) = useGetState(default = false)
  val (testResult, setTestResult) = useGetState(default = "")
  val (isTesting, setIsTesting) = useGetState(default = false)
  val (testSuccess, setTestSuccess) = useGetState(default = false)
  val (isApiKeyVisible, setApiKeyVisible) = useGetState(default = false)
  val (showDeleteDialog, setShowDeleteDialog) = useGetState(default = false)

  // Dark mode support
  val isDark = com.eatwhat.ui.theme.LocalDarkTheme.current
  val pageBackground = if (isDark) MaterialTheme.colorScheme.background else Color(0xFFF5F5F5)
  val inputBackground = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF8F8F8)
  val textColor = if (isDark) MaterialTheme.colorScheme.onSurface else Color.Black
  val subTextColor = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color.Gray

  // Load initial state if editing
  useEffect(providerId) {
    if (providerId != null) {
      scope.launch {
        val provider = repository.getProviderById(providerId).first()
        if (provider != null) {
          setName(provider.name)
          setBaseUrl(provider.baseUrl)
          setApiKey(provider.apiKey)
          setModel(provider.model)
          setIsActive(provider.isActive)
        }
      }
    }
  }

  // Auto-hide test result after 3 seconds if successful
  useTimeoutFn({
    if (testSuccess.value && testResult.value.isNotBlank()) {
      setTestResult("")
      setTestSuccess(false)
    }
  }, 2.seconds)

  // Fetch models
  val fetchModels = {
    if (baseUrl.value.isNotBlank() && apiKey.value.isNotBlank()) {
      setIsFetchingModels(true)
      setTestResult("")
      setTestSuccess(false)
      scope.launch {
        val config = AIConfig(baseUrl.value, apiKey.value, model.value)
        val result = openAIService.fetchModels(config)
        setIsFetchingModels(false)
        result.onSuccess {
          setAvailableModels(it)
          // Don't auto-open dropdown, just show the models card below
        }.onFailure {
          setTestResult("获取模型列表失败: ${it.message}")
          setTestSuccess(false)
        }
      }
    }
  }

  // Test connection
  val testConnection = {
    if (baseUrl.value.isNotBlank() && apiKey.value.isNotBlank()) {
      setIsTesting(true)
      setTestResult("")
      setTestSuccess(false)
      scope.launch {
        val config = AIConfig(baseUrl.value, apiKey.value, model.value)
        val result = openAIService.testConnection(config)
        setIsTesting(false)
        result.onSuccess {
          if (it.isSuccess) {
            setTestResult("Success! Latency: ${it.latencyMs}ms\nResponse: ${it.message.take(50)}...")
            setTestSuccess(true)
          } else {
            setTestResult("Test failed: ${it.message}")
            setTestSuccess(false)
          }
        }.onFailure {
          setTestResult("Test failed: ${it.message}")
          setTestSuccess(false)
        }
      }
    }
  }

  val onSave = {
    scope.launch {
      if (name.value.isBlank()) {
        setName("Default") // Default name if empty
      }

      val provider = AIProviderEntity(
        id = providerId ?: 0, // 0 for new insertion
        syncId = if (providerId == null) java.util.UUID.randomUUID().toString() else {
          // If editing, we keep the old syncId. Since we don't have it in state, we might need to fetch it again or
          // just let Room handle it if we fetch the whole entity.
          // However, simplified approach: if ID > 0, we are updating. Room's Update requires the full entity.
          // So better to re-fetch if ID > 0 or just ignore syncId updates for now (assuming it doesn't change).
          // A better way is to hold the entity in state.
          // For now, let's just create a new one, but if updating, we need the original syncId to preserve sync continuity.
          // Let's refactor slightly to keep the original entity if editing.
          java.util.UUID.randomUUID().toString() // Placeholder, logic below fixes this
        },
        name = name.value,
        baseUrl = baseUrl.value,
        apiKey = apiKey.value,
        model = model.value,
        isActive = isActive.value
      )

      if (providerId != null) {
        // Updating: Get original to preserve syncId and other fields
        val original = repository.getProviderById(providerId).first()
        if (original != null) {
          repository.update(
            original.copy(
              name = name.value,
              baseUrl = baseUrl.value,
              apiKey = apiKey.value,
              model = model.value,
              isActive = isActive.value,
              lastModified = System.currentTimeMillis()
            )
          )
        }
      } else {
        // Creating new
        val newId = repository.insert(provider)
        // If it's the first one or user checked 'active' (not implemented in UI for new yet), set active
        // Logic: If there are no other active providers, set this one as active
        val active = repository.activeProvider.first()
        if (active == null) {
          repository.setActive(newId)
        }
      }
      navController.popBackStack()
    }
  }

  val onDelete = {
    if (providerId != null) {
      scope.launch {
        repository.delete(providerId)
        navController.popBackStack()
      }
    }
  }

  Scaffold(
    topBar = {
      AppToolbar(
        title = if (providerId == null) "添加模型供应商" else "编辑模型供应商",
        containerColor = Color.Transparent,
        onNavigateUp = { navController.popBackStack() },
        actions = {
          if (providerId != null) {
            IconButton(onClick = { setShowDeleteDialog(true) }) {
              Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
            }
          }
          IconButton(onClick = { onSave() }) {
            Icon(Icons.Default.Check, contentDescription = "Save", tint = Color(0xFF4CAF50))
          }
        }
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
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Configuration Card
      PCard(
        modifier = Modifier
          .fillMaxWidth(),
        variant = CardVariant.Elevated
      ) {
        Column(
          verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
          // Header
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFFF6B35).copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = Color(0xFFFF6B35) // PrimaryOrange
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            PText(
              text = "供应商配置",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = textColor
            )
          }

          // Name Input
          StyledTextField(
            value = name.value,
            onValueChange = { setName(it) },
            label = "供应商名称",
            placeholder = "例如: OpenAI, DeepSeek...",
            backgroundColor = inputBackground,
            textColor = textColor,
            placeholderColor = subTextColor
          )

          // Base URL Input
          StyledTextField(
            value = baseUrl.value,
            onValueChange = { setBaseUrl(it) },
            label = "API Base URL",
            placeholder = "https://api.openai.com/v1",
            backgroundColor = inputBackground,
            textColor = textColor,
            placeholderColor = subTextColor
          )

          // API Key Input
          StyledTextField(
            value = apiKey.value,
            onValueChange = { setApiKey(it) },
            label = "API Key",
            placeholder = "sk-...",
            isPassword = !isApiKeyVisible.value,
            backgroundColor = inputBackground,
            textColor = textColor,
            placeholderColor = subTextColor,
            trailingIcon = {
              Icon(
                imageVector = if (isApiKeyVisible.value) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = if (isApiKeyVisible.value) "Hide API Key" else "Show API Key",
                tint = subTextColor,
                modifier = Modifier
                  .size(24.dp)
                  .clickable { setApiKeyVisible(!isApiKeyVisible.value) }
              )
            }
          )

          // Model Selection Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Model TextField
            StyledTextField(
              value = model.value,
              onValueChange = { setModel(it) },
              label = "模型名称",
              placeholder = "gpt-3.5-turbo",
              modifier = Modifier.weight(1f),
              trailingIcon = {
                if (isFetchingModels.value) {
                  PLoading(
                    size = 20.dp,
                    color = Color(0xFFFF6B35)
                  )
                } else {
                  IconButton(
                    onClick = {
                      focusManager.clearFocus()
                      fetchModels()
                    },
                    modifier = Modifier.size(24.dp)
                  ) {
                    Icon(
                      Icons.Default.Refresh,
                      contentDescription = "Fetch Models",
                      tint = subTextColor,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                }
              },
              backgroundColor = inputBackground,
              textColor = textColor,
              placeholderColor = subTextColor
            )

            // Test Button
            Surface(
              onClick = { testConnection() },
              shape = RoundedCornerShape(12.dp),
              color = inputBackground, // Same as input background
              enabled = !isTesting.value && baseUrl.value.isNotBlank() && apiKey.value.isNotBlank(),
              modifier = Modifier
                .height(56.dp)
                .width(56.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                if (isTesting.value) {
                  PLoading(
                    size = 24.dp,
                    color = Color(0xFFFF6B35)
                  )
                } else {
                  Icon(
                    Icons.Default.Bolt,
                    contentDescription = "Test Connection",
                    tint = if (baseUrl.value.isNotBlank() && apiKey.value.isNotBlank())
                      Color(0xFFFF6B35) else subTextColor
                  )
                }
              }
            }
          }
        }
      }

      // Test Result Card
      AnimatedVisibility(
        visible = testResult.value.isNotBlank(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
      ) {
        PAlert(
          message = if (testSuccess.value) "连接成功" else "连接失败",
          description = testResult.value,
          type = if (testSuccess.value) AlertType.Success else AlertType.Error
        )
      }

      // Models Grid Card
      AnimatedVisibility(
        visible = availableModels.value.isNotEmpty(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
      ) {
        PCard(
          modifier = Modifier
            .fillMaxWidth(),
          variant = CardVariant.Elevated
        ) {
          Column(
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
                  imageVector = Icons.Default.Settings,
                  contentDescription = null,
                  tint = Color(0xFF2196F3) // SoftBlue
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                PText(
                  text = "可用模型列表",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = textColor
                )
                PText(
                  text = "共 ${availableModels.value.size} 个模型",
                  style = MaterialTheme.typography.bodySmall,
                  color = subTextColor
                )
              }
            }

            // Models Grid
            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              availableModels.value.forEach { modelName ->
                val selected = model.value == modelName
                PTag(
                  text = modelName,
                  size = TagSize.Large,
                  variant = if (selected) TagVariant.Outlined else TagVariant.Default,
                  onClick = { setModel(modelName) },
                  colors = TagColors(
                    containerColor = if (selected)
                      Color(0xFF2196F3).copy(alpha = 0.15f)
                    else
                      inputBackground,
                    contentColor = if (selected) Color(0xFF2196F3) else textColor,
                    borderColor = if (selected) Color(0xFF2196F3) else Color.Transparent
                  )
                )
              }
            }
          }
        }
      }

      // Bottom spacing
      Spacer(modifier = Modifier.height(80.dp))
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog.value) {
      PaletteConfirmDialog(
        title = "确认删除",
        message = "确定要删除此模型供应商吗？此操作无法撤销。",
        confirmText = "删除",
        confirmType = ButtonType.DANGER,
        icon = Icons.Default.Delete,
        iconTint = Color.Red,
        onDismiss = { setShowDeleteDialog(false) },
        onConfirm = {
          setShowDeleteDialog(false)
          onDelete()
        }
      )
    }
  }
}
