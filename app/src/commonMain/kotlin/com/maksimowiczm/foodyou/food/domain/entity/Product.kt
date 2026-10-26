package com.maksimowiczm.foodyou.food.domain.entity

import com.maksimowiczm.foodyou.common.domain.food.ProductSource
import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import com.maksimowiczm.foodyou.common.domain.food.ProductFactBasis
import com.maksimowiczm.foodyou.common.domain.food.PortionDescription
import com.maksimowiczm.foodyou.common.domain.food.NutritionMetadata
import com.maksimowiczm.foodyou.common.domain.food.ReportedNutrition
import com.maksimowiczm.foodyou.common.domain.food.ReportedBasis
import com.maksimowiczm.foodyou.common.domain.food.referenceServingNutrition
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementResolver

/**
 * Represents a product in the food domain.
 *
 * @param id Unique identifier for the product.
 * @param name Name of the product.
 * @param brand Brand of the product, if available.
 * @param barcode Barcode of the product, if available.
 * @param note Additional note about the product, if available.
 * @param isLiquid Indicates whether the product is liquid (e.g., juice, milk).
 * @param packageWeight Weight of the product package, if available.
 * @param servingWeight Weight of a single serving of the product, if available.
 * @param source Source of the product.
 * @param nutritionFacts Nutrition facts of the product per 100g or 100ml, depending on whether the
 *   product is solid or liquid.
 */
data class Product(
    override val id: NutritionalItemId.Product,
    val name: String,
    val brand: String?,
    val barcode: String?,
    val note: String?,
    override val isLiquid: Boolean,
    val packageWeight: Double?,
    override val servingWeight: Double?,
    val source: ProductSource,
    override val nutritionFacts: NutritionFacts,
    val factBasis: ProductFactBasis = ProductFactBasis.METRIC_100,
    val referencePortion: PortionDescription? = null,
    val nutritionMetadata: NutritionMetadata? = null,
) : NutritionalItem {
    override val totalWeight: Double? = packageWeight

    override fun nutritionFor(measurement: Measurement): NutritionFacts? {
        return if (factBasis == ProductFactBasis.METRIC_100) super.nutritionFor(measurement)
        else referenceServingNutrition(nutritionFacts, referencePortion, measurement, isLiquid, packageWeight)
    }

    override fun weight(measurement: Measurement): Double? =
        if (factBasis == ProductFactBasis.METRIC_100) super.weight(measurement)
        else MeasurementResolver.metricAmount(measurement, isLiquid, packageWeight, referencePortion?.metric(isLiquid))


    /** CSV remains a per-100 metric format. A missing metric reference is not exportable. */
    fun metricNutrition(): NutritionFacts? = nutritionFor(if (isLiquid) Measurement.Milliliter(100.0) else Measurement.Gram(100.0))

    override val headline = run {
        val brandSuffix =
            if (!brand.isNullOrEmpty()) {
                " ($brand)"
            } else {
                ""
            }

        "${name}$brandSuffix"
    }
}
