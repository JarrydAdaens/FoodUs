package com.maksimowiczm.foodyou.food.domain.entity

import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import com.maksimowiczm.foodyou.common.domain.food.RecipeCalculation
import com.maksimowiczm.foodyou.common.domain.food.hasValidValues
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementResolver
import com.maksimowiczm.foodyou.common.domain.food.ReportedNutrition
import com.maksimowiczm.foodyou.common.domain.food.ReportedRecipeComponent

/** A combined item. Yield is reference servings; diary portions scale captured ingredient totals. */
data class Recipe(
    override val id: NutritionalItemId.Recipe,
    val name: String,
    val servings: Int?,
    val ingredients: List<RecipeIngredient>,
    val note: String?,
    override val isLiquid: Boolean,
    val reportedNutrition: ReportedNutrition? = null,
    val reportedComponents: List<ReportedRecipeComponent> = emptyList(),
) : NutritionalItem {
    val isReported: Boolean get() = reportedNutrition != null
    override val headline: String = name

    override val totalWeight: Double? = if (isReported) reportedNutrition?.totalMetric(isLiquid, servings) else RecipeCalculation.totalWeight(
        isLiquid, ingredients.map { it.food.isLiquid to it.weight },
    )
    override val servingWeight: Double? = if (isReported) {
        if (reportedNutrition?.basis == com.maksimowiczm.foodyou.common.domain.food.ReportedBasis.REFERENCE_SERVING) reportedNutrition.referenceMetric(isLiquid)
        else servings?.takeIf { it > 0 }?.let { totalWeight?.div(it) }
    } else servings?.takeIf { it > 0 }?.let { totalWeight?.div(it)?.takeIf { it.isFinite() && it > 0.0 } }
    val totalNutrition: NutritionFacts = if (isReported) reportedNutrition?.nutritionFor(Measurement.Package(1.0), isLiquid, servings) ?: NutritionFacts()
        else RecipeCalculation.totalNutrition(ingredients.map { it.nutritionFacts })
    val isResolvable: Boolean = if (isReported) reportedNutrition?.let { !it.needsReview && it.facts.hasValidValues() && it.basis in listOf(com.maksimowiczm.foodyou.common.domain.food.ReportedBasis.WHOLE_ITEM, com.maksimowiczm.foodyou.common.domain.food.ReportedBasis.REFERENCE_SERVING) } == true
        else servings != null && servings > 0 && ingredients.isNotEmpty() && ingredients.all { it.nutritionFacts != null } &&
        !RecipeCalculation.hasAggregateOverflow(isLiquid, ingredients.map { it.food.isLiquid to it.weight })

    /** Per-100 facts exist only for a complete compatible metric basis. */
    override val nutritionFacts: NutritionFacts = if (isReported) reportedNutrition?.nutritionFor(
        if (isLiquid) Measurement.Milliliter(100.0) else Measurement.Gram(100.0), isLiquid, servings,
    ) ?: NutritionFacts() else totalWeight?.let { totalNutrition / it * 100.0 } ?: NutritionFacts()

    override fun weight(measurement: Measurement): Double? =
        MeasurementResolver.metricAmount(measurement, isLiquid, totalWeight, servingWeight)

    fun fraction(measurement: Measurement): Double? =
        RecipeCalculation.fraction(measurement, servings, isLiquid, totalWeight)

    override fun nutritionFor(measurement: Measurement): NutritionFacts? {
        if (isReported) return reportedNutrition?.nutritionFor(measurement, isLiquid, servings)
        if (!isResolvable) return null
        val fraction = fraction(measurement) ?: return null
        return (totalNutrition * fraction).takeIf { it.hasValidValues() }
    }

    fun flatIngredients(): List<NutritionalItem> = ingredients.flatMap {
        val item = it.food
        if (item is Recipe) listOf(item) + item.flatIngredients() else listOf(item)
    }.distinct()

    /** Unpack only direct constituents. Nested Recipes stay combined until explicitly unpacked. */
    fun unpack(measurement: Measurement): List<RecipeIngredient>? {
        if (isReported) return null
        if (nutritionFor(measurement) == null) return null
        val fraction = fraction(measurement) ?: return null
        val scaled = ingredients.map { it.copy(measurement = it.measurement * fraction) }
        return scaled.takeIf { list -> list.all { it.nutritionFacts != null } }
    }
}
