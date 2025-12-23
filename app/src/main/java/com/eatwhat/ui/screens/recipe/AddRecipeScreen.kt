package com.eatwhat.ui.screens.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.eatwhat.EatWhatApplication
import com.eatwhat.domain.model.*
import com.eatwhat.domain.model.Unit as IngredientUnit
import com.eatwhat.ui.components.FoodEmojis
import com.eatwhat.ui.components.RecipeIconPicker
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.*
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen(
    navController: NavController,
    recipeId: Long? = null
) {
    val context = LocalContext.current
    val app = context.applicationContext as EatWhatApplication
    val recipeRepository = remember { app.recipeRepository }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val isEditMode = recipeId != null

    // Form state
    val (name, setName) = useState("")
    val (type, setType) = useState(RecipeType.MEAT)
    val (icon, setIcon) = useState(FoodEmojis.DEFAULT_EMOJI)
    val (imageBase64, setImageBase64) = useState<String?>(null)
    val (difficulty, setDifficulty) = useState(Difficulty.EASY)
    val (estimatedTime, setEstimatedTime) = useState("30")
    val (ingredients, setIngredients) = useState<List<IngredientInput>>(listOf(IngredientInput()))
    val (steps, setSteps) = useState<List<StepInput>>(listOf(StepInput()))
    val (tags, setTags) = useState<List<String>>(emptyList())
    val (newTag, setNewTag) = useState("")
    val (isSaving, setIsSaving) = useState(false)
    val (showTagInput, setShowTagInput) = useState(false)
    
    // Generate stable random colors for tags
    val tagColors = remember(tags) {
        tags.associateWith { generatePastelColor() }
    }

    // Load existing recipe if editing
    useEffect(recipeId ?: 0L) {
        recipeId?.let { id ->
            scope.launch {
                recipeRepository.getRecipeById(id).collect { recipe ->
                    recipe?.let {
                        setName(it.name)
                        setType(it.type)
                        setIcon(it.icon)
                        setImageBase64(it.imageBase64)
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

    // Save function extracted for use in TopAppBar
    val onSave: () -> Unit = {
        // Validation
        if (name.isBlank()) {
            scope.launch {
                snackbarHostState.showSnackbar("请输入菜名")
            }
        } else {
            val time = estimatedTime.toIntOrNull()
            if (time == null || time < 1 || time > 300) {
                scope.launch {
                    snackbarHostState.showSnackbar("预计时间必须在1-300分钟之间")
                }
            } else if (ingredients.any { it.name.isBlank() }) {
                scope.launch {
                    snackbarHostState.showSnackbar("请填写所有食材名称")
                }
            } else if (steps.any { it.description.isBlank() }) {
                scope.launch {
                    snackbarHostState.showSnackbar("请填写所有步骤描述")
                }
            } else {
                // Save recipe
                scope.launch {
                    setIsSaving(true)
                    try {
                        val recipe = Recipe(
                            id = recipeId ?: 0,
                            syncId = java.util.UUID.randomUUID().toString(),
                            name = name,
                            type = type,
                            icon = icon,
                            imageBase64 = imageBase64,
                            difficulty = difficulty,
                            estimatedTime = time,
                            ingredients = ingredients.mapIndexed { index, ing ->
                                Ingredient(
                                    name = ing.name,
                                    amount = ing.amount,
                                    unit = ing.unit,
                                    orderIndex = index
                                )
                            },
                            steps = steps.mapIndexed { index, step ->
                                CookingStep(
                                    stepNumber = index + 1,
                                    description = step.description
                                )
                            },
                            tags = tags.map { Tag(name = it) }
                        )

                        if (isEditMode) {
                            recipeRepository.updateRecipe(recipe)
                        } else {
                            recipeRepository.insertRecipe(recipe)
                        }

                        navController.navigateUp()
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("保存失败: ${e.message}")
                    } finally {
                        setIsSaving(false)
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "编辑菜谱" else "添加菜谱") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // Save button with check icon
                    IconButton(
                        onClick = onSave,
                        enabled = !isSaving
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = if (isEditMode) "保存" else "添加",
                            tint = if (isSaving) {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Basic Info
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("基本信息", style = MaterialTheme.typography.titleMedium)

                        OutlinedTextField(
                            value = name,
                            onValueChange = setName,
                            label = { Text("菜名") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Type selector
                        Text("类型", style = MaterialTheme.typography.labelMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RecipeType.values().forEach { recipeType ->
                                FilterChip(
                                    selected = type == recipeType,
                                    onClick = { setType(recipeType) },
                                    label = {
                                        Text(
                                            when (recipeType) {
                                                RecipeType.MEAT -> "荤菜"
                                                RecipeType.VEG -> "素菜"
                                                RecipeType.SOUP -> "汤"
                                                RecipeType.STAPLE -> "主食"
                                            }
                                        )
                                    }
                                )
                            }
                        }

                        // Tags section with FlowRow layout
                        Text("标签", style = MaterialTheme.typography.labelMedium)
                        
                        TagsFlowRow(
                            tags = tags,
                            tagColors = tagColors,
                            showTagInput = showTagInput,
                            newTag = newTag,
                            onNewTagChange = setNewTag,
                            onAddTag = {
                                if (newTag.isNotBlank() && !tags.contains(newTag)) {
                                    setTags(tags + newTag)
                                    setNewTag("")
                                }
                                setShowTagInput(false)
                            },
                            onRemoveTag = { tag -> setTags(tags.filter { it != tag }) },
                            onShowInput = { setShowTagInput(true) },
                            onHideInput = {
                                setShowTagInput(false)
                                setNewTag("")
                            }
                        )

                        // Icon/Image picker
                        RecipeIconPicker(
                            selectedEmoji = icon,
                            selectedImageBase64 = imageBase64,
                            recipeType = type.name,
                            onEmojiSelected = { setIcon(it) },
                            onImageSelected = { setImageBase64(it) },
                            onImageCleared = { setImageBase64(null) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Difficulty selector
                        Text("难度", style = MaterialTheme.typography.labelMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Difficulty.values().forEach { diff ->
                                FilterChip(
                                    selected = difficulty == diff,
                                    onClick = { setDifficulty(diff) },
                                    label = {
                                        Text(
                                            when (diff) {
                                                Difficulty.EASY -> "简单"
                                                Difficulty.MEDIUM -> "中等"
                                                Difficulty.HARD -> "困难"
                                            }
                                        )
                                    }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = estimatedTime,
                            onValueChange = setEstimatedTime,
                            label = { Text("预计时间 (分钟)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                }
            }

            // Ingredients
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("食材", style = MaterialTheme.typography.titleMedium)
                            IconButton(
                                onClick = {
                                    setIngredients(ingredients + IngredientInput())
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "添加食材")
                            }
                        }

                        ingredients.forEachIndexed { index, ingredient ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = ingredient.name,
                                    onValueChange = { newName ->
                                        setIngredients(
                                            ingredients.toMutableList().apply {
                                                this[index] = ingredient.copy(name = newName)
                                            }
                                        )
                                    },
                                    label = { Text("名称") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = ingredient.amount,
                                    onValueChange = { newAmount ->
                                        setIngredients(
                                            ingredients.toMutableList().apply {
                                                this[index] = ingredient.copy(amount = newAmount)
                                            }
                                        )
                                    },
                                    label = { Text("数量") },
                                    modifier = Modifier.width(80.dp),
                                    singleLine = true
                                )

                                IconButton(
                                    onClick = {
                                        if (ingredients.size > 1) {
                                            setIngredients(ingredients.filterIndexed { i, _ -> i != index })
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "删除")
                                }
                            }
                        }
                    }
                }
            }

            // Cooking Steps
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("烹饪步骤", style = MaterialTheme.typography.titleMedium)
                            IconButton(
                                onClick = {
                                    setSteps(steps + StepInput())
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "添加步骤")
                            }
                        }

                        steps.forEachIndexed { index, step ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "${index + 1}.",
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(top = 16.dp)
                                )

                                OutlinedTextField(
                                    value = step.description,
                                    onValueChange = { newDesc ->
                                        setSteps(
                                            steps.toMutableList().apply {
                                                this[index] = step.copy(description = newDesc)
                                            }
                                        )
                                    },
                                    label = { Text("步骤描述") },
                                    modifier = Modifier.weight(1f),
                                    minLines = 2
                                )

                                IconButton(
                                    onClick = {
                                        if (steps.size > 1) {
                                            setSteps(steps.filterIndexed { i, _ -> i != index })
                                        }
                                    },
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "删除")
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}

data class IngredientInput(
    val name: String = "",
    val amount: String = "",
    val unit: IngredientUnit = IngredientUnit.G
)

data class StepInput(
    val description: String = ""
)

/**
 * Generate a random pastel (light) color for tags
 */
private fun generatePastelColor(): Color {
    val pastelColors = listOf(
        Color(0xFFFFCDD2), // Light Red
        Color(0xFFF8BBD9), // Light Pink
        Color(0xFFE1BEE7), // Light Purple
        Color(0xFFD1C4E9), // Light Deep Purple
        Color(0xFFC5CAE9), // Light Indigo
        Color(0xFFBBDEFB), // Light Blue
        Color(0xFFB3E5FC), // Light Light Blue
        Color(0xFFB2EBF2), // Light Cyan
        Color(0xFFB2DFDB), // Light Teal
        Color(0xFFC8E6C9), // Light Green
        Color(0xFFDCEDC8), // Light Light Green
        Color(0xFFF0F4C3), // Light Lime
        Color(0xFFFFF9C4), // Light Yellow
        Color(0xFFFFECB3), // Light Amber
        Color(0xFFFFE0B2), // Light Orange
        Color(0xFFFFCCBC), // Light Deep Orange
    )
    return pastelColors[Random.nextInt(pastelColors.size)]
}

/**
 * FlowRow-like layout for tags with add button
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagsFlowRow(
    tags: List<String>,
    tagColors: Map<String, Color>,
    showTagInput: Boolean,
    newTag: String,
    onNewTagChange: (String) -> Unit,
    onAddTag: () -> Unit,
    onRemoveTag: (String) -> Unit,
    onShowInput: () -> Unit,
    onHideInput: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Existing tags
        tags.forEach { tag ->
            val backgroundColor = tagColors[tag] ?: generatePastelColor()
            val contentColor = Color.Black.copy(alpha = 0.8f)
            
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = backgroundColor,
                modifier = Modifier.height(32.dp)
            ) {
                Row(
                    modifier = Modifier.padding(start = 12.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor,
                        maxLines = 1
                    )
                    IconButton(
                        onClick = { onRemoveTag(tag) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "删除标签",
                            modifier = Modifier.size(14.dp),
                            tint = contentColor
                        )
                    }
                }
            }
        }
        
        // Add tag button or input field
        if (showTagInput) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.height(32.dp)
            ) {
                Row(
                    modifier = Modifier.padding(start = 12.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = newTag,
                        onValueChange = onNewTagChange,
                        modifier = Modifier
                            .width(100.dp)
                            .padding(vertical = 6.dp),
                        textStyle = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                onAddTag()
                                focusManager.clearFocus()
                            }
                        ),
                        decorationBox = { innerTextField ->
                            Box(
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (newTag.isEmpty()) {
                                    Text(
                                        text = "标签名",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                    IconButton(
                        onClick = {
                            onAddTag()
                            focusManager.clearFocus()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "确认添加",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = {
                            onHideInput()
                            focusManager.clearFocus()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "取消",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // Add tag button - always at the end
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .height(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onShowInput() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "添加标签",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "添加标签",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}
