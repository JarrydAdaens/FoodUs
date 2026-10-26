package com.maksimowiczm.foodyou.fooddiary.domain.entity

import com.maksimowiczm.foodyou.common.domain.food.hasValidValues
import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementType
import com.maksimowiczm.foodyou.common.domain.measurement.from
import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementResolver
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement

/**
 * Represents a food item in the food diary.
 *
 * @param name The name of the food item.
 * @param nutritionFacts The nutrition facts of the food item per 100g or 100ml.
 * @param servingWeight The weight of a single serving of the food item, if applicable.
 * @param totalWeight The total weight of the food item, if applicable.
 * @param isLiquid Indicates whether the food item is a liquid.
 */
sealed interface NutritionalItemSnapshot {
    val name: String
    val nutritionFacts: NutritionFacts
    val servingWeight: Double?
    val totalWeight: Double?
    val isLiquid: Boolean
    val note: String?

    fun weight(measurement: Measurement): Double?
    fun supportedMeasurementTypes(): List<MeasurementType> =
        MeasurementType.entries.filter { nutritionFor(Measurement.from(it, 1.0)) != null }

    fun nutritionFor(measurement: Measurement): NutritionFacts? {
        val amount = MeasurementResolver.metricAmount(measurement, isLiquid, totalWeight, servingWeight) ?: return null
        if (amount / 100.0 <= 0.0) return null
        return (nutritionFacts * (amount / 100.0)).takeIf { it.hasValidValues() }
    }
}
