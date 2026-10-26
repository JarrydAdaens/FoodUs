package com.maksimowiczm.foodyou.common.domain.food

import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementResolver
import com.maksimowiczm.foodyou.common.domain.measurement.rawValue
import kotlinx.serialization.Serializable

@Serializable enum class ProductFactBasis { METRIC_100, REFERENCE_SERVING }
@Serializable enum class ReportedBasis { WHOLE_ITEM, REFERENCE_SERVING, UNSPECIFIED, UNAVAILABLE }
@Serializable enum class MetricUnit { GRAM, MILLILITER }

/** Supplied context only. Text is never parsed into a conversion or a nutrition multiplier. */
@Serializable
data class PortionDescription(
    val amount: Double? = null,
    val unitText: String? = null,
    val text: String? = null,
    val metricAmount: Double? = null,
    val metricUnit: MetricUnit? = null,
) {
    fun metric(isLiquid: Boolean): Double? = metricAmount?.takeIf {
        it.isFinite() && it > 0.0 && metricUnit == if (isLiquid) MetricUnit.MILLILITER else MetricUnit.GRAM
    }
}

/** Percentage daily values are retained separately and never participate in nutrition arithmetic. */
@Serializable
data class NutritionMetadata(
    val notes: String? = null,
    val provenance: String? = null,
    val percentDailyValues: Map<String, Double> = emptyMap(),
    val observations: List<NutritionObservation> = emptyList(),
    val textTruncated: Boolean? = null,
    val reference: DefinitionProvenance? = null,
)

/** Secondary claims and unsupported nutrient observations remain evidence, never active facts. */
@Serializable
data class NutritionObservation(
    val kind: String,
    val value: Double? = null,
    val unitText: String? = null,
    val text: String? = null,
)

@Serializable
data class ReportedNutrition(
    val facts: NutritionFacts,
    val basis: ReportedBasis,
    val reference: PortionDescription? = null,
    val metadata: NutritionMetadata? = null,
    val needsReview: Boolean = false,
    val definitionMetadata: NutritionMetadata? = null,
) {
    fun referenceMetric(isLiquid: Boolean): Double? = reference?.metric(isLiquid)

    fun totalMetric(isLiquid: Boolean, yield: Int?): Double? {
        val metric = referenceMetric(isLiquid) ?: return null
        return when (basis) {
            ReportedBasis.WHOLE_ITEM -> metric
            ReportedBasis.REFERENCE_SERVING -> yield?.takeIf { it > 0 }?.let { metric * it }
            else -> null
        }?.takeIf { it.isFinite() && it > 0.0 }
    }

    fun nutritionFor(measurement: Measurement, isLiquid: Boolean, yield: Int?): NutritionFacts? {
        if (needsReview || !facts.hasValidValues() || !measurement.rawValue.isFinite() || measurement.rawValue <= 0.0) return null
        val multiplier = when (basis) {
            ReportedBasis.WHOLE_ITEM -> when (measurement) {
                is Measurement.Package -> measurement.quantity
                is Measurement.Serving -> yield?.takeIf { it > 0 }?.let { measurement.quantity / it }
                else -> metricMultiplier(measurement, isLiquid)
            }
            ReportedBasis.REFERENCE_SERVING -> when (measurement) {
                is Measurement.Serving -> measurement.quantity
                is Measurement.Package -> yield?.takeIf { it > 0 }?.let { measurement.quantity * it }
                else -> metricMultiplier(measurement, isLiquid)
            }
            else -> null
        } ?: return null
        if (!multiplier.isFinite() || multiplier <= 0.0) return null
        return (facts * multiplier).takeIf { it.hasValidValues() }
    }

    private fun metricMultiplier(measurement: Measurement, isLiquid: Boolean): Double? {
        val basisMetric = referenceMetric(isLiquid) ?: return null
        val amount = MeasurementResolver.metricAmount(measurement, isLiquid, null, null) ?: return null
        return amount / basisMetric
    }
}

@Serializable enum class DefinitionKind { PRODUCT, RECIPE, MEAL }

/** A live local ID is optional; snapshots strip it and retain opaque immutable provenance. */
@Serializable
data class DefinitionProvenance(
    val kind: DefinitionKind,
    val namespace: String? = null,
    val externalId: String? = null,
    val localId: Long? = null,
)

@Serializable
data class ReportedRecipeComponent(
    val name: String,
    val portion: PortionDescription? = null,
    val facts: NutritionFacts? = null,
    val reference: DefinitionProvenance? = null,
    val metadata: NutritionMetadata? = null,
) {
    fun captured(): ReportedRecipeComponent = copy(reference = reference?.copy(localId = null))
}

/** Product packages convert through an independently known metric ratio, never an integer yield. */
fun referenceServingNutrition(
    facts: NutritionFacts, reference: PortionDescription?, measurement: Measurement,
    isLiquid: Boolean, packageWeight: Double?,
): NutritionFacts? {
    val portion = if (measurement is Measurement.Package) {
        val metric = MeasurementResolver.metricAmount(measurement, isLiquid, packageWeight, null) ?: return null
        if (isLiquid) Measurement.Milliliter(metric) else Measurement.Gram(metric)
    } else measurement
    return ReportedNutrition(facts, ReportedBasis.REFERENCE_SERVING, reference).nutritionFor(portion, isLiquid, null)
}
