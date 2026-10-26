package com.maksimowiczm.foodyou.common.domain.food

import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementResolver
import com.maksimowiczm.foodyou.common.domain.measurement.rawValue

/** Recipe totals precede portion scaling; a serving does not require an invented metric basis. */
object RecipeCalculation {
    fun totalWeight(isLiquid: Boolean, amounts: List<Pair<Boolean, Double?>>): Double? {
        if (amounts.isEmpty() || amounts.any { it.first != isLiquid || !MeasurementResolver.isKnownBasis(it.second) }) return null
        return amounts.sumOf { it.second!! }.takeIf { it.isFinite() && it > 0.0 }
    }

    /** An overflowing known basis is invalid, rather than a genuinely unknown/mixed basis. */
    fun hasAggregateOverflow(isLiquid: Boolean, amounts: List<Pair<Boolean, Double?>>): Boolean =
        amounts.isNotEmpty() && amounts.all { it.first == isLiquid && MeasurementResolver.isKnownBasis(it.second) } &&
            !MeasurementResolver.isKnownBasis(amounts.sumOf { it.second!! })

    fun fraction(measurement: Measurement, servings: Int?, isLiquid: Boolean, totalWeight: Double?): Double? {
        if (servings == null || servings <= 0 || !measurement.rawValue.isFinite() || measurement.rawValue <= 0.0) return null
        val fraction = when (measurement) {
            is Measurement.Serving -> measurement.quantity / servings
            is Measurement.Package -> measurement.quantity
            else -> {
                if (!MeasurementResolver.isKnownBasis(totalWeight)) return null
                val metric = MeasurementResolver.metricAmount(measurement, isLiquid, totalWeight, null) ?: return null
                metric / totalWeight!!
            }
        }
        return fraction.takeIf { it.isFinite() && it > 0.0 }
    }

    /** Unknown contributors keep a nutrient unknown, rather than presenting a partial sum as its total. */
    fun totalNutrition(facts: List<NutritionFacts?>): NutritionFacts {
        if (facts.isEmpty() || facts.any { it == null }) return NutritionFacts()
        val known = facts.filterNotNull()
        fun sum(field: (NutritionFacts) -> NutrientValue): NutrientValue {
            val values = known.map(field)
            if (values.any { !it.isComplete }) return NutrientValue.Incomplete(null)
            val total = values.sumOf { it.value!! }
            return NutrientValue.Complete(total)
        }
        return NutritionFacts(
            proteins = sum { it.proteins },
            carbohydrates = sum { it.carbohydrates },
            energy = sum { it.energy },
            fats = sum { it.fats },
            saturatedFats = sum { it.saturatedFats },
            transFats = sum { it.transFats },
            monounsaturatedFats = sum { it.monounsaturatedFats },
            polyunsaturatedFats = sum { it.polyunsaturatedFats },
            omega3 = sum { it.omega3 },
            omega6 = sum { it.omega6 },
            sugars = sum { it.sugars },
            addedSugars = sum { it.addedSugars },
            dietaryFiber = sum { it.dietaryFiber },
            solubleFiber = sum { it.solubleFiber },
            insolubleFiber = sum { it.insolubleFiber },
            salt = sum { it.salt },
            cholesterol = sum { it.cholesterol },
            caffeine = sum { it.caffeine },
            vitaminA = sum { it.vitaminA },
            vitaminB1 = sum { it.vitaminB1 },
            vitaminB2 = sum { it.vitaminB2 },
            vitaminB3 = sum { it.vitaminB3 },
            vitaminB5 = sum { it.vitaminB5 },
            vitaminB6 = sum { it.vitaminB6 },
            vitaminB7 = sum { it.vitaminB7 },
            vitaminB9 = sum { it.vitaminB9 },
            vitaminB12 = sum { it.vitaminB12 },
            vitaminC = sum { it.vitaminC },
            vitaminD = sum { it.vitaminD },
            vitaminE = sum { it.vitaminE },
            vitaminK = sum { it.vitaminK },
            manganese = sum { it.manganese },
            magnesium = sum { it.magnesium },
            potassium = sum { it.potassium },
            calcium = sum { it.calcium },
            copper = sum { it.copper },
            zinc = sum { it.zinc },
            sodium = sum { it.sodium },
            iron = sum { it.iron },
            phosphorus = sum { it.phosphorus },
            selenium = sum { it.selenium },
            iodine = sum { it.iodine },
            chromium = sum { it.chromium },
        )
    }
}
