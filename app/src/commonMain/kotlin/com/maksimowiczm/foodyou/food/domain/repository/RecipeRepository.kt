package com.maksimowiczm.foodyou.food.domain.repository

import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import com.maksimowiczm.foodyou.food.domain.entity.Recipe
import com.maksimowiczm.foodyou.food.domain.entity.RecipeIngredient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

interface RecipeRepository {
    /** Personal definitions only. Lightweight listing does not resolve nutritional graphs. */
    fun observeLibraryNames(): Flow<List<com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemName>> =
        kotlinx.coroutines.flow.flow { error("Library listing unavailable") }

    fun observeRecipe(recipeId: NutritionalItemId.Recipe): Flow<Recipe?>

    /** Direct recipe edges, before recursive hydration, for candidate graph validation. */
    suspend fun recipeDependencies(id: NutritionalItemId.Recipe): List<NutritionalItemId.Recipe> =
        observeRecipe(id).firstOrNull()?.ingredients?.mapNotNull { it.food.id as? NutritionalItemId.Recipe }.orEmpty()

    suspend fun insertRecipe(
        name: String,
        servings: Int,
        note: String?,
        isLiquid: Boolean,
        ingredients: List<RecipeIngredient>,
    ): NutritionalItemId.Recipe

    suspend fun updateRecipe(recipe: Recipe)

    /** Inserts a complete reported definition without fabricating linked ingredients or yield. */
    suspend fun insertRecipe(recipe: Recipe): NutritionalItemId.Recipe = error("Reported Recipe insertion unavailable")

    suspend fun deleteRecipe(recipe: Recipe)
}
