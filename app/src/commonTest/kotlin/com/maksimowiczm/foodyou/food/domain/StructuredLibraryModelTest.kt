package com.maksimowiczm.foodyou.food.domain

import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.infrastructure.room.LibraryValueConverters
import com.maksimowiczm.foodyou.common.infrastructure.room.toValidatedEntityNutrients
import com.maksimowiczm.foodyou.common.infrastructure.room.toNutritionFacts
import com.maksimowiczm.foodyou.food.domain.entity.*
import com.maksimowiczm.foodyou.fooddiary.domain.entity.*
import kotlin.test.*

class StructuredLibraryModelTest {
    private val facts = NutritionFacts(energy = NutrientValue.Complete(123.0), proteins = NutrientValue.Complete(0.0))
    private fun food(reference: PortionDescription = PortionDescription(2.0, "pieces", "2 pieces")) = Product(
        NutritionalItemId.Product(0), "Synthetic reference Food", null, null, null, false, null, null,
        ProductSource(ProductSource.Type.User), facts, ProductFactBasis.REFERENCE_SERVING, reference,
    )

    @Test fun referenceFoodSupportsServingsWithoutInventingMetricMass() {
        val product = food()
        assertEquals(246.0, product.nutritionFor(Measurement.Serving(2.0))!!.energy.value)
        assertEquals(0.0, product.nutritionFor(Measurement.Serving(2.0))!!.proteins.value)
        assertNull(product.nutritionFor(Measurement.Serving(2.0))!!.fats.value)
        assertNull(product.weight(Measurement.Serving(1.0)))
        assertNull(product.nutritionFor(Measurement.Gram(100.0)))
        assertNull(product.metricNutrition())
        assertNull(product.nutritionFor(Measurement.Package(1.0)))
    }

    @Test fun encodedUnitsRejectOverflowWithoutCappingFiniteCanonicalNumbers() {
        val overflow = facts.copy(vitaminA = NutrientValue.Complete(1e303))
        assertTrue(overflow.hasValidValues())
        assertFailsWith<IllegalArgumentException> { toValidatedEntityNutrients(overflow) }
        val supported = facts.copy(vitaminA = NutrientValue.Complete(1e300))
        val encoded = toValidatedEntityNutrients(supported)
        val decoded = toNutritionFacts(encoded.first, encoded.second, encoded.third)
        assertTrue(decoded.hasValidValues())
        assertEquals(supported.vitaminA.value, decoded.vitaminA.value)
        assertEquals(0.0, decoded.proteins.value)
        assertNull(decoded.fats.value)
    }

    @Test fun metricConversionNeedsAnExplicitCompatibleReferenceAndPreservesDirectEnergy() {
        val product = food(PortionDescription(2.0, "pieces", metricAmount = 50.0, metricUnit = MetricUnit.GRAM))
        assertEquals(246.0, product.metricNutrition()!!.energy.value)
        assertEquals(25.0, product.weight(Measurement.Serving(0.5)))
        assertNull(product.nutritionFor(Measurement.Milliliter(100.0)))
        assertNull(food(PortionDescription(metricAmount = 50.0)).metricNutrition())
        assertNull(food(PortionDescription(metricAmount = 50.0, metricUnit = MetricUnit.MILLILITER)).metricNutrition())
        assertEquals(492.0, product.copy(packageWeight = 200.0).nutritionFor(Measurement.Package(1.0))!!.energy.value)
    }

    @Test fun reportedWholeRecipeWithUnknownYieldSupportsOnlyItsKnownBasis() {
        val recipe = Recipe(NutritionalItemId.Recipe(0), "Synthetic whole Recipe", null, emptyList(), null, false,
            ReportedNutrition(facts, ReportedBasis.WHOLE_ITEM), listOf(ReportedRecipeComponent("Synthetic component", PortionDescription(text = "amount unknown"))))
        assertEquals(61.5, recipe.nutritionFor(Measurement.Package(0.5))!!.energy.value)
        assertNull(recipe.servings)
        assertNull(recipe.nutritionFor(Measurement.Serving(1.0)))
        assertNull(recipe.totalWeight)
        assertNull(recipe.unpack(Measurement.Package(1.0)))
        assertEquals(41.0, recipe.copy(servings = 3).nutritionFor(Measurement.Serving(1.0))!!.energy.value)
    }

    @Test fun reportedServingRecipeDoesNotNeedYieldAndNeverDividesFactsAmongComponents() {
        val recipe = Recipe(NutritionalItemId.Recipe(0), "Synthetic serving Recipe", null, emptyList(), null, false,
            ReportedNutrition(facts, ReportedBasis.REFERENCE_SERVING), listOf(ReportedRecipeComponent("Synthetic unknown component")))
        assertEquals(246.0, recipe.nutritionFor(Measurement.Serving(2.0))!!.energy.value)
        assertNull(recipe.nutritionFor(Measurement.Package(1.0)))
        assertNull(recipe.reportedComponents.single().facts)
        assertNull(recipe.unpack(Measurement.Serving(1.0)))
        assertNull(recipe.copy(reportedNutrition = recipe.reportedNutrition!!.copy(needsReview = true)).nutritionFor(Measurement.Serving(1.0)))
    }

    @Test fun reportedSnapshotsAreIndependentAndCannotUnpackOrAcquireLiveLinks() {
        val component = ReportedRecipeComponent("Synthetic captured component", reference = DefinitionProvenance(DefinitionKind.PRODUCT, "synthetic", "1", 7))
        val captured = component.captured()
        assertNull(captured.reference!!.localId)
        assertEquals("1", captured.reference.externalId)
        val snapshot = RecipeSnapshot("Synthetic captured", null, emptyList(), false, null, 2,
            ReportedNutrition(facts, ReportedBasis.REFERENCE_SERVING), listOf(captured))
        assertEquals(123.0, snapshot.nutritionFor(Measurement.Serving(1.0))!!.energy.value)
        assertNull(snapshot.unpack(Measurement.Serving(1.0)))
        assertNull(snapshot.copy(calculationVersion = 99).nutritionFor(Measurement.Serving(1.0)))
        assertEquals(snapshot.reportedComponents, snapshot.copy(name = "Synthetic copy").reportedComponents)
    }

    @Test fun missingBasisAndInvalidRequestedNumbersNeverBecomeUsableFacts() {
        for (basis in listOf(ReportedBasis.UNSPECIFIED, ReportedBasis.UNAVAILABLE)) {
            assertNull(ReportedNutrition(facts, basis).nutritionFor(Measurement.Serving(1.0), false, null))
        }
        for (amount in listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY, Double.MAX_VALUE)) {
            assertNull(food().nutritionFor(Measurement.Serving(amount)))
        }
    }

    @Test fun internalStoragePreservesFactsPercentagesSecondaryClaimsAndInlineOrder() {
        val metadata = NutritionMetadata(percentDailyValues = mapOf("protein" to 0.0), observations = listOf(
            NutritionObservation("energy-claim", 155.0, "kcal", "separate claim")), textTruncated = true)
        val converter = LibraryValueConverters()
        val panel = ReportedNutrition(facts, ReportedBasis.REFERENCE_SERVING, PortionDescription(2.0, "pieces"), metadata)
        assertEquals(panel, converter.panel(converter.panel(panel)))
        val component = ReportedRecipeComponent("Synthetic inline", PortionDescription(0.5, "bowl", "about half a bowl"), facts, metadata = metadata)
        assertEquals(component, converter.component(converter.component(component)))
        assertEquals(123.0, panel.nutritionFor(Measurement.Serving(1.0), false, null)!!.energy.value)
        assertEquals(0.0, panel.nutritionFor(Measurement.Serving(1.0), false, null)!!.proteins.value)
    }

    @Test fun all43FieldsAndCompletenessSurviveInternalStorage() {
        val allFacts = NutritionFacts(
        proteins = NutrientValue.Complete(0.0),
        carbohydrates = NutrientValue.Incomplete(null),
        energy = NutrientValue.Complete(3.0),
        fats = NutrientValue.Complete(0.0),
        saturatedFats = NutrientValue.Incomplete(null),
        transFats = NutrientValue.Complete(6.0),
        monounsaturatedFats = NutrientValue.Complete(0.0),
        polyunsaturatedFats = NutrientValue.Incomplete(null),
        omega3 = NutrientValue.Complete(9.0),
        omega6 = NutrientValue.Complete(0.0),
        sugars = NutrientValue.Incomplete(null),
        addedSugars = NutrientValue.Complete(12.0),
        dietaryFiber = NutrientValue.Complete(0.0),
        solubleFiber = NutrientValue.Incomplete(null),
        insolubleFiber = NutrientValue.Complete(15.0),
        salt = NutrientValue.Complete(0.0),
        cholesterol = NutrientValue.Incomplete(null),
        caffeine = NutrientValue.Complete(18.0),
        vitaminA = NutrientValue.Complete(0.0),
        vitaminB1 = NutrientValue.Incomplete(null),
        vitaminB2 = NutrientValue.Complete(21.0),
        vitaminB3 = NutrientValue.Complete(0.0),
        vitaminB5 = NutrientValue.Incomplete(null),
        vitaminB6 = NutrientValue.Complete(24.0),
        vitaminB7 = NutrientValue.Complete(0.0),
        vitaminB9 = NutrientValue.Incomplete(null),
        vitaminB12 = NutrientValue.Complete(27.0),
        vitaminC = NutrientValue.Complete(0.0),
        vitaminD = NutrientValue.Incomplete(null),
        vitaminE = NutrientValue.Complete(30.0),
        vitaminK = NutrientValue.Complete(0.0),
        manganese = NutrientValue.Incomplete(null),
        magnesium = NutrientValue.Complete(33.0),
        potassium = NutrientValue.Complete(0.0),
        calcium = NutrientValue.Incomplete(null),
        copper = NutrientValue.Complete(36.0),
        zinc = NutrientValue.Complete(0.0),
        sodium = NutrientValue.Incomplete(null),
        iron = NutrientValue.Complete(39.0),
        phosphorus = NutrientValue.Complete(0.0),
        selenium = NutrientValue.Incomplete(null),
        iodine = NutrientValue.Complete(42.0),
        chromium = NutrientValue.Complete(0.0),
        )
        assertEquals(43, NutritionFactsField.entries.size)
        val panel = ReportedNutrition(allFacts, ReportedBasis.WHOLE_ITEM)
        val converters = LibraryValueConverters()
        val roundTrip = converters.panel(converters.panel(panel))!!
        for (field in NutritionFactsField.entries) assertEquals(allFacts[field], roundTrip.facts[field], field.name)
        val partial = panel.copy(facts = allFacts.copy(proteins = NutrientValue.Incomplete(5.0)))
        assertEquals(partial, converters.panel(converters.panel(partial)))
    }

    @Test fun absoluteMealItemScalingNeverUsesItsRawDescriptionAsAMultiplier() {
        val item = MealItem("Synthetic absolute", facts, portionDescription = PortionDescription(3.0, "pieces", "3 pieces"))
        assertEquals(123.0, item.nutritionFacts.energy.value)
        val scaled = item.scaleCapturedPortion(2.0)!!
        assertEquals(246.0, scaled.nutritionFacts.energy.value)
        assertEquals(item.portionDescription, scaled.portionDescription)
    }
}
