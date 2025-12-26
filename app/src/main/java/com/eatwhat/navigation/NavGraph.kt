package com.eatwhat.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 定义应该隐藏底部导航栏的页面
    val hideBottomBarRoutes = setOf(
        Destinations.RollResult.route,
        Destinations.Prep.route,
        Destinations.HistoryDetail.route,
        Destinations.RecipeDetail.route,
        Destinations.AddRecipe.route,
        Destinations.EditRecipe.route,
        Destinations.Cooking.route,
        Destinations.Settings.route,
        Destinations.WebDAVConfig.route,
        Destinations.Sync.route
    )

    // 判断当前路由是否应该隐藏底部栏（支持带参数的路由）
    val shouldHideBottomBar = hideBottomBarRoutes.any { route ->
        currentRoute?.startsWith(route.substringBefore("{")) == true
    }

    Scaffold(
        bottomBar = {
            if (!shouldHideBottomBar) {
                BottomNavBar(navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Destinations.Roll.route,
            // 不应用 paddingValues，让各 Screen 自行处理 insets
            modifier = Modifier
        ) {
            composable(Destinations.Roll.route) {
                RollScreen(navController)
            }

            composable(Destinations.RecipeList.route) {
                RecipeListScreen(navController)
            }

            composable(
                route = Destinations.History.route,
                arguments = listOf(
                    navArgument("highlightId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val highlightId = backStackEntry.arguments?.getString("highlightId")?.toLongOrNull()
                HistoryListScreen(navController, highlightId)
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

            // Settings routes
            composable(Destinations.Settings.route) {
                com.eatwhat.ui.screens.settings.SettingsScreen(navController)
            }

            composable(Destinations.WebDAVConfig.route) {
                com.eatwhat.ui.screens.settings.WebDAVConfigScreen(navController)
            }

            composable(Destinations.Sync.route) {
                com.eatwhat.ui.screens.settings.SyncScreen(navController)
            }
        }
    }
}
