package com.maksimowiczm.foodyou.app.ui.food.recipe

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import com.maksimowiczm.foodyou.app.ui.food.component.CapturedDefinitionViewModel

fun Module.foodRecipe() {
    viewModelOf(::CreateRecipeViewModel)
    viewModelOf(::UpdateRecipeViewModel)
    viewModelOf(::MeasureIngredientViewModel)
    viewModelOf(::CapturedDefinitionViewModel)
}
