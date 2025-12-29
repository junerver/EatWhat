package com.eatwhat.data.database.relations

import android.util.Log
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
    val ingredients: List<IngredientEntity> = emptyList(),

    @Relation(
        parentColumn = "id",
        entityColumn = "recipe_id"
    )
    val steps: List<CookingStepEntity> = emptyList(),

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = RecipeTagCrossRef::class,
            parentColumn = "recipe_id",
            entityColumn = "tag_id"
        )
    )
    val tags: List<TagEntity> = emptyList()
)

/**
 * Convert RecipeWithDetails to Recipe for UI display
 */
fun RecipeWithDetails.toDomain(): com.eatwhat.domain.model.Recipe {
  Log.d("RecipeWithDetails", "开始转换菜谱: ${recipe.name}")
  Log.d("RecipeWithDetails", "  - id: ${recipe.id}")
  Log.d("RecipeWithDetails", "  - syncId: ${recipe.syncId}")
  Log.d("RecipeWithDetails", "  - type: ${recipe.type}")
  Log.d("RecipeWithDetails", "  - icon: ${recipe.icon}")
  Log.d("RecipeWithDetails", "  - imageBase64 是否为空: ${recipe.imageBase64 == null}")
  Log.d("RecipeWithDetails", "  - difficulty: ${recipe.difficulty}")
  Log.d("RecipeWithDetails", "  - estimatedTime: ${recipe.estimatedTime}")
  Log.d("RecipeWithDetails", "  - ingredients 数量: ${ingredients.size}")
  Log.d("RecipeWithDetails", "  - steps 数量: ${steps.size}")
  Log.d("RecipeWithDetails", "  - tags 数量: ${tags.size}")

  return try {
    val domainRecipe = com.eatwhat.domain.model.Recipe(
      id = recipe.id,
      syncId = recipe.syncId,
      name = recipe.name,
      type = com.eatwhat.domain.model.RecipeType.fromString(recipe.type),
      icon = recipe.icon,
      imageBase64 = recipe.imageBase64,
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
    Log.d("RecipeWithDetails", "菜谱转换成功: ${recipe.name}")
    domainRecipe
  } catch (e: Exception) {
    Log.e("RecipeWithDetails", "菜谱转换失败: ${recipe.name}", e)
    throw e
  }
}
