package com.maksimowiczm.foodyou.app.ui.food.diary.add

import androidx.compose.runtime.*
import com.maksimowiczm.foodyou.common.domain.food.ProductSource
import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import com.maksimowiczm.foodyou.common.domain.food.WeightCalculator
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.domain.food.ProductFactBasis
import com.maksimowiczm.foodyou.common.domain.food.PortionDescription
import com.maksimowiczm.foodyou.common.domain.food.referenceServingNutrition
import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementResolver
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import com.maksimowiczm.foodyou.food.domain.entity.Product

@Immutable
internal data class ProductModel(
    override val foodId: NutritionalItemId.Product,
    override val name: String,
    override val nutritionFacts: NutritionFacts,
    override val isLiquid: Boolean,
    override val note: String?,
    val source: ProductSource,
    override val totalWeight: Double?,
    override val servingWeight: Double?,
    val factBasis: ProductFactBasis = ProductFactBasis.METRIC_100,
    val referencePortion: PortionDescription? = null,
    val nutritionMetadata:com.maksimowiczm.foodyou.common.domain.food.NutritionMetadata?=null,
) : FoodModel {
    constructor(
        product: Product
    ) : this(
        foodId = product.id,
        name = product.headline,
        nutritionFacts = product.nutritionFacts,
        isLiquid = product.isLiquid,
        note = product.note,
        source = product.source,
        totalWeight = product.totalWeight,
        servingWeight = product.servingWeight,
        factBasis = product.factBasis,
        referencePortion = product.referencePortion,
        nutritionMetadata = product.nutritionMetadata,
    )

    override fun weight(measurement: Measurement): Double? =
        MeasurementResolver.metricAmount(
            measurement = measurement,
            servingWeight = if (factBasis == ProductFactBasis.REFERENCE_SERVING) referencePortion?.metric(isLiquid) else servingWeight,
            totalWeight = totalWeight,
            isLiquid = isLiquid,
        )

    override fun nutritionFor(measurement: Measurement): NutritionFacts? =
        if (factBasis == ProductFactBasis.REFERENCE_SERVING) referenceServingNutrition(nutritionFacts, referencePortion, measurement, isLiquid, totalWeight)
        else super.nutritionFor(measurement)
}
