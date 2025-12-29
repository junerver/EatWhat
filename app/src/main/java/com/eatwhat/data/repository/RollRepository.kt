package com.eatwhat.data.repository

import android.util.Log
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
      Log.d("RollRepository", "开始 Roll，配置: $config")
        val effectiveConfig = if (config.autoBalance) config.withAutoBalance() else config
      Log.d("RollRepository", "有效配置: $effectiveConfig")

        val selectedRecipes = mutableListOf<Recipe>()

        // Select meat recipes
        if (effectiveConfig.meatCount > 0) {
          Log.d("RollRepository", "开始获取 ${effectiveConfig.meatCount} 个荤菜")
            val meatRecipes = recipeRepository.getRandomRecipesByType(
                RecipeType.MEAT,
                effectiveConfig.meatCount
            )
          Log.d(
            "RollRepository",
            "获取到 ${meatRecipes.size} 个荤菜: ${meatRecipes.map { it.name }}"
          )
            selectedRecipes.addAll(meatRecipes)
        }

        // Select veg recipes
        if (effectiveConfig.vegCount > 0) {
          Log.d("RollRepository", "开始获取 ${effectiveConfig.vegCount} 个素菜")
            val vegRecipes = recipeRepository.getRandomRecipesByType(
                RecipeType.VEG,
                effectiveConfig.vegCount
            )
          Log.d("RollRepository", "获取到 ${vegRecipes.size} 个素菜: ${vegRecipes.map { it.name }}")
            selectedRecipes.addAll(vegRecipes)
        }

        // Select soup recipes
        if (effectiveConfig.soupCount > 0) {
          Log.d("RollRepository", "开始获取 ${effectiveConfig.soupCount} 个汤")
            val soupRecipes = recipeRepository.getRandomRecipesByType(
                RecipeType.SOUP,
                effectiveConfig.soupCount
            )
          Log.d("RollRepository", "获取到 ${soupRecipes.size} 个汤: ${soupRecipes.map { it.name }}")
            selectedRecipes.addAll(soupRecipes)
        }

        // Select staple recipes
        if (effectiveConfig.stapleCount > 0) {
          Log.d("RollRepository", "开始获取 ${effectiveConfig.stapleCount} 个主食")
            val stapleRecipes = recipeRepository.getRandomRecipesByType(
                RecipeType.STAPLE,
                effectiveConfig.stapleCount
            )
          Log.d(
            "RollRepository",
            "获取到 ${stapleRecipes.size} 个主食: ${stapleRecipes.map { it.name }}"
          )
            selectedRecipes.addAll(stapleRecipes)
        }

      Log.d("RollRepository", "Roll 完成，共选中 ${selectedRecipes.size} 个菜谱")
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
