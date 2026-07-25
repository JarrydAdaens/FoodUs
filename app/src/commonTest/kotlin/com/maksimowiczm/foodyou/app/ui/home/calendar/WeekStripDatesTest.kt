package com.maksimowiczm.foodyou.app.ui.home.calendar

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate

class WeekStripDatesTest {
    // 2026-07-20 is a Monday; 2026-07-25 (the current test date) is the Saturday of that week.
    private val monday = LocalDate(2026, 7, 20)

    @Test
    fun mondayOf_returns_same_monday_for_a_monday() {
        assertEquals(monday, mondayOf(monday))
    }

    @Test
    fun mondayOf_returns_week_start_for_mid_week_and_sunday() {
        assertEquals(monday, mondayOf(LocalDate(2026, 7, 25))) // Saturday
        assertEquals(monday, mondayOf(LocalDate(2026, 7, 26))) // Sunday closes the same ISO week
        assertEquals(LocalDate(2026, 7, 27), mondayOf(LocalDate(2026, 7, 27))) // next Monday
    }

    @Test
    fun weeksBetween_counts_whole_weeks_signed() {
        assertEquals(0, weeksBetween(monday, LocalDate(2026, 7, 25)))
        assertEquals(1, weeksBetween(monday, LocalDate(2026, 7, 27)))
        assertEquals(-2, weeksBetween(monday, LocalDate(2026, 7, 6)))
    }
}
