package com.maksimowiczm.foodyou.fooddiary.domain.entity

import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import kotlin.jvm.JvmInline
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

@JvmInline value class NutritionalItemDiaryEntryId(val value: Long)

/**
 * Represents a food diary entry.
 *
 * @param id The unique identifier of the food diary entry.
 * @param diarySlotId The identifier of the diarySlot to which this entry belongs.
 * @param date The date of the diary entry.
 * @param measurement The measurement details for the food item.
 * @param food The food item associated with this diary entry.
 * @param createdAt The timestamp when the entry was created.
 * @param updatedAt The timestamp when the entry was last updated.
 */
data class NutritionalItemDiaryEntry(
    val id: NutritionalItemDiaryEntryId,
    override val diarySlotId: Long,
    override val date: LocalDate,
    val measurement: Measurement,
    val food: NutritionalItemSnapshot,
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    override val sortOrder: Long? = null,
) : DiaryEntry {
    override val name: String = food.name

    val weight: Double? = food.weight(measurement)

    /**
     * Total nutrition facts for the entry based on the food's nutrition facts and the weight of the
     * measurement.
     */
    override val nutritionFacts: NutritionFacts by lazy {
        if (food is ProductSnapshot && food.factBasis == com.maksimowiczm.foodyou.common.domain.food.ProductFactBasis.METRIC_100) weight?.let { food.nutritionFacts * (it / 100.0) } ?: NutritionFacts()
        else food.nutritionFor(measurement) ?: NutritionFacts()
    }
}
