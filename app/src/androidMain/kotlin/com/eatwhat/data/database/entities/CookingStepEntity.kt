package com.eatwhat.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Cooking step entity for Room database
 * Represents a cooking step in a recipe
 */
@Entity(
    tableName = "cooking_steps",
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
data class CookingStepEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "recipe_id")
    val recipeId: Long,

    @ColumnInfo(name = "step_number")
    val stepNumber: Int,

    val description: String
)

/**
 * Convert CookingStepEntity to CookingStep domain model
 */
fun CookingStepEntity.toDomain(): com.eatwhat.domain.model.CookingStep {
    return com.eatwhat.domain.model.CookingStep(
        id = id,
        stepNumber = stepNumber,
        description = description
    )
}
