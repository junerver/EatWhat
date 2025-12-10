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
import com.eatwhat.ui.screens.recipe.RecipeListScreen
import com.eatwhat.ui.screens.history.HistoryListScreen

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
        }
    }
}
