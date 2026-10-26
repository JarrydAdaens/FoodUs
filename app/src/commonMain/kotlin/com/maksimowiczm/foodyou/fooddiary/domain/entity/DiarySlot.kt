package com.maksimowiczm.foodyou.fooddiary.domain.entity

import kotlinx.datetime.LocalTime

/**
 * A configured destination for dated diary entries, independent of a reusable [Meal].
 *
 * @property id Stable category identifier; existing assignments retain this value.
 * @property name Configured name (e.g., Breakfast, Lunch).
 * @property from Start of the slot's time window.
 * @property to End of the slot's time window; equal to [from] for an all-day slot.
 * @property rank Display order within the day.
 */
data class DiarySlot(
    val id: Long,
    val name: String,
    val from: LocalTime,
    val to: LocalTime,
    val rank: Int,
)
