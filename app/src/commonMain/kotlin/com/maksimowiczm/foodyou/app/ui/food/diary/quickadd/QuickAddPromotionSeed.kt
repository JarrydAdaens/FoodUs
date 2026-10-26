package com.maksimowiczm.foodyou.app.ui.food.diary.quickadd

import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts

/**
 * Immutable snapshot of the Quick Add form values captured at the instant the user chooses to promote
 * an entry into a reusable Product or Recipe (Milestone 2, Story 19).
 *
 * The seed is a plain copy: promotion prefills the product/recipe editor from it and never mutates or
 * replaces the source diary entry (spec §6.2/§6.3, §6.5). Energy is in kilocalories; masses are in
 * grams. Absent values stay null so promotion can honour "do not invent values for fields not
 * present" (spec §6.2).
 */
data class QuickAddPromotionSeed(
    val name: String,
    val description: String?,
    val energyKcal: Double?,
    val proteins: Double?,
    val carbohydrates: Double?,
    val fats: Double?,
    val fibre: Double?,
    val servingCount: Double?,
    val weightGrams: Double?,
    // Full supported snapshot for scan-origin and historical fields outside the small editor.
    // Null retains compatibility with older callers that supply the scalar fields only.
    val nutritionFacts: NutritionFacts? = null,
)
