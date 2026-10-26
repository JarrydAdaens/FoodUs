package com.maksimowiczm.foodyou.food.domain.entity

import com.maksimowiczm.foodyou.common.domain.food.hasValidValues
import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementType
import com.maksimowiczm.foodyou.common.domain.measurement.from
import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementResolver

/** A reusable Product or Recipe definition. Dated entries capture separate immutable snapshots. */
sealed interface NutritionalItem {
    val id: NutritionalItemId
    val headline: String
    val totalWeight: Double?
    val servingWeight: Double?
    val nutritionFacts: NutritionFacts
    val isLiquid: Boolean

    fun weight(measurement: Measurement): Double? =
        MeasurementResolver.metricAmount(
            measurement = measurement,
            isLiquid = isLiquid,
            totalWeight = totalWeight,
            servingWeight = servingWeight,
        )
    fun supportedMeasurementTypes(): List<MeasurementType> =
        MeasurementType.entries.filter { nutritionFor(Measurement.from(it, 1.0)) != null }

    fun nutritionFor(measurement: Measurement): NutritionFacts? {
        val amount = MeasurementResolver.metricAmount(measurement, isLiquid, totalWeight, servingWeight) ?: return null
        if (amount / 100.0 <= 0.0) return null
        return (nutritionFacts * (amount / 100.0)).takeIf { it.hasValidValues() }
    }
}
