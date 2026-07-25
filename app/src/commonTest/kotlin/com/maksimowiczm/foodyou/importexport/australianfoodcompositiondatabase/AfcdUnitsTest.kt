package com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase

import com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase.domain.AfcdUnits
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AfcdUnitsTest {

    @Test
    fun convertsKilojoulesToKilocalories() {
        // 4.184 kJ == 1 kcal
        assertEquals(1.0, AfcdUnits.kilojoulesToKilocalories(4.184)!!, ABSOLUTE_TOLERANCE)
        assertEquals(100.0, AfcdUnits.kilojoulesToKilocalories(418.4)!!, ABSOLUTE_TOLERANCE)
    }

    @Test
    fun preservesMissingEnergy() {
        assertNull(AfcdUnits.kilojoulesToKilocalories(null))
    }

    @Test
    fun convertsMilligramsToGrams() {
        assertEquals(0.018, AfcdUnits.milligramsToGrams(18.0)!!, ABSOLUTE_TOLERANCE)
        assertEquals(0.383, AfcdUnits.milligramsToGrams(383.0)!!, ABSOLUTE_TOLERANCE)
    }

    @Test
    fun preservesMissingMineral() {
        assertNull(AfcdUnits.milligramsToGrams(null))
    }

    @Test
    fun parsesNumbersAndRejectsNonNumeric() {
        assertEquals(4.4, AfcdUnits.parseNumber("4.4")!!, ABSOLUTE_TOLERANCE)
        assertEquals(1236.0, AfcdUnits.parseNumber(" 1236 ")!!, ABSOLUTE_TOLERANCE)
        assertNull(AfcdUnits.parseNumber(""))
        assertNull(AfcdUnits.parseNumber(null))
        assertNull(AfcdUnits.parseNumber("n/a"))
    }

    private companion object {
        const val ABSOLUTE_TOLERANCE = 1e-9
    }
}
