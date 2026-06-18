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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eatwhat.domain.model.Difficulty
import com.eatwhat.domain.model.Recipe
import com.eatwhat.domain.model.RecipeType
import com.eatwhat.ui.components.AppToolbar
import com.eatwhat.ui.components.IconSize
import com.eatwhat.ui.components.PaletteConfirmDialog
import com.eatwhat.ui.components.RecipeIcon
import com.eatwhat.ui.theme.ErrorRed
import com.eatwhat.ui.theme.IngredientCardBackground
import com.eatwhat.ui.theme.LocalDarkTheme
import com.eatwhat.ui.theme.MeatRed
import com.eatwhat.ui.theme.OtherPurple
import com.eatwhat.ui.theme.PrimaryOrange
import com.eatwhat.ui.theme.SoftBlue
import com.eatwhat.ui.theme.SoftGreen
import com.eatwhat.ui.theme.SoupBlue
import com.eatwhat.ui.theme.StapleOrange
import com.eatwhat.ui.theme.StepCardBackground
import com.eatwhat.ui.theme.TagPastelColors
import com.eatwhat.ui.theme.VegGreen
import com.eatwhat.ui.theme.WarmYellow
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.compose.palette.components.button.ButtonType
import xyz.junerver.compose.palette.components.card.CardColors
import xyz.junerver.compose.palette.components.card.CardVariant
import xyz.junerver.compose.palette.components.card.PCard
import xyz.junerver.compose.palette.components.loading.PLoading
import xyz.junerver.compose.palette.components.scaffold.PScaffold
import xyz.junerver.compose.palette.components.scaffold.ScaffoldDefaults
import xyz.junerver.compose.palette.components.tag.PTag
import xyz.junerver.compose.palette.components.tag.TagColors
import xyz.junerver.compose.palette.components.tag.TagSize
import xyz.junerver.compose.palette.components.text.PText

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipeDetailContent(
    recipe: Recipe?,
    onNavigateUp: () -> Unit,
    onEdit: () -> Unit,
    onDeleteConfirmed: () -> Unit
) {
    val (showDeleteDialog, setShowDeleteDialog) = useGetState(default = false)

    PScaffold(
        topBar = {
            AppToolbar(
                title = "菜谱详情",
                onNavigateUp = onNavigateUp,
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "编辑",
                            tint = PrimaryOrange
                        )
                    }
                    IconButton(onClick = { setShowDeleteDialog(true) }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = ErrorRed
                        )
                    }
                }
            )
        },
        colors = ScaffoldDefaults.colors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) { paddingValues ->
        recipe?.let { recipeData ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    RecipeHeaderCard(recipeData)
                }

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
                                unit = formatIngredientUnit(ingredient.unit.name)
                            )
                            if (index < recipeData.ingredients.lastIndex) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                item {
                    SectionCard(
                        title = "烹饪步骤",
                        icon = Icons.AutoMirrored.Outlined.MenuBook,
                        iconBackgroundColor = SoftBlue.copy(alpha = 0.1f),
                        iconTint = SoftBlue
                    ) {
                        recipeData.steps.forEachIndexed { index, step ->
                            StepRow(
                                stepNumber = step.stepNumber,
                                description = step.description
                            )
                            if (index < recipeData.steps.lastIndex) {
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

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        } ?: LoadingState(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }

    if (showDeleteDialog.value) {
        PaletteConfirmDialog(
            title = "确认删除",
            message = "确定要删除这个菜谱吗？此操作无法撤销。",
            confirmText = "删除",
            confirmType = ButtonType.DANGER,
            icon = Icons.Default.Delete,
            iconTint = ErrorRed,
            onDismiss = { setShowDeleteDialog(false) },
            onConfirm = {
                setShowDeleteDialog(false)
                onDeleteConfirmed()
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecipeHeaderCard(recipe: Recipe) {
    PCard(
        modifier = Modifier.fillMaxWidth(),
        variant = CardVariant.Elevated,
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RecipeIcon(
                emoji = recipe.icon,
                imageBase64 = recipe.imageBase64,
                size = IconSize.LARGE
            )

            Spacer(modifier = Modifier.height(16.dp))

            PText(
                text = recipe.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val (typeEmoji, typeName, typeColor) = when (recipe.type) {
                    RecipeType.MEAT -> Triple("🍗", "荤菜", MeatRed)
                    RecipeType.VEG -> Triple("🥬", "素菜", VegGreen)
                    RecipeType.SOUP -> Triple("🍲", "汤", SoupBlue)
                    RecipeType.STAPLE -> Triple("🍚", "主食", StapleOrange)
                    RecipeType.OTHER -> Triple("🍽️", "其他", OtherPurple)
                }
                InfoChip(
                    emoji = typeEmoji,
                    text = typeName,
                    color = typeColor
                )

                val (diffEmoji, diffName, diffColor) = when (recipe.difficulty) {
                    Difficulty.EASY -> Triple("⭐", "简单", SoftGreen)
                    Difficulty.MEDIUM -> Triple("⭐⭐", "中等", WarmYellow)
                    Difficulty.HARD -> Triple("⭐⭐⭐", "困难", MeatRed)
                }
                InfoChip(
                    emoji = diffEmoji,
                    text = diffName,
                    color = diffColor
                )

                InfoChip(
                    emoji = "⏱️",
                    text = "${recipe.estimatedTime}分钟",
                    color = SoftBlue
                )
            }

            if (recipe.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    recipe.tags.forEach { tag ->
                        TagChip(tag.name)
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PLoading(
                size = 48.dp,
                color = PrimaryOrange
            )
            PText(
                text = "加载中...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    iconBackgroundColor: Color,
    iconTint: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    PCard(
        modifier = Modifier.fillMaxWidth(),
        variant = CardVariant.Elevated,
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column {
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
                PText(
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

@Composable
private fun InfoChip(
    emoji: String,
    text: String,
    color: Color
) {
    PTag(
        text = "$emoji $text",
        size = TagSize.Large,
        colors = TagColors(
            containerColor = color.copy(alpha = 0.1f),
            contentColor = color,
            borderColor = Color.Transparent
        )
    )
}

@Composable
private fun IngredientRow(
    index: Int,
    name: String,
    amount: String,
    unit: String
) {
    val isDark = LocalDarkTheme.current
    val rowBackground = if (isDark) MaterialTheme.colorScheme.surfaceVariant else IngredientCardBackground

    PCard(
        variant = CardVariant.Filled,
        colors = CardColors(
            containerColor = rowBackground,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(SoftGreen.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    PText(
                        "$index",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftGreen
                    )
                }
                PText(
                    name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            PText(
                text = if (unit == "适量") "适量" else "$amount$unit",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StepRow(
    stepNumber: Int,
    description: String
) {
    val isDark = LocalDarkTheme.current
    val stepBackground = if (isDark) MaterialTheme.colorScheme.surfaceVariant else StepCardBackground

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
            PText(
                "$stepNumber",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        PCard(
            variant = CardVariant.Filled,
            colors = CardColors(
                containerColor = stepBackground,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.weight(1f)
        ) {
            PText(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun TagChip(text: String) {
    val backgroundColor = TagPastelColors[text.hashCode().mod(TagPastelColors.size).let { if (it < 0) -it else it }]

    PTag(
        text = text,
        size = TagSize.Medium,
        colors = TagColors(
            containerColor = backgroundColor,
            contentColor = Color.Black.copy(alpha = 0.8f),
            borderColor = Color.Transparent
        )
    )
}

private fun formatIngredientUnit(unit: String): String {
    return when (unit) {
        "G" -> "克"
        "ML" -> "毫升"
        "PIECE" -> "个"
        "SPOON" -> "勺"
        "MODERATE" -> "适量"
        else -> unit
    }
}
