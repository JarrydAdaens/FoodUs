package com.maksimowiczm.foodyou.app.ui.food.diary.add

import androidx.compose.runtime.*
import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId

@Immutable
internal sealed interface FoodModel {
    val foodId: NutritionalItemId
    val name: String
    val nutritionFacts: NutritionFacts
    val isLiquid: Boolean
    val note: String?
    val totalWeight: Double?
    val servingWeight: Double?

    val canUnpack: Boolean
        get() = foodId is NutritionalItemId.Recipe

    fun weight(measurement: Measurement): Double?

    fun nutritionFor(measurement: Measurement): NutritionFacts? {
        val weight = com.maksimowiczm.foodyou.common.domain.measurement.MeasurementResolver.metricAmount(
            measurement, isLiquid, totalWeight, servingWeight,
        ) ?: return null
        return nutritionFacts * (weight / 100.0)
    }
}
