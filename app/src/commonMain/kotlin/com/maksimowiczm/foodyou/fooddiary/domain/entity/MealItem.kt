package com.maksimowiczm.foodyou.fooddiary.domain.entity

import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import kotlin.jvm.JvmInline

@JvmInline value class MealItemId(val value: Long)

/** Absolute nutrition for one captured portion; never canonical per-100 facts or a live link. */
data class MealItem(
    val name: String,
    val nutritionFacts: NutritionFacts,
    val id: MealItemId? = null,
    val capturedPortion: Measurement? = null,
    val servingCount: Double? = null,
    val weightGrams: Double? = null,
    val portionDescription: com.maksimowiczm.foodyou.common.domain.food.PortionDescription? = null,
    val metadata: com.maksimowiczm.foodyou.common.domain.food.NutritionMetadata? = null,
)

/** Flat storage cannot retain partial-value completeness. Only complete new facts become numeric. */
fun DiaryEntry.toMealItem(): MealItem =
    MealItem(
        name = name,
        nutritionFacts = nutritionFacts.captureMealNutrition(),
        capturedPortion = when (this) {
            is NutritionalItemDiaryEntry -> measurement
            is ManualDiaryEntry -> capturedPortion
        },
        servingCount = (this as? ManualDiaryEntry)?.servingCount,
        weightGrams = (this as? ManualDiaryEntry)?.weightGrams,
        portionDescription = (this as? ManualDiaryEntry)?.portionDescription,
        metadata = (this as? ManualDiaryEntry)?.nutritionMetadata,
    )
