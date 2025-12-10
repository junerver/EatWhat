package com.eatwhat.navigation

/**
 * Navigation destinations for the app
 * Sealed class ensures type safety for routes
 */
sealed class Destinations(val route: String) {
    // Bottom navigation destinations
    object Roll : Destinations("roll")
    object RecipeList : Destinations("recipes")
    object History : Destinations("history")

    // Detail destinations
    object RecipeDetail : Destinations("recipe/{recipeId}") {
        fun createRoute(recipeId: Long) = "recipe/$recipeId"
    }

    object AddRecipe : Destinations("recipe/add")

    object EditRecipe : Destinations("recipe/edit/{recipeId}") {
        fun createRoute(recipeId: Long) = "recipe/edit/$recipeId"
    }

    object Prep : Destinations("prep")

    object HistoryDetail : Destinations("history/{historyId}") {
        fun createRoute(historyId: Long) = "history/$historyId"
    }
}
