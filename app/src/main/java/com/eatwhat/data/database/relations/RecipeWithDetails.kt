package com.eatwhat.data.database.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.eatwhat.data.database.entities.CookingStepEntity
import com.eatwhat.data.database.entities.IngredientEntity
import com.eatwhat.data.database.entities.RecipeEntity
import com.eatwhat.data.database.entities.RecipeTagCrossRef
import com.eatwhat.data.database.entities.TagEntity

/**
 * Recipe with all its details (ingredients, steps, tags)
 * Used for querying complete recipe information
 */
data class RecipeWithDetails(
    @Embedded
    val recipe: RecipeEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "recipe_id"
    )
    val ingredients: List<IngredientEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "recipe_id"
    )
    val steps: List<CookingStepEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = RecipeTagCrossRef::class,
            parentColumn = "recipe_id",
            entityColumn = "tag_id"
        )
    )
    val tags: List<TagEntity>
)

/**
 * Convert RecipeWithDetails to Recipe for UI display
 */
fun RecipeWithDetails.toDomain(): com.eatwhat.domain.model.Recipe {
    return com.eatwhat.domain.model.Recipe(
        id = recipe.id,
        syncId = recipe.syncId,
        name = recipe.name,
        type = com.eatwhat.domain.model.RecipeType.fromString(recipe.type),
        icon = recipe.icon,
        difficulty = com.eatwhat.domain.model.Difficulty.fromString(recipe.difficulty),
        estimatedTime = recipe.estimatedTime,
        ingredients = ingredients.map { ingredient ->
            com.eatwhat.domain.model.Ingredient(
                id = ingredient.id,
                name = ingredient.name,
                amount = ingredient.amount,
                unit = com.eatwhat.domain.model.Unit.fromString(ingredient.unit),
                orderIndex = ingredient.orderIndex
            )
        },
        steps = steps.map { step ->
            com.eatwhat.domain.model.CookingStep(
                id = step.id,
                stepNumber = step.stepNumber,
                description = step.description
            )
        },
        tags = tags.map { tag ->
            com.eatwhat.domain.model.Tag(
                id = tag.id,
                name = tag.name,
                createdAt = tag.createdAt
            )
        },
        createdAt = recipe.createdAt,
        lastModified = recipe.lastModified
    )
}
