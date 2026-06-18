package com.eatwhat.data.repository

import android.util.Log
import com.eatwhat.data.database.EatWhatDatabase
import com.eatwhat.data.database.entities.CookingStepEntity
import com.eatwhat.data.database.entities.IngredientEntity
import com.eatwhat.data.database.entities.RecipeEntity
import com.eatwhat.data.database.entities.RecipeTagCrossRef
import com.eatwhat.data.database.entities.TagEntity
import com.eatwhat.data.database.relations.RecipeWithDetails
import com.eatwhat.domain.model.CookingStep
import com.eatwhat.domain.model.Difficulty
import com.eatwhat.domain.model.Ingredient
import com.eatwhat.domain.model.Recipe
import com.eatwhat.domain.model.RecipeType
import com.eatwhat.domain.model.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for Recipe operations
 * Handles data access and entity-to-domain mapping
 */
class RecipeRepository(private val database: EatWhatDatabase) {

    private val recipeDao = database.recipeDao()
    private val tagDao = database.tagDao()

    // ========== Query Operations ==========

    fun getAllRecipes(): Flow<List<Recipe>> {
        return recipeDao.getAllRecipesWithDetails().map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getRecipeById(recipeId: Long): Flow<Recipe?> {
        return recipeDao.getRecipeWithDetails(recipeId).map { it?.toDomain() }
    }

    fun getRecipesByType(type: RecipeType): Flow<List<Recipe>> {
        return recipeDao.getRecipesByType(type.name).map { list ->
            list.map { it.toSimpleDomain() }
        }
    }

    suspend fun getRandomRecipesByType(type: RecipeType, count: Int): List<Recipe> {
      Log.d("RecipeRepository", "查询随机菜谱: type=${type.name}, count=$count")

      // Get entities first (avoiding @Transaction issue)
      val recipeEntities = recipeDao.getRandomRecipesByType(type.name, count)
      Log.d("RecipeRepository", "从数据库获取到 ${recipeEntities.size} 个 RecipeEntity")

      val recipes = recipeEntities.map { entity ->
        // Manually fetch details for each recipe to avoid Room Transaction issues
        val ingredients = recipeDao.getIngredientsByRecipeIdSync(entity.id)
        val steps = recipeDao.getCookingStepsByRecipeIdSync(entity.id)
        val tags = recipeDao.getTagsByRecipeIdSync(entity.id)

        Recipe(
          id = entity.id,
          syncId = entity.syncId,
          name = entity.name,
          type = RecipeType.fromString(entity.type),
          icon = entity.icon,
          imageBase64 = entity.imageBase64,
          difficulty = Difficulty.fromString(entity.difficulty),
          estimatedTime = entity.estimatedTime,
          ingredients = ingredients.map { it.toDomain() },
          steps = steps.map { it.toDomain() },
          tags = tags.map { it.toDomain() },
          createdAt = entity.createdAt,
          lastModified = entity.lastModified
        )
      }

      Log.d("RecipeRepository", "成功构建 ${recipes.size} 个完整菜谱")
      return recipes
    }

  fun searchRecipes(query: String): Flow<List<Recipe>> {
        return recipeDao.searchRecipes("%$query%").map { list ->
            list.map { it.toSimpleDomain() }
        }
    }

    // ========== Mutation Operations ==========

    suspend fun insertRecipe(recipe: Recipe): Long {
        val recipeEntity = recipe.toEntity()
        val recipeId = recipeDao.insertRecipe(recipeEntity)

        // Insert ingredients
        val ingredients = recipe.ingredients.map { it.toEntity(recipeId) }
        recipeDao.insertIngredients(ingredients)

        // Insert cooking steps
        val steps = recipe.steps.map { it.toEntity(recipeId) }
        recipeDao.insertCookingSteps(steps)

        // Insert tags
        recipe.tags.forEach { tag ->
            val existingTag = tagDao.getTagByName(tag.name)
            val tagId = existingTag?.id ?: tagDao.insertTag(tag.toEntity())
            recipeDao.insertRecipeTagCrossRef(RecipeTagCrossRef(recipeId, tagId))
        }

        return recipeId
    }

    suspend fun updateRecipe(recipe: Recipe) {
        val recipeEntity = recipe.toEntity()
        recipeDao.updateRecipe(recipeEntity)

        // Update ingredients
        recipeDao.deleteIngredientsByRecipeId(recipe.id)
        val ingredients = recipe.ingredients.map { it.toEntity(recipe.id) }
        recipeDao.insertIngredients(ingredients)

        // Update cooking steps
        recipeDao.deleteCookingStepsByRecipeId(recipe.id)
        val steps = recipe.steps.map { it.toEntity(recipe.id) }
        recipeDao.insertCookingSteps(steps)

        // Update tags
        recipeDao.deleteRecipeTagsByRecipeId(recipe.id)
        recipe.tags.forEach { tag ->
            val existingTag = tagDao.getTagByName(tag.name)
            val tagId = existingTag?.id ?: tagDao.insertTag(tag.toEntity())
            recipeDao.insertRecipeTagCrossRef(RecipeTagCrossRef(recipe.id, tagId))
        }
    }

    suspend fun deleteRecipe(recipeId: Long) {
        recipeDao.softDeleteRecipe(recipeId)
    }

    // ========== Mapping Functions ==========

    private fun RecipeWithDetails.toDomain(): Recipe {
        return Recipe(
            id = recipe.id,
            syncId = recipe.syncId,
            name = recipe.name,
            type = RecipeType.fromString(recipe.type),
            icon = recipe.icon,
            imageBase64 = recipe.imageBase64,
            difficulty = Difficulty.fromString(recipe.difficulty),
            estimatedTime = recipe.estimatedTime,
            ingredients = ingredients.map { it.toDomain() },
            steps = steps.map { it.toDomain() },
            tags = tags.map { it.toDomain() },
            createdAt = recipe.createdAt,
            lastModified = recipe.lastModified
        )
    }

    private fun RecipeEntity.toSimpleDomain(): Recipe {
        return Recipe(
            id = id,
            syncId = syncId,
            name = name,
            type = RecipeType.fromString(type),
            icon = icon,
            imageBase64 = imageBase64,
            difficulty = Difficulty.fromString(difficulty),
            estimatedTime = estimatedTime,
            createdAt = createdAt,
            lastModified = lastModified
        )
    }

    private fun Recipe.toEntity(): RecipeEntity {
        return RecipeEntity(
            id = id,
            syncId = syncId,
            name = name,
            type = type.name,
            icon = icon,
            imageBase64 = imageBase64,
            difficulty = difficulty.name,
            estimatedTime = estimatedTime,
            createdAt = createdAt,
            lastModified = lastModified
        )
    }

    private fun IngredientEntity.toDomain(): Ingredient {
        return Ingredient(
            id = id,
            name = name,
            amount = amount,
            unit = com.eatwhat.domain.model.Unit.fromString(unit),
            orderIndex = orderIndex
        )
    }

    private fun Ingredient.toEntity(recipeId: Long): IngredientEntity {
        return IngredientEntity(
            id = id,
            recipeId = recipeId,
            name = name,
            amount = amount,
            unit = unit.name,
            orderIndex = orderIndex
        )
    }

    private fun CookingStepEntity.toDomain(): CookingStep {
        return CookingStep(
            id = id,
            stepNumber = stepNumber,
            description = description
        )
    }

    private fun CookingStep.toEntity(recipeId: Long): CookingStepEntity {
        return CookingStepEntity(
            id = id,
            recipeId = recipeId,
            stepNumber = stepNumber,
            description = description
        )
    }

    private fun TagEntity.toDomain(): Tag {
        return Tag(
            id = id,
            name = name,
            createdAt = createdAt
        )
    }

    private fun Tag.toEntity(): TagEntity {
        return TagEntity(
            id = id,
            name = name,
            createdAt = createdAt
        )
    }
}
