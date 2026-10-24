package com.maksimowiczm.foodyou.ai.domain

/** Layer-1 machinery only. Personal context belongs in the optional user system prompt or hint. */
object AiScanPrompt {
    const val PROMPT =
        "This photo shows food. Identify the food in the photo. " +
            "Respond with ONLY a JSON object (no Markdown, no commentary). " +
            "Use these keys: \"name\" (string), \"certainty\" (number 0 to 1), " +
            "\"description\" (string), \"servingCount\" (positive number), \"weightGrams\" " +
            "(positive total grams), \"calories\" (kilocalories), \"protein\", \"fat\", " +
            "\"carbohydrates\", \"fibre\", \"sugar\" (all grams). " +
            "All nutrition values are absolute totals for the entire pictured portion, " +
            "not per serving or per 100 grams. Serving count and weight are descriptive only. " +
            "Omit or use null for unknown values; never use zero to mean unknown. " +
            "When available, optional nutrient keys (all in grams, including vitamins and minerals) " +
            "are: saturatedFats, transFats, monounsaturatedFats, polyunsaturatedFats, omega3, omega6, addedSugars, solubleFiber, insolubleFiber, salt, cholesterol, caffeine, vitaminA, vitaminB1, vitaminB2, vitaminB3, vitaminB5, vitaminB6, vitaminB7, vitaminB9, vitaminB12, vitaminC, vitaminD, vitaminE, vitaminK, manganese, magnesium, potassium, calcium, copper, zinc, sodium, iron, phosphorus, selenium, iodine, chromium. Do not invent unavailable nutrients."
}
