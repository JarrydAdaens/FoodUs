package com.maksimowiczm.foodyou.app.ui.home.calendar

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.until

/**
 * Returns the Monday that starts the ISO week containing [date]. Monday-first is assumed to match
 * the fixed week strip layout, where Monday is pinned to the far left.
 */
internal fun mondayOf(date: LocalDate): LocalDate =
    date.minus((date.dayOfWeek.isoDayNumber - 1).toLong(), DateTimeUnit.DAY)

/** Whole-week distance from [fromMonday] to [toMonday]; negative when [toMonday] is earlier. */
internal fun weeksBetween(fromMonday: LocalDate, toMonday: LocalDate): Int =
    fromMonday.until(toMonday, DateTimeUnit.WEEK).toInt()
