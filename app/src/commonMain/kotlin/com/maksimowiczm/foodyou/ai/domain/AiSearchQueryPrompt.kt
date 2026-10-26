package com.maksimowiczm.foodyou.ai.domain

/**
 * Builds the layer-1 developer prompt that asks the model to convert a placeholder note into a
 * food-search query (Milestone 2, Story 9; rebuilt for Story 21's three-layer architecture). Pure
 * machinery: the diarySlot context and note plus the output-shape contract, carrying no locale or
 * personal data — that belongs to the user's optional system prompt (layer 2). Kept pure and
 * separate so its shaping can be unit tested without a live endpoint.
 */
object AiSearchQueryPrompt {
    fun build(diarySlotName: String?, note: String): String {
        val diarySlot = diarySlotName?.trim()?.lowercase()?.takeIf { it.isNotBlank() } ?: "a meal"
        val cleanedNote = note.trim()
        return "This is what was eaten for $diarySlot: \"$cleanedNote\". Turn it into a short " +
            "food-search query I can type into a food database to find the closest matching " +
            "food. Respond with ONLY the search query itself — no quotes, no labels, no " +
            "explanation, a few words at most."
    }
}
