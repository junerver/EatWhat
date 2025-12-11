package com.eatwhat.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Ingredient entity for Room database
 * Represents an ingredient in a recipe
 */
@Entity(
    tableName = "ingredients",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipe_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["recipe_id"])]
)
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "recipe_id")
    val recipeId: Long,

    val name: String,

    val amount: String,

    val unit: String, // Unit: G, ML, PIECE, SPOON, MODERATE

    @ColumnInfo(name = "order_index")
    val orderIndex: Int
)

/**
 * Ingredient unit enum
 */
enum class Unit {
    G,        // 克
    ML,       // 毫升
    PIECE,    // 个
    SPOON,    // 勺
    MODERATE  // 适量
}

/**
 * Convert IngredientEntity to Ingredient domain model
 */
fun IngredientEntity.toDomain(): com.eatwhat.domain.model.Ingredient {
    return com.eatwhat.domain.model.Ingredient(
        id = id,
        name = name,
        amount = amount,
        unit = com.eatwhat.domain.model.Unit.fromString(unit),
        orderIndex = orderIndex
    )
}
