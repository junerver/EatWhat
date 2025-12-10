package com.eatwhat.domain.usecase

import com.eatwhat.data.repository.RollRepository
import com.eatwhat.data.repository.ValidationResult
import com.eatwhat.domain.model.RollConfig
import com.eatwhat.domain.model.RollResult

/**
 * Use case for rolling recipes based on configuration
 * Validates config and performs random recipe selection
 */
class RollRecipesUseCase(private val rollRepository: RollRepository) {

    /**
     * Execute roll operation
     * @return Result with selected recipes or error
     */
    suspend operator fun invoke(config: RollConfig): Result<RollResult> {
        return try {
            // Validate configuration
            when (val validation = rollRepository.validateConfig(config)) {
                is ValidationResult.Success -> {
                    // Perform roll
                    val result = rollRepository.rollRecipes(config)
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

/**
 * Exception thrown when there are insufficient recipes for the roll config
 */
class InsufficientRecipesException(val errors: List<String>) : Exception(errors.joinToString(", "))
