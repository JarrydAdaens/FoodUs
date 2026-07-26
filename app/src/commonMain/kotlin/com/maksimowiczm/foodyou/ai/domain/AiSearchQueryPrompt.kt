package com.maksimowiczm.foodyou.ai.domain

/**
 * Builds the hidden prompt that asks the model to convert a placeholder note into a food-search
 * query (Milestone 2, Story 9). The Australian / Melbourne locale and the meal context are embedded
 * here so the model returns a query tuned to local foods. Kept pure and separate so its shaping can
 * be unit tested without a live endpoint.
 */
object AiSearchQueryPrompt {
    fun build(mealName: String?, note: String): String {
        val meal = mealName?.trim()?.lowercase()?.takeIf { it.isNotBlank() } ?: "a meal"
        val cleanedNote = note.trim()
        return "I am Australian, living in Melbourne. This is what I had for $meal: " +
            "\"$cleanedNote\". Turn it into a short food-search query I can type into a food " +
            "database to find the closest matching food. Use Australian food names. Respond with " +
            "ONLY the search query itself — no quotes, no labels, no explanation, a few words " +
            "at most."
    }
}
