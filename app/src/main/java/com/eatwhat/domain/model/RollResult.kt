package com.eatwhat.domain.model

/**
 * Domain model for Roll result
 * Contains the randomly selected recipes
 */
data class RollResult(
    val recipes: List<Recipe>,
    val config: RollConfig,
    val timestamp: Long = System.currentTimeMillis()
) {
    val meatRecipes: List<Recipe>
        get() = recipes.filter { it.type == RecipeType.MEAT }

    val vegRecipes: List<Recipe>
        get() = recipes.filter { it.type == RecipeType.VEG }

    val soupRecipes: List<Recipe>
        get() = recipes.filter { it.type == RecipeType.SOUP }

    val stapleRecipes: List<Recipe>
        get() = recipes.filter { it.type == RecipeType.STAPLE }

    fun getSummary(): String {
        val parts = mutableListOf<String>()
        if (meatRecipes.isNotEmpty()) parts.add("${meatRecipes.size}荤")
        if (vegRecipes.isNotEmpty()) parts.add("${vegRecipes.size}素")
        if (soupRecipes.isNotEmpty()) parts.add("${soupRecipes.size}汤")
        if (stapleRecipes.isNotEmpty()) parts.add("${stapleRecipes.size}主食")
        return parts.joinToString(" ")
    }
}
