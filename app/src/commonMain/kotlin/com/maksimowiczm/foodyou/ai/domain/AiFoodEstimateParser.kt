package com.maksimowiczm.foodyou.ai.domain

import kotlinx.serialization.json.*

/** Parses canonical portion totals, tolerating legacy spellings and Markdown wrappers.
 * Unsupported keys are ignored. Invalid individual optional values become unknown, never zero.
 */
object AiFoodEstimateParser {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun parse(content: String): AiFoodEstimate? {
        val start = content.indexOf('{')
        val end = content.lastIndexOf('}')
        if (start < 0 || end <= start) return null
        val objectText = content.substring(start, end + 1)
        val objectValue = runCatching { json.parseToJsonElement(objectText).jsonObject }.getOrNull()
            ?: return null
        val dto = runCatching { json.decodeFromString<AiFoodEstimate>(objectText) }.getOrNull()
            ?: return null
        val name = dto.name.trim().takeIf { it.isNotBlank() } ?: return null
        // Only explicit alternate units are converted. Canonical values take precedence.
        val kcal = dto.calories ?: (objectValue["energyKj"] as? JsonPrimitive)?.doubleOrNull?.div(4.184)
        val grams = dto.weightGrams ?: (objectValue["weightKg"] as? JsonPrimitive)?.doubleOrNull?.times(1000.0)
        return dto.copy(
            name = name,
            certainty = dto.certainty?.takeIf { it.isFinite() }?.coerceIn(0.0, 1.0),
            description = dto.description?.trim()?.takeIf { it.isNotBlank() },
            servingCount = dto.servingCount.positive(),
            weightGrams = grams.positive(),
            calories = kcal.nonNegative(),
            protein = dto.protein.nonNegative(),
            fat = dto.fat.nonNegative(),
            fibre = dto.fibre.nonNegative(),
            sugar = dto.sugar.nonNegative(),
            carbohydrates = dto.carbohydrates.nonNegative(),
            saturatedFats = dto.saturatedFats.nonNegative(),
            transFats = dto.transFats.nonNegative(),
            monounsaturatedFats = dto.monounsaturatedFats.nonNegative(),
            polyunsaturatedFats = dto.polyunsaturatedFats.nonNegative(),
            omega3 = dto.omega3.nonNegative(),
            omega6 = dto.omega6.nonNegative(),
            addedSugars = dto.addedSugars.nonNegative(),
            solubleFiber = dto.solubleFiber.nonNegative(),
            insolubleFiber = dto.insolubleFiber.nonNegative(),
            salt = dto.salt.nonNegative(),
            cholesterol = dto.cholesterol.nonNegative(),
            caffeine = dto.caffeine.nonNegative(),
            vitaminA = dto.vitaminA.nonNegative(),
            vitaminB1 = dto.vitaminB1.nonNegative(),
            vitaminB2 = dto.vitaminB2.nonNegative(),
            vitaminB3 = dto.vitaminB3.nonNegative(),
            vitaminB5 = dto.vitaminB5.nonNegative(),
            vitaminB6 = dto.vitaminB6.nonNegative(),
            vitaminB7 = dto.vitaminB7.nonNegative(),
            vitaminB9 = dto.vitaminB9.nonNegative(),
            vitaminB12 = dto.vitaminB12.nonNegative(),
            vitaminC = dto.vitaminC.nonNegative(),
            vitaminD = dto.vitaminD.nonNegative(),
            vitaminE = dto.vitaminE.nonNegative(),
            vitaminK = dto.vitaminK.nonNegative(),
            manganese = dto.manganese.nonNegative(),
            magnesium = dto.magnesium.nonNegative(),
            potassium = dto.potassium.nonNegative(),
            calcium = dto.calcium.nonNegative(),
            copper = dto.copper.nonNegative(),
            zinc = dto.zinc.nonNegative(),
            sodium = dto.sodium.nonNegative(),
            iron = dto.iron.nonNegative(),
            phosphorus = dto.phosphorus.nonNegative(),
            selenium = dto.selenium.nonNegative(),
            iodine = dto.iodine.nonNegative(),
            chromium = dto.chromium.nonNegative(),
        )
    }

    private fun Double?.nonNegative() = this?.takeIf { it.isFinite() && it >= 0.0 }
    private fun Double?.positive() = this?.takeIf { it.isFinite() && it > 0.0 }
}
