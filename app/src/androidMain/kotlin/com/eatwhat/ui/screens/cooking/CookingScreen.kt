package com.eatwhat.ui.screens.cooking

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.eatwhat.EatWhatApplication
import com.eatwhat.data.database.entities.CookingStepEntity
import com.eatwhat.ui.theme.DarkBackground
import com.eatwhat.ui.theme.InputBackground
import com.eatwhat.ui.theme.LightBorder
import com.eatwhat.ui.theme.PageBackground
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
import xyz.junerver.compose.palette.components.progress.PProgress
import xyz.junerver.compose.palette.components.tag.PTag
import xyz.junerver.compose.palette.components.tag.TagColors
import xyz.junerver.compose.palette.components.tag.TagSize
import xyz.junerver.compose.palette.components.text.PText


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookingScreen(
    navController: NavController
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as EatWhatApplication
    val recipes = app.currentCookingRecipes ?: emptyList()

    if (recipes.isEmpty()) {
        // No recipes, navigate back
        LaunchedEffect(Unit) {
            navController.navigateUp()
        }
        return
    }

  var currentRecipeIndex by useState(0)
  var currentStepIndex by useState(0)
  var completedSteps by useState(setOf<String>())

    val currentRecipe = recipes.getOrNull(currentRecipeIndex)
    val currentStep = currentRecipe?.steps?.getOrNull(currentStepIndex)
    val totalSteps = recipes.sumOf { it.steps?.size ?: 0 }
    val progress = if (totalSteps > 0) completedSteps.size.toFloat() / totalSteps else 0f

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        PText(
                            "做菜指导",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    ),
                    windowInsets = WindowInsets.statusBars
                )
                
                // Progress bar
                Surface(
                    color = Color.White
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
                                color = Color.Gray
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
                            modifier = Modifier
                                .fillMaxWidth(),
                            progressColor = if (completedSteps.size == totalSteps) SoftGreen else PrimaryOrange,
                            trackColor = LightBorder,
                            formatter = null
                        )
                    }
                }
            }
        },
        containerColor = PageBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(paddingValues)
        ) {
            // Recipe selector (if multiple recipes)
            if (recipes.size > 1) {
                Surface(
                    color = Color.White,
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
                            val recipeSteps = recipe.steps ?: emptyList()
                            val recipeCompletedSteps = recipeSteps.count { step ->
                                completedSteps.contains("${recipe.recipe.id}_${step.stepNumber}")
                            }
                            val isComplete = recipeCompletedSteps == recipeSteps.size && recipeSteps.isNotEmpty()

                            Surface(
                                onClick = { 
                                    currentRecipeIndex = index
                                    currentStepIndex = 0
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = when {
                                    isComplete -> SoftGreen.copy(alpha = 0.1f)
                                    isSelected -> PrimaryOrange.copy(alpha = 0.1f)
                                    else -> UnselectedBackground
                                },
                                border = when {
                                    isComplete -> androidx.compose.foundation.BorderStroke(2.dp, SoftGreen)
                                    isSelected -> androidx.compose.foundation.BorderStroke(2.dp, PrimaryOrange)
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
                                        text = recipe.recipe.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isComplete -> SoftGreen
                                            isSelected -> PrimaryOrange
                                            else -> Color.Gray
                                        },
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current recipe card
            currentRecipe?.let { recipe ->
                LazyColumn(
                    modifier = Modifier
                      .weight(1f)
                      .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Recipe header
                    item {
                        PCard(
                            modifier = Modifier
                              .fillMaxWidth(),
                            variant = CardVariant.Elevated,
                            colors = CardColors(
                                containerColor = Color.White,
                                contentColor = DarkBackground
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                  .fillMaxWidth(),
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
                                        Icons.Outlined.MenuBook,
                                        contentDescription = null,
                                        tint = SoftBlue,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Column {
                                    PText(
                                        text = recipe.recipe.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkBackground
                                    )
                                    PText(
                                        text = "共${recipe.steps?.size ?: 0}个步骤",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }

                    // Steps list
                    val steps = recipe.steps ?: emptyList()
                    itemsIndexed(steps) { index, step ->
                        val stepKey = "${recipe.recipe.id}_${step.stepNumber}"
                        val isCompleted = completedSteps.contains(stepKey)
                        val isCurrent = index == currentStepIndex

                        CookingStepCard(
                            step = step,
                            stepNumber = index + 1,
                            isCurrent = isCurrent,
                            isCompleted = isCompleted,
                            isLast = index == steps.lastIndex,
                            onStepClick = {
                                completedSteps = if (isCompleted) {
                                    completedSteps - stepKey
                                } else {
                                    completedSteps + stepKey
                                }
                            }
                        )
                    }

                    // Bottom spacing
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            // Navigation buttons
            Surface(
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val canGoBack = currentStepIndex > 0 || currentRecipeIndex > 0
                    OutlinedNavigationButton(
                        text = "上一步",
                        enabled = canGoBack,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (currentStepIndex > 0) {
                                currentStepIndex--
                            } else if (currentRecipeIndex > 0) {
                                currentRecipeIndex--
                                currentStepIndex =
                                    (recipes.getOrNull(currentRecipeIndex)?.steps?.size ?: 1) - 1
                            }
                        }
                    )

                    val currentSteps = currentRecipe?.steps ?: emptyList()
                    val isLastStep = currentStepIndex >= currentSteps.size - 1
                    val isLastRecipe = currentRecipeIndex >= recipes.size - 1
                    val isFinished = isLastStep && isLastRecipe

                    PButton(
                        text = if (isFinished) "✓ 完成" else "下一步",
                        modifier = Modifier.weight(1f),
                        colors = ButtonColors(
                            containerColor = if (isFinished) SoftGreen else PrimaryOrange,
                            contentColor = Color.White
                        ),
                        onClick = {
                            if (!isLastStep) {
                                currentStepIndex++
                            } else if (!isLastRecipe) {
                                currentRecipeIndex++
                                currentStepIndex = 0
                            } else {
                                // All done, navigate back
                                navController.navigateUp()
                            }
                        }
                    )
                }
            }
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
            borderColor = if (enabled) PrimaryOrange else Color.Gray.copy(alpha = 0.3f)
        ),
        onClick = onClick
    )
}

@Composable
private fun CookingStepCard(
    step: CookingStepEntity,
    stepNumber: Int,
    isCurrent: Boolean,
    isCompleted: Boolean,
    isLast: Boolean,
    onStepClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Step number badge with timeline
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
                            else -> LightBorder
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
                        color = if (isCurrent) Color.White else Color.Gray
                    )
                }
            }
            
            // Timeline connector
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(12.dp)
                        .background(
                            if (isCompleted) SoftGreen.copy(alpha = 0.5f) else LightBorder
                        )
                )
            }
        }

        // Step content card
        PCard(
            modifier = Modifier
                .weight(1f),
            variant = if (isCurrent) CardVariant.Elevated else CardVariant.Filled,
            colors = CardColors(
                containerColor = when {
                    isCompleted -> SoftGreen.copy(alpha = 0.1f)
                    isCurrent -> Color.White
                    else -> InputBackground
                },
                contentColor = DarkBackground
            ),
            onClick = onStepClick
        ) {
            Column(
                modifier = Modifier
            ) {
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
                            else -> Color.Gray
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
                    color = when {
                        isCompleted -> DarkBackground.copy(alpha = 0.6f)
                        else -> DarkBackground
                    },
                    lineHeight = 22.sp
                )
            }
        }
    }
}
