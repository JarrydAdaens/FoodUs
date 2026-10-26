package com.maksimowiczm.foodyou.food.domain

import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.common.domain.measurement.*
import com.maksimowiczm.foodyou.app.ui.common.form.positiveFloatValidator
import com.maksimowiczm.foodyou.app.ui.common.form.nonNegativeDoubleValidator
import com.maksimowiczm.foodyou.food.domain.entity.*
import com.maksimowiczm.foodyou.fooddiary.domain.entity.*
import kotlin.test.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

class ProductPortionTest {
    private val facts = NutritionFacts(energy = NutrientValue.Complete(250.0), proteins = NutrientValue.Complete(10.0))
    private fun product(liquid: Boolean = false, serving: Double? = 40.0, pack: Double? = 200.0) =
        Product(NutritionalItemId.Product(7), "Synthetic food", "Brand", "123", "Note", liquid,
            pack, serving, ProductSource(ProductSource.Type.User), facts)
    private fun entry(snapshot: ProductSnapshot, measurement: Measurement) = NutritionalItemDiaryEntry(
        NutritionalItemDiaryEntryId(1), 1, LocalDate(2026, 10, 25), measurement, snapshot,
        LocalDateTime(2026, 10, 25, 12, 0), LocalDateTime(2026, 10, 25, 12, 0))
    private fun Product.snapshot() = ProductSnapshot(headline, nutritionFacts, servingWeight, totalWeight, isLiquid, source, note)

    @Test fun twoReferenceServingsEqualDirectEightyGramsIncludingNutrition() {
        val food = product()
        assertEquals(80.0, food.weight(Measurement.Serving(2.0)))
        val servingEntry = entry(food.snapshot(), Measurement.Serving(2.0))
        val directEntry = entry(food.snapshot(), Measurement.Gram(80.0))
        assertEquals(directEntry.nutritionFacts, servingEntry.nutritionFacts)
        assertEquals(200.0, servingEntry.nutritionFacts.energy.value)
        assertEquals(8.0, servingEntry.nutritionFacts.proteins.value)
        assertNull(servingEntry.nutritionFacts.fats.value)
    }

    @Test fun liquidAndFractionalServingsAndPackagesRespectKnownReferenceBasis() {
        assertEquals(20.0, product().weight(Measurement.Serving(0.5)))
        assertEquals(50.0, product().weight(Measurement.Package(0.25)))
        val liquid = product(liquid = true)
        assertEquals(80.0, liquid.weight(Measurement.Serving(2.0)))
        assertEquals(entry(liquid.snapshot(), Measurement.Milliliter(80.0)).nutritionFacts,
            entry(liquid.snapshot(), Measurement.Serving(2.0)).nutritionFacts)
        assertNull(liquid.weight(Measurement.Gram(80.0)))
        assertNull(product().weight(Measurement.Milliliter(80.0)))
    }

    @Test fun supportedImperialUnitsAgreeWithTheirMetricAmounts() {
        assertEquals(100.0, product().weight(Measurement.Ounce(100.0 * Measurement.Ounce.OUNCES_IN_GRAM))!!, 0.000001)
        assertEquals(80.0, product(true).weight(Measurement.FluidOunce(80.0 * Measurement.FluidOunce.FLUID_OUNCES_IN_MILLILITER))!!, 0.000001)
        assertNull(product().weight(Measurement.FluidOunce(1.0)))
        assertNull(product(true).weight(Measurement.Ounce(1.0)))
    }

    @Test fun unknownReferenceWeightsStayUnknownAndNeverAcquireGramFallbacks() {
        val food = product(serving = null, pack = null)
        assertNull(food.servingWeight)
        assertNull(food.packageWeight)
        assertNull(food.weight(Measurement.Serving(1.0)))
        assertNull(food.weight(Measurement.Package(1.0)))
        assertEquals(80.0, food.weight(Measurement.Gram(80.0)))
        assertFalse(MeasurementType.Serving in MeasurementResolver.supportedTypes(false, null, null))
    }

    @Test fun invalidAmountsBasesAndOverflowAreUnresolvable() {
        for (invalid in listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)) {
            for (type in MeasurementType.entries) {
                assertNull(MeasurementResolver.metricAmount(Measurement.from(type, invalid), type in listOf(MeasurementType.Milliliter, MeasurementType.FluidOunce), 200.0, 40.0))
            }
            assertNull(product(serving = invalid).weight(Measurement.Serving(1.0)))
            assertNull(product(pack = invalid).weight(Measurement.Package(1.0)))
        }
        assertNull(product(serving = Double.MAX_VALUE).weight(Measurement.Serving(2.0)))
        assertNull(product().weight(Measurement.Ounce(Double.MAX_VALUE)))
    }

    @Test fun editorReferenceNutritionNormalizesExactlyOnceAndRejectsUnknownOrOverflow() {
        val multiplier = ProductNutritionBasis.multiplier(Measurement.Serving(1.0), false, null, 40.0)!!
        val perServing = NutritionFacts(energy = NutrientValue.Complete(100.0))
        val canonical = perServing * multiplier
        assertEquals(250.0, canonical.energy.value)
        assertEquals(200.0, entry(product().snapshot().copy(nutritionFacts = canonical), Measurement.Serving(2.0)).nutritionFacts.energy.value)
        assertEquals(1.0, ProductNutritionBasis.multiplier(Measurement.Gram(100.0), false, null, null))
        assertNull(ProductNutritionBasis.multiplier(Measurement.Serving(1.0), false, null, null))
        assertNull(ProductNutritionBasis.multiplier(Measurement.Gram(Double.MIN_VALUE), false, null, null))
        assertNull(ProductNutritionBasis.multiplier(Measurement.Gram(100.0), true, null, null))
    }

    @Test fun switchingDisplayedBasisPreservesCanonicalNutritionInBothDirectionsAndFractions() {
        val canonicalBasis = Measurement.Gram(100.0)
        val servingBasis = Measurement.Serving(1.0)
        val display = ProductNutritionBasis.displayMultiplier(canonicalBasis, servingBasis, false, 200.0, 40.0)!!
        assertEquals(100.0, 250.0 * display, 0.000001)
        val normalize = ProductNutritionBasis.multiplier(servingBasis, false, 200.0, 40.0)!!
        assertEquals(250.0, 250.0 * display * normalize, 0.000001)
        assertEquals(250.0, 100.0 * ProductNutritionBasis.displayMultiplier(servingBasis, canonicalBasis, false, 200.0, 40.0)!!, 0.000001)
        val halfServing = Measurement.Serving(0.5)
        val fractionDisplay = ProductNutritionBasis.displayMultiplier(canonicalBasis, halfServing, false, 200.0, 40.0)!!
        assertEquals(50.0, 250.0 * fractionDisplay, 0.000001)
        assertEquals(250.0, 250.0 * fractionDisplay * ProductNutritionBasis.multiplier(halfServing, false, 200.0, 40.0)!!, 0.000001)
        assertNull(ProductNutritionBasis.displayMultiplier(canonicalBasis, servingBasis, false, null, null))
        assertNull(ProductNutritionBasis.displayMultiplier(canonicalBasis, Measurement.Milliliter(100.0), false, null, null))
    }

    @Test fun identitiesRemainDistinctAndSnapshotReadsDoNotFollowDefinitionEdits() {
        assertNotEquals<NutritionalItemId>(NutritionalItemId.Product(7), NutritionalItemId.Recipe(7))
        assertEquals(NutritionalItemId.Product(7), NutritionalItemId.fromReferences(7, null))
        assertEquals(NutritionalItemId.Recipe(7), NutritionalItemId.fromReferences(null, 7))
        assertFailsWith<IllegalArgumentException> { NutritionalItemId.fromReferences(7, 7) }
        assertFailsWith<IllegalArgumentException> { NutritionalItemId.fromReferences(null, null) }
        val original = product()
        val logged = entry(original.snapshot(), Measurement.Serving(2.0))
        val changed = original.copy(name = "Future definition", servingWeight = 50.0,
            nutritionFacts = facts.copy(energy = NutrientValue.Complete(500.0)))
        assertEquals(200.0, logged.nutritionFacts.energy.value)
        assertEquals(80.0, logged.weight)
        assertEquals("Synthetic food (Brand)", logged.name)
        assertEquals(500.0, entry(changed.snapshot(), Measurement.Serving(2.0)).nutritionFacts.energy.value)
        // Historical getters keep their former semantics; stricter rules apply to new writes.
        assertEquals(0.0, entry(original.snapshot(), Measurement.Gram(0.0)).weight)
    }

    @Test fun validationRejectsEveryInvalidKnownNutrientAndRetainsUnknowns() {
        assertTrue(NutritionFacts().hasValidValues())
        assertTrue(facts.hasValidValues())
        assertFalse(facts.copy(energy = NutrientValue.Complete(Double.NaN)).hasValidValues())
        assertFalse(facts.copy(vitaminA = NutrientValue.Complete(Double.POSITIVE_INFINITY)).hasValidValues())
        assertFalse(facts.copy(sodium = NutrientValue.Complete(-1.0)).hasValidValues())
        assertFalse((facts * Double.MAX_VALUE).hasValidValues())
    }

    @Test fun numericFormValidationRejectsNonfiniteValuesAndKeepsValidFractions() {
        val positive = positiveFloatValidator(onNotPositive = { "invalid" })
        assertNull(positive(0.5f))
        assertEquals("invalid", positive(0f))
        assertEquals("invalid", positive(Float.NaN))
        assertEquals("invalid", positive(Float.POSITIVE_INFINITY))
        val nonnegative = nonNegativeDoubleValidator(onNegative = { "invalid" })
        assertNull(nonnegative(0.0))
        assertNull(nonnegative(null))
        assertEquals("invalid", nonnegative(Double.NaN))
        assertEquals("invalid", nonnegative(Double.NEGATIVE_INFINITY))
    }
}
