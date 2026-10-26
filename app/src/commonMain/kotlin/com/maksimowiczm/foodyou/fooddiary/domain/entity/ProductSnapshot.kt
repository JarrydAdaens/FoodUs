package com.maksimowiczm.foodyou.fooddiary.domain.entity

import com.maksimowiczm.foodyou.common.domain.food.ProductSource
import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import com.maksimowiczm.foodyou.common.domain.food.WeightCalculator
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementResolver
import com.maksimowiczm.foodyou.common.domain.food.ProductFactBasis
import com.maksimowiczm.foodyou.common.domain.food.PortionDescription
import com.maksimowiczm.foodyou.common.domain.food.NutritionMetadata
import com.maksimowiczm.foodyou.common.domain.food.ReportedNutrition
import com.maksimowiczm.foodyou.common.domain.food.ReportedBasis
import com.maksimowiczm.foodyou.common.domain.food.referenceServingNutrition

data class ProductSnapshot(
    override val name: String,
    override val nutritionFacts: NutritionFacts,
    override val servingWeight: Double?,
    override val totalWeight: Double?,
    override val isLiquid: Boolean,
    val source: ProductSource,
    override val note: String?,
    val factBasis: ProductFactBasis = ProductFactBasis.METRIC_100,
    val referencePortion: PortionDescription? = null,
    val nutritionMetadata: NutritionMetadata? = null,
) : NutritionalItemSnapshot {
    override fun weight(measurement: Measurement): Double? =
        if (factBasis == ProductFactBasis.REFERENCE_SERVING) {
            MeasurementResolver.metricAmount(measurement, isLiquid, totalWeight, referencePortion?.metric(isLiquid))
        } else WeightCalculator.calculateWeight(
            measurement = measurement,
            servingWeight = servingWeight,
            totalWeight = totalWeight,
        )

    override fun nutritionFor(measurement: Measurement): NutritionFacts? {
        if (factBasis == ProductFactBasis.METRIC_100) return super.nutritionFor(measurement)
        return referenceServingNutrition(nutritionFacts, referencePortion, measurement, isLiquid, totalWeight)
    }
}
