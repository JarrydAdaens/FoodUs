package com.maksimowiczm.foodyou.fooddiary.domain.entity

import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import com.maksimowiczm.foodyou.common.domain.food.RecipeCalculation
import com.maksimowiczm.foodyou.common.domain.food.hasValidValues
import com.maksimowiczm.foodyou.common.domain.food.WeightCalculator
import com.maksimowiczm.foodyou.common.domain.food.sum
import com.maksimowiczm.foodyou.common.domain.measurement.rawValue
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementResolver
import com.maksimowiczm.foodyou.common.domain.food.ReportedNutrition
import com.maksimowiczm.foodyou.common.domain.food.ReportedRecipeComponent
import com.maksimowiczm.foodyou.common.domain.food.ReportedBasis

/** A combined item. Yield is reference servings; diary portions scale captured ingredient totals. */
data class RecipeSnapshot(
    override val name: String,
    val servings: Int?,
    val ingredients: List<RecipeIngredientSnapshot>,
    override val isLiquid: Boolean,
    override val note: String?,
    val calculationVersion: Int = 1,
    val reportedNutrition: ReportedNutrition? = null,
    val reportedComponents: List<ReportedRecipeComponent> = emptyList(),
) : NutritionalItemSnapshot {
    val isReported: Boolean get() = calculationVersion == 2

    override val totalWeight: Double? = if (isReported) reportedNutrition?.totalMetric(isLiquid, servings) else if (calculationVersion == 0) {
        val weights = ingredients.map { it.legacyWeight }
        if (weights.any { it == null }) null else weights.filterNotNull().sum().takeIf { it.isFinite() && it > 0.0 }
    } else RecipeCalculation.totalWeight(
        isLiquid, ingredients.map { it.food.isLiquid to it.weight },
    )
    override val servingWeight: Double? = if (isReported && reportedNutrition?.basis == ReportedBasis.REFERENCE_SERVING) reportedNutrition.referenceMetric(isLiquid)
        else servings?.takeIf { it > 0 }?.let { totalWeight?.div(it)?.takeIf { it.isFinite() && it > 0.0 } }
    val totalNutrition: NutritionFacts = if (isReported) reportedNutrition?.nutritionFor(Measurement.Package(1.0), isLiquid, servings) ?: NutritionFacts()
        else if (calculationVersion == 0) {
        if (ingredients.any { it.legacyNutrition == null }) NutritionFacts()
        else ingredients.mapNotNull { it.legacyNutrition }.sum()
    } else RecipeCalculation.totalNutrition(ingredients.map { it.nutritionFacts })
    val isResolvable: Boolean = if (isReported) reportedNutrition?.let { !it.needsReview && it.facts.hasValidValues() && it.basis in listOf(ReportedBasis.WHOLE_ITEM, ReportedBasis.REFERENCE_SERVING) } == true
        else calculationVersion in 0..1 && servings != null && servings > 0 && ingredients.isNotEmpty() && ingredients.all { if (calculationVersion == 0) it.legacyNutrition != null else it.nutritionFacts != null } &&
        (calculationVersion == 0 || !RecipeCalculation.hasAggregateOverflow(isLiquid, ingredients.map { it.food.isLiquid to it.weight }))

    /** Per-100 facts exist only for a complete compatible metric basis. */
    override val nutritionFacts: NutritionFacts = if (isReported) reportedNutrition?.nutritionFor(
        if (isLiquid) Measurement.Milliliter(100.0) else Measurement.Gram(100.0), isLiquid, servings,
    ) ?: NutritionFacts() else totalWeight?.let { totalNutrition / it * 100.0 } ?: NutritionFacts()

    override fun weight(measurement: Measurement): Double? =
        if (calculationVersion == 0) WeightCalculator.calculateWeight(measurement, totalWeight, servingWeight)
        else MeasurementResolver.metricAmount(measurement, isLiquid, totalWeight, servingWeight)

    fun fraction(measurement: Measurement): Double? =
        if (calculationVersion == 0) weight(measurement)?.let { amount -> totalWeight?.let { amount / it } }?.takeIf { it.isFinite() && it > 0.0 }
        else RecipeCalculation.fraction(measurement, servings, isLiquid, totalWeight)

    override fun nutritionFor(measurement: Measurement): NutritionFacts? {
        if (isReported) return reportedNutrition?.nutritionFor(measurement, isLiquid, servings)
        if (!isResolvable || !measurement.rawValue.isFinite() || measurement.rawValue <= 0.0) return null
        if (calculationVersion == 0) {
            val metric = weight(measurement) ?: return null
            return (nutritionFacts * (metric / 100.0)).takeIf { it.hasValidValues() }
        }
        val fraction = fraction(measurement) ?: return null
        return (totalNutrition * fraction).takeIf { it.hasValidValues() }
    }

    fun flatIngredients(): List<NutritionalItemSnapshot> = ingredients.flatMap {
        val item = it.food
        if (item is RecipeSnapshot) listOf(item) + item.flatIngredients() else listOf(item)
    }.distinct()

    /** Unpack only direct constituents. Nested Recipes stay combined until explicitly unpacked. */
    fun unpack(measurement: Measurement): List<RecipeIngredientSnapshot>? {
        if (isReported) return null
        if (nutritionFor(measurement) == null) return null
        val fraction = fraction(measurement) ?: return null
        val scaled = ingredients.map { it.copy(measurement = it.measurement * fraction) }
        return scaled.takeIf { list -> list.all { if (calculationVersion == 0) it.legacyNutrition != null else it.nutritionFacts != null } }
    }
}
