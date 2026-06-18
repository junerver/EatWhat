package com.eatwhat.ui.screens.recipe

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.eatwhat.EatWhatApplication
import com.eatwhat.navigation.Destinations
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.getValue
import xyz.junerver.compose.hooks.useCreation

@Composable
fun RecipeDetailScreen(
    navController: NavController,
    recipeId: Long
) {
    val context = LocalContext.current
    val app = context.applicationContext as EatWhatApplication
    val repository by useCreation { app.recipeRepository }
    val scope = rememberCoroutineScope()

    val recipe by repository.getRecipeById(recipeId).collectAsState(initial = null)

    RecipeDetailContent(
        recipe = recipe,
        onNavigateUp = { navController.navigateUp() },
        onEdit = { navController.navigate(Destinations.EditRecipe.createRoute(recipeId)) },
        onDeleteConfirmed = {
            scope.launch {
                repository.deleteRecipe(recipeId)
                navController.navigateUp()
            }
        }
    )
}
