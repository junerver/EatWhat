package com.eatwhat.domain.usecase

import com.eatwhat.domain.model.RollConfig
import com.eatwhat.domain.model.RollResult

interface RollRecipeProvider {
    suspend fun rollRecipes(config: RollConfig): RollResult

    suspend fun validateConfig(config: RollConfig): ValidationResult
}

/**
 * Use case for rolling recipes based on configuration
 * Validates config and performs random recipe selection
 */
class RollRecipesUseCase(private val rollRecipeProvider: RollRecipeProvider) {

    /**
     * Execute roll operation
     * @return Result with selected recipes or error
     */
    suspend operator fun invoke(config: RollConfig): Result<RollResult> {
        return try {
            // Validate configuration
            when (val validation = rollRecipeProvider.validateConfig(config)) {
                is ValidationResult.Success -> {
                    // Perform roll
                    val result = rollRecipeProvider.rollRecipes(config)
                    Result.success(result)
                }
                is ValidationResult.Error -> {
                    Result.failure(InsufficientRecipesException(validation.messages))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

sealed class ValidationResult {
    data object Success : ValidationResult()

    data class Error(val messages: List<String>) : ValidationResult()
}

/**
 * Exception thrown when there are insufficient recipes for the roll config
 */
class InsufficientRecipesException(val errors: List<String>) : Exception(errors.joinToString(", "))
