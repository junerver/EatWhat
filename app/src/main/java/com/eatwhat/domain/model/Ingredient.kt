package com.eatwhat.domain.model

/**
 * Domain model for Ingredient
 */
data class Ingredient(
    val id: Long = 0,
    val name: String,
    val amount: String,
    val unit: Unit,
    val orderIndex: Int = 0
)

enum class Unit {
    G, ML, PIECE, SPOON, MODERATE;

    companion object {
        fun fromString(value: String): Unit {
            return valueOf(value.uppercase())
        }
    }
}
