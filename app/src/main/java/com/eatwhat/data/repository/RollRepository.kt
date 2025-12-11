package com.eatwhat.data.repository

import com.eatwhat.domain.model.Recipe
import com.eatwhat.domain.model.RecipeType
import com.eatwhat.domain.model.RollConfig
import com.eatwhat.domain.model.RollResult

/**
 * Repository for Roll operations
 * Handles random recipe selection logic
 */
class RollRepository(private val recipeRepository: RecipeRepository) {

    /**
     * Roll recipes based on configuration
     * Returns randomly selected recipes matching the config
     */
    suspend fun rollRecipes(config: RollConfig): RollResult {
        val effectiveConfig = if (config.autoBalance) config.withAutoBalance() else config

        val selectedRecipes = mutableListOf<Recipe>()

        // Select meat recipes
        if (effectiveConfig.meatCount > 0) {
            val meatRecipes = recipeRepository.getRandomRecipesByType(
                RecipeType.MEAT,
                effectiveConfig.meatCount
            )
            selectedRecipes.addAll(meatRecipes)
        }

        // Select veg recipes
        if (effectiveConfig.vegCount > 0) {
            val vegRecipes = recipeRepository.getRandomRecipesByType(
                RecipeType.VEG,
                effectiveConfig.vegCount
            )
            selectedRecipes.addAll(vegRecipes)
        }

        // Select soup recipes
        if (effectiveConfig.soupCount > 0) {
            val soupRecipes = recipeRepository.getRandomRecipesByType(
                RecipeType.SOUP,
                effectiveConfig.soupCount
            )
            selectedRecipes.addAll(soupRecipes)
        }

        // Select staple recipes
        if (effectiveConfig.stapleCount > 0) {
            val stapleRecipes = recipeRepository.getRandomRecipesByType(
                RecipeType.STAPLE,
                effectiveConfig.stapleCount
            )
            selectedRecipes.addAll(stapleRecipes)
        }

        return RollResult(
            recipes = selectedRecipes,
            config = effectiveConfig
        )
    }


    /**
     * Validate if there are enough recipes for the config
     */
    suspend fun validateConfig(config: RollConfig): ValidationResult {
        val effectiveConfig = if (config.autoBalance) config.withAutoBalance() else config

        val errors = mutableListOf<String>()

        if (effectiveConfig.meatCount > 0) {
            val available = recipeRepository.getRandomRecipesByType(RecipeType.MEAT, 999).size
            if (available < effectiveConfig.meatCount) {
                errors.add("荤菜不足: 需要${effectiveConfig.meatCount}道,只有${available}道")
            }
        }

        if (effectiveConfig.vegCount > 0) {
            val available = recipeRepository.getRandomRecipesByType(RecipeType.VEG, 999).size
            if (available < effectiveConfig.vegCount) {
                errors.add("素菜不足: 需要${effectiveConfig.vegCount}道,只有${available}道")
            }
        }

        if (effectiveConfig.soupCount > 0) {
            val available = recipeRepository.getRandomRecipesByType(RecipeType.SOUP, 999).size
            if (available < effectiveConfig.soupCount) {
                errors.add("汤不足: 需要${effectiveConfig.soupCount}道,只有${available}道")
            }
        }

        if (effectiveConfig.stapleCount > 0) {
            val available = recipeRepository.getRandomRecipesByType(RecipeType.STAPLE, 999).size
            if (available < effectiveConfig.stapleCount) {
                errors.add("主食不足: 需要${effectiveConfig.stapleCount}道,只有${available}道")
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(errors)
        }
    }
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val messages: List<String>) : ValidationResult()
}
