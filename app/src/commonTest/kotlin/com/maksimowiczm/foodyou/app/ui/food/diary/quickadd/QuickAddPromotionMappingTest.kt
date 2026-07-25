package com.maksimowiczm.foodyou.app.ui.food.diary.quickadd

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Covers the pure Quick Add promotion mapping (Milestone 2, Story 19, spec §6.2/§6.3): the
 * total to per-100 g conversion for a promoted product, the absent-value rule, and the placeholder
 * ingredient that seeds a promoted recipe without corrupting its derived nutrition.
 */
class QuickAddPromotionMappingTest {

    private fun seed(
        name: String = "Beef and onion bakery pie",
        description: String? = "From the bakery",
        energyKcal: Double? = 500.0,
        proteins: Double? = 20.0,
        carbohydrates: Double? = 40.0,
        fats: Double? = 30.0,
        fibre: Double? = 10.0,
        servingCount: Double? = 2.0,
        weightGrams: Double? = 200.0,
    ) =
        QuickAddPromotionSeed(
            name = name,
            description = description,
            energyKcal = energyKcal,
            proteins = proteins,
            carbohydrates = carbohydrates,
            fats = fats,
            fibre = fibre,
            servingCount = servingCount,
            weightGrams = weightGrams,
        )

    @Test
    fun with_weight_converts_totals_to_per_100g() {
        // 200 g total -> per-100 g divides every value by 2.
        val product = seed(weightGrams = 200.0).toProductPrefill()

        assertEquals(250.0, product.nutritionFacts.energy.value)
        assertEquals(10.0, product.nutritionFacts.proteins.value)
        assertEquals(20.0, product.nutritionFacts.carbohydrates.value)
        assertEquals(15.0, product.nutritionFacts.fats.value)
        assertEquals(5.0, product.nutritionFacts.dietaryFiber.value)
        assertEquals(200.0, product.packageWeight)
        assertEquals("From the bakery", product.note)
    }

    @Test
    fun without_weight_keeps_totals_as_per_100g_and_no_package_weight() {
        val product = seed(weightGrams = null).toProductPrefill()

        // No usable weight -> totals are placed as-entered for the user to correct, never guessed.
        assertEquals(500.0, product.nutritionFacts.energy.value)
        assertEquals(20.0, product.nutritionFacts.proteins.value)
        assertNull(product.packageWeight)
    }

    @Test
    fun absent_nutrients_are_not_invented() {
        val product = seed(proteins = null, fibre = null, energyKcal = null).toProductPrefill()

        assertNull(product.nutritionFacts.proteins.value)
        assertNull(product.nutritionFacts.dietaryFiber.value)
        assertNull(product.nutritionFacts.energy.value)
    }

    @Test
    fun recipe_placeholder_reproduces_estimate_and_maps_servings() {
        // The placeholder's per-100 g nutrition times its weight must reproduce the original total,
        // so the ingredient-derived recipe model stays exact.
        fun reproducedEnergy(s: QuickAddPromotionSeed): Double {
            val per100 = s.toProductPrefill().nutritionFacts.energy.value!!
            return per100 * s.recipeIngredientWeightGrams / 100.0
        }

        assertEquals(500.0, reproducedEnergy(seed(weightGrams = 200.0)), 1e-9)
        assertEquals(500.0, reproducedEnergy(seed(weightGrams = null)), 1e-9)

        // Servings: rounded to a whole number, at least one.
        assertEquals(2, seed(servingCount = 2.0).recipeServings)
        assertEquals(3, seed(servingCount = 2.6).recipeServings)
        assertEquals(1, seed(servingCount = null).recipeServings)
        assertEquals(1, seed(servingCount = 0.0).recipeServings)
    }
}
