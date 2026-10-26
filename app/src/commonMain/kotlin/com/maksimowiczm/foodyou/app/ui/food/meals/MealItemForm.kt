package com.maksimowiczm.foodyou.app.ui.food.meals

import androidx.compose.runtime.*
import com.maksimowiczm.foodyou.app.ui.common.utility.EnergyFormatter
import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.common.domain.measurement.rawValue
import com.maksimowiczm.foodyou.fooddiary.domain.entity.*

/** Text remains separate from saved facts: untouched fields never take a lossy display round trip. */
internal class MealItemForm(item: MealItem, private val energy: EnergyFormatter) {
    private var original = item
    var name by mutableStateOf(item.name)
    var amount by mutableStateOf(item.capturedPortion?.rawValue?.toString().orEmpty())
    var servings by mutableStateOf(item.servingCount?.toString().orEmpty())
    var grams by mutableStateOf(item.weightGrams?.toString().orEmpty())
    var factor by mutableStateOf("1")
    var error by mutableStateOf(false)
    val nutrients = mutableStateMapOf<NutritionFactsField, String>()
    private var initialNutrients = emptyMap<NutritionFactsField, String>()
    val portion get() = original.capturedPortion

    init { reset(item) }

    private fun reset(item: MealItem) {
        original = item
        name = item.name
        amount = item.capturedPortion?.rawValue?.toString().orEmpty()
        servings = item.servingCount?.toString().orEmpty()
        grams = item.weightGrams?.toString().orEmpty()
        initialNutrients = NutritionFactsField.entries.associateWith { field ->
            item.nutritionFacts[field].value?.let { value ->
                (if (field == NutritionFactsField.Energy) energy.fromKcal(value) else value * field.mealDisplayFactor()).toString()
            }.orEmpty()
        }
        nutrients.clear()
        nutrients.putAll(initialNutrients)
        factor = "1"
        error = false
    }

    fun candidate(): MealItem? {
        var result = original.copy(name = name.trim())
        val oldAmount = original.capturedPortion?.rawValue
        if (oldAmount != null && amount != oldAmount.toString()) {
            val next = amount.toDoubleOrNull()?.takeIf { it.isFinite() && it > 0.0 } ?: return null
            result = result.scaleCapturedPortion(next / oldAmount) ?: return null
        }
        for (field in NutritionFactsField.entries) {
            val text = nutrients[field].orEmpty()
            if (text == initialNutrients[field]) continue
            val value = if (text.isBlank()) null else {
                val displayed = text.toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0.0 } ?: return null
                val converted = if (field == NutritionFactsField.Energy) energy.toKcal(displayed) else displayed / field.mealDisplayFactor()
                if (!converted.isFinite() || (displayed > 0.0 && converted == 0.0)) return null
                converted
            }
            result = result.copy(nutritionFacts = result.nutritionFacts.withMealNutrient(field, value))
        }
        fun positive(text: String): Double? = text.toDoubleOrNull()?.takeIf { it.isFinite() && it > 0.0 }
        val parsedServings = if (servings.isBlank()) null else positive(servings) ?: return null
        val parsedGrams = if (grams.isBlank()) null else positive(grams) ?: return null
        val changedServings = parsedServings != original.servingCount
        val changedGrams = parsedGrams != original.weightGrams
        if (changedServings || changedGrams) {
            val count = parsedServings
            val weight = parsedGrams
            result = result.copy(
                servingCount = if (changedServings) count else result.servingCount,
                weightGrams = if (changedGrams) weight else result.weightGrams,
                capturedPortion = null,
            )
        }
        return result.takeIf { validateMeal("Meal", listOf(it)) == null && canDisplay(it.nutritionFacts) }
    }

    private fun canDisplay(facts: NutritionFacts): Boolean = NutritionFactsField.entries.all { field ->
        facts[field].value?.let { value ->
            (if (field == NutritionFactsField.Energy) energy.fromKcal(value) else value * field.mealDisplayFactor()).isFinite()
        } ?: true
    }

    fun scale() {
        val current = candidate()
        val multiplier = factor.toDoubleOrNull()
        val scaled = if (current != null && multiplier != null) current.scaleCapturedPortion(multiplier) else null
        if (scaled == null || !canDisplay(scaled.nutritionFacts)) error = true else reset(scaled)
    }

    fun confirm(onSave: (MealItem) -> Unit) {
        val current = candidate()
        if (current == null) error = true else onSave(current)
    }
}

internal fun NutritionFactsField.mealDisplayFactor(): Double = when (this) {
    NutritionFactsField.VitaminA, NutritionFactsField.VitaminB7, NutritionFactsField.VitaminB9,
    NutritionFactsField.VitaminB12, NutritionFactsField.VitaminD, NutritionFactsField.VitaminK,
    NutritionFactsField.Selenium, NutritionFactsField.Iodine, NutritionFactsField.Chromium -> 1_000_000.0
    NutritionFactsField.Cholesterol, NutritionFactsField.Caffeine, NutritionFactsField.VitaminB1,
    NutritionFactsField.VitaminB2, NutritionFactsField.VitaminB3, NutritionFactsField.VitaminB5,
    NutritionFactsField.VitaminB6, NutritionFactsField.VitaminC, NutritionFactsField.VitaminE,
    NutritionFactsField.Manganese, NutritionFactsField.Magnesium, NutritionFactsField.Potassium,
    NutritionFactsField.Calcium, NutritionFactsField.Copper, NutritionFactsField.Zinc,
    NutritionFactsField.Sodium, NutritionFactsField.Iron, NutritionFactsField.Phosphorus -> 1_000.0
    else -> 1.0
}
