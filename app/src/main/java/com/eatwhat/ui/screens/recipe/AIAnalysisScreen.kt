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
import com.eatwhat.data.repository.AIProviderRepository
import com.eatwhat.domain.model.RecipeAIResult
import com.eatwhat.domain.model.jsonSchemaString
import com.eatwhat.ui.theme.LocalDarkTheme
import com.eatwhat.ui.theme.PrimaryOrange
import com.eatwhat.ui.theme.SoftPurple
import com.eatwhat.util.ImageUtils
import kotlinx.coroutines.launch
import xyz.junerver.compose.ai.invoke
import xyz.junerver.compose.ai.usechat.Providers
import xyz.junerver.compose.ai.usegenerateobject.useGenerateObject
import xyz.junerver.compose.hooks._useGetState
import xyz.junerver.compose.hooks._useState
import xyz.junerver.compose.hooks.getValue
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.useGetState
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAnalysisScreen(navController: NavController, initialPrompt: String? = null) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val database by useCreation { EatWhatDatabase.getInstance(context) }
  val repository by useCreation { AIProviderRepository(database.aiProviderDao()) }

  val activeProvider by repository.activeProvider.collectAsState(initial = null)

  val (prompt, setPrompt) = useGetState(initialPrompt ?: "")
  val (localError, setLocalError) = _useGetState<String?>(null)
  var selectedImageBase64 by _useState<String?>(null)

  // 接收从分享传来的图片
  val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
  val sharedImageBase64 =
    savedStateHandle?.getStateFlow<String?>("shared_image", null)?.collectAsState()

  // 判断是否从分享进入：有 initialPrompt 或有 shared_image
  val isFromShare by useCreation { initialPrompt != null || sharedImageBase64?.value != null }

  // 如果有分享的图片，设置为选中的图片
  useEffect(sharedImageBase64?.value) {
    sharedImageBase64?.value?.let { imageBase64 ->
      selectedImageBase64 = imageBase64
      savedStateHandle?.remove<String>("shared_image")
    }
  }

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
            setLocalError(result.message)
          }
        }
      }
    }
  }

  // 系统提示词
  val systemPrompt = """
    你是一个专业的菜谱分析助手。请分析用户的输入（菜谱描述、做法、图片等），并输出符合 JSON Schema 的菜谱数据。

    注意：
    1. type 必须是 MEAT(荤菜), VEG(素菜), SOUP(汤), STAPLE(主食), OTHER(其他) 之一。
       注意：OTHER 类型用于蘸汁、酱料、汤底等辅助型配方，或者不能单独作为一道菜品的食谱。
    2. unit 必须是 G(克), ML(毫升), PIECE(个), SPOON(勺), MODERATE(适量) 之一。
    3. icon 请根据菜品内容选择一个最合适的 Emoji。
    4. 如果输入信息不全，请根据经验合理补全。
    5. estimatedTime 应在 1-300 之间。
    6. 如果用户上传了图片且该图片是做好的食物照片（成品图），请将 isFoodImage 设为 true；如果是纯文字截图或非食物照片，请设为 false。
    7. 如果 isFoodImage 为 true，icon 字段仍需生成一个 emoji 作为备用，但前端会优先使用用户上传的图片。
  """.trimIndent()

  // 使用 useGenerateObject 钩子
  val (recipe, rawJson, isLoading, error, submit, _) = useGenerateObject<RecipeAIResult>(
    schemaString = RecipeAIResult::class.jsonSchemaString,
  ) {
    activeProvider?.let { provider ->
      this.provider = Providers.OpenAI(
        baseUrl = provider.baseUrl,
        apiKey = provider.apiKey
      )
      this.model = provider.model
    }
    this.systemPrompt = systemPrompt
    timeout = 60.seconds
  }

  // 处理分析结果
  useEffect(recipe.value) {
    recipe.value?.let { recipeResult ->
      val jsonString = rawJson.value

      // 根据是否从分享进入来决定导航行为
      if (isFromShare) {
        // 从分享进入: 导航到 AddRecipe 页面
        val targetRoute = com.eatwhat.navigation.Destinations.AddRecipe.route
        navController.navigate(targetRoute) {
          popUpTo(com.eatwhat.navigation.Destinations.Roll.route) {
            inclusive = false
          }
          launchSingleTop = true
        }
        // 延迟设置数据,确保导航完成
        scope.launch {
          kotlinx.coroutines.delay(100)
          navController.currentBackStackEntry?.savedStateHandle?.apply {
            set("ai_result", jsonString)
            if (recipeResult.isFoodImage && selectedImageBase64 != null) {
              set("ai_image", selectedImageBase64)
            }
          }
        }
      } else {
        // 从 AddRecipe 进入: 返回到 AddRecipe 页面
        navController.previousBackStackEntry?.savedStateHandle?.apply {
          set("ai_result", jsonString)
          if (recipeResult.isFoodImage && selectedImageBase64 != null) {
            set("ai_image", selectedImageBase64)
          }
        }
        navController.popBackStack()
      }
    }
  }

  val onAnalyze = {
    if ((prompt.value.isNotBlank() || selectedImageBase64 != null) && !isLoading.value) {
      if (activeProvider == null || activeProvider?.apiKey.isNullOrBlank()) {
        setLocalError("请先在设置中配置有效的 AI 供应商")
      } else {
        setLocalError(null)
        val promptText = prompt.value.ifBlank { "请分析这张图片中的内容并创建菜谱" }
        if (selectedImageBase64 != null) {
          submit(promptText, selectedImageBase64!!, "image/webp")
        } else {
          submit(promptText)
        }
      }
    }
  }

  // 合并错误信息
  val displayError = localError.value ?: error.value?.message

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
            if (displayError != null) {
              Text(
                displayError,
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
