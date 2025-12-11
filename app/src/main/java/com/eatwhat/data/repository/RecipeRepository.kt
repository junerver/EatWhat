package com.eatwhat.data.repository

import com.eatwhat.data.database.EatWhatDatabase
import com.eatwhat.data.database.entities.*
import com.eatwhat.data.database.relations.RecipeWithDetails
import com.eatwhat.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import com.eatwhat.domain.model.RecipeType
import com.eatwhat.domain.model.Difficulty

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
        return recipeDao.getRandomRecipesWithDetailsByType(type.name, count).map { it.toDomain() }
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
