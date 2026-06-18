package com.eatwhat.ui.screens.recipe

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.NavController
import com.eatwhat.EatWhatApplication
import com.eatwhat.domain.model.CookingStep
import com.eatwhat.domain.model.Difficulty
import com.eatwhat.domain.model.Ingredient
import com.eatwhat.domain.model.Recipe
import com.eatwhat.domain.model.RecipeAIResult
import com.eatwhat.domain.model.RecipeType
import com.eatwhat.domain.model.Tag
import com.eatwhat.ui.components.FoodEmojis
import com.eatwhat.ui.components.RecipeIconPicker
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks._useState
import xyz.junerver.compose.hooks.getValue
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.palette.components.message.MessageType
import xyz.junerver.compose.palette.components.message.rememberMessageState
import com.eatwhat.domain.model.Unit as IngredientUnit

@Composable
fun AddRecipeScreen(
  navController: NavController,
  recipeId: Long? = null
) {
  val context = LocalContext.current
  val app = context.applicationContext as EatWhatApplication
  val recipeRepository by useCreation { app.recipeRepository }
  val scope = rememberCoroutineScope()
  val messageState = rememberMessageState()
  val focusManager = LocalFocusManager.current

  val density = LocalDensity.current
  val imeInsets = WindowInsets.ime
  var isKeyboardVisible by useState(false)

  LaunchedEffect(Unit) {
    snapshotFlow {
      imeInsets.getBottom(density)
    }.collect { imeBottom ->
      val newState = imeBottom > 0
      if (isKeyboardVisible && !newState) {
        focusManager.clearFocus()
      }
      isKeyboardVisible = newState
    }
  }

  val isEditMode = recipeId != null

  val (name, setName) = useGetState(default = "")
  val (type, setType) = useGetState(default = RecipeType.MEAT)
  val (icon, setIcon) = useGetState(default = FoodEmojis.DEFAULT_EMOJI)
  var imageBase64 by _useState<String?>(null)
  val (difficulty, setDifficulty) = useGetState(default = Difficulty.EASY)
  val (estimatedTime, setEstimatedTime) = useGetState(default = "30")
  val (ingredients, setIngredients) = useGetState(default = listOf(IngredientInput()))
  val (steps, setSteps) = useGetState(default = listOf(StepInput()))
  val (tags, setTags) = useGetState(default = emptyList<String>())
  val (isSaving, setIsSaving) = useGetState(default = false)

  var draggedStepIndex by useState(-1)
  var draggedStepOffset by useState(0f)

  useEffect(recipeId ?: 0L) {
    recipeId?.let { id ->
      scope.launch {
        recipeRepository.getRecipeById(id).collect { recipe ->
          recipe?.let {
            setName(it.name)
            setType(it.type)
            setIcon(it.icon)
            imageBase64 = it.imageBase64
            setDifficulty(it.difficulty)
            setEstimatedTime(it.estimatedTime.toString())
            setIngredients(it.ingredients.map { ing ->
              IngredientInput(ing.name, ing.amount, ing.unit)
            })
            setSteps(it.steps.map { step ->
              StepInput(step.description)
            })
            setTags(it.tags.map { tag -> tag.name })
          }
        }
      }
    }
  }

  val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
  val aiResultJson = savedStateHandle?.getStateFlow<String?>("ai_result", null)?.collectAsState()
  val aiImageBase64 = savedStateHandle?.getStateFlow<String?>("ai_image", null)?.collectAsState()

  useEffect(aiResultJson?.value) {
    aiResultJson?.value?.let { jsonString ->
      try {
        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
        val aiResult = json.decodeFromString(
          RecipeAIResult.serializer(),
          jsonString
        )

        setName(aiResult.name)
        setIcon(aiResult.icon)
        setEstimatedTime(aiResult.estimatedTime.toString())
        setTags(aiResult.tags)

        aiImageBase64?.value?.let {
          imageBase64 = it
          savedStateHandle.remove<String>("ai_image")
        }

        runCatching {
          setType(RecipeType.valueOf(aiResult.type.uppercase()))
        }
        runCatching {
          setDifficulty(Difficulty.valueOf(aiResult.difficulty.uppercase()))
        }

        setIngredients(aiResult.ingredients.map { ing ->
          val unit = runCatching {
            IngredientUnit.valueOf(ing.unit.uppercase())
          }.getOrDefault(IngredientUnit.MODERATE)
          IngredientInput(ing.name, ing.amount, unit)
        })
        setSteps(aiResult.steps.map { StepInput(it) })

        savedStateHandle.remove<String>("ai_result")
        messageState.show("AI 分析完成，已自动填充表单", MessageType.Success)
      } catch (e: Exception) {
        messageState.show("解析 AI 结果失败: ${e.message}", MessageType.Error)
      }
    }
  }

  val onSave: () -> Unit = {
    when {
      name.value.isBlank() -> {
        messageState.show("请输入菜名", MessageType.Warning)
      }

      estimatedTime.value.toIntOrNull()?.let { it in 1..300 } != true -> {
        messageState.show("预计时间必须在1-300分钟之间", MessageType.Warning)
      }

      ingredients.value.any { it.name.isBlank() } -> {
        messageState.show("请填写所有食材名称", MessageType.Warning)
      }

      steps.value.any { it.description.isBlank() } -> {
        messageState.show("请填写所有步骤描述", MessageType.Warning)
      }

      else -> {
        scope.launch {
          setIsSaving(true)
          try {
            val time = estimatedTime.value.toInt()
            val recipe = Recipe(
              id = recipeId ?: 0,
              syncId = java.util.UUID.randomUUID().toString(),
              name = name.value,
              type = type.value,
              icon = icon.value,
              imageBase64 = imageBase64,
              difficulty = difficulty.value,
              estimatedTime = time,
              ingredients = ingredients.value.mapIndexed { index, ing ->
                Ingredient(
                  name = ing.name,
                  amount = ing.amount,
                  unit = ing.unit,
                  orderIndex = index
                )
              },
              steps = steps.value.mapIndexed { index, step ->
                CookingStep(
                  stepNumber = index + 1,
                  description = step.description
                )
              },
              tags = tags.value.map { Tag(name = it) }
            )

            if (isEditMode) {
              recipeRepository.updateRecipe(recipe)
            } else {
              recipeRepository.insertRecipe(recipe)
            }

            navController.navigateUp()
          } catch (e: Exception) {
            messageState.show("保存失败: ${e.message}", MessageType.Error)
          } finally {
            setIsSaving(false)
          }
        }
      }
    }
  }

  AddRecipeContent(
    isEditMode = isEditMode,
    name = name.value,
    type = type.value,
    icon = icon.value,
    imageBase64 = imageBase64,
    difficulty = difficulty.value,
    estimatedTime = estimatedTime.value,
    ingredients = ingredients.value,
    steps = steps.value,
    tags = tags.value,
    isSaving = isSaving.value,
    draggedStepIndex = draggedStepIndex,
    draggedStepOffset = draggedStepOffset,
    onNameChange = { setName(it) },
    onTypeChange = { setType(it) },
    onIconChange = { setIcon(it) },
    onImageChange = { imageBase64 = it },
    onDifficultyChange = { setDifficulty(it) },
    onEstimatedTimeChange = { setEstimatedTime(it) },
    onIngredientsChange = { setIngredients(it) },
    onStepsChange = { setSteps(it) },
    onTagsChange = { setTags(it) },
    onDraggedStepIndexChange = { draggedStepIndex = it },
    onDraggedStepOffsetChange = { draggedStepOffset = it },
    onNavigateUp = { navController.navigateUp() },
    onAIAnalysisClick = { navController.navigate(com.eatwhat.navigation.Destinations.AIAnalysis.route) },
    onSave = onSave,
    recipeIconPicker = { selectedEmoji, selectedImageBase64, recipeType, onEmojiSelected, onImageSelected, onImageCleared, modifier ->
      RecipeIconPicker(
        selectedEmoji = selectedEmoji,
        selectedImageBase64 = selectedImageBase64,
        recipeType = recipeType,
        onEmojiSelected = onEmojiSelected,
        onImageSelected = onImageSelected,
        onImageCleared = onImageCleared,
        modifier = modifier.fillMaxWidth()
      )
    }
  )
}
