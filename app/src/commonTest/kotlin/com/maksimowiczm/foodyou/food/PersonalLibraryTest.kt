package com.maksimowiczm.foodyou.food

import com.maksimowiczm.foodyou.app.ui.food.library.*
import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.result.*
import com.maksimowiczm.foodyou.food.domain.entity.*
import com.maksimowiczm.foodyou.fooddiary.domain.entity.*
import com.maksimowiczm.foodyou.fooddiary.domain.repository.MealRepository
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.CreateMealDefinitionUseCase
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import kotlin.test.*

class PersonalLibraryTest {
    private val facts = NutritionFacts(energy = NutrientValue.Complete(240.0),
        carbohydrates = NutrientValue.Complete(0.0), fats = NutrientValue.Incomplete(7.0))
    private fun product(liquid: Boolean = false) = Product(NutritionalItemId.Product(1), "Synthetic Food", null,
        null, null, liquid, null, null, ProductSource(ProductSource.Type.User), facts)

    @Test fun literalSearchHasNoPageCapAndPreservesTypedIdentity() {
        val names = (1L..180L).map { NutritionalItemName(NutritionalItemId.Product(it), "Synthetic item $it") } +
            listOf(NutritionalItemName(NutritionalItemId.Recipe(1), "ÉCLAIR 100%_"),
                NutritionalItemName(NutritionalItemId.Product(1), "Éclair 100%_"))
        assertEquals(182, filterLibraryNames(names, "  ").size)
        assertEquals(1, filterLibraryNames(names, "item 180").size)
        assertEquals(2, filterLibraryNames(names, "éclair 100%_").size)
        assertTrue(filterLibraryNames(names, "100.*").isEmpty())
        assertEquals(2, filterLibraryNames(names, "éclair").map { it.id }.toSet().size)
    }

    @Test fun metricAndReferenceProductsUseDeclaredFactsWithoutInventingMassOrZero() {
        for (liquid in listOf(false, true)) {
            val row = libraryNutrition(product(liquid))
            assertEquals(if(liquid) LibraryBasis.Per100Milliliters else LibraryBasis.Per100Grams, row.basis)
            assertEquals(facts, row.facts)
            assertEquals(0.0, row.facts!!.carbohydrates.value)
            assertNull(row.facts.proteins.value)
            assertTrue(row.facts.fats.isIncomplete)
        }
        val source = product().copy(factBasis = ProductFactBasis.REFERENCE_SERVING,
            referencePortion = PortionDescription(2.0, "pieces"))
        val row = libraryNutrition(source)
        assertEquals(LibraryBasis.ReferenceServing, row.basis)
        assertEquals(facts, row.facts)
        assertEquals(source.referencePortion, row.reference)
        assertNull(source.weight(Measurement.Serving(1.0)))
    }

    @Test fun mixedRecipeServingDoesNotRequireMetricWeightAndKeepsStrictUnknowns() {
        val solid = product().copy(nutritionFacts = NutritionFacts(energy = NutrientValue.Complete(200.0)))
        val liquid = product(true).copy(id = NutritionalItemId.Product(2), nutritionFacts = NutritionFacts(energy = NutrientValue.Complete(100.0)))
        val recipe = Recipe(NutritionalItemId.Recipe(1), "Synthetic mixed", 2,
            listOf(RecipeIngredient(solid, Measurement.Gram(100.0)), RecipeIngredient(liquid, Measurement.Milliliter(100.0))), null, false)
        assertNull(recipe.totalWeight)
        assertEquals(150.0, libraryNutrition(recipe).facts!!.energy.value)
        assertNull(libraryNutrition(recipe).facts!!.proteins.value)
        val nested = Recipe(NutritionalItemId.Recipe(2), "Synthetic nested", 3,
            listOf(RecipeIngredient(recipe, Measurement.Serving(2.0))), null, false)
        assertEquals(100.0, libraryNutrition(nested).facts!!.energy.value)
    }

    @Test fun reportedRecipeNeverLabelsWholeFactsAsServingWhenYieldIsUnknown() {
        val whole = Recipe(NutritionalItemId.Recipe(2), "Synthetic reported", null, emptyList(), null, false,
            ReportedNutrition(facts, ReportedBasis.WHOLE_ITEM))
        assertNull(libraryNutrition(whole).facts)
        assertTrue(libraryNutrition(whole).reported)
        val knownYield = whole.copy(servings = 2, reportedNutrition = requireNotNull(whole.reportedNutrition).copy(reference = PortionDescription(1.0, "whole pot")))
        assertEquals(120.0, libraryNutrition(knownYield).facts!!.energy.value)
        assertNull(libraryNutrition(knownYield).reference, "Whole-panel context must not be labelled as the serving reference")
        val referenced = whole.copy(reportedNutrition = ReportedNutrition(facts, ReportedBasis.REFERENCE_SERVING,
            PortionDescription(1.0, "bowl")))
        assertEquals(facts, libraryNutrition(referenced).facts)
        assertNull(libraryNutrition(referenced.copy(reportedNutrition = requireNotNull(referenced.reportedNutrition).copy(needsReview = true))).facts)
        assertTrue(libraryNutrition(referenced.copy(reportedNutrition = requireNotNull(referenced.reportedNutrition).copy(needsReview = true))).needsReview)
        assertNull(libraryNutrition(whole.copy(reportedNutrition = requireNotNull(whole.reportedNutrition).copy(basis = ReportedBasis.UNSPECIFIED))).facts)
    }

    @Test fun mealSummariesNeverSubstituteIndependentReportedPanelOrPartialTotals() {
        val meal = Meal(MealId(1), "Synthetic bundle", LocalDateTime(2026, 1, 1, 0, 0),
            listOf(MealItem("Known", facts), MealItem("Unknown", NutritionFacts())),
            ReportedNutrition(NutritionFacts(energy = NutrientValue.Complete(999.0)), ReportedBasis.WHOLE_ITEM))
        val row = libraryNutrition(meal)
        assertEquals(LibraryBasis.SavedMeal, row.basis)
        assertNull(row.facts!!.energy.value)
        assertTrue(row.reported)
        assertEquals(999.0, meal.reportedNutrition!!.facts.energy.value)
    }

    @Test fun definitionCreationValidatesBeforeWritesAndPreservesCapturedAmounts() = runBlocking {
        var writes = 0
        var savedName = ""
        var savedItems = emptyList<MealItem>()
        var fail = false
        val repository = object : MealRepository {
            override fun observeAll() = flowOf(emptyList<Meal>())
            override suspend fun getMeal(id: MealId): Meal? = null
            override suspend fun save(name: String, items: List<MealItem>): MealId {
                if(fail) error("Synthetic failure")
                writes++; savedName = name; savedItems = items; return MealId(1)
            }
            override suspend fun update(id: MealId, name: String, items: List<MealItem>) = false
            override suspend fun delete(id: MealId) = Unit
        }
        val create = CreateMealDefinitionUseCase(repository)
        val item = MealItem("Synthetic item", facts, capturedPortion = Measurement.Serving(2.0))
        assertTrue(create.create(" ", listOf(item)).isError())
        assertTrue(create.create("Synthetic", emptyList()).isError())
        assertTrue(create.create("Synthetic", listOf(item.copy(nutritionFacts = NutritionFacts(energy = NutrientValue.Complete(Double.NaN))))).isError())
        assertEquals(0, writes)
        assertTrue(create.create("  Synthetic definition  ", listOf(item)).isSuccess())
        assertEquals(1, writes)
        assertEquals("Synthetic definition", savedName)
        assertEquals(listOf(item), savedItems)
        fail = true
        assertTrue(create.create("Synthetic failed", listOf(item)).isError())
        assertEquals(1, writes)
    }
}
