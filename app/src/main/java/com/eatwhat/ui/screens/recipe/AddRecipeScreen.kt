package com.eatwhat.ui.screens.recipe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.eatwhat.EatWhatApplication
import com.eatwhat.data.repository.TagRepository
import com.eatwhat.domain.model.*
import com.eatwhat.domain.model.Unit as IngredientUnit
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen(
    navController: NavController,
    recipeId: Long? = null
) {
    val context = LocalContext.current
    val app = context.applicationContext as EatWhatApplication
    val recipeRepository = remember { app.recipeRepository }
    val tagRepository = remember { TagRepository(app.database) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val isEditMode = recipeId != null

    // Form state
    val (name, setName) = useState("")
    val (type, setType) = useState(RecipeType.MEAT)
    val (icon, setIcon) = useState("🍗")
    val (difficulty, setDifficulty) = useState(Difficulty.EASY)
    val (estimatedTime, setEstimatedTime) = useState("30")
    val (ingredients, setIngredients) = useState<List<IngredientInput>>(listOf(IngredientInput()))
    val (steps, setSteps) = useState<List<StepInput>>(listOf(StepInput()))
    val (tags, setTags) = useState<List<String>>(emptyList())
    val (newTag, setNewTag) = useState("")
    val (isSaving, setIsSaving) = useState(false)

    // Load existing recipe if editing
    useEffect(recipeId ?: 0L) {
        recipeId?.let { id ->
            scope.launch {
                recipeRepository.getRecipeById(id).collect { recipe ->
                    recipe?.let {
                        setName(it.name)
                        setType(it.type)
                        setIcon(it.icon)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "编辑菜谱" else "添加菜谱") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
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

                        // Icon picker
                        OutlinedTextField(
                            value = icon,
                            onValueChange = setIcon,
                            label = { Text("图标 (Emoji)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
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

            // Tags
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("标签", style = MaterialTheme.typography.titleMedium)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newTag,
                                onValueChange = setNewTag,
                                label = { Text("新标签") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            IconButton(
                                onClick = {
                                    if (newTag.isNotBlank() && !tags.contains(newTag)) {
                                        setTags(tags + newTag)
                                        setNewTag("")
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "添加标签")
                            }
                        }

                        if (tags.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                tags.forEach { tag ->
                                    AssistChip(
                                        onClick = { setTags(tags.filter { it != tag }) },
                                        label = { Text(tag) },
                                        trailingIcon = {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "删除",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Save Button
            item {
                Button(
                    onClick = {
                        // Validation
                        if (name.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("请输入菜名")
                            }
                            return@Button
                        }

                        val time = estimatedTime.toIntOrNull()
                        if (time == null || time < 1 || time > 300) {
                            scope.launch {
                                snackbarHostState.showSnackbar("预计时间必须在1-300分钟之间")
                            }
                            return@Button
                        }

                        if (ingredients.any { it.name.isBlank() }) {
                            scope.launch {
                                snackbarHostState.showSnackbar("请填写所有食材名称")
                            }
                            return@Button
                        }

                        if (steps.any { it.description.isBlank() }) {
                            scope.launch {
                                snackbarHostState.showSnackbar("请填写所有步骤描述")
                            }
                            return@Button
                        }

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
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving
                ) {
                    Text(
                        text = if (isSaving) {
                            if (isEditMode) "保存中..." else "添加中..."
                        } else {
                            if (isEditMode) "保存" else "添加"
                        }
                    )
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
