package com.eatwhat.ui.screens.recipe

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
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
import com.eatwhat.util.ImageUtils
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
  var selectedImageBase64 by remember { mutableStateOf<String?>(null) }

  // Image picker launcher
  val launcher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri ->
    uri?.let {
      scope.launch {
        val result = ImageUtils.processImageToBase64(context, it)
        when (result) {
          is ImageUtils.ImageProcessingResult.Success -> {
            selectedImageBase64 = result.base64
          }

          is ImageUtils.ImageProcessingResult.Error -> {
            setError(result.message)
          }
        }
      }
    }
  }

  val onAnalyze = {
    if ((prompt.value.isNotBlank() || selectedImageBase64 != null) && !isLoading.value) {
      if (activeProvider == null || aiConfig.apiKey.isBlank()) {
        setError("请先在设置中配置有效的 AI 供应商")
      } else {
        setIsLoading(true)
        setError(null)
        scope.launch {
          val result = openAIService.analyzeRecipe(aiConfig, prompt.value, selectedImageBase64)
          setIsLoading(false)
          result.fold(
            onSuccess = { recipeResult ->
              val finalResult = if (recipeResult.isFoodImage && selectedImageBase64 != null) {
                // If AI says it's a food image, attach the image to the result but use AI-generated icon as backup
                // Wait, RecipeAIResult doesn't have imageBase64 field, we need to pass it separately or add it?
                // Actually the result is JSON string passed to next screen.
                // We can add the image to the result if we modify RecipeAIResult, OR we can pass it via savedStateHandle separately.
                // But wait, the requirement says: "if the picture is a food photo, bring it back to the icon position in the recipe instead of generating an emoji"
                // RecipeAIResult is immutable. We can create a new object or modify the JSON.
                // But actually, the next screen (AddRecipeScreen) will handle the filling.
                // We should pass the image separately in savedStateHandle or include it in the logic.
                // Let's rely on passing the imageBase64 via savedStateHandle if it's a food image.
                recipeResult
              } else {
                recipeResult
              }

              val jsonString = Json.encodeToString(RecipeAIResult.serializer(), finalResult)

              // 在导航之前先设置 savedStateHandle
              val stateHandle = if (initialPrompt != null) {
                // From share: 目标是新的 AddRecipe 页面
                null // 需要导航后才能获取
              } else {
                // 从 AddRecipe 进入的: 目标是上一个页面 (AddRecipe)
                navController.previousBackStackEntry?.savedStateHandle
              }

              // 如果是从 AddRecipe 进入,先设置数据再返回
              if (initialPrompt == null) {
                stateHandle?.set("ai_result", jsonString)
                if (finalResult.isFoodImage && selectedImageBase64 != null) {
                  stateHandle?.set("ai_image", selectedImageBase64)
                }
              }

              // 执行导航
              val targetRoute = if (initialPrompt != null) {
                // From share
                com.eatwhat.navigation.Destinations.AddRecipe.route
              } else {
                null // Pop back
              }

              if (initialPrompt != null) {
                navController.navigate(targetRoute!!) {
                  popUpTo(com.eatwhat.navigation.Destinations.Roll.route)
                }
                // 导航后设置数据
                navController.currentBackStackEntry?.savedStateHandle?.apply {
                  set("ai_result", jsonString)
                  if (finalResult.isFoodImage && selectedImageBase64 != null) {
                    set("ai_image", selectedImageBase64)
                  }
                }
              } else {
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
              "输入菜谱描述或上传图片，AI 将自动生成菜谱信息",
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
                  .height(150.dp),
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

            // Image Picker Section
            Text(
              "添加图片 (可选)",
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            if (selectedImageBase64 != null) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(200.dp)
                  .clip(RoundedCornerShape(12.dp))
              ) {
                val bitmap = remember(selectedImageBase64) {
                  ImageUtils.decodeBase64ToBitmap(selectedImageBase64!!)
                }

                if (bitmap != null) {
                  Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Selected image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                  )
                }

                // Remove button
                IconButton(
                  onClick = { selectedImageBase64 = null },
                  modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                ) {
                  Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove image",
                    tint = Color.White
                  )
                }
              }
            } else {
              Surface(
                onClick = { launcher.launch("image/*") },
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF8F8F8),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(100.dp)
              ) {
                Column(
                  modifier = Modifier.fillMaxSize(),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.Center
                ) {
                  Icon(
                    Icons.Default.AddAPhoto,
                    contentDescription = null,
                    tint = PrimaryOrange,
                    modifier = Modifier.size(32.dp)
                  )
                  Spacer(modifier = Modifier.height(8.dp))
                  Text(
                    "点击上传图片",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
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
          enabled = !isLoading.value && (prompt.value.isNotBlank() || selectedImageBase64 != null),
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
