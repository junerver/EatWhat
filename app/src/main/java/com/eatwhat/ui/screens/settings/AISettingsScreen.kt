package com.eatwhat.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.eatwhat.data.preferences.AIConfig
import com.eatwhat.data.preferences.AIPreferences
import com.eatwhat.domain.service.OpenAIService
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.useGetState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISettingsScreen(navController: NavController) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val aiPreferences = remember { AIPreferences(context) }
  val openAIService = remember { OpenAIService() }

  // Load initial state
  val aiConfig by aiPreferences.aiConfigFlow.collectAsState(initial = AIConfig())

  // Form state
  val (baseUrl, setBaseUrl) = useGetState(default = "")
  val (apiKey, setApiKey) = useGetState(default = "")
  val (model, setModel) = useGetState(default = "")

  // Model fetch & test state
  val (availableModels, setAvailableModels) = useGetState(default = emptyList<String>())
  val (isFetchingModels, setIsFetchingModels) = useGetState(default = false)
  val (isExpanded, setIsExpanded) = useGetState(default = false)
  val (testResult, setTestResult) = useGetState(default = "")
  val (isTesting, setIsTesting) = useGetState(default = false)
  val (testSuccess, setTestSuccess) = useGetState(default = false)
  val (isApiKeyVisible, setApiKeyVisible) = useGetState(default = false)

  // Dark mode support
  val isDark = isSystemInDarkTheme()
  val pageBackground = if (isDark) MaterialTheme.colorScheme.background else Color(0xFFF5F5F5)
  val cardBackground = if (isDark) MaterialTheme.colorScheme.surface else Color.White
  val inputBackground = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF8F8F8)
  val textColor = if (isDark) MaterialTheme.colorScheme.onSurface else Color.Black
  val subTextColor = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color.Gray

  // Update form state when aiConfig loads
  useEffect(aiConfig) {
    setBaseUrl(aiConfig.baseUrl)
    setApiKey(aiConfig.apiKey)
    setModel(aiConfig.model)
  }

  // Fetch models
  val fetchModels = {
    if (baseUrl.value.isNotBlank() && apiKey.value.isNotBlank()) {
      setIsFetchingModels(true)
      scope.launch {
        val config = AIConfig(baseUrl.value, apiKey.value, model.value)
        val result = openAIService.fetchModels(config)
        setIsFetchingModels(false)
        result.onSuccess {
          setAvailableModels(it)
          setIsExpanded(true)
        }.onFailure {
          setTestResult("Error fetching models: ${it.message}")
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
          setTestResult(it)
          setTestSuccess(true)
        }.onFailure {
          setTestResult("Test failed: ${it.message}")
          setTestSuccess(false)
        }
      }
    }
  }

  val onSave = {
    scope.launch {
      aiPreferences.saveConfig(
        AIConfig(
          baseUrl = baseUrl.value,
          apiKey = apiKey.value,
          model = model.value
        )
      )
      navController.popBackStack()
    }
  }

  // Filter models based on input
  val filteredModels = availableModels.value.filter {
    it.contains(model.value, ignoreCase = true)
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text("AI 配置", fontWeight = FontWeight.Bold)
        },
        navigationIcon = {
          IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
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
              text = "OpenAI API 设置",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = textColor
            )
          }

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
            ExposedDropdownMenuBox(
              expanded = isExpanded.value,
              onExpandedChange = { setIsExpanded(it) },
              modifier = Modifier.weight(1f)
            ) {
              // Custom TextField for Model
              StyledTextField(
                value = model.value,
                onValueChange = {
                  setModel(it)
                  setIsExpanded(true)
                },
                label = "Model Name",
                placeholder = "gpt-3.5-turbo",
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                trailingIcon = {
                  if (isFetchingModels.value) {
                    CircularProgressIndicator(
                      modifier = Modifier.size(20.dp),
                      strokeWidth = 2.dp,
                      color = Color(0xFFFF6B35)
                    )
                  } else {
                    Icon(
                      Icons.Default.Refresh,
                      contentDescription = "Fetch Models",
                      tint = subTextColor,
                      modifier = Modifier
                        .size(24.dp)
                        .clickable { fetchModels() }
                    )
                  }
                },
                backgroundColor = inputBackground,
                textColor = textColor,
                placeholderColor = subTextColor
              )

              ExposedDropdownMenu(
                expanded = isExpanded.value && filteredModels.isNotEmpty(),
                onDismissRequest = { setIsExpanded(false) },
                containerColor = cardBackground
              ) {
                filteredModels.take(5).forEach { selectionOption ->
                  DropdownMenuItem(
                    text = { Text(selectionOption, color = textColor) },
                    onClick = {
                      setModel(selectionOption)
                      setIsExpanded(false)
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                  )
                }
              }
            }

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
                text = if (testSuccess.value) "Connection Successful" else "Connection Failed",
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

      Spacer(modifier = Modifier.weight(1f))

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
    }
  }
}

@Composable
fun StyledTextField(
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
  placeholder: String = "",
  isPassword: Boolean = false,
  modifier: Modifier = Modifier,
  trailingIcon: @Composable (() -> Unit)? = null,
  backgroundColor: Color = Color(0xFFF8F8F8),
  textColor: Color = Color.Black,
  placeholderColor: Color = Color.Gray
) {
  Column(modifier = modifier) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium,
      color = placeholderColor,
      modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = backgroundColor
    ) {
      BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(
          fontSize = 16.sp,
          color = textColor
        ),
        singleLine = true,
        cursorBrush = SolidColor(textColor),
        visualTransformation = if (isPassword) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        decorationBox = { innerTextField ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(modifier = Modifier.weight(1f)) {
              if (value.isEmpty()) {
                Text(
                  text = placeholder,
                  color = placeholderColor.copy(alpha = 0.5f),
                  fontSize = 16.sp
                )
              }
              innerTextField()
            }

            if (value.isNotEmpty()) {
              Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Clear",
                tint = placeholderColor,
                modifier = Modifier
                  .size(20.dp)
                  .clickable { onValueChange("") }
              )
            }

            if (trailingIcon != null) {
              Spacer(modifier = Modifier.width(8.dp))
              trailingIcon()
            }
          }
        }
      )
    }
  }
}
