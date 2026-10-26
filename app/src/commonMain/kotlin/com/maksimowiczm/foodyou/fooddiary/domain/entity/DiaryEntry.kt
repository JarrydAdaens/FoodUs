package com.maksimowiczm.foodyou.fooddiary.domain.entity

import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/** A dated, portion-total nutritional snapshot assigned to a [DiarySlot]. */
sealed interface DiaryEntry {
    val sortOrder: Long?
    val diarySlotId: Long
    val date: LocalDate
    val name: String
    val nutritionFacts: NutritionFacts
    val createdAt: LocalDateTime
    val updatedAt: LocalDateTime
}

/** Shared deterministic order for the diary projection and bulk preparation. */
fun Iterable<DiaryEntry>.orderedDiaryEntries(): List<DiaryEntry> = sortedWith(
    compareBy<DiaryEntry> { it.sortOrder ?: Long.MAX_VALUE }
        .thenBy { it.name }
        .thenBy { if (it is ManualDiaryEntry) 0 else 1 }
        .thenBy { when (it) { is ManualDiaryEntry -> it.id.value; is NutritionalItemDiaryEntry -> it.id.value } }
)
