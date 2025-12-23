
package com.eatwhat.ui.screens.recipe

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.eatwhat.EatWhatApplication
import com.eatwhat.domain.model.*
import com.eatwhat.domain.model.Unit as IngredientUnit
import com.eatwhat.ui.components.FoodEmojis
import com.eatwhat.ui.components.RecipeIconPicker
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.*
import kotlin.random.Random

// 定义主题色
private val PrimaryOrange = Color(0xFFFF6B35)
private val PrimaryOrangeLight = Color(0xFFFF8C5A)
private val PrimaryOrangeDark = Color(0xFFE55A2B)
private val SoftGreen = Color(0xFF4CAF50)
private val SoftBlue = Color(0xFF2196F3)
private val SoftPurple = Color(0xFF9C27B0)
private val WarmYellow = Color(0xFFFFC107)
private val CardBackground = Color(0xFFFFFBF8)
private val SectionIconBackground = Color(0xFFFFF3E0)

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
                title = { 
                    Text(
                        if (isEditMode) "编辑菜谱" else "创建新菜谱",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // Save button
                    FilledTonalButton(
                        onClick = onSave,
                        enabled = !isSaving,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = PrimaryOrange,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isEditMode) "保存" else "创建")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Basic Info Section
            item {
                SectionCard(
                    title = "基本信息",
                    icon = Icons.Outlined.Restaurant,
                    iconBackgroundColor = PrimaryOrange.copy(alpha = 0.1f),
                    iconTint = PrimaryOrange
                ) {
                    // Recipe name input with emoji decoration
                    StyledTextField(
                        value = name,
                        onValueChange = setName,
                        label = "菜名",
                        placeholder = "给你的美食起个名字吧",
                        leadingIcon = {
                            Text("🍳", fontSize = 20.sp)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Type selector with colorful chips
                    Text(
                        "菜品类型",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RecipeType.entries.forEach { recipeType ->
                            RecipeTypeChip(
                                type = recipeType,
                                isSelected = type == recipeType,
                                onClick = { setType(recipeType) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tags section
                    Text(
                        "标签",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
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

                    Spacer(modifier = Modifier.height(16.dp))

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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Difficulty selector
                    Text(
                        "难度等级",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Difficulty.entries.forEach { diff ->
                            DifficultyChip(
                                difficulty = diff,
                                isSelected = difficulty == diff,
                                onClick = { setDifficulty(diff) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Time input
                    StyledTextField(
                        value = estimatedTime,
                        onValueChange = setEstimatedTime,
                        label = "预计时间",
                        placeholder = "30",
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Timer,
                                contentDescription = null,
                                tint = PrimaryOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            Text(
                                "分钟",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        },
                        keyboardType = KeyboardType.Number
                    )
                }
            }

            // Ingredients Section
            item {
                SectionCard(
                    title = "食材清单",
                    icon = Icons.Outlined.ShoppingCart,
                    iconBackgroundColor = SoftGreen.copy(alpha = 0.1f),
                    iconTint = SoftGreen,
                    action = {
                        AddButton(
                            onClick = { setIngredients(ingredients + IngredientInput()) },
                            color = SoftGreen
                        )
                    }
                ) {
                    ingredients.forEachIndexed { index, ingredient ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            IngredientInputCard(
                                index = index,
                                ingredient = ingredient,
                                onIngredientChange = { newIngredient ->
                                    setIngredients(
                                        ingredients.toMutableList().apply {
                                            this[index] = newIngredient
                                        }
                                    )
                                },
                                onDelete = {
                                    if (ingredients.size > 1) {
                                        setIngredients(ingredients.filterIndexed { i, _ -> i != index })
                                    }
                                },
                                canDelete = ingredients.size > 1
                            )
                        }
                        if (index < ingredients.lastIndex) {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }

            // Cooking Steps Section
            item {
                SectionCard(
                    title = "烹饪步骤",
                    icon = Icons.Outlined.MenuBook,
                    iconBackgroundColor = SoftBlue.copy(alpha = 0.1f),
                    iconTint = SoftBlue,
                    action = {
                        AddButton(
                            onClick = { setSteps(steps + StepInput()) },
                            color = SoftBlue
                        )
                    }
                ) {
                    steps.forEachIndexed { index, step ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            StepInputCard(
                                stepNumber = index + 1,
                                step = step,
                                onStepChange = { newStep ->
                                    setSteps(
                                        steps.toMutableList().apply {
                                            this[index] = newStep
                                        }
                                    )
                                },
                                onDelete = {
                                    if (steps.size > 1) {
                                        setSteps(steps.filterIndexed { i, _ -> i != index })
                                    }
                                },
                                canDelete = steps.size > 1,
                                isLast = index == steps.lastIndex
                            )
                        }
                        if (index < steps.lastIndex) {
                            // Timeline connector
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(16.dp)
                                        .background(SoftBlue.copy(alpha = 0.3f))
                                )
                            }
                        }
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

/**
 * Section card with title, icon, and content
 */
@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    iconBackgroundColor: Color,
    iconTint: Color,
    action: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color.Black.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Icon with background
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(iconBackgroundColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                action?.invoke()
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            content()
        }
    }
}

/**
 * Add button with icon
 */
@Composable
private fun AddButton(
    onClick: () -> Unit,
    color: Color
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = color.copy(alpha = 0.1f),
        modifier = Modifier.size(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                Icons.Default.Add,
                contentDescription = "添加",
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Styled text field with modern design
 */
@Composable
private fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1
) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF8F8F8),
            border = null
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                leadingIcon?.invoke()
                
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    singleLine = minLines == 1,
                    decorationBox = { innerTextField ->
                        Box {
                            if (value.isEmpty()) {
                                Text(
                                    placeholder,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                
                trailingIcon?.invoke()
            }
        }
    }
}

/**
 * Recipe type chip with emoji and color
 */
@Composable
private fun RecipeTypeChip(
    type: RecipeType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (emoji, label, color) = when (type) {
        RecipeType.MEAT -> Triple("🥩", "荤菜", Color(0xFFE57373))
        RecipeType.VEG -> Triple("🥬", "素菜", Color(0xFF81C784))
        RecipeType.SOUP -> Triple("🍲", "汤", Color(0xFF64B5F6))
        RecipeType.STAPLE -> Triple("🍚", "主食", Color(0xFFFFB74D))
    }
    
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) color.copy(alpha = 0.15f) else Color(0xFFF5F5F5),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, color)
        } else null,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 20.sp)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Difficulty chip with stars
 */
@Composable
private fun DifficultyChip(
    difficulty: Difficulty,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (label, stars, color) = when (difficulty) {
        Difficulty.EASY -> Triple("简单", 1, SoftGreen)
        Difficulty.MEDIUM -> Triple("中等", 2, WarmYellow)
        Difficulty.HARD -> Triple("困难", 3, Color(0xFFE57373))
    }
    
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) color.copy(alpha = 0.15f) else Color(0xFFF5F5F5),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, color)
        } else null,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(stars) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = if (isSelected) color else Color.Gray.copy(alpha = 0.4f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Ingredient input card with modern design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientInputCard(
    index: Int,
    ingredient: IngredientInput,
    onIngredientChange: (IngredientInput) -> Unit,
    onDelete: () -> Unit,
    canDelete: Boolean
) {
    var unitExpanded by remember { mutableStateOf(false) }
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8FBF8),
        border = androidx.compose.foundation.BorderStroke(1.dp, SoftGreen.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Index badge
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(SoftGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${index + 1}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = SoftGreen
                )
            }
            
            // Name input
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "食材",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                BasicTextField(
                    value = ingredient.name,
                    onValueChange = { onIngredientChange(ingredient.copy(name = it)) },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    ),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Box {
                            if (ingredient.name.isEmpty()) {
                                Text(
                                    "例如：鸡蛋",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
            
            // Amount and unit row
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Amount input
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    modifier = Modifier.width(50.dp)
                ) {
                    BasicTextField(
                        value = ingredient.amount,
                        onValueChange = { onIngredientChange(ingredient.copy(amount = it)) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                        decorationBox = { innerTextField ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (ingredient.amount.isEmpty()) {
                                    Text(
                                        "数量",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
                
                // Unit selector
                ExposedDropdownMenuBox(
                    expanded = unitExpanded,
                    onExpandedChange = { unitExpanded = it }
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SoftGreen.copy(alpha = 0.1f),
                        modifier = Modifier
                            .menuAnchor()
                            .width(56.dp)
                            .clickable { unitExpanded = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                ingredient.unit.getDisplayName(),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = SoftGreen
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = SoftGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    ExposedDropdownMenu(
                        expanded = unitExpanded,
                        onDismissRequest = { unitExpanded = false }
                    ) {
                        IngredientUnit.entries.forEach { unit ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        unit.getDisplayName(),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                onClick = {
                                    onIngredientChange(ingredient.copy(unit = unit))
                                    unitExpanded = false
                                },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
            
            // Delete button
            if (canDelete) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = "删除",
                        tint = Color.Gray.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Step input card with timeline design
 */
@Composable
private fun StepInputCard(
    stepNumber: Int,
    step: StepInput,
    onStepChange: (StepInput) -> Unit,
    onDelete: () -> Unit,
    canDelete: Boolean,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Step number badge
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(SoftBlue, SoftBlue.copy(alpha = 0.7f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "$stepNumber",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        // Step content
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF5F9FF),
            border = androidx.compose.foundation.BorderStroke(1.dp, SoftBlue.copy(alpha = 0.2f)),
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "步骤 $stepNumber",
                        style = MaterialTheme.typography.labelMedium,
                        color = SoftBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (canDelete) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = "删除",
                                tint = Color.Gray.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                BasicTextField(
                    value = step.description,
                    onValueChange = { onStepChange(step.copy(description = it)) },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        Box {
                            if (step.description.isEmpty()) {
                                Text(
                                    "描述这一步的操作...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
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
 * Get display name for Unit enum in Chinese
 */
private fun IngredientUnit.getDisplayName(): String {
    return when (this) {
        IngredientUnit.G -> "克"
        IngredientUnit.ML -> "毫升"
        IngredientUnit.PIECE -> "个"
        IngredientUnit.SPOON -> "勺"
        IngredientUnit.MODERATE -> "适量"
    }
}

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
                shape = RoundedCornerShape(20.dp),
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
                shape = RoundedCornerShape(20.dp),
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
                            .width(80.dp)
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
                            tint = PrimaryOrange
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
                shape = RoundedCornerShape(20.dp),
                color = PrimaryOrange.copy(alpha = 0.1f),
                modifier = Modifier
                    .height(32.dp)
                    .clip(RoundedCornerShape(20.dp))
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
                        tint = PrimaryOrange
                    )
                    Text(
                        text = "添加",
                        style = MaterialTheme.typography.labelMedium,
                        color = PrimaryOrange
                    )
                }
            }
        }
    }
}
