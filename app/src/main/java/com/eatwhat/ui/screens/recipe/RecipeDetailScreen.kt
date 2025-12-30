package com.eatwhat.ui.screens.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.eatwhat.EatWhatApplication
import com.eatwhat.navigation.Destinations
import com.eatwhat.ui.components.IconSize
import com.eatwhat.ui.components.RecipeIcon
import com.eatwhat.ui.components.SimpleCircularProgressIndicator
import com.eatwhat.ui.theme.ErrorRed
import com.eatwhat.ui.theme.IngredientCardBackground
import com.eatwhat.ui.theme.LocalDarkTheme
import com.eatwhat.ui.theme.MeatRed
import com.eatwhat.ui.theme.PrimaryOrange
import com.eatwhat.ui.theme.SoftBlue
import com.eatwhat.ui.theme.SoftGreen
import com.eatwhat.ui.theme.SoupBlue
import com.eatwhat.ui.theme.StapleOrange
import com.eatwhat.ui.theme.StepCardBackground
import com.eatwhat.ui.theme.TagPastelColors
import com.eatwhat.ui.theme.VegGreen
import com.eatwhat.ui.theme.WarmYellow
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useGetState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecipeDetailScreen(
    navController: NavController,
    recipeId: Long
) {
    val context = LocalContext.current
    val app = context.applicationContext as EatWhatApplication
    val repository = remember { app.recipeRepository }
    val scope = rememberCoroutineScope()

    val recipe by repository.getRecipeById(recipeId).collectAsState(initial = null)
  val (showDeleteDialog, setShowDeleteDialog) = useGetState(default = false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("菜谱详情", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 编辑按钮
                    IconButton(onClick = {
                        navController.navigate(Destinations.EditRecipe.createRoute(recipeId))
                    }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "编辑",
                            tint = PrimaryOrange
                        )
                    }
                    // 删除按钮
                    IconButton(onClick = { setShowDeleteDialog(true) }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = ErrorRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                windowInsets = WindowInsets.statusBars
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        recipe?.let { recipeData ->
            LazyColumn(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Card
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
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Recipe Icon/Image
                            RecipeIcon(
                                emoji = recipeData.icon,
                                imageBase64 = recipeData.imageBase64,
                                size = IconSize.LARGE
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Recipe Name
                            Text(
                                text = recipeData.name,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Info chips row
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Type chip
                                val (typeEmoji, typeName, typeColor) = when (recipeData.type.name) {
                                    "MEAT" -> Triple("🍗", "荤菜", MeatRed)
                                    "VEG" -> Triple("🥬", "素菜", VegGreen)
                                    "SOUP" -> Triple("🍲", "汤", SoupBlue)
                                    "STAPLE" -> Triple("🍚", "主食", StapleOrange)
                                    else -> Triple("🍽️", recipeData.type.name, Color.Gray)
                                }
                                InfoChip(
                                    emoji = typeEmoji,
                                    text = typeName,
                                    color = typeColor
                                )

                                // Difficulty chip
                                val (diffEmoji, diffName, diffColor) = when (recipeData.difficulty.name) {
                                    "EASY" -> Triple("⭐", "简单", SoftGreen)
                                    "MEDIUM" -> Triple("⭐⭐", "中等", WarmYellow)
                                    "HARD" -> Triple("⭐⭐⭐", "困难", MeatRed)
                                    else -> Triple("⭐", recipeData.difficulty.name, Color.Gray)
                                }
                                InfoChip(
                                    emoji = diffEmoji,
                                    text = diffName,
                                    color = diffColor
                                )

                              // Time chip
                                InfoChip(
                                    emoji = "⏱️",
                                    text = "${recipeData.estimatedTime}分钟",
                                    color = SoftBlue
                                )
                            }

                          // Tags section
                          if (recipeData.tags.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))

                            FlowRow(
                              modifier = Modifier.fillMaxWidth(),
                              horizontalArrangement = Arrangement.spacedBy(8.dp),
                              verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                              recipeData.tags.forEach { tag ->
                                TagChip(tag.name)
                              }
                            }
                            }
                        }
                    }
                }

                // Ingredients Section
                item {
                    SectionCard(
                        title = "食材清单",
                        icon = Icons.Outlined.ShoppingCart,
                        iconBackgroundColor = SoftGreen.copy(alpha = 0.1f),
                        iconTint = SoftGreen
                    ) {
                        recipeData.ingredients.forEachIndexed { index, ingredient ->
                            IngredientRow(
                                index = index + 1,
                                name = ingredient.name,
                                amount = ingredient.amount,
                                unit = when (ingredient.unit.name) {
                                    "G" -> "克"
                                    "ML" -> "毫升"
                                    "PIECE" -> "个"
                                    "SPOON" -> "勺"
                                    "MODERATE" -> "适量"
                                    else -> ingredient.unit.name
                                }
                            )
                            if (index < recipeData.ingredients.lastIndex) {
                                Spacer(modifier = Modifier.height(8.dp))
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
                        iconTint = SoftBlue
                    ) {
                        recipeData.steps.forEachIndexed { index, step ->
                            StepRow(
                                stepNumber = step.stepNumber,
                                description = step.description,
                                isLast = index == recipeData.steps.lastIndex
                            )
                            if (index < recipeData.steps.lastIndex) {
                                // Timeline connector
                                Row(
                                    modifier = Modifier
                                      .fillMaxWidth()
                                      .padding(start = 20.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                          .width(2.dp)
                                          .height(12.dp)
                                          .background(SoftBlue.copy(alpha = 0.3f))
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        } ?: Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SimpleCircularProgressIndicator(
                    color = PrimaryOrange,
                    strokeWidth = 4.dp
                )
                Text(
                    text = "加载中...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Delete confirmation dialog
  if (showDeleteDialog.value) {
        AlertDialog(
            onDismissRequest = { setShowDeleteDialog(false) },
            icon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = ErrorRed
                )
            },
            title = { Text("删除菜谱") },
            text = { Text("确定要删除这个菜谱吗？此操作无法撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            repository.deleteRecipe(recipeId)
                            setShowDeleteDialog(false)
                            navController.navigateUp()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = ErrorRed
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { setShowDeleteDialog(false) }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * Section card with title and icon
 */
@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    iconBackgroundColor: Color,
    iconTint: Color,
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            content()
        }
    }
}

/**
 * Info chip for recipe metadata
 */
@Composable
private fun InfoChip(
    emoji: String,
    text: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 14.sp)
            Text(
                text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

/**
 * Ingredient row with index
 */
@Composable
private fun IngredientRow(
    index: Int,
    name: String,
    amount: String,
    unit: String
) {
  val isDark = LocalDarkTheme.current
    val rowBackground = if (isDark) MaterialTheme.colorScheme.surfaceVariant else IngredientCardBackground

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = rowBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, SoftGreen.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                        "$index",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftGreen
                    )
                }
                Text(
                    name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = if (unit == "适量") "适量" else "$amount$unit",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Step row with timeline design
 */
@Composable
private fun StepRow(
    stepNumber: Int,
    description: String,
    isLast: Boolean
) {
  val isDark = LocalDarkTheme.current
    val stepBackground = if (isDark) MaterialTheme.colorScheme.surfaceVariant else StepCardBackground

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
            shape = RoundedCornerShape(12.dp),
            color = stepBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, SoftBlue.copy(alpha = 0.2f)),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(12.dp),
                lineHeight = 22.sp
            )
        }
    }
}

/**
 * Tag chip with pastel color
 */
@Composable
private fun TagChip(text: String) {
    val backgroundColor = TagPastelColors[text.hashCode().mod(TagPastelColors.size).let { if (it < 0) -it else it }]

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = Color.Black.copy(alpha = 0.8f)
        )
    }
}
