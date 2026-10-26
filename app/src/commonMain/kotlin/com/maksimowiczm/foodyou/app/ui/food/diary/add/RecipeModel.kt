package com.maksimowiczm.foodyou.app.ui.food.diary.add

import androidx.compose.runtime.Immutable
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.food.domain.entity.Recipe

@Immutable
internal data class RecipeModel(private val recipe: Recipe) : FoodModel {
    override val foodId = recipe.id
    override val name = recipe.name
    override val nutritionFacts = recipe.nutritionFacts
    override val isLiquid = recipe.isLiquid
    override val note = recipe.note
    override val totalWeight = recipe.totalWeight
    override val servingWeight = recipe.servingWeight
    override val canUnpack = !recipe.isReported
    val reportedNutrition=recipe.reportedNutrition
    val reportedComponents=recipe.reportedComponents
    val knownYield=recipe.servings
    val isValid = recipe.isResolvable
    val allIngredients = recipe.flatIngredients().map { Triple(it.id, it.headline, it.nutritionFacts) }
    fun unpack(measurement: Measurement): List<IngredientModel> = recipe.unpack(measurement)?.map(::IngredientModel).orEmpty()
    override fun weight(measurement: Measurement) = recipe.weight(measurement)
    override fun nutritionFor(measurement: Measurement) = recipe.nutritionFor(measurement)
}
