package com.eatwhat.ui.screens.recipe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.eatwhat.data.preferences.AIConfig
import com.eatwhat.data.preferences.AIPreferences
import com.eatwhat.domain.service.OpenAIService
import com.eatwhat.domain.service.RecipeAIResult
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import xyz.junerver.compose.hooks._useGetState
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useGetState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAnalysisScreen(navController: NavController) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val aiPreferences = remember { AIPreferences(context) }
  val openAIService = remember { OpenAIService() }

  val aiConfig by aiPreferences.aiConfigFlow.collectAsState(initial = AIConfig())

  val (prompt, setPrompt) = useGetState("")
  val (isLoading, setIsLoading) = useGetState(false)
  val (error, setError) = _useGetState<String?>(null)

  val onAnalyze = {
    if (prompt.value.isNotBlank() && !isLoading.value) {
      if (aiConfig.apiKey.isBlank()) {
        setError("请先在设置中配置 OpenAI API Key")
      } else {
        setIsLoading(true)
        setError(null)
        scope.launch {
          val result = openAIService.analyzeRecipe(aiConfig, prompt.value)
          setIsLoading(false)
          result.fold(
            onSuccess = { recipeResult ->
              val jsonString = Json.encodeToString(RecipeAIResult.serializer(), recipeResult)
              navController.previousBackStackEntry?.savedStateHandle?.set("ai_result", jsonString)
              navController.popBackStack()
            },
            onFailure = { e ->
              setError(e.message ?: "分析失败")
            }
          )
        }
      }
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("AI 菜谱分析") },
        navigationIcon = {
          IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
          }
        }
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text(
        "输入菜谱描述，AI 将自动生成菜谱信息。",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      OutlinedTextField(
        value = prompt.value,
        onValueChange = { setPrompt(it) },
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        label = { Text("菜谱描述") },
        placeholder = { Text("例如：西红柿炒鸡蛋，需要两个西红柿和三个鸡蛋，先炒鸡蛋...") },
        supportingText = error.value?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
      )

      Button(
        onClick = onAnalyze,
        modifier = Modifier.fillMaxWidth(),
        enabled = !isLoading.value && prompt.value.isNotBlank()
      ) {
        if (isLoading.value) {
          CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = MaterialTheme.colorScheme.onPrimary
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text("分析中...")
        } else {
          Text("开始分析")
        }
      }
    }
  }
}
