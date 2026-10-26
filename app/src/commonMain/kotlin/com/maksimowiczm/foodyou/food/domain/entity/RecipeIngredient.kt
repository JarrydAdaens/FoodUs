package com.maksimowiczm.foodyou.food.domain.entity

import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement

data class RecipeIngredient(val food: NutritionalItem, val measurement: Measurement) {
    val weight: Double? get() = food.weight(measurement)
    val nutritionFacts: NutritionFacts? get() = food.nutritionFor(measurement)
}
