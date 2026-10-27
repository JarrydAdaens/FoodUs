package com.maksimowiczm.foodyou.ai.domain

/** Layer-1 machinery only. Personal context belongs in the optional user system prompt or hint. */
object AiScanPrompt {
    fun forInput(hasImage: Boolean): String = if (hasImage) PROMPT else PROMPT
        .replace("This photo shows food. Identify distinct foods in the photo.", "Identify the distinct foods in the user description.")
        .replace("pictured portion", "described portion")

    const val PROMPT =
        "This photo shows food. Identify distinct foods in the photo. " +
            "Respond with ONLY a JSON object (no Markdown, no commentary). " +
            "Use a \"foods\" array of food objects. Keep one coherent dish such as a pie as one food; " +
            "do not decompose it into ingredients. Separate distinct foods such as bread, pasta and chicken. " +
            "Use an empty array if no food can be identified. For each food use these keys: " +
            "\"name\" (string), \"certainty\" (number 0 to 1), " +
            "\"description\" (string), \"servingCount\" (positive number), \"weightGrams\" " +
            "(positive total grams), \"calories\" (kilocalories), \"protein\", \"fat\", " +
            "\"carbohydrates\", \"fibre\", \"sugar\" (all grams). " +
            "All nutrition values are absolute totals for that food's pictured portion, " +
            "not per serving or per 100 grams. Serving count and weight are descriptive only. " +
            "Omit or use null for unknown values; never use zero to mean unknown. " +
            "When available, optional nutrient keys (all in grams, including vitamins and minerals) " +
            "are: saturatedFats, transFats, monounsaturatedFats, polyunsaturatedFats, omega3, omega6, addedSugars, solubleFiber, insolubleFiber, salt, cholesterol, caffeine, vitaminA, vitaminB1, vitaminB2, vitaminB3, vitaminB5, vitaminB6, vitaminB7, vitaminB9, vitaminB12, vitaminC, vitaminD, vitaminE, vitaminK, manganese, magnesium, potassium, calcium, copper, zinc, sodium, iron, phosphorus, selenium, iodine, chromium. Do not invent unavailable nutrients."
}
