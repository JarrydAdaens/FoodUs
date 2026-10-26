package com.maksimowiczm.foodyou.app.ui.food.diary.quickadd

import com.maksimowiczm.foodyou.common.domain.food.NutrientValue.Companion.toNutrientValue
import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import com.maksimowiczm.foodyou.common.domain.food.NutritionFactsField
import com.maksimowiczm.foodyou.common.domain.food.get
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Local promotion-route payload only. Flat nullable values avoid polymorphic serialization of
 * domain inline nutrient classes; shared domain, Room and relay contracts stay unchanged.
 * Canonical energy is kcal, all nutrient masses are grams, and null remains unknown.
 */
internal object QuickAddNutritionSnapshot {
    fun encode(facts: NutritionFacts): String = Json.encodeToString(
        Values(NutritionFactsField.entries.associate { it.name to facts[it].value })
    )

    fun decode(content: String): NutritionFacts {
        val values = Json.decodeFromString<Values>(content).values
        return NutritionFacts(
            energy = values["Energy"].toNutrientValue(),
            proteins = values["Proteins"].toNutrientValue(),
            fats = values["Fats"].toNutrientValue(),
            saturatedFats = values["SaturatedFats"].toNutrientValue(),
            transFats = values["TransFats"].toNutrientValue(),
            monounsaturatedFats = values["MonounsaturatedFats"].toNutrientValue(),
            polyunsaturatedFats = values["PolyunsaturatedFats"].toNutrientValue(),
            omega3 = values["Omega3"].toNutrientValue(),
            omega6 = values["Omega6"].toNutrientValue(),
            carbohydrates = values["Carbohydrates"].toNutrientValue(),
            sugars = values["Sugars"].toNutrientValue(),
            addedSugars = values["AddedSugars"].toNutrientValue(),
            dietaryFiber = values["DietaryFiber"].toNutrientValue(),
            solubleFiber = values["SolubleFiber"].toNutrientValue(),
            insolubleFiber = values["InsolubleFiber"].toNutrientValue(),
            salt = values["Salt"].toNutrientValue(),
            cholesterol = values["Cholesterol"].toNutrientValue(),
            caffeine = values["Caffeine"].toNutrientValue(),
            vitaminA = values["VitaminA"].toNutrientValue(),
            vitaminB1 = values["VitaminB1"].toNutrientValue(),
            vitaminB2 = values["VitaminB2"].toNutrientValue(),
            vitaminB3 = values["VitaminB3"].toNutrientValue(),
            vitaminB5 = values["VitaminB5"].toNutrientValue(),
            vitaminB6 = values["VitaminB6"].toNutrientValue(),
            vitaminB7 = values["VitaminB7"].toNutrientValue(),
            vitaminB9 = values["VitaminB9"].toNutrientValue(),
            vitaminB12 = values["VitaminB12"].toNutrientValue(),
            vitaminC = values["VitaminC"].toNutrientValue(),
            vitaminD = values["VitaminD"].toNutrientValue(),
            vitaminE = values["VitaminE"].toNutrientValue(),
            vitaminK = values["VitaminK"].toNutrientValue(),
            manganese = values["Manganese"].toNutrientValue(),
            magnesium = values["Magnesium"].toNutrientValue(),
            potassium = values["Potassium"].toNutrientValue(),
            calcium = values["Calcium"].toNutrientValue(),
            copper = values["Copper"].toNutrientValue(),
            zinc = values["Zinc"].toNutrientValue(),
            sodium = values["Sodium"].toNutrientValue(),
            iron = values["Iron"].toNutrientValue(),
            phosphorus = values["Phosphorus"].toNutrientValue(),
            selenium = values["Selenium"].toNutrientValue(),
            iodine = values["Iodine"].toNutrientValue(),
            chromium = values["Chromium"].toNutrientValue(),
        )
    }

    @Serializable private data class Values(val values: Map<String, Double?>)
}
