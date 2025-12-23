package com.eatwhat.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eatwhat.ui.components.BottomNavBar
import com.eatwhat.ui.screens.roll.RollScreen
import com.eatwhat.ui.screens.roll.RollResultScreen
import com.eatwhat.ui.screens.recipe.RecipeListScreen
import com.eatwhat.ui.screens.history.HistoryListScreen
import com.eatwhat.ui.screens.cooking.CookingScreen

/**
 * Main navigation graph for the app
 * Sets up NavHost with all destinations
 */
@Composable
fun EatWhatApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Destinations.Roll.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Destinations.Roll.route) {
                RollScreen(navController)
            }

            composable(Destinations.RecipeList.route) {
                RecipeListScreen(navController)
            }

            composable(Destinations.History.route) {
                HistoryListScreen(navController)
            }

            composable(Destinations.RecipeDetail.route) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getString("recipeId")?.toLongOrNull()
                if (recipeId != null) {
                    com.eatwhat.ui.screens.recipe.RecipeDetailScreen(navController, recipeId)
                }
            }

            composable(Destinations.AddRecipe.route) {
                com.eatwhat.ui.screens.recipe.AddRecipeScreen(navController)
            }

            composable(Destinations.EditRecipe.route) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getString("recipeId")?.toLongOrNull()
                com.eatwhat.ui.screens.recipe.AddRecipeScreen(navController, recipeId)
            }

            composable(Destinations.HistoryDetail.route) { backStackEntry ->
                val historyId = backStackEntry.arguments?.getString("historyId")?.toLongOrNull()
                if (historyId != null) {
                    com.eatwhat.ui.screens.history.HistoryDetailScreen(navController, historyId)
                }
            }

            composable(Destinations.RollResult.route) { backStackEntry ->
                val meatCount = backStackEntry.arguments?.getString("meatCount")?.toIntOrNull() ?: 0
                val vegCount = backStackEntry.arguments?.getString("vegCount")?.toIntOrNull() ?: 0
                val soupCount = backStackEntry.arguments?.getString("soupCount")?.toIntOrNull() ?: 0
                val stapleCount = backStackEntry.arguments?.getString("stapleCount")?.toIntOrNull() ?: 0
                RollResultScreen(navController, meatCount, vegCount, soupCount, stapleCount)
            }

            composable(Destinations.Prep.route) {
                com.eatwhat.ui.screens.prep.PrepScreen(navController)
            }

            composable(Destinations.Cooking.route) {
                CookingScreen(navController)
            }
        }
    }
}
