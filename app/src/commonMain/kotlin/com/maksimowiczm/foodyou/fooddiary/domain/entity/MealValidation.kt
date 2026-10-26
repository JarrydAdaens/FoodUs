package com.maksimowiczm.foodyou.fooddiary.domain.entity

import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.common.domain.measurement.rawValue

enum class MealValidationError { BlankName, EmptyMeal, InvalidItem, InvalidNutrition, InvalidPortion }

/** Validate present source numbers before capture normalization; unknown nutrition is valid. */
fun NutritionFacts.validMealNutrition(): Boolean = NutritionFactsField.entries.all {
    this[it].value?.let { value -> value.isFinite() && value >= 0.0 } ?: true
}

fun validateMeal(name: String, items: List<MealItem>): MealValidationError? = when {
    name.isBlank() -> MealValidationError.BlankName
    items.isEmpty() -> MealValidationError.EmptyMeal
    items.any { it.name.isBlank() } -> MealValidationError.InvalidItem
    items.any { !it.nutritionFacts.validMealNutrition() } -> MealValidationError.InvalidNutrition
    items.any { item ->
        listOf(item.capturedPortion?.rawValue, item.servingCount, item.weightGrams)
            .any { it != null && (!it.isFinite() || it <= 0.0) }
    } -> MealValidationError.InvalidPortion
    else -> null
}

/** A deliberately requested whole-portion change; descriptive edits alone never invoke this. */
fun MealItem.scaleCapturedPortion(factor: Double): MealItem? {
    if (!factor.isFinite() || factor <= 0.0 || !nutritionFacts.validMealNutrition()) return null
    val facts = nutritionFacts * factor
    if (!facts.validMealNutrition() || NutritionFactsField.entries.any {
        val old = nutritionFacts[it].value
        old != null && old > 0.0 && facts[it].value == 0.0
    }) return null
    fun scaled(value: Double?): Double? = value?.times(factor)
    val result = copy(
        nutritionFacts = facts,
        capturedPortion = capturedPortion?.times(factor),
        servingCount = scaled(servingCount),
        weightGrams = scaled(weightGrams),
    )
    return result.takeIf { validateMeal("Meal", listOf(it)) == null }
}
