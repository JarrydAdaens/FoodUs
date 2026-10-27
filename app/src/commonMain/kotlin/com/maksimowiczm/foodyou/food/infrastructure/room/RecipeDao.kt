package com.maksimowiczm.foodyou.food.infrastructure.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
abstract class RecipeDao {
    @Query("""
        SELECT d.id, d.name, d.note,
            (SELECT MIN(e.epochSeconds) FROM FoodEvent e
             WHERE e.recipeId = d.id AND e.type IN (0, 1, 2)) AS createdEpochSeconds
        FROM Recipe d
    """)
    internal abstract fun observeLibraryNames(): Flow<List<DefinitionName>>


    /** Only a change signal: Room re-emits when any nutrition dependency table changes. */
    @Query("""SELECT EXISTS(SELECT 1 FROM Product) + EXISTS(SELECT 1 FROM RecipeIngredient)
        + EXISTS(SELECT 1 FROM ReportedRecipeComponent)""")
    internal abstract fun observeLibraryContentChanges(): Flow<Int>

    @Query("SELECT * FROM Recipe WHERE id = :recipeId")
    abstract fun observeRecipe(recipeId: Long): Flow<RecipeEntity?>

    @Query("SELECT * FROM RecipeIngredient WHERE recipeId = :recipeId")
    abstract fun observeRecipeIngredients(recipeId: Long): Flow<List<RecipeIngredientEntity>>

    @Query("SELECT * FROM RecipeIngredient WHERE recipeId = :recipeId")
    abstract suspend fun getRecipeIngredients(recipeId: Long): List<RecipeIngredientEntity>

    @Query("SELECT * FROM ReportedRecipeComponent WHERE recipeId = :recipeId ORDER BY position")
    abstract fun observeReportedComponents(recipeId: Long): Flow<List<ReportedRecipeComponentEntity>>

    @Query("SELECT * FROM ReportedRecipeComponent WHERE recipeId = :recipeId ORDER BY position")
    abstract suspend fun getReportedComponents(recipeId: Long): List<ReportedRecipeComponentEntity>

    @Insert protected abstract suspend fun insertReportedComponents(components: List<ReportedRecipeComponentEntity>)

    @Query("DELETE FROM ReportedRecipeComponent WHERE recipeId = :recipeId")
    protected abstract suspend fun deleteReportedComponents(recipeId: Long)

    @Insert protected abstract suspend fun insertRecipe(recipe: RecipeEntity): Long

    @Insert
    protected abstract suspend fun insertRecipeIngredient(ingredient: RecipeIngredientEntity): Long

    @Transaction
    open suspend fun insertRecipeWithIngredients(
        recipe: RecipeEntity,
        ingredients: List<RecipeIngredientEntity>,
        reportedComponents: List<ReportedRecipeComponentEntity> = emptyList(),
    ): Long {
        val recipeId = insertRecipe(recipe)

        ingredients.forEach { ingredient ->
            val recipeIngredient = ingredient.copy(recipeId = recipeId)
            insertRecipeIngredient(recipeIngredient)
        }

        insertReportedComponents(reportedComponents.map { it.copy(recipeId = recipeId) })
        return recipeId
    }

    @Update protected abstract suspend fun updateRecipeEntity(recipeEntity: RecipeEntity)

    @Query("DELETE FROM RecipeIngredient WHERE recipeId = :recipeId")
    protected abstract suspend fun deleteRecipeIngredientsByRecipeId(recipeId: Long)

    // Deletes all ingredients for the recipe and then inserts new ones
    @Transaction
    open suspend fun updateRecipeWithIngredients(
        recipe: RecipeEntity,
        ingredients: List<RecipeIngredientEntity>,
        reportedComponents: List<ReportedRecipeComponentEntity> = emptyList(),
    ) {
        updateRecipeEntity(recipe)
        deleteRecipeIngredientsByRecipeId(recipe.id)
        deleteReportedComponents(recipe.id)
        insertReportedComponents(reportedComponents.map { it.copy(recipeId = recipe.id) })

        ingredients.forEach { ingredient ->
            val recipeIngredientEntity = ingredient.copy(recipeId = recipe.id)
            insertRecipeIngredient(recipeIngredientEntity)
        }
    }

    @Delete abstract suspend fun delete(recipe: RecipeEntity)
}
