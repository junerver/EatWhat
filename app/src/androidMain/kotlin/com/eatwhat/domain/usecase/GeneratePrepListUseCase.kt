package com.eatwhat.domain.usecase

import com.eatwhat.domain.model.Ingredient
import com.eatwhat.domain.model.Recipe

/**
 * Use case for generating prep list from selected recipes
 * Aggregates and merges ingredients from multiple recipes
 */
class GeneratePrepListUseCase {

    /**
     * Generate prep list from recipes
     * Merges ingredients with same name and unit
     */
    operator fun invoke(recipes: List<Recipe>): List<PrepListItem> {
        val ingredientMap = mutableMapOf<String, PrepListItem>()

        recipes.forEach { recipe ->
            recipe.ingredients.forEach { ingredient ->
                val key = "${ingredient.name}_${ingredient.unit.name}"

                if (ingredientMap.containsKey(key)) {
                    // Merge with existing ingredient
                    val existing = ingredientMap[key]!!
                    val mergedAmount = mergeAmounts(existing.amount, ingredient.amount)
                    ingredientMap[key] = existing.copy(amount = mergedAmount)
                } else {
                    // Add new ingredient
                    ingredientMap[key] = PrepListItem(
                        name = ingredient.name,
                        amount = ingredient.amount,
                        unit = ingredient.unit.name,
                        isChecked = false
                    )
                }
            }
        }

        return ingredientMap.values.sortedBy { it.name }
    }

    private fun mergeAmounts(amount1: String, amount2: String): String {
        // Try to parse as numbers and sum
        val num1 = amount1.toDoubleOrNull()
        val num2 = amount2.toDoubleOrNull()

        return if (num1 != null && num2 != null) {
            val sum = num1 + num2
            if (sum % 1.0 == 0.0) {
                sum.toInt().toString()
            } else {
                sum.toString()
            }
        } else {
            // Cannot merge, concatenate
            "$amount1 + $amount2"
        }
    }
}

data class PrepListItem(
    val name: String,
    val amount: String,
    val unit: String,
    val isChecked: Boolean = false
)
