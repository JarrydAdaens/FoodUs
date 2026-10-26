package com.maksimowiczm.foodyou.fooddiary.domain

import com.maksimowiczm.foodyou.app.ui.common.utility.EnergyFormatter
import com.maksimowiczm.foodyou.app.ui.food.meals.MealItemForm
import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.common.domain.measurement.*
import com.maksimowiczm.foodyou.common.result.*
import com.maksimowiczm.foodyou.fooddiary.domain.entity.*
import com.maksimowiczm.foodyou.fooddiary.domain.repository.MealRepository
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.UpdateMealUseCase
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import kotlin.test.*

class MealEditingTest {
    @Test fun captureNeverPromotesAnyPartialNutrientToComplete() {
        for (field in NutritionFactsField.entries) {
            for (value in listOf(NutrientValue.Complete(10.0), NutrientValue.Complete(0.0),
                NutrientValue.Incomplete(10.0), NutrientValue.Incomplete(0.0), NutrientValue.Incomplete(null))) {
                val source = NutritionFacts().mapMealNutrition { current, old -> if (current == field) value else old }
                val captured = source.captureMealNutrition()
                assertEquals(if (value is NutrientValue.Complete) value else NutrientValue.Incomplete(null), captured[field], field.name)
                assertEquals(value, source[field], "Capture does not rewrite source completeness")
            }
        }
    }

    @Test fun explicitScalingSupportsAllKnownUnitsAndKeepsContextCoherent() {
        val portions = listOf(Measurement.Gram(80.0), Measurement.Ounce(2.0), Measurement.Milliliter(80.0),
            Measurement.FluidOunce(2.0), Measurement.Serving(2.0), Measurement.Package(2.0))
        for (portion in portions) {
            val original = MealItem("Captured", NutritionFacts(energy = NutrientValue.Complete(400.0)),
                capturedPortion = portion, servingCount = 2.0, weightGrams = 80.0)
            val scaled = assertNotNull(original.scaleCapturedPortion(0.75))
            assertEquals(portion.type, scaled.capturedPortion!!.type)
            assertEquals(portion.rawValue * 0.75, scaled.capturedPortion.rawValue)
            assertEquals(300.0, scaled.nutritionFacts.energy.value)
            assertNull(scaled.nutritionFacts.proteins.value)
            assertEquals(1.5, scaled.servingCount)
            assertEquals(60.0, scaled.weightGrams)
            assertEquals(400.0, original.nutritionFacts.energy.value)
        }
    }

    @Test fun nameOnlyFormEditPreservesEveryFieldPrecisionAndUnknownInKilojoules() {
        val facts = NutritionFacts().mapMealNutrition { field, _ ->
            if (field.ordinal % 7 == 0) NutrientValue.Incomplete(null)
            else NutrientValue.Complete(0.12345678901234567 + field.ordinal)
        }
        val original = MealItem("Original", facts, MealItemId(17), Measurement.Milliliter(123.45678901234567))
        val form = MealItemForm(original, EnergyFormatter.kilojoules)
        form.name = "Renamed"
        val result = assertNotNull(form.candidate())
        assertEquals("Renamed", result.name)
        assertEquals(original.nutritionFacts, result.nutritionFacts)
        assertEquals(original.capturedPortion, result.capturedPortion)
        assertEquals(original.id, result.id)
    }

    @Test fun amountEditScalesOnceWhileDescriptiveEditNeverScalesNutrition() {
        val original = MealItem("Captured", NutritionFacts(energy = NutrientValue.Complete(400.0)),
            capturedPortion = Measurement.Serving(2.0), servingCount = 2.0, weightGrams = 80.0)
        val amount = MealItemForm(original, EnergyFormatter.kilocalories)
        amount.amount = "1.5"
        val changed = assertNotNull(amount.candidate())
        assertEquals(Measurement.Serving(1.5), changed.capturedPortion)
        assertEquals(300.0, changed.nutritionFacts.energy.value)
        assertEquals(1.5, changed.servingCount)
        assertEquals(60.0, changed.weightGrams)
        val descriptive = MealItemForm(original, EnergyFormatter.kilocalories)
        descriptive.servings = "3"
        val context = assertNotNull(descriptive.candidate())
        assertEquals(400.0, context.nutritionFacts.energy.value)
        assertEquals(3.0, context.servingCount)
        assertNull(context.capturedPortion)
        val noOp = MealItemForm(original, EnergyFormatter.kilocalories)
        noOp.servings = "2"
        noOp.grams = "80"
        assertEquals(original, noOp.candidate(), "Equivalent text is a metadata no-op")
    }

    @Test fun unknownPortionRemainsUnknownAndDirectNutritionDoesNotChangeQuantity() {
        val legacy = MealItem("Legacy", NutritionFacts(energy = NutrientValue.Complete(780.0)))
        assertNull(assertNotNull(legacy.scaleCapturedPortion(0.5)).capturedPortion)
        assertEquals(390.0, legacy.scaleCapturedPortion(0.5)!!.nutritionFacts.energy.value)
        val known = legacy.copy(capturedPortion = Measurement.Package(0.5))
        val form = MealItemForm(known, EnergyFormatter.kilocalories)
        form.nutrients[NutritionFactsField.Energy] = "800"
        val edited = assertNotNull(form.candidate())
        assertEquals(800.0, edited.nutritionFacts.energy.value)
        assertEquals(known.capturedPortion, edited.capturedPortion)
        assertEquals(780.0, legacy.nutritionFacts.energy.value)
    }

    @Test fun invalidNumbersOverflowAndUnderflowNeverProduceCandidates() {
        val original = MealItem("Item", NutritionFacts(energy = NutrientValue.Complete(10.0)),
            capturedPortion = Measurement.Gram(10.0))
        for (bad in listOf("0", "-1", "NaN", "Infinity", "not a number", "1e999")) {
            val form = MealItemForm(original, EnergyFormatter.kilocalories)
            form.amount = bad
            assertNull(form.candidate(), bad)
        }
        for (factor in listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY, Double.MAX_VALUE)) {
            assertNull(original.scaleCapturedPortion(factor))
        }
        val tiny = original.copy(nutritionFacts = NutritionFacts(energy = NutrientValue.Complete(Double.MIN_VALUE)))
        assertNull(tiny.scaleCapturedPortion(0.1))
        val invalidField = MealItemForm(original, EnergyFormatter.kilocalories)
        invalidField.nutrients[NutritionFactsField.VitaminB12] = "NaN"
        var saved = false
        invalidField.confirm { saved = true }
        assertFalse(saved)
        assertTrue(invalidField.error)
    }

    @Test fun finiteCanonicalTotalsCannotOverflowDisplayUnitsDuringPortionEdits() {
        for (field in listOf(NutritionFactsField.VitaminA, NutritionFactsField.Cholesterol)) {
            val initial = if (field == NutritionFactsField.VitaminA) 0.001 else 1.0
            val unitFactor = if (field == NutritionFactsField.VitaminA) 1_000_000.0 else 1_000.0
            val facts = NutritionFacts(energy = NutrientValue.Complete(100.0)).withMealNutrient(field, initial)
            val original = MealItem("Finite captured", facts, capturedPortion = Measurement.Serving(1.0))
            val canonical = assertNotNull(original.scaleCapturedPortion(1e306))
            assertTrue(canonical.nutritionFacts.validMealNutrition())
            assertFalse((assertNotNull(canonical.nutritionFacts[field].value) * unitFactor).isFinite())

            val form = MealItemForm(original, EnergyFormatter.kilocalories)
            val priorText = form.nutrients.toMap()
            form.factor = "1e306"
            form.scale()
            assertTrue(form.error, field.name)
            assertEquals(priorText, form.nutrients.toMap(), "Rejected scale preserves editable facts")
            assertEquals(original, form.candidate())
            form.amount = "1e306"
            assertNull(form.candidate(), "Same-unit amount edits reject the same conversion overflow")
            var saved = false
            form.confirm { saved = true }
            assertFalse(saved)

            val valid = MealItemForm(original, EnergyFormatter.kilocalories)
            valid.factor = "2"
            valid.scale()
            assertFalse(valid.error)
            val doubled = assertNotNull(valid.candidate())
            assertEquals(initial * 2, doubled.nutritionFacts[field].value)
            assertEquals(200.0, doubled.nutritionFacts.energy.value)
            assertNull(doubled.nutritionFacts.proteins.value)
        }
    }

    @Test fun invalidAggregateAndFailedUpdatePreservePriorSavedDefinition() = runBlocking {
        val prior = Meal(MealId(1), "Prior", LocalDateTime(2026, 10, 25, 0, 0),
            listOf(MealItem("Entry", NutritionFacts(energy = NutrientValue.Complete(10.0)))))
        var writes = 0
        val repository = object : MealRepository {
            override fun observeAll() = flowOf(listOf(prior))
            override suspend fun getMeal(id: MealId) = prior
            override suspend fun save(name: String, items: List<MealItem>) = error("Not create")
            override suspend fun delete(id: MealId) = error("Not delete")
            override suspend fun update(id: MealId, name: String, items: List<MealItem>): Boolean { writes++; return false }
        }
        val update = UpdateMealUseCase(repository)
        assertTrue(update.update(prior.id, " ", prior.items).isError())
        assertTrue(update.update(prior.id, "Empty", emptyList()).isError())
        assertTrue(update.update(prior.id, "Bad", listOf(prior.items[0].copy(name = ""))).isError())
        assertEquals(0, writes)
        assertTrue(update.update(prior.id, "Renamed", prior.items).isError())
        assertEquals(prior, repository.getMeal(prior.id))
        assertEquals(1, writes)
    }
}
