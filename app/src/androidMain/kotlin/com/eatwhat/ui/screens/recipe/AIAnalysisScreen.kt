package com.eatwhat.ui.screens.recipe

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.eatwhat.data.database.EatWhatDatabase
import com.eatwhat.data.repository.AIProviderRepository
import com.eatwhat.domain.model.AIProviderSummary
import com.eatwhat.domain.model.RecipeAIResult
import com.eatwhat.domain.model.jsonSchemaString
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
  val allProviders by repository.allProviders.collectAsState(initial = emptyList())

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
      savedStateHandle.remove<String>("shared_image")
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

  // 使用 useGenerateObject 钩子
  val generateObject = useGenerateObject<RecipeAIResult>(
    schema = RecipeAIResult::class.jsonSchemaString,
  ) {
    activeProvider?.let { provider ->
      this.provider = Providers.OpenAI(
        baseUrl = provider.baseUrl,
        apiKey = provider.apiKey
      )
      this.model = provider.model
    }
    this.systemPrompt = """
      你是一个专业的菜谱分析助手。请分析用户的输入（菜谱描述、做法、图片等），并输出符合 JSON Schema 的菜谱数据。
      
      注意：
      1. type 必须是 MEAT(荤菜), VEG(素菜), SOUP(汤), STAPLE(主食), OTHER(其他) 之一。
         注意：OTHER 类型用于蘸汁、酱料、汤底等辅助型配方，或者不能单独作为一道菜品的食谱。
      2. unit 必须是 G(克), ML(毫升), PIECE(个), SPOON(勺), MODERATE(适量) 之一，当unit为MODERATE(适量)时，amount应为空字符串。
      3. icon 请根据菜品内容选择一个最合适的 Emoji。
      4. 如果输入信息不全，请根据经验合理补全。
      5. estimatedTime 应在 1-300 之间。
      6. 如果用户上传了图片且该图片是做好的食物照片（成品图），请将 isFoodImage 设为 true；如果是纯文字截图或非食物照片，请设为 false。
      7. 如果 isFoodImage 为 true，icon 字段仍需生成一个 emoji 作为备用，但前端会优先使用用户上传的图片。
    """.trimIndent()
    timeout = 60.seconds
  }
  val recipe = generateObject.object_
  val rawJson = generateObject.rawJson
  val isLoading = generateObject.isLoading
  val error = generateObject.error
  val submit = generateObject.submit

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

  AIAnalysisContent(
    prompt = prompt.value,
    selectedImageBase64 = selectedImageBase64,
    activeProvider = activeProvider?.toSummary(),
    providers = allProviders.map { it.toSummary() },
    isLoading = isLoading.value,
    displayError = displayError,
    onPromptChange = { setPrompt(it) },
    onPickImage = { launcher.launch("image/*") },
    onRemoveImage = { selectedImageBase64 = null },
    onAnalyze = onAnalyze,
    onNavigateUp = { navController.popBackStack() },
    onConfigureAI = { navController.navigate(com.eatwhat.navigation.Destinations.AIConfig.route) },
    onProviderSelected = { providerId ->
      scope.launch {
        repository.setActive(providerId)
      }
    },
    selectedImagePreview = { imageBase64 ->
      val bitmap = remember(imageBase64) {
        ImageUtils.decodeBase64ToBitmap(imageBase64)
      }
      if (bitmap != null) {
        Image(
          bitmap = bitmap.asImageBitmap(),
          contentDescription = "Selected image",
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop
        )
      }
    }
  )
}

private fun com.eatwhat.data.database.entities.AIProviderEntity.toSummary(): AIProviderSummary =
  AIProviderSummary(
    id = id,
    name = name,
    baseUrl = baseUrl,
    model = model,
    isActive = isActive
  )
