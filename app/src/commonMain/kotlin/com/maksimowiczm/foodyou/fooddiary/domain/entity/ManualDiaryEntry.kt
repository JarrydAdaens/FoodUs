package com.maksimowiczm.foodyou.fooddiary.domain.entity

import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import kotlin.jvm.JvmInline
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

@JvmInline value class ManualDiaryEntryId(val value: Long)

/**
 * Represents a manually added diary entry. When a user adds a entry without referring to any food
 * item.
 *
 * @param id The unique identifier of the manual diary entry.
 * @param mealId The identifier of the meal to which this entry belongs.
 * @param date The date of the diary entry.
 * @param nutritionFacts The nutrition facts for the food item based on the weight.
 * @param createdAt The timestamp when the entry was created.
 * @param updatedAt The timestamp when the entry was last updated.
 * @param description Optional free-text context, used by fast-text placeholders (Milestone 2,
 *   Story 8) to describe what was eaten and by expanded Quick Add (Story 18) for additional detail.
 * @param isPlaceholder Whether this entry is a zero-nutrition fast-text placeholder awaiting
 *   resolution. Story 9 uses this flag to route placeholders to their dedicated editor.
 * @param servingCount Optional number of servings this entry represents (Story 18). Quantity context
 *   only: [nutritionFacts] remains the absolute total for the entry and is never scaled by this
 *   value. Null on entries created before Story 18; treated as 1 where a value is required.
 * @param weightGrams Optional total weight of the entry in grams (Story 18). Quantity context only;
 *   like [servingCount] it never scales [nutritionFacts]. Null when not supplied.
 */
data class ManualDiaryEntry(
    val id: ManualDiaryEntryId,
    override val mealId: Long,
    override val date: LocalDate,
    override val name: String,
    override val nutritionFacts: NutritionFacts,
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    val description: String? = null,
    val isPlaceholder: Boolean = false,
    val servingCount: Double? = null,
    val weightGrams: Double? = null,
) : DiaryEntry
