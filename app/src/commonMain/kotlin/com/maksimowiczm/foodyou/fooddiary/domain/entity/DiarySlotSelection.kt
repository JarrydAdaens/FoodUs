package com.maksimowiczm.foodyou.fooddiary.domain.entity

import kotlinx.datetime.LocalTime

/** Inclusive configured windows; equal endpoints denote an all-day fallback. */
fun DiarySlot.containsTime(time: LocalTime): Boolean = when {
    from == to -> true
    from <= to -> time >= from && time <= to
    else -> time >= from || time <= to
}

/** A timed match wins over all-day; rank and identity resolve overlaps deterministically. */
fun selectDiarySlot(slots: List<DiarySlot>, time: LocalTime): DiarySlot? = slots
    .filter { it.containsTime(time) }
    .minWithOrNull(compareBy<DiarySlot> { it.from == it.to }.thenBy { it.rank }.thenBy { it.id })
