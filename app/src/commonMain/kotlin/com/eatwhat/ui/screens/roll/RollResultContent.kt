package com.eatwhat.ui.screens.roll

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eatwhat.domain.model.Difficulty
import com.eatwhat.domain.model.Recipe
import com.eatwhat.domain.model.RecipeType
import com.eatwhat.domain.model.RollConfig
import com.eatwhat.ui.components.IconSize
import com.eatwhat.ui.components.RecipeIcon
import com.eatwhat.ui.theme.MeatRed
import com.eatwhat.ui.theme.OtherPurple
import com.eatwhat.ui.theme.PrimaryOrange
import com.eatwhat.ui.theme.SoftBlue
import com.eatwhat.ui.theme.SoftGreen
import com.eatwhat.ui.theme.WarmYellow
import xyz.junerver.compose.palette.components.button.ButtonColors
import xyz.junerver.compose.palette.components.button.ButtonType
import xyz.junerver.compose.palette.components.button.PButton
import xyz.junerver.compose.palette.components.card.CardColors
import xyz.junerver.compose.palette.components.card.CardVariant
import xyz.junerver.compose.palette.components.card.PCard
import xyz.junerver.compose.palette.components.container.PContainer
import xyz.junerver.compose.palette.components.tag.PTag
import xyz.junerver.compose.palette.components.tag.TagColors
import xyz.junerver.compose.palette.components.tag.TagSize
import xyz.junerver.compose.palette.components.text.PText

@Composable
fun RollResultContent(
    recipes: List<Recipe>,
    config: RollConfig,
    onRecipeClick: (Recipe) -> Unit,
    onReRoll: () -> Unit,
    onReRollSingle: (Recipe) -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        PContainer(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                PText(
                    text = "今日菜单",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                PCard(
                    modifier = Modifier.fillMaxWidth(),
                    variant = CardVariant.Elevated,
                    colors = CardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        PText(
                            text = "🎉",
                            fontSize = 40.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        PText(
                            text = "今天就做这些！",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        PText(
                            text = buildSummary(config),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(recipes, key = { it.id }) { recipe ->
                DishCard(
                    recipe = recipe,
                    onClick = { onRecipeClick(recipe) },
                    onReRoll = { onReRollSingle(recipe) }
                )
            }
        }

        PContainer(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PButton(
                    text = "🎲 重新Roll",
                    modifier = Modifier.weight(1f),
                    type = ButtonType.OUTLINED,
                    colors = ButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = PrimaryOrange,
                        borderColor = PrimaryOrange
                    ),
                    onClick = onReRoll
                )

                PButton(
                    text = "✓ 就这些了",
                    modifier = Modifier.weight(1f),
                    colors = ButtonColors(
                        containerColor = PrimaryOrange,
                        contentColor = Color.White
                    ),
                    onClick = onConfirm
                )
            }
        }
    }
}

@Composable
private fun DishCard(
    recipe: Recipe,
    onClick: () -> Unit,
    onReRoll: () -> Unit
) {
    PCard(
        modifier = Modifier.fillMaxWidth(),
        variant = CardVariant.Elevated,
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.clickable(onClick = onClick)) {
                RecipeIcon(
                    emoji = recipe.icon,
                    imageBase64 = recipe.imageBase64,
                    size = IconSize.MEDIUM
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onClick)
            ) {
                PText(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val (typeColor, typeName) = when (recipe.type) {
                        RecipeType.MEAT -> MeatRed to "荤菜"
                        RecipeType.VEG -> SoftGreen to "素菜"
                        RecipeType.SOUP -> SoftBlue to "汤"
                        RecipeType.STAPLE -> WarmYellow to "主食"
                        RecipeType.OTHER -> OtherPurple to "其他"
                    }
                    InfoTag(text = typeName, color = typeColor)
                    InfoTag(text = getDifficultyName(recipe.difficulty), color = Color.Gray)
                    if (recipe.estimatedTime > 0) {
                        InfoTag(text = "${recipe.estimatedTime}分钟", color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            PContainer(
                onClick = onReRoll,
                shape = CircleShape,
                color = PrimaryOrange.copy(alpha = 0.1f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    PText(
                        text = "🎲",
                        fontSize = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoTag(text: String, color: Color) {
    PTag(
        text = text,
        size = TagSize.Small,
        colors = TagColors(
            containerColor = color.copy(alpha = 0.1f),
            contentColor = color,
            borderColor = Color.Transparent
        )
    )
}

private fun buildSummary(config: RollConfig): String {
    val parts = mutableListOf<String>()
    if (config.meatCount > 0) parts.add("${config.meatCount}荤")
    if (config.vegCount > 0) parts.add("${config.vegCount}素")
    if (config.soupCount > 0) parts.add("${config.soupCount}汤")
    if (config.stapleCount > 0) parts.add("${config.stapleCount}主食")
    if (config.randomCount > 0) parts.add("${config.randomCount}随机")
    return if (parts.isEmpty()) "随机菜品" else parts.joinToString(" + ")
}

private fun getDifficultyName(difficulty: Difficulty): String {
    return when (difficulty) {
        Difficulty.EASY -> "简单"
        Difficulty.MEDIUM -> "中等"
        Difficulty.HARD -> "困难"
    }
}
