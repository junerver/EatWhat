package com.eatwhat.ui.screens.cooking

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.eatwhat.data.database.entities.CookingStepEntity
import com.eatwhat.ui.theme.EatWhatTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookingScreen(
  navController: NavController
) {
  val context = androidx.compose.ui.platform.LocalContext.current
  val app = context.applicationContext as com.eatwhat.EatWhatApplication
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

  EatWhatTheme {
    Scaffold(
      topBar = {
        TopAppBar(
          title = { Text("做菜指导") },
          navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
              Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
            }
          }
        )
      }
    ) { paddingValues ->
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .padding(16.dp)
      ) {
        // Progress bar
        LinearProgressIndicator(
          progress = if (totalSteps > 0) {
            completedSteps.size.toFloat() / totalSteps
          } else 0f,
          modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Recipe selector
        if (recipes.size > 1) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            recipes.forEachIndexed { index, recipe ->
              FilterChip(
                onClick = { currentRecipeIndex = index },
                selected = index == currentRecipeIndex,
                label = { Text(recipe.recipe.name) },
                modifier = Modifier.weight(1f)
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Current recipe name
          currentRecipe?.recipe?.name?.let { recipeName ->
            Text(
              text = recipeName,
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Steps list
          LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            itemsIndexed(currentRecipe?.steps ?: emptyList()) { index, step ->
              CookingStepItem(
                step = step,
                stepNumber = index + 1,
                isCurrent = index == currentStepIndex,
                isCompleted = completedSteps.contains("${currentRecipe?.recipe?.id}_${step.stepNumber}"),
                onStepClick = {
                  val stepKey = "${currentRecipe?.recipe?.id}_${step.stepNumber}"
                  completedSteps = if (completedSteps.contains(stepKey)) {
                    completedSteps - stepKey
                  } else {
                    completedSteps + stepKey
                  }
                }
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Navigation buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            OutlinedButton(
              onClick = {
                if (currentStepIndex > 0) {
                  currentStepIndex--
                } else if (currentRecipeIndex > 0) {
                  currentRecipeIndex--
                  currentStepIndex =
                    (recipes.getOrNull(currentRecipeIndex - 1)?.steps?.size ?: 1) - 1
                }
              },
              enabled = currentStepIndex > 0 || currentRecipeIndex > 0,
              modifier = Modifier.weight(1f)
            ) {
              Text("上一步")
            }

            Button(
              onClick = {
                val currentSteps = currentRecipe?.steps ?: emptyList()
                if (currentStepIndex < currentSteps.size - 1) {
                  currentStepIndex++
                } else if (currentRecipeIndex < recipes.size - 1) {
                  currentRecipeIndex++
                  currentStepIndex = 0
                }
              },
              modifier = Modifier.weight(1f)
            ) {
              Text(
                if (currentStepIndex < (currentRecipe?.steps?.size
                    ?: 0) - 1 || currentRecipeIndex < recipes.size - 1
                ) "下一步" else "完成"
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun CookingStepItem(
  step: CookingStepEntity,
  stepNumber: Int,
  isCurrent: Boolean,
  isCompleted: Boolean,
  onStepClick: () -> Unit
) {
  val backgroundColor = when {
    isCurrent -> MaterialTheme.colorScheme.primaryContainer
    isCompleted -> MaterialTheme.colorScheme.surfaceVariant
    else -> MaterialTheme.colorScheme.surface
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onStepClick() },
    colors = CardDefaults.cardColors(
      containerColor = backgroundColor
    )
  ) {
    Row(
      modifier = Modifier.padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Step number
      Surface(
        modifier = Modifier.size(40.dp),
        shape = MaterialTheme.shapes.small,
        color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
      ) {
        Box(contentAlignment = Alignment.Center) {
          if (isCompleted) {
            Icon(
              Icons.Default.Check,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.size(24.dp)
            )
          } else {
            Text(
              text = "$stepNumber",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.width(16.dp))

      // Step description
      Text(
        text = step.description,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.weight(1f),
        color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
      )
    }
  }
}
