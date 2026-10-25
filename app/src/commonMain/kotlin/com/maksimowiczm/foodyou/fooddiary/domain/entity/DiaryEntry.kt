package com.maksimowiczm.foodyou.fooddiary.domain.entity

import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/** A dated, portion-total nutritional snapshot assigned to a [DiarySlot]. */
sealed interface DiaryEntry {
    val diarySlotId: Long
    val date: LocalDate
    val name: String
    val nutritionFacts: NutritionFacts
    val createdAt: LocalDateTime
    val updatedAt: LocalDateTime
}
