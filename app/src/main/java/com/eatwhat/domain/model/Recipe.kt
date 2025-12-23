package com.eatwhat.domain.model

/**
 * Domain model for Recipe
 * Business logic representation of a recipe
 *
 * @property icon Emoji icon for the recipe (fallback when imageBase64 is null)
 * @property imageBase64 Base64 encoded WebP image of the finished dish (optional, takes precedence over icon)
 */
data class Recipe(
    val id: Long = 0,
    val syncId: String,
    val name: String,
    val type: RecipeType,
    val icon: String,
    /**
     * Base64 encoded WebP image of the finished dish
     * When not null, this takes precedence over the icon emoji for display
     * Supports data export/import and cloud sync
     */
    val imageBase64: String? = null,
    val difficulty: Difficulty,
    val estimatedTime: Int,
    val ingredients: List<Ingredient> = emptyList(),
    val steps: List<CookingStep> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis()
) {
    /**
     * Check if this recipe has a custom image
     */
    fun hasCustomImage(): Boolean = imageBase64 != null
}

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
