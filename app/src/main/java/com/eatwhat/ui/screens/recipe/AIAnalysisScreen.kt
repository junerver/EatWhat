package com.eatwhat.ui.screens.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.eatwhat.data.database.EatWhatDatabase
import com.eatwhat.data.preferences.AIConfig
import com.eatwhat.data.repository.AIProviderRepository
import com.eatwhat.domain.service.OpenAIService
import com.eatwhat.domain.service.RecipeAIResult
import com.eatwhat.ui.theme.LocalDarkTheme
import com.eatwhat.ui.theme.PrimaryOrange
import com.eatwhat.ui.theme.SoftPurple
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import xyz.junerver.compose.hooks._useGetState
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useGetState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAnalysisScreen(navController: NavController, initialPrompt: String? = null) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val database = remember { EatWhatDatabase.getInstance(context) }
  val repository = remember { AIProviderRepository(database.aiProviderDao()) }
  val openAIService = remember { OpenAIService() }

  val activeProvider by repository.activeProvider.collectAsState(initial = null)
  val aiConfig = activeProvider?.toAIConfig() ?: AIConfig()

  val (prompt, setPrompt) = useGetState(initialPrompt ?: "")
  val (isLoading, setIsLoading) = useGetState(false)
  val (error, setError) = _useGetState<String?>(null)

  val onAnalyze = {
    if (prompt.value.isNotBlank() && !isLoading.value) {
      if (activeProvider == null || aiConfig.apiKey.isBlank()) {
        setError("请先在设置中配置有效的 AI 供应商")
      } else {
        setIsLoading(true)
        setError(null)
        scope.launch {
          val result = openAIService.analyzeRecipe(aiConfig, prompt.value)
          setIsLoading(false)
          result.fold(
            onSuccess = { recipeResult ->
              val jsonString = Json.encodeToString(RecipeAIResult.serializer(), recipeResult)
              if (initialPrompt != null) {
                // 来自分享，导航到 AddRecipe
                navController.navigate(com.eatwhat.navigation.Destinations.AddRecipe.route) {
                  popUpTo(com.eatwhat.navigation.Destinations.Roll.route)
                }
                navController.currentBackStackEntry?.savedStateHandle?.set("ai_result", jsonString)
              } else {
                // 来自 AddRecipe，返回并传递结果
                navController.previousBackStackEntry?.savedStateHandle?.set("ai_result", jsonString)
                navController.popBackStack()
              }
            },
            onFailure = { e ->
              setError(e.message ?: "分析失败")
            }
          )
        }
      }
    }
  }

  val isDark = LocalDarkTheme.current

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            "AI 菜谱分析",
            fontWeight = FontWeight.Bold
          )
        },
        navigationIcon = {
          IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        windowInsets = WindowInsets.statusBars
      )
    },
    containerColor = MaterialTheme.colorScheme.background
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
      // Info card
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .shadow(
              elevation = 4.dp,
              shape = RoundedCornerShape(20.dp),
              spotColor = Color.Black.copy(alpha = 0.1f)
            ),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SoftPurple.copy(alpha = 0.1f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = SoftPurple,
                modifier = Modifier.size(22.dp)
              )
            }
            Text(
              "输入菜谱描述，AI 将自动生成菜谱信息",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      // Input section
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .shadow(
              elevation = 4.dp,
              shape = RoundedCornerShape(20.dp),
              spotColor = Color.Black.copy(alpha = 0.1f)
            ),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Text(
              "菜谱描述",
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            // Multi-line input
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF8F8F8)
            ) {
              BasicTextField(
                value = prompt.value,
                onValueChange = { setPrompt(it) },
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                  color = MaterialTheme.colorScheme.onSurface,
                  lineHeight = 24.sp
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(200.dp),
                decorationBox = { innerTextField ->
                  Box(
                    modifier = Modifier
                      .fillMaxSize()
                      .padding(16.dp)
                  ) {
                    if (prompt.value.isEmpty()) {
                      Text(
                        "例如：\n西红柿炒鸡蛋\n需要两个西红柿和三个鸡蛋\n先炒鸡蛋后加西红柿...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        lineHeight = 24.sp
                      )
                    }
                    innerTextField()
                  }
                }
              )
            }

            // Error message
            if (error.value != null) {
              Text(
                error.value!!,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
              )
            }
          }
        }
      }

      // Analyze button
      item {
        FilledTonalButton(
          onClick = onAnalyze,
          modifier = Modifier.fillMaxWidth(),
          enabled = !isLoading.value && prompt.value.isNotBlank(),
          colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = PrimaryOrange,
            contentColor = Color.White
          ),
          contentPadding = PaddingValues(vertical = 16.dp)
        ) {
          if (isLoading.value) {
            CircularProgressIndicator(
              modifier = Modifier.size(20.dp),
              color = Color.White,
              strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              "分析中...",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold
            )
          } else {
            Icon(
              Icons.Outlined.AutoAwesome,
              contentDescription = null,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              "开始分析",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }

      // Bottom spacing
      item {
        Spacer(modifier = Modifier.height(32.dp))
      }
    }
  }
}
