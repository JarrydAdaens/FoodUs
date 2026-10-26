package com.maksimowiczm.foodyou.app.ui.food.recipe

import androidx.lifecycle.ViewModel
import com.maksimowiczm.foodyou.common.extension.combine
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import com.maksimowiczm.foodyou.food.domain.entity.Recipe
import com.maksimowiczm.foodyou.food.domain.entity.RecipeIngredient
import com.maksimowiczm.foodyou.food.domain.usecase.ObserveNutritionalItemUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

internal abstract class RecipeViewModel(private val observeFoodUseCase: ObserveNutritionalItemUseCase) :
    ViewModel() {

    fun intoRecipe(state: RecipeDraft): Flow<Recipe?> {
        if (state.ingredients.isEmpty()) {
            return flowOf(null)
        }

        return state.ingredients
            .map { ingredient ->
                observeFoodUseCase.observe(ingredient.foodId).map { food ->
                    food?.let { RecipeIngredient(food = it, measurement = ingredient.measurement) }
                }
            }
            .combine()
            .map { ingredients ->
                if (ingredients.any { it == null }) return@map null
                Recipe(
                    id = NutritionalItemId.Recipe(-1),
                    name = state.name,
                    servings = state.servings,
                    note = state.note,
                    ingredients = ingredients.filterNotNull(),
                    isLiquid = state.isLiquid,
                )
            }
    }
}
