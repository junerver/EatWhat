package com.eatwhat.domain.model

/**
 * Domain model for Prep Item
 * Represents an item in the prep checklist
 */
data class PrepItem(
    val id: Long = 0,
    val ingredientName: String,
    val isChecked: Boolean = false,
    val orderIndex: Int = 0
)
