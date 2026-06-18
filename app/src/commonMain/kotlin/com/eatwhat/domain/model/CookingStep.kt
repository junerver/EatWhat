package com.eatwhat.domain.model

/**
 * Domain model for Cooking Step
 */
data class CookingStep(
    val id: Long = 0,
    val stepNumber: Int,
    val description: String
)
