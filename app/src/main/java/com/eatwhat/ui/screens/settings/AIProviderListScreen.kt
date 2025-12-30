package com.eatwhat.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.eatwhat.data.database.EatWhatDatabase
import com.eatwhat.data.database.entities.AIProviderEntity
import com.eatwhat.data.preferences.AIConfig
import com.eatwhat.data.repository.AIProviderRepository
import com.eatwhat.domain.service.OpenAIService
import com.eatwhat.navigation.Destinations
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useGetState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIProviderListScreen(navController: NavController) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val database = remember { EatWhatDatabase.getInstance(context) }
  val repository = remember { AIProviderRepository(database.aiProviderDao()) }
  val openAIService = remember { OpenAIService() }

  val providers by repository.allProviders.collectAsState(initial = emptyList())

  // Test connection state
  val (testResult, setTestResult) = useGetState(default = "")
  val (isTesting, setIsTesting) = useGetState(default = false)
  val (testSuccess, setTestSuccess) = useGetState(default = false)
  val (testingProviderId, setTestingProviderId) = useGetState(default = -1L)

  // Dark mode support
  val isDark = com.eatwhat.ui.theme.LocalDarkTheme.current
  val pageBackground = if (isDark) MaterialTheme.colorScheme.background else Color(0xFFF5F5F5)
  val cardBackground = if (isDark) MaterialTheme.colorScheme.surface else Color.White
  val textColor = if (isDark) MaterialTheme.colorScheme.onSurface else Color.Black
  val subTextColor = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color.Gray
  val primaryColor = Color(0xFFFF6B35)

  // Test connection function
  val testConnection = { provider: AIProviderEntity ->
    setIsTesting(true)
    setTestingProviderId(provider.id)
    setTestResult("")
    setTestSuccess(false)
    scope.launch {
      val config = AIConfig(provider.baseUrl, provider.apiKey, provider.model)
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

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text("AI 模型供应商", fontWeight = FontWeight.Bold)
        },
        navigationIcon = {
          IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
          }
        },
        actions = {
          IconButton(onClick = { navController.navigate(Destinations.AIProviderEdit.createRoute()) }) {
            Icon(Icons.Default.Add, contentDescription = "Add Provider", tint = primaryColor)
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
    ) {
      // Test Result Card (Global for the screen)
      AnimatedVisibility(
        visible = testResult.value.isNotBlank(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
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
          Column(modifier = Modifier.padding(16.dp)) {
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
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = testResult.value,
              style = MaterialTheme.typography.bodySmall,
              fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
              color = textColor
            )
          }
        }
      }

      LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        items(providers) { provider ->
          AIProviderItem(
            provider = provider,
            isTesting = isTesting.value && testingProviderId.value == provider.id,
            isDark = isDark,
            textColor = textColor,
            subTextColor = subTextColor,
            cardBackground = cardBackground,
            primaryColor = primaryColor,
            onActivate = {
              scope.launch { repository.setActive(provider.id) }
            },
            onEdit = {
              navController.navigate(Destinations.AIProviderEdit.createRoute(provider.id))
            },
            onTest = { testConnection(provider) }
          )
        }

        if (providers.isEmpty()) {
          item {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                  Icons.Default.Settings,
                  contentDescription = null,
                  tint = subTextColor.copy(alpha = 0.5f),
                  modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                  text = "暂无配置的供应商",
                  color = subTextColor,
                  fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = "点击右上角 + 添加",
                  color = subTextColor.copy(alpha = 0.7f),
                  fontSize = 14.sp
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun AIProviderItem(
  provider: AIProviderEntity,
  isTesting: Boolean,
  isDark: Boolean,
  textColor: Color,
  subTextColor: Color,
  cardBackground: Color,
  primaryColor: Color,
  onActivate: () -> Unit,
  onEdit: () -> Unit,
  onTest: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .shadow(
        elevation = if (provider.isActive) 4.dp else 2.dp,
        shape = RoundedCornerShape(20.dp),
        spotColor = Color.Black.copy(alpha = 0.1f)
      )
      .clickable { onActivate() },
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = cardBackground),
    border = if (provider.isActive) androidx.compose.foundation.BorderStroke(
      2.dp,
      primaryColor.copy(alpha = 0.5f)
    ) else null
  ) {
    Column(
      modifier = Modifier.padding(16.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        // Active Indicator
        Icon(
          imageVector = if (provider.isActive) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
          contentDescription = if (provider.isActive) "Active" else "Inactive",
          tint = if (provider.isActive) primaryColor else subTextColor,
          modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = provider.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = textColor
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = provider.model,
            style = MaterialTheme.typography.bodyMedium,
            color = subTextColor
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = provider.baseUrl,
            style = MaterialTheme.typography.labelSmall,
            color = subTextColor.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
          )
        }

        // Actions
        Row {
          // Test Button
          IconButton(
            onClick = onTest,
            enabled = !isTesting
          ) {
            if (isTesting) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = primaryColor
              )
            } else {
              Icon(
                Icons.Default.Bolt,
                contentDescription = "Test",
                tint = subTextColor
              )
            }
          }

          // Edit Button
          IconButton(onClick = onEdit) {
            Icon(
              Icons.Default.Edit,
              contentDescription = "Edit",
              tint = subTextColor
            )
          }
        }
      }
    }
  }
}