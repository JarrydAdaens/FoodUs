package com.maksimowiczm.foodyou.app.ui.food.recipe

import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId

internal sealed interface CreateRecipeEvent {
    data object SaveFailed : CreateRecipeEvent
    data class Created(val recipeId: NutritionalItemId.Recipe) : CreateRecipeEvent
}
