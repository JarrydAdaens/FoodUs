package com.maksimowiczm.foodyou.fooddiary.domain.entity

import com.maksimowiczm.foodyou.common.domain.food.*

/** Maps every supported field without silently dropping hidden nutrients. */
internal fun NutritionFacts.mapMealNutrition(transform: (NutritionFactsField, NutrientValue) -> NutrientValue): NutritionFacts =
    NutritionFacts(
        proteins = transform(NutritionFactsField.Proteins, proteins),
        carbohydrates = transform(NutritionFactsField.Carbohydrates, carbohydrates),
        energy = transform(NutritionFactsField.Energy, energy),
        fats = transform(NutritionFactsField.Fats, fats),
        saturatedFats = transform(NutritionFactsField.SaturatedFats, saturatedFats),
        transFats = transform(NutritionFactsField.TransFats, transFats),
        monounsaturatedFats = transform(NutritionFactsField.MonounsaturatedFats, monounsaturatedFats),
        polyunsaturatedFats = transform(NutritionFactsField.PolyunsaturatedFats, polyunsaturatedFats),
        omega3 = transform(NutritionFactsField.Omega3, omega3),
        omega6 = transform(NutritionFactsField.Omega6, omega6),
        sugars = transform(NutritionFactsField.Sugars, sugars),
        addedSugars = transform(NutritionFactsField.AddedSugars, addedSugars),
        dietaryFiber = transform(NutritionFactsField.DietaryFiber, dietaryFiber),
        solubleFiber = transform(NutritionFactsField.SolubleFiber, solubleFiber),
        insolubleFiber = transform(NutritionFactsField.InsolubleFiber, insolubleFiber),
        salt = transform(NutritionFactsField.Salt, salt),
        cholesterol = transform(NutritionFactsField.Cholesterol, cholesterol),
        caffeine = transform(NutritionFactsField.Caffeine, caffeine),
        vitaminA = transform(NutritionFactsField.VitaminA, vitaminA),
        vitaminB1 = transform(NutritionFactsField.VitaminB1, vitaminB1),
        vitaminB2 = transform(NutritionFactsField.VitaminB2, vitaminB2),
        vitaminB3 = transform(NutritionFactsField.VitaminB3, vitaminB3),
        vitaminB5 = transform(NutritionFactsField.VitaminB5, vitaminB5),
        vitaminB6 = transform(NutritionFactsField.VitaminB6, vitaminB6),
        vitaminB7 = transform(NutritionFactsField.VitaminB7, vitaminB7),
        vitaminB9 = transform(NutritionFactsField.VitaminB9, vitaminB9),
        vitaminB12 = transform(NutritionFactsField.VitaminB12, vitaminB12),
        vitaminC = transform(NutritionFactsField.VitaminC, vitaminC),
        vitaminD = transform(NutritionFactsField.VitaminD, vitaminD),
        vitaminE = transform(NutritionFactsField.VitaminE, vitaminE),
        vitaminK = transform(NutritionFactsField.VitaminK, vitaminK),
        manganese = transform(NutritionFactsField.Manganese, manganese),
        magnesium = transform(NutritionFactsField.Magnesium, magnesium),
        potassium = transform(NutritionFactsField.Potassium, potassium),
        calcium = transform(NutritionFactsField.Calcium, calcium),
        copper = transform(NutritionFactsField.Copper, copper),
        zinc = transform(NutritionFactsField.Zinc, zinc),
        sodium = transform(NutritionFactsField.Sodium, sodium),
        iron = transform(NutritionFactsField.Iron, iron),
        phosphorus = transform(NutritionFactsField.Phosphorus, phosphorus),
        selenium = transform(NutritionFactsField.Selenium, selenium),
        iodine = transform(NutritionFactsField.Iodine, iodine),
        chromium = transform(NutritionFactsField.Chromium, chromium),
    )

fun NutritionFacts.captureMealNutrition(): NutritionFacts = mapMealNutrition { _, value ->
    if (value is NutrientValue.Complete) value else NutrientValue.Incomplete(null)
}

/** Existing numeric Meal facts retain their known values; edits replace only the chosen field. */
fun NutritionFacts.withMealNutrient(field: NutritionFactsField, value: Double?): NutritionFacts =
    mapMealNutrition { current, old -> if (current == field) NutrientValue.from(value) else old }
