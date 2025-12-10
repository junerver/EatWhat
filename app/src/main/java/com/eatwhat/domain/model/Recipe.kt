package com.eatwhat.domain.model

/**
 * Domain model for Recipe
 * Business logic representation of a recipe
 */
data class Recipe(
    val id: Long = 0,
    val syncId: String,
    val name: String,
    val type: RecipeType,
    val icon: String,
    val difficulty: Difficulty,
    val estimatedTime: Int,
    val ingredients: List<Ingredient> = emptyList(),
    val steps: List<CookingStep> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis()
)

enum class RecipeType {
    MEAT, VEG, SOUP, STAPLE;

    companion object {
        fun fromString(value: String): RecipeType {
            return valueOf(value.uppercase())
        }
    }
}

enum class Difficulty {
    EASY, MEDIUM, HARD;

    companion object {
        fun fromString(value: String): Difficulty {
            return valueOf(value.uppercase())
        }
    }
}
