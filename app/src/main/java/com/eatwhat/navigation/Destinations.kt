package com.eatwhat.navigation

/**
 * Navigation destinations for the app
 * Sealed class ensures type safety for routes
 */
sealed class Destinations(val route: String) {
    // Bottom navigation destinations
    object Roll : Destinations("roll")
    object RecipeList : Destinations("recipes")
    object History : Destinations("history?highlightId={highlightId}") {
        val routeWithoutArgs = "history"
        fun createRoute(highlightId: Long? = null) = if (highlightId != null) {
            "history?highlightId=$highlightId"
        } else {
            "history"
        }
    }

    // Detail destinations
    object RecipeDetail : Destinations("recipe/{recipeId}") {
        fun createRoute(recipeId: Long) = "recipe/$recipeId"
    }

    object AddRecipe : Destinations("recipe/add")

    object EditRecipe : Destinations("recipe/edit/{recipeId}") {
        fun createRoute(recipeId: Long) = "recipe/edit/$recipeId"
    }

    object Prep : Destinations("prep")

    object Cooking : Destinations("cooking")

    object HistoryDetail : Destinations("history/{historyId}") {
        fun createRoute(historyId: Long) = "history/$historyId"
    }

    object RollResult : Destinations("roll/result/{meatCount}/{vegCount}/{soupCount}/{stapleCount}") {
        fun createRoute(meatCount: Int, vegCount: Int, soupCount: Int, stapleCount: Int) =
            "roll/result/$meatCount/$vegCount/$soupCount/$stapleCount"
    }

    // Settings destinations
    object Settings : Destinations("settings")
    object WebDAVConfig : Destinations("settings/webdav")
    object Sync : Destinations("settings/sync")

  // AI destinations
  object AIConfig : Destinations("settings/ai")
  object AIAnalysis : Destinations("ai/analysis")
}
