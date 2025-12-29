package com.eatwhat.domain.usecase

import android.util.Log
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
          Log.d("RollRecipesUseCase", "开始执行 Roll UseCase，配置: $config")
            
            // Validate configuration
            when (val validation = rollRepository.validateConfig(config)) {
                is ValidationResult.Success -> {
                  Log.d("RollRecipesUseCase", "配置验证成功")
                    // Perform roll
                    val result = rollRepository.rollRecipes(config)
                  Log.d("RollRecipesUseCase", "Roll 执行成功，返回 ${result.recipes.size} 个菜谱")
                    Result.success(result)
                }
                is ValidationResult.Error -> {
                  Log.e("RollRecipesUseCase", "配置验证失败: ${validation.messages}")
                    Result.failure(InsufficientRecipesException(validation.messages))
                }
            }
        } catch (e: Exception) {
          Log.e("RollRecipesUseCase", "UseCase 执行异常", e)
            Result.failure(e)
        }
    }
}

/**
 * Exception thrown when there are insufficient recipes for the roll config
 */
class InsufficientRecipesException(val errors: List<String>) : Exception(errors.joinToString(", "))
