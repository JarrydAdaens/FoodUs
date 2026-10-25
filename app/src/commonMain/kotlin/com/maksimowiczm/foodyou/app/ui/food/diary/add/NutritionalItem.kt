package com.maksimowiczm.foodyou.app.ui.food.diary.add

import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItem
import com.maksimowiczm.foodyou.food.domain.entity.Product
import com.maksimowiczm.foodyou.food.domain.entity.Recipe
import com.maksimowiczm.foodyou.food.domain.entity.RecipeIngredient
import com.maksimowiczm.foodyou.fooddiary.domain.entity.NutritionalItemSnapshot
import com.maksimowiczm.foodyou.fooddiary.domain.entity.ProductSnapshot
import com.maksimowiczm.foodyou.fooddiary.domain.entity.RecipeSnapshot
import com.maksimowiczm.foodyou.fooddiary.domain.entity.RecipeIngredientSnapshot

internal fun NutritionalItem.toDiaryFood(): NutritionalItemSnapshot =
    when (this) {
        is Product -> toDiaryProduct()
        is Recipe -> toDiaryRecipe()
    }

private fun Product.toDiaryProduct(): ProductSnapshot =
    ProductSnapshot(
        name = headline,
        nutritionFacts = nutritionFacts,
        servingWeight = servingWeight,
        totalWeight = totalWeight,
        isLiquid = isLiquid,
        source = source,
        note = note,
        factBasis = factBasis,
        referencePortion = referencePortion,
        nutritionMetadata = nutritionMetadata,
    )

private fun Recipe.toDiaryRecipe(): RecipeSnapshot =
    RecipeSnapshot(
        name = headline,
        servings = servings,
        ingredients = ingredients.map { it.toDiaryRecipeIngredient() },
        isLiquid = isLiquid,
        note = note,
        calculationVersion = if (isReported) 2 else 1,
        reportedNutrition = reportedNutrition,
        reportedComponents = reportedComponents.map { it.captured() },
    )

private fun RecipeIngredient.toDiaryRecipeIngredient(): RecipeIngredientSnapshot =
    RecipeIngredientSnapshot(food = food.toDiaryFood(), measurement = measurement)
