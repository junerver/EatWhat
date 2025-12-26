package com.eatwhat.data.database.dao

import androidx.room.*
import com.eatwhat.data.database.entities.*
import com.eatwhat.data.database.relations.RecipeWithDetails
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Recipe operations
 */
@Dao
interface RecipeDao {

    // ========== Recipe CRUD ==========

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity): Long

    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    @Query("UPDATE recipes SET is_deleted = 1, last_modified = :timestamp WHERE id = :recipeId")
    suspend fun softDeleteRecipe(recipeId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM recipes WHERE id = :recipeId AND is_deleted = 0")
    fun getRecipeById(recipeId: Long): Flow<RecipeEntity?>

    @Query("SELECT * FROM recipes WHERE is_deleted = 0 ORDER BY name ASC")
    fun getAllRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE is_deleted = 0 AND type = :type ORDER BY name ASC")
    fun getRecipesByType(type: String): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE is_deleted = 0 AND type = :type ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomRecipesByType(type: String, count: Int): List<RecipeEntity>

    @Transaction
    @Query("SELECT * FROM recipes WHERE is_deleted = 0 AND type = :type ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomRecipesWithDetailsByType(type: String, count: Int): List<RecipeWithDetails>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :recipeId AND is_deleted = 0")
    fun getRecipeWithDetails(recipeId: Long): Flow<RecipeWithDetails?>

    @Transaction
    @Query("SELECT * FROM recipes WHERE is_deleted = 0 ORDER BY name ASC")
    fun getAllRecipesWithDetails(): Flow<List<RecipeWithDetails>>

    @Query("""
        SELECT DISTINCT r.* FROM recipes r
        LEFT JOIN recipe_tag_cross_ref rt ON r.id = rt.recipe_id
        LEFT JOIN tags t ON rt.tag_id = t.id
        WHERE r.is_deleted = 0 AND (r.name LIKE :query OR t.name LIKE :query)
        ORDER BY r.name ASC
    """)
    fun searchRecipes(query: String): Flow<List<RecipeEntity>>

    // ========== Ingredient CRUD ==========

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<IngredientEntity>)

    @Query("DELETE FROM ingredients WHERE recipe_id = :recipeId")
    suspend fun deleteIngredientsByRecipeId(recipeId: Long)

    @Query("SELECT * FROM ingredients WHERE recipe_id = :recipeId ORDER BY order_index ASC")
    fun getIngredientsByRecipeId(recipeId: Long): Flow<List<IngredientEntity>>

    // ========== Cooking Step CRUD ==========

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCookingSteps(steps: List<CookingStepEntity>)

    @Query("DELETE FROM cooking_steps WHERE recipe_id = :recipeId")
    suspend fun deleteCookingStepsByRecipeId(recipeId: Long)

    @Query("SELECT * FROM cooking_steps WHERE recipe_id = :recipeId ORDER BY step_number ASC")
    fun getCookingStepsByRecipeId(recipeId: Long): Flow<List<CookingStepEntity>>

    // ========== Recipe-Tag Relationship ==========

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeTagCrossRef(crossRef: RecipeTagCrossRef)

    @Query("DELETE FROM recipe_tag_cross_ref WHERE recipe_id = :recipeId")
    suspend fun deleteRecipeTagsByRecipeId(recipeId: Long)

    @Query("SELECT t.* FROM tags t INNER JOIN recipe_tag_cross_ref rt ON t.id = rt.tag_id WHERE rt.recipe_id = :recipeId")
    fun getTagsByRecipeId(recipeId: Long): Flow<List<TagEntity>>

    // ========== Export/Import Operations ==========

    @Transaction
    @Query("SELECT * FROM recipes WHERE is_deleted = 0 ORDER BY name ASC")
    suspend fun getAllRecipesWithDetailsSync(): List<RecipeWithDetails>

    @Query("SELECT COUNT(*) FROM recipes WHERE is_deleted = 0")
    suspend fun getRecipeCount(): Int

    @Query("SELECT * FROM recipes WHERE sync_id = :syncId AND is_deleted = 0")
    suspend fun getRecipeBySyncId(syncId: String): RecipeEntity?
}
