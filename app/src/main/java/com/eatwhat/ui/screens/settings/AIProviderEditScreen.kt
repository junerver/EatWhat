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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.eatwhat.data.database.EatWhatDatabase
import com.eatwhat.data.database.entities.AIProviderEntity
import com.eatwhat.data.preferences.AIConfig
import com.eatwhat.data.repository.AIProviderRepository
import com.eatwhat.domain.service.OpenAIService
import com.eatwhat.ui.components.StyledTextField
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.compose.hooks.useTimeoutFn
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AIProviderEditScreen(navController: NavController, providerId: Long? = null) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val database = remember { EatWhatDatabase.getInstance(context) }
  val repository = remember { AIProviderRepository(database.aiProviderDao()) }
  val openAIService = remember { OpenAIService() }
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

  // Dark mode support
  val isDark = com.eatwhat.ui.theme.LocalDarkTheme.current
  val pageBackground = if (isDark) MaterialTheme.colorScheme.background else Color(0xFFF5F5F5)
  val cardBackground = if (isDark) MaterialTheme.colorScheme.surface else Color.White
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
      TopAppBar(
        title = {
          Text(
            if (providerId == null) "添加模型供应商" else "编辑模型供应商",
            fontWeight = FontWeight.Bold
          )
        },
        navigationIcon = {
          IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
          }
        },
        actions = {
          if (providerId != null) {
            IconButton(onClick = { onDelete() }) {
              Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
            }
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
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Configuration Card
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .shadow(
            elevation = 4.dp,
            shape = RoundedCornerShape(20.dp),
            spotColor = Color.Black.copy(alpha = 0.1f)
          ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground)
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
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
            Text(
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
                  CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
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
                  CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
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
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .shadow(
              elevation = 2.dp,
              shape = RoundedCornerShape(20.dp),
              spotColor = Color.Black.copy(alpha = 0.05f)
            ),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (testSuccess.value)
              if (isDark) Color(0xFF1E331E) else Color(0xFFF8FBF8)
            else
              if (isDark) Color(0xFF331E1E) else Color(0xFFFFF8F8)
          )
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = if (testSuccess.value) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = if (testSuccess.value) Color(0xFF4CAF50) else Color(0xFFFF5252),
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (testSuccess.value) "连接成功" else "连接失败",
                style = MaterialTheme.typography.labelLarge,
                color = if (testSuccess.value) Color(0xFF4CAF50) else Color(0xFFFF5252),
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = testResult.value,
              style = MaterialTheme.typography.bodySmall,
              fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
              color = textColor
            )
          }
        }
      }

      // Models Grid Card
      AnimatedVisibility(
        visible = availableModels.value.isNotEmpty(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
      ) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .shadow(
              elevation = 4.dp,
              shape = RoundedCornerShape(20.dp),
              spotColor = Color.Black.copy(alpha = 0.1f)
            ),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = cardBackground)
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
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
                Text(
                  text = "可用模型列表",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = textColor
                )
                Text(
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
                Surface(
                  onClick = {
                    setModel(modelName)
                    // Optional: scroll to top to show the filled model
                  },
                  shape = RoundedCornerShape(12.dp),
                  color = if (model.value == modelName)
                    Color(0xFF2196F3).copy(alpha = 0.15f)
                  else
                    inputBackground,
                  border = if (model.value == modelName)
                    androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2196F3))
                  else
                    null,
                  modifier = Modifier.padding(0.dp)
                ) {
                  Text(
                    text = modelName,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (model.value == modelName)
                      Color(0xFF2196F3)
                    else
                      textColor,
                    fontWeight = if (model.value == modelName)
                      FontWeight.Bold
                    else
                      FontWeight.Normal
                  )
                }
              }
            }
          }
        }
      }

      // Bottom spacing and save button
      Spacer(modifier = Modifier.height(16.dp))

      Button(
        onClick = { onSave() },
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35)),
        shape = RoundedCornerShape(28.dp)
      ) {
        Text("保存配置", fontSize = 16.sp, fontWeight = FontWeight.Bold)
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}