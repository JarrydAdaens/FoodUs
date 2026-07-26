package com.maksimowiczm.foodyou.fooddiary.domain.entity

import kotlin.jvm.JvmInline
import kotlinx.datetime.LocalDateTime

@JvmInline value class MealTemplateId(val value: Long)

/**
 * A reusable, named set of diary items saved from a day's meal (Milestone 2, Story 13).
 *
 * A template is a first-class concept, distinct from a [Meal] (a time-window category) and a
 * `Recipe` (which collapses to a single entry). It stores a snapshot of each logged item so it can
 * be "stamped" onto another day: applying the template recreates every [item][MealTemplateItem] as
 * its own separate diary entry.
 *
 * @param id Stable identifier of the template.
 * @param name User-supplied name shown in the template list.
 * @param createdAt When the template was saved.
 * @param items The snapshotted items that will be recreated on apply.
 */
data class MealTemplate(
    val id: MealTemplateId,
    val name: String,
    val createdAt: LocalDateTime,
    val items: List<MealTemplateItem>,
) {
    val itemCount: Int = items.size
}
