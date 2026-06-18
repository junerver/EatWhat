package com.eatwhat.ui.screens.cooking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eatwhat.domain.model.CookingStep
import com.eatwhat.domain.model.Recipe
import com.eatwhat.ui.components.AppToolbar
import com.eatwhat.ui.theme.DarkProgressTrack
import com.eatwhat.ui.theme.InputBackground
import com.eatwhat.ui.theme.LightBorder
import com.eatwhat.ui.theme.LocalDarkTheme
import com.eatwhat.ui.theme.PrimaryOrange
import com.eatwhat.ui.theme.SoftBlue
import com.eatwhat.ui.theme.SoftGreen
import com.eatwhat.ui.theme.UnselectedBackground
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.palette.components.button.ButtonColors
import xyz.junerver.compose.palette.components.button.ButtonType
import xyz.junerver.compose.palette.components.button.PButton
import xyz.junerver.compose.palette.components.card.CardColors
import xyz.junerver.compose.palette.components.card.CardVariant
import xyz.junerver.compose.palette.components.card.PCard
import xyz.junerver.compose.palette.components.container.PContainer
import xyz.junerver.compose.palette.components.progress.PProgress
import xyz.junerver.compose.palette.components.scaffold.PScaffold
import xyz.junerver.compose.palette.components.scaffold.ScaffoldDefaults
import xyz.junerver.compose.palette.components.tag.PTag
import xyz.junerver.compose.palette.components.tag.TagColors
import xyz.junerver.compose.palette.components.tag.TagSize
import xyz.junerver.compose.palette.components.text.PText

@Composable
fun CookingContent(
    recipes: List<Recipe>,
    onNavigateUp: () -> Unit,
    onFinish: () -> Unit
) {
    var currentRecipeIndex by useState(0)
    var currentStepIndex by useState(0)
    var completedSteps by useState(setOf<String>())

    val isDark = LocalDarkTheme.current
    val currentRecipe = recipes.getOrNull(currentRecipeIndex)
    val totalSteps = recipes.sumOf { it.steps.size }
    val progress = if (totalSteps > 0) completedSteps.size.toFloat() / totalSteps else 0f
    val secondaryContainer = if (isDark) MaterialTheme.colorScheme.surfaceVariant else UnselectedBackground
    val stepContainer = if (isDark) MaterialTheme.colorScheme.surfaceVariant else InputBackground
    val progressTrackColor = if (isDark) DarkProgressTrack else LightBorder

    PScaffold(
        topBar = {
            Column {
                AppToolbar(
                    title = "做菜指导",
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    onNavigateUp = onNavigateUp
                )

                PContainer(
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PText(
                                text = "完成进度",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            PText(
                                text = "${completedSteps.size} / $totalSteps 步",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (completedSteps.size == totalSteps) SoftGreen else PrimaryOrange
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        PProgress(
                            percent = progress * 100f,
                            modifier = Modifier.fillMaxWidth(),
                            progressColor = if (completedSteps.size == totalSteps) SoftGreen else PrimaryOrange,
                            trackColor = progressTrackColor,
                            formatter = null
                        )
                    }
                }
            }
        },
        colors = ScaffoldDefaults.colors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (recipes.size > 1) {
                RecipeSelector(
                    recipes = recipes,
                    currentRecipeIndex = currentRecipeIndex,
                    completedSteps = completedSteps,
                    secondaryContainer = secondaryContainer,
                    onRecipeSelected = { index ->
                        currentRecipeIndex = index
                        currentStepIndex = 0
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            currentRecipe?.let { recipe ->
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SoftBlue.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Outlined.MenuBook,
                                        contentDescription = null,
                                        tint = SoftBlue,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Column {
                                    PText(
                                        text = recipe.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    PText(
                                        text = "共${recipe.steps.size}个步骤",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    val steps = recipe.steps
                    itemsIndexed(steps) { index, step ->
                        val stepKey = "${recipe.id}_${step.stepNumber}"
                        val isCompleted = completedSteps.contains(stepKey)
                        val isCurrent = index == currentStepIndex

                        CookingStepCard(
                            step = step,
                            stepNumber = index + 1,
                            isCurrent = isCurrent,
                            isCompleted = isCompleted,
                            isLast = index == steps.lastIndex,
                            stepContainer = stepContainer,
                            progressTrackColor = progressTrackColor,
                            onStepClick = {
                                completedSteps = if (isCompleted) {
                                    completedSteps - stepKey
                                } else {
                                    completedSteps + stepKey
                                }
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            NavigationButtons(
                recipes = recipes,
                currentRecipeIndex = currentRecipeIndex,
                currentStepIndex = currentStepIndex,
                onPrevious = {
                    if (currentStepIndex > 0) {
                        currentStepIndex--
                    } else if (currentRecipeIndex > 0) {
                        currentRecipeIndex--
                        currentStepIndex = recipes.getOrNull(currentRecipeIndex)?.steps?.lastIndex?.coerceAtLeast(0) ?: 0
                    }
                },
                onNext = {
                    val currentSteps = currentRecipe?.steps.orEmpty()
                    val isLastStep = currentStepIndex >= currentSteps.size - 1
                    val isLastRecipe = currentRecipeIndex >= recipes.size - 1

                    if (!isLastStep) {
                        currentStepIndex++
                    } else if (!isLastRecipe) {
                        currentRecipeIndex++
                        currentStepIndex = 0
                    } else {
                        onFinish()
                    }
                }
            )
        }
    }
}

@Composable
private fun RecipeSelector(
    recipes: List<Recipe>,
    currentRecipeIndex: Int,
    completedSteps: Set<String>,
    secondaryContainer: Color,
    onRecipeSelected: (Int) -> Unit
) {
    PContainer(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            recipes.forEachIndexed { index, recipe ->
                val isSelected = index == currentRecipeIndex
                val recipeCompletedSteps = recipe.steps.count { step ->
                    completedSteps.contains("${recipe.id}_${step.stepNumber}")
                }
                val isComplete = recipeCompletedSteps == recipe.steps.size && recipe.steps.isNotEmpty()

                PContainer(
                    onClick = { onRecipeSelected(index) },
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        isComplete -> SoftGreen.copy(alpha = 0.1f)
                        isSelected -> PrimaryOrange.copy(alpha = 0.1f)
                        else -> secondaryContainer
                    },
                    border = when {
                        isComplete -> BorderStroke(2.dp, SoftGreen)
                        isSelected -> BorderStroke(2.dp, PrimaryOrange)
                        else -> null
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isComplete) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = SoftGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        PText(
                            text = recipe.name,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = when {
                                isComplete -> SoftGreen
                                isSelected -> PrimaryOrange
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavigationButtons(
    recipes: List<Recipe>,
    currentRecipeIndex: Int,
    currentStepIndex: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val currentRecipe = recipes.getOrNull(currentRecipeIndex)
    val currentSteps = currentRecipe?.steps.orEmpty()
    val canGoBack = currentStepIndex > 0 || currentRecipeIndex > 0
    val isLastStep = currentStepIndex >= currentSteps.size - 1
    val isLastRecipe = currentRecipeIndex >= recipes.size - 1
    val isFinished = isLastStep && isLastRecipe

    PContainer(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedNavigationButton(
                text = "上一步",
                enabled = canGoBack,
                modifier = Modifier.weight(1f),
                onClick = onPrevious
            )

            PButton(
                text = if (isFinished) "✓ 完成" else "下一步",
                modifier = Modifier.weight(1f),
                colors = ButtonColors(
                    containerColor = if (isFinished) SoftGreen else PrimaryOrange,
                    contentColor = Color.White
                ),
                onClick = onNext
            )
        }
    }
}

@Composable
private fun OutlinedNavigationButton(
    text: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    PButton(
        text = text,
        modifier = modifier,
        type = ButtonType.OUTLINED,
        disabled = !enabled,
        colors = ButtonColors(
            containerColor = Color.Transparent,
            contentColor = PrimaryOrange,
            borderColor = if (enabled) PrimaryOrange else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        ),
        onClick = onClick
    )
}

@Composable
private fun CookingStepCard(
    step: CookingStep,
    stepNumber: Int,
    isCurrent: Boolean,
    isCompleted: Boolean,
    isLast: Boolean,
    stepContainer: Color,
    progressTrackColor: Color,
    onStepClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompleted -> SoftGreen
                            isCurrent -> PrimaryOrange
                            else -> progressTrackColor
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    PText(
                        "$stepNumber",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(12.dp)
                        .background(
                            if (isCompleted) SoftGreen.copy(alpha = 0.5f) else progressTrackColor
                        )
                )
            }
        }

        PCard(
            modifier = Modifier.weight(1f),
            variant = if (isCurrent) CardVariant.Elevated else CardVariant.Filled,
            colors = CardColors(
                containerColor = when {
                    isCompleted -> SoftGreen.copy(alpha = 0.1f)
                    isCurrent -> MaterialTheme.colorScheme.surface
                    else -> stepContainer
                },
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            onClick = onStepClick
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PText(
                        text = "步骤 $stepNumber",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = when {
                            isCompleted -> SoftGreen
                            isCurrent -> PrimaryOrange
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    if (isCompleted) {
                        PTag(
                            text = "已完成",
                            size = TagSize.Small,
                            colors = TagColors(
                                containerColor = SoftGreen.copy(alpha = 0.1f),
                                contentColor = SoftGreen,
                                borderColor = Color.Transparent
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                PText(
                    text = step.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isCompleted) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    lineHeight = 22.sp
                )
            }
        }
    }
}
