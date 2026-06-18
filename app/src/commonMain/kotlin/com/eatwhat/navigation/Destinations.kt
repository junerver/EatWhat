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

  object RollResult :
    Destinations("roll/result/{meatCount}/{vegCount}/{soupCount}/{stapleCount}/{randomCount}") {
    fun createRoute(
      meatCount: Int,
      vegCount: Int,
      soupCount: Int,
      stapleCount: Int,
      randomCount: Int = 0
    ) = "roll/result/$meatCount/$vegCount/$soupCount/$stapleCount/$randomCount"
    }

    // Settings destinations
    object Settings : Destinations("settings")
    object WebDAVConfig : Destinations("settings/webdav")
    object Sync : Destinations("settings/sync")

  // AI destinations
  object AIConfig : Destinations("settings/ai") // Lists all providers
  object AIProviderEdit : Destinations("settings/ai/edit?providerId={providerId}") {
    fun createRoute(providerId: Long? = null) = if (providerId != null) {
      "settings/ai/edit?providerId=$providerId"
    } else {
      "settings/ai/edit"
    }
  }

  object AIAnalysis : Destinations("ai/analysis?initialPrompt={initialPrompt}") {
    fun createRoute(initialPrompt: String? = null): String {
      return if (initialPrompt != null) {
        val encodedPrompt = encodeRouteComponent(initialPrompt)
        "ai/analysis?initialPrompt=$encodedPrompt"
      } else {
        "ai/analysis"
      }
    }
  }
}

private fun encodeRouteComponent(value: String): String {
  val hex = "0123456789ABCDEF"
  return buildString {
    value.encodeToByteArray().forEach { byte ->
      val code = byte.toInt() and 0xFF
      val char = code.toChar()
      if (
        char in 'A'..'Z' ||
        char in 'a'..'z' ||
        char in '0'..'9' ||
        char == '-' ||
        char == '_' ||
        char == '.' ||
        char == '~'
      ) {
        append(char)
      } else {
        append('%')
        append(hex[code shr 4])
        append(hex[code and 0x0F])
      }
    }
  }
}
