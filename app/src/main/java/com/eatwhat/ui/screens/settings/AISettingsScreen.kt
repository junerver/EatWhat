package com.eatwhat.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.eatwhat.data.preferences.AIConfig
import com.eatwhat.data.preferences.AIPreferences
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useGetState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISettingsScreen(navController: NavController) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val aiPreferences = remember { AIPreferences(context) }

  // Load initial state
  val aiConfig by aiPreferences.aiConfigFlow.collectAsState(initial = AIConfig())

  // Form state
  val (baseUrl, setBaseUrl) = useGetState(default = aiConfig.baseUrl)
  val (apiKey, setApiKey) = useGetState(default = aiConfig.apiKey)
  val (model, setModel) = useGetState(default = aiConfig.model)

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
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      OutlinedTextField(
        value = baseUrl.value,
        onValueChange = { setBaseUrl(it) },
        label = { Text("API Base URL") },
        placeholder = { Text("https://api.openai.com/v1") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
      )

      OutlinedTextField(
        value = apiKey.value,
        onValueChange = { setApiKey(it) },
        label = { Text("API Key") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        // visualTransformation = PasswordVisualTransformation() // Optional: hide key
      )

      OutlinedTextField(
        value = model.value,
        onValueChange = { setModel(it) },
        label = { Text("Model Name") },
        placeholder = { Text("gpt-3.5-turbo") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
      )

      Button(
        onClick = { onSave() },
        modifier = Modifier.fillMaxWidth()
      ) {
        Text("保存配置")
      }
    }
  }
}
