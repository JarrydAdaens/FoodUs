package com.maksimowiczm.foodyou.fooddiary.domain.entity

import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import com.maksimowiczm.foodyou.common.domain.food.sum

/** A diary projection grouping one configured slot with its entries for the observed date. */
data class DiarySlotEntries(val diarySlot: DiarySlot, val entries: List<DiaryEntry>) {
    val nutritionFacts: NutritionFacts = entries.map { it.nutritionFacts }.sum()
}
