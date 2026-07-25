package com.maksimowiczm.foodyou.ai.domain

/**
 * Structured food identification returned by the AI vision model (Milestone 2, Story 6).
 *
 * Every nutritional value is optional: the model may omit or be unable to estimate any of them, and
 * downstream flows must tolerate missing data. Energy is in kilocalories; macros are in grams.
 */
data class AiFoodEstimate(
    val name: String,
    /** Model confidence in the range 0..1, or null when not provided. */
    val certainty: Double?,
    val calories: Double?,
    val protein: Double?,
    val fat: Double?,
    val fibre: Double?,
    val sugar: Double?,
)
