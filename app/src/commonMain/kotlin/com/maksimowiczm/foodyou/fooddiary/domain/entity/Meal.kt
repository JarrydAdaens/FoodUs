package com.maksimowiczm.foodyou.fooddiary.domain.entity

import kotlin.jvm.JvmInline
import kotlinx.datetime.LocalDateTime

@JvmInline value class MealId(val value: Long)

/**
 * A reusable, named set of diary items saved from a day's diary slot (Milestone 2, Story 13).
 *
 * A Meal is a first-class concept, distinct from a [DiarySlot] (a time-window category) and a
 * `Recipe` (which collapses to a single entry). It stores a snapshot of each logged item so it can
 * be "stamped" onto another day: applying the Meal recreates every [item][MealItem] as
 * its own separate diary entry.
 *
 * @param id Stable identifier of the Meal.
 * @param name User-supplied name shown in the Meal list.
 * @param createdAt When the Meal was saved.
 * @param items The snapshotted items that will be recreated on apply.
 */
data class Meal(
    val id: MealId,
    val name: String,
    val createdAt: LocalDateTime,
    val items: List<MealItem>,
    val reportedNutrition: com.maksimowiczm.foodyou.common.domain.food.ReportedNutrition? = null,
    val metadata: com.maksimowiczm.foodyou.common.domain.food.NutritionMetadata? = null,
) {
    val itemCount: Int = items.size
}
