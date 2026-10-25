package com.maksimowiczm.foodyou.food.search.domain

import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import com.maksimowiczm.foodyou.common.domain.food.ProductFactBasis
import com.maksimowiczm.foodyou.common.domain.food.PortionDescription
import com.maksimowiczm.foodyou.common.domain.food.referenceServingNutrition
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementResolver
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId

sealed interface NutritionalItemSearch {
    val id: NutritionalItemId
    val headline: String
    val isLiquid: Boolean
    val suggestedMeasurement: Measurement

    data class Product(
        override val id: NutritionalItemId.Product,
        override val headline: String,
        override val isLiquid: Boolean,
        val nutritionFacts: NutritionFacts,
        val totalWeight: Double?,
        val servingWeight: Double?,
        override val suggestedMeasurement: Measurement,
        val factBasis:ProductFactBasis=ProductFactBasis.METRIC_100,
        val referencePortion:PortionDescription?=null,
    ) : NutritionalItemSearch {
        fun weight(measurement: Measurement): Double? =
            MeasurementResolver.metricAmount(
                measurement = measurement,
                isLiquid = isLiquid,
                totalWeight = totalWeight,
                servingWeight = if(factBasis==ProductFactBasis.REFERENCE_SERVING)referencePortion?.metric(isLiquid)else servingWeight,
            )
        fun nutritionFor(measurement:Measurement):NutritionFacts? = if(factBasis==ProductFactBasis.REFERENCE_SERVING)
            referenceServingNutrition(nutritionFacts,referencePortion,measurement,isLiquid,totalWeight)
            else weight(measurement)?.let { nutritionFacts*(it/100.0) }
    }

    data class Recipe(
        override val id: NutritionalItemId.Recipe,
        override val headline: String,
        override val isLiquid: Boolean,
        override val suggestedMeasurement: Measurement,
    ) : NutritionalItemSearch
}
