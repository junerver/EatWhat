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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.eatwhat.EatWhatApplication
import com.eatwhat.data.database.entities.CookingStepEntity
import com.eatwhat.ui.theme.*


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

    var currentRecipeIndex by remember { mutableStateOf(0) }
    var currentStepIndex by remember { mutableStateOf(0) }
    var completedSteps by remember { mutableStateOf(setOf<String>()) }

    val currentRecipe = recipes.getOrNull(currentRecipeIndex)
    val currentStep = currentRecipe?.steps?.getOrNull(currentStepIndex)
    val totalSteps = recipes.sumOf { it.steps?.size ?: 0 }
    val progress = if (totalSteps > 0) completedSteps.size.toFloat() / totalSteps else 0f

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
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
                            Text(
                                text = "完成进度",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray
                            )
                            Text(
                                text = "${completedSteps.size} / $totalSteps 步",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (completedSteps.size == totalSteps) SoftGreen else PrimaryOrange
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (completedSteps.size == totalSteps) SoftGreen else PrimaryOrange,
                            trackColor = LightBorder
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
                                    Text(
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
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
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
                                    Text(
                                        text = recipe.recipe.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkBackground
                                    )
                                    Text(
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
                    OutlinedButton(
                        onClick = {
                            if (currentStepIndex > 0) {
                                currentStepIndex--
                            } else if (currentRecipeIndex > 0) {
                                currentRecipeIndex--
                                currentStepIndex =
                                    (recipes.getOrNull(currentRecipeIndex)?.steps?.size ?: 1) - 1
                            }
                        },
                        enabled = currentStepIndex > 0 || currentRecipeIndex > 0,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PrimaryOrange
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp,
                            if (currentStepIndex > 0 || currentRecipeIndex > 0) PrimaryOrange else Color.Gray.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "上一步",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    val currentSteps = currentRecipe?.steps ?: emptyList()
                    val isLastStep = currentStepIndex >= currentSteps.size - 1
                    val isLastRecipe = currentRecipeIndex >= recipes.size - 1
                    val isFinished = isLastStep && isLastRecipe

                    Button(
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
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFinished) SoftGreen else PrimaryOrange
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isFinished) "✓ 完成" else "下一步",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
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
                    Text(
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
        Card(
            modifier = Modifier
                .weight(1f)
                .shadow(
                    elevation = if (isCurrent) 4.dp else 0.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = Color.Black.copy(alpha = 0.1f)
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isCompleted -> SoftGreen.copy(alpha = 0.1f)
                    isCurrent -> Color.White
                    else -> InputBackground
                }
            ),
            border = when {
                isCompleted -> androidx.compose.foundation.BorderStroke(1.dp, SoftGreen.copy(alpha = 0.3f))
                isCurrent -> androidx.compose.foundation.BorderStroke(2.dp, PrimaryOrange)
                else -> null
            },
            onClick = onStepClick
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
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
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SoftGreen.copy(alpha = 0.1f)
                        ) {
                            Text(
                                "已完成",
                                style = MaterialTheme.typography.labelSmall,
                                color = SoftGreen,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
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
