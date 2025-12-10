package com.eatwhat.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eatwhat.ui.components.BottomNavBar

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
            // Bottom navigation screens
            composable(Destinations.Roll.route) {
                // RollScreen will be implemented in Phase 3
                PlaceholderScreen("Roll点")
            }

            composable(Destinations.RecipeList.route) {
                // RecipeListScreen will be implemented in Phase 4
                PlaceholderScreen("菜谱")
            }

            composable(Destinations.History.route) {
                // HistoryListScreen will be implemented in Phase 6
                PlaceholderScreen("历史")
            }

            // Detail screens will be added in later phases
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    androidx.compose.material3.Text(
        text = title,
        modifier = Modifier.padding(16.dp)
    )
}
