package com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase

import com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase.domain.AfcdNutrientMapper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AfcdNutrientMapperTest {

    // Header cells keyed by column letter, mirroring the real AFCD nutrient sheet (row 3), including
    // the embedded newlines AFCD uses before the unit.
    private val headerCells =
        mapOf(
            "A" to "Public Food Key",
            "B" to "Classification",
            "D" to "Food Name",
            "E" to "Energy with dietary fibre, equated \n(kJ)",
            "H" to "Protein \n(g)",
            "J" to "Fat, total \n(g)",
            "L" to "Total dietary fibre \n(g)",
            "T" to "Total sugars (g)",
            "U" to "Added sugars (g)",
            "AM" to "Available carbohydrate, without sugar alcohols \n(g)",
            "AN" to "Available carbohydrate, with sugar alcohols \n(g)",
            "BD" to "Calcium (Ca) \n(mg)",
            "BK" to "Iron (Fe) \n(mg)",
            "BU" to "Sodium (Na) \n(mg)",
        )

    @Test
    fun mapsRowWithNormalizedUnits() {
        val index = AfcdNutrientMapper.buildColumnIndex(headerCells)
        val row =
            mapOf(
                "A" to "F002258",
                "D" to "Cardamom seed, dried, ground",
                "E" to "1236",
                "H" to "10.8",
                "J" to "6.7",
                "L" to "28",
                "T" to "4.4",
                "U" to "0",
                "AN" to "34.4",
                "BD" to "383",
                "BK" to "13.97",
                "BU" to "18",
            )

        val product = AfcdNutrientMapper.map(index, row)!!

        assertEquals("F002258", product.publicFoodKey)
        assertEquals("Cardamom seed, dried, ground", product.name)

        val facts = product.nutritionFacts
        // Energy kJ -> kcal
        assertEquals(1236.0 / 4.184, facts.energy.value!!, TOLERANCE)
        assertEquals(10.8, facts.proteins.value!!, TOLERANCE)
        assertEquals(6.7, facts.fats.value!!, TOLERANCE)
        assertEquals(34.4, facts.carbohydrates.value!!, TOLERANCE)
        assertEquals(28.0, facts.dietaryFiber.value!!, TOLERANCE)
        assertEquals(4.4, facts.sugars.value!!, TOLERANCE)
        // Minerals mg -> g (domain stores grams; Room re-multiplies to *Milli)
        assertEquals(0.018, facts.sodium.value!!, TOLERANCE)
        assertEquals(0.383, facts.calcium.value!!, TOLERANCE)
        assertEquals(0.01397, facts.iron.value!!, TOLERANCE)
    }

    @Test
    fun leavesAbsentOptionalValuesIncomplete() {
        val index = AfcdNutrientMapper.buildColumnIndex(headerCells)
        val row =
            mapOf(
                "A" to "F000001",
                "D" to "Test food",
                "E" to "500",
                "H" to "1",
                "J" to "2",
                "AN" to "3",
                // no sodium / calcium / iron / fibre supplied
            )

        val product = AfcdNutrientMapper.map(index, row)!!
        assertTrue(product.nutritionFacts.sodium.isIncomplete)
        assertNull(product.nutritionFacts.sodium.value)
        assertTrue(product.nutritionFacts.dietaryFiber.isIncomplete)
    }

    @Test
    fun skipsRowsWithoutKeyOrName() {
        val index = AfcdNutrientMapper.buildColumnIndex(headerCells)
        assertNull(AfcdNutrientMapper.map(index, mapOf("D" to "No key")))
        assertNull(AfcdNutrientMapper.map(index, mapOf("A" to "F1")))
    }

    @Test
    fun rejectsWorkbookMissingRequiredColumn() {
        val incomplete = headerCells - "H" // no protein column
        assertFailsWith<com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase.domain.AfcdFormatException> {
            AfcdNutrientMapper.buildColumnIndex(incomplete)
        }
    }

    private companion object {
        const val TOLERANCE = 1e-6
    }
}
