package com.maksimowiczm.foodyou.ai.domain

/**
 * Layer-1 developer prompt for the AI food scan (Milestone 2, Story 21). Pure machinery: task
 * framing plus the exact JSON output contract the parser expects, carrying zero personal data.
 * Locale, diet, and household context belong to the user's optional system prompt (layer 2) and the
 * per-scan hint (layer 3), never to the baked build. Kept as a domain object so its shape can be
 * unit tested without a live endpoint.
 */
object AiScanPrompt {
    const val PROMPT =
        "This photo shows food. Identify the food in the photo. " +
            "Respond with ONLY a JSON object (no Markdown, no commentary) with exactly these " +
            "keys: \"name\" (string), \"certainty\" (number between 0 and 1), \"calories\" " +
            "(kilocalories, number), \"protein\" (grams, number), \"fat\" (grams, number), " +
            "\"fibre\" (grams, number), \"sugar\" (grams, number). If unsure, still provide " +
            "your best estimate."
}
