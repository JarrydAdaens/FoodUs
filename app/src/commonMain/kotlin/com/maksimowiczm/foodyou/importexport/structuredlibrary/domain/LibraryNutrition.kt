package com.maksimowiczm.foodyou.importexport.structuredlibrary.domain

import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.common.domain.food.NutrientValue.Companion.toNutrientValue

val libraryNutrientKeys: Set<String> = setOf("proteins", "carbohydrates", "energy", "fats", "saturatedFats", "transFats", "monounsaturatedFats", "polyunsaturatedFats", "omega3", "omega6", "sugars", "addedSugars", "dietaryFiber", "solubleFiber", "insolubleFiber", "salt", "cholesterol", "caffeine", "vitaminA", "vitaminB1", "vitaminB2", "vitaminB3", "vitaminB5", "vitaminB6", "vitaminB7", "vitaminB9", "vitaminB12", "vitaminC", "vitaminD", "vitaminE", "vitaminK", "manganese", "magnesium", "potassium", "calcium", "copper", "zinc", "sodium", "iron", "phosphorus", "selenium", "iodine", "chromium")

fun LibraryFacts.toNutritionFacts(): NutritionFacts = NutritionFacts(
    proteins = values["proteins"].toNutrientValue(),
    carbohydrates = values["carbohydrates"].toNutrientValue(),
    energy = values["energy"].toNutrientValue(),
    fats = values["fats"].toNutrientValue(),
    saturatedFats = values["saturatedFats"].toNutrientValue(),
    transFats = values["transFats"].toNutrientValue(),
    monounsaturatedFats = values["monounsaturatedFats"].toNutrientValue(),
    polyunsaturatedFats = values["polyunsaturatedFats"].toNutrientValue(),
    omega3 = values["omega3"].toNutrientValue(),
    omega6 = values["omega6"].toNutrientValue(),
    sugars = values["sugars"].toNutrientValue(),
    addedSugars = values["addedSugars"].toNutrientValue(),
    dietaryFiber = values["dietaryFiber"].toNutrientValue(),
    solubleFiber = values["solubleFiber"].toNutrientValue(),
    insolubleFiber = values["insolubleFiber"].toNutrientValue(),
    salt = values["salt"].toNutrientValue(),
    cholesterol = values["cholesterol"].toNutrientValue(),
    caffeine = values["caffeine"].toNutrientValue(),
    vitaminA = values["vitaminA"].toNutrientValue(),
    vitaminB1 = values["vitaminB1"].toNutrientValue(),
    vitaminB2 = values["vitaminB2"].toNutrientValue(),
    vitaminB3 = values["vitaminB3"].toNutrientValue(),
    vitaminB5 = values["vitaminB5"].toNutrientValue(),
    vitaminB6 = values["vitaminB6"].toNutrientValue(),
    vitaminB7 = values["vitaminB7"].toNutrientValue(),
    vitaminB9 = values["vitaminB9"].toNutrientValue(),
    vitaminB12 = values["vitaminB12"].toNutrientValue(),
    vitaminC = values["vitaminC"].toNutrientValue(),
    vitaminD = values["vitaminD"].toNutrientValue(),
    vitaminE = values["vitaminE"].toNutrientValue(),
    vitaminK = values["vitaminK"].toNutrientValue(),
    manganese = values["manganese"].toNutrientValue(),
    magnesium = values["magnesium"].toNutrientValue(),
    potassium = values["potassium"].toNutrientValue(),
    calcium = values["calcium"].toNutrientValue(),
    copper = values["copper"].toNutrientValue(),
    zinc = values["zinc"].toNutrientValue(),
    sodium = values["sodium"].toNutrientValue(),
    iron = values["iron"].toNutrientValue(),
    phosphorus = values["phosphorus"].toNutrientValue(),
    selenium = values["selenium"].toNutrientValue(),
    iodine = values["iodine"].toNutrientValue(),
    chromium = values["chromium"].toNutrientValue(),
)

fun LibraryPanel.captured(): ReportedNutrition = ReportedNutrition(nutrition.toNutritionFacts(), basis, reference, metadata?.captured())

fun LibraryRecipe.capturedPanel(): ReportedNutrition? = reportedPanel?.captured()?.copy(definitionMetadata=metadata?.captured())

fun LibraryProduct.metricUnit(): MetricUnit? = reference.metricUnit ?: packageMetric?.unit ?: servingMetric?.unit
