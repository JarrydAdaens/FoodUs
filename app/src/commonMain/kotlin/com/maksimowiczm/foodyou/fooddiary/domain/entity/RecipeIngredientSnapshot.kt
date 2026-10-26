package com.maksimowiczm.foodyou.fooddiary.domain.entity

import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementResolver

data class RecipeIngredientSnapshot(val food: NutritionalItemSnapshot, val measurement: Measurement) {
    internal val legacyWeight: Double? = com.maksimowiczm.foodyou.common.domain.food.WeightCalculator.calculateWeight(
        measurement, food.totalWeight, food.servingWeight,
    )
    internal val legacyNutrition: NutritionFacts? = legacyWeight?.let { food.nutritionFacts * (it / 100.0) }
    val weight: Double? = if (food is ProductSnapshot && food.factBasis == com.maksimowiczm.foodyou.common.domain.food.ProductFactBasis.REFERENCE_SERVING)
        food.weight(measurement) else MeasurementResolver.metricAmount(measurement, food.isLiquid, food.totalWeight, food.servingWeight)
    val nutritionFacts: NutritionFacts? = food.nutritionFor(measurement)
}
