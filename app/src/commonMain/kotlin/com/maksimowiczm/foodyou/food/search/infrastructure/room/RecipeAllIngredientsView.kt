package com.maksimowiczm.foodyou.food.search.infrastructure.room

import androidx.room.DatabaseView

/** Shared by Room schema export and migration so ancestry semantics cannot drift. */
internal const val RECIPE_ANCESTRY_SQL = """
    WITH RECURSIVE recipeIngredients(targetRecipeId, ingredientId) AS (
        SELECT recipeId, ingredientRecipeId
        FROM RecipeIngredient
        WHERE ingredientRecipeId IS NOT NULL
        UNION
        SELECT previous.targetRecipeId, child.ingredientRecipeId
        FROM RecipeIngredient child
        INNER JOIN recipeIngredients previous ON child.recipeId = previous.ingredientId
        WHERE child.ingredientRecipeId IS NOT NULL
    )
    SELECT targetRecipeId, ingredientId FROM recipeIngredients
"""

/** Recipe-only reachability. Distinct edges terminate even for malformed legacy cycles. */
@DatabaseView(value = RECIPE_ANCESTRY_SQL)
data class RecipeAllIngredientsView(val targetRecipeId: Long, val ingredientId: Long)
