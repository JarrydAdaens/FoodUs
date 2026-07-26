package com.maksimowiczm.foodyou.app.ui.home.goals

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MacroPieChartTest {

    @Test
    fun slicesAreProportionalAndSumToFullCircle() {
        val sweeps = macroPieSweepDegrees(listOf(25, 25, 50))

        assertEquals(90f, sweeps[0], 0.001f)
        assertEquals(90f, sweeps[1], 0.001f)
        assertEquals(180f, sweeps[2], 0.001f)
        assertEquals(360f, sweeps.sum(), 0.001f)
    }

    @Test
    fun emptyDayProducesNoSweep() {
        val sweeps = macroPieSweepDegrees(listOf(0, 0, 0))

        assertEquals(3, sweeps.size)
        assertTrue(sweeps.all { it == 0f }, "an empty day should render no filled slices")
    }

    @Test
    fun negativeValuesAreTreatedAsZero() {
        val sweeps = macroPieSweepDegrees(listOf(-10, 30, 10))

        assertEquals(0f, sweeps[0], 0.001f)
        assertEquals(270f, sweeps[1], 0.001f)
        assertEquals(90f, sweeps[2], 0.001f)
    }
}
