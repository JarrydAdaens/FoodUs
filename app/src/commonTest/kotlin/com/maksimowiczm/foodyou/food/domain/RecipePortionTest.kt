package com.maksimowiczm.foodyou.food.domain

import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.common.domain.measurement.*
import com.maksimowiczm.foodyou.food.domain.entity.*
import com.maksimowiczm.foodyou.food.domain.repository.RecipeRepository
import com.maksimowiczm.foodyou.food.domain.usecase.hasRecipeCycle
import com.maksimowiczm.foodyou.fooddiary.domain.entity.*
import kotlin.test.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

class RecipePortionTest {
    private fun product(id: Long = 1, liquid: Boolean = false, protein: NutrientValue = NutrientValue.Complete(10.0), serving: Double? = null) =
        Product(NutritionalItemId.Product(id), "Synthetic ingredient", null, null, null, liquid, null, serving,
            ProductSource(ProductSource.Type.User), NutritionFacts(energy = NutrientValue.Complete(200.0), proteins = protein))
    private fun recipe(ingredients: List<RecipeIngredient>, yield: Int = 3, liquid: Boolean = false) =
        Recipe(NutritionalItemId.Recipe(1), "Synthetic recipe", yield, ingredients, "Synthetic note", liquid)
    private fun Product.snapshot() = ProductSnapshot(headline, nutritionFacts, servingWeight, totalWeight, isLiquid, source, note)
    private fun Recipe.snapshot(version: Int = 1): RecipeSnapshot = RecipeSnapshot(name, servings, ingredients.map {
        RecipeIngredientSnapshot(when (val item = it.food) { is Product -> item.snapshot(); is Recipe -> item.snapshot(version) }, it.measurement)
    }, isLiquid, note, version)

    @Test fun twoOfThreeServingsScaleTotalOnceAndAgreeWithKnownMetric() {
        val recipe = recipe(listOf(RecipeIngredient(product(), Measurement.Gram(300.0))))
        assertEquals(600.0, recipe.totalNutrition.energy.value)
        assertEquals(300.0, recipe.totalWeight)
        assertEquals(100.0, recipe.servingWeight)
        assertEquals(400.0, recipe.nutritionFor(Measurement.Serving(2.0))!!.energy.value!!, 0.000001)
        assertEquals(recipe.nutritionFor(Measurement.Gram(200.0)), recipe.nutritionFor(Measurement.Serving(2.0)))
        assertEquals(recipe.nutritionFor(Measurement.Serving(2.0)), recipe.snapshot().nutritionFor(Measurement.Serving(2.0)))
        assertEquals(100.0, recipe.nutritionFor(Measurement.Serving(0.5))!!.energy.value!!, 0.000001)
        assertEquals(150.0, recipe.nutritionFor(Measurement.Package(0.25))!!.energy.value)
        assertEquals(200.0, (recipe.unpack(Measurement.Serving(2.0))!!.single().measurement as Measurement.Gram).value)
    }

    @Test fun mixedIngredientsKeepUnknownMetricBasisAndStillSupportServingFractions() {
        val mixed = recipe(listOf(RecipeIngredient(product(), Measurement.Gram(100.0)),
            RecipeIngredient(product(2, true), Measurement.Milliliter(200.0))))
        assertNull(mixed.totalWeight)
        assertNull(mixed.servingWeight)
        assertNull(mixed.nutritionFor(Measurement.Gram(100.0)))
        assertEquals(400.0, mixed.nutritionFor(Measurement.Serving(2.0))!!.energy.value!!, 0.000001)
        assertTrue(MeasurementType.Serving in mixed.supportedMeasurementTypes())
        assertFalse(MeasurementType.Gram in mixed.supportedMeasurementTypes())
        val nested = recipe(listOf(RecipeIngredient(mixed, Measurement.Serving(1.5))), yield = 2)
        assertNull(nested.totalWeight)
        assertEquals(150.0, nested.nutritionFor(Measurement.Serving(1.0))!!.energy.value)
        assertEquals(nested.nutritionFor(Measurement.Serving(1.0)), nested.snapshot().nutritionFor(Measurement.Serving(1.0)))
        assertTrue(nested.unpack(Measurement.Serving(1.0))!!.single().food is Recipe)
    }

    @Test fun unknownReferenceBasesAndNutrientsNeverBecomeZeroOrPartialTotal() {
        val unknownAmount = recipe(listOf(RecipeIngredient(product(), Measurement.Serving(1.0))))
        assertNull(unknownAmount.totalWeight)
        assertNull(unknownAmount.totalNutrition.energy.value)
        assertNull(unknownAmount.nutritionFor(Measurement.Serving(1.0)))
        val partial = recipe(listOf(RecipeIngredient(product(), Measurement.Gram(100.0)),
            RecipeIngredient(product(2, protein = NutrientValue.Incomplete(null)), Measurement.Gram(100.0))))
        assertNull(partial.totalNutrition.proteins.value)
        assertFalse(partial.totalNutrition.proteins.isComplete)
        assertNull(partial.nutritionFor(Measurement.Serving(2.0))!!.proteins.value)
        assertEquals(400.0, partial.totalNutrition.energy.value)
        val allUnknown = recipe(listOf(RecipeIngredient(product(protein = NutrientValue.Incomplete(null)), Measurement.Gram(100.0))))
        assertNull(allUnknown.totalNutrition.proteins.value)
    }

    @Test fun legacySnapshotsPreserveMixedMetric150AndPartialProtein10ButNewCapturesStayStrict() {
        val a = product().copy(nutritionFacts = NutritionFacts(energy = NutrientValue.Complete(100.0), proteins = NutrientValue.Complete(10.0)))
        val b = product(2, true, NutrientValue.Incomplete(null))
        val definition = recipe(listOf(RecipeIngredient(a, Measurement.Gram(100.0)), RecipeIngredient(b, Measurement.Milliliter(100.0))), 1)
        val legacy = definition.snapshot(0)
        assertEquals(200.0, legacy.totalWeight)
        assertEquals(150.0, legacy.nutritionFor(Measurement.Gram(100.0))!!.energy.value)
        assertEquals(10.0, legacy.nutritionFor(Measurement.Serving(1.0))!!.proteins.value)
        assertFalse(legacy.nutritionFor(Measurement.Serving(1.0))!!.proteins.isComplete)
        val current = definition.snapshot()
        assertNull(current.totalWeight)
        assertNull(current.nutritionFor(Measurement.Gram(100.0)))
        assertEquals(300.0, current.nutritionFor(Measurement.Serving(1.0))!!.energy.value)
        assertNull(current.nutritionFor(Measurement.Serving(1.0))!!.proteins.value)
        assertEquals(0, legacy.copy(name = "Copied capture").calculationVersion)
        val parentLegacy = recipe(listOf(RecipeIngredient(definition, Measurement.Serving(1.0))), 1).snapshot(0)
        assertEquals(0, (parentLegacy.unpack(Measurement.Serving(1.0))!!.single().food as RecipeSnapshot).calculationVersion)
    }

    @Test fun invalidAndOverflowingMeasurementsAndYieldsFailWithoutThrowingOnLegacyModels() {
        val valid = recipe(listOf(RecipeIngredient(product(), Measurement.Gram(300.0))))
        for (bad in listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.MAX_VALUE)) {
            assertNull(valid.nutritionFor(Measurement.Serving(bad)))
            assertNull(valid.unpack(Measurement.Serving(bad)))
        }
        assertNull(valid.copy(servings = 0).nutritionFor(Measurement.Serving(1.0)))
        assertNull(valid.copy(servings = -1).servingWeight)
        assertNull(valid.nutritionFor(Measurement.Milliliter(100.0)))
        assertNull(valid.nutritionFor(Measurement.Serving(Double.MIN_VALUE)))
        val unknown = product().copy(nutritionFacts = NutritionFacts())
        val overflow = recipe(listOf(RecipeIngredient(unknown,Measurement.Gram(Double.MAX_VALUE)),RecipeIngredient(unknown,Measurement.Gram(Double.MAX_VALUE))))
        assertFalse(overflow.isResolvable)
        assertNull(overflow.nutritionFor(Measurement.Serving(1.0)))
        assertNull(overflow.snapshot().nutritionFor(Measurement.Serving(1.0)))
    }

    @Test fun candidateGraphRejectsSelfAndIndirectCyclesButAllowsSharedBranchesAndProductIdOverlap() = runBlocking {
        val edges = mutableMapOf<Long, List<Long>>(2L to listOf(3), 3L to emptyList(), 4L to listOf(3))
        val repository = object : RecipeRepository {
            override fun observeRecipe(recipeId: NutritionalItemId.Recipe): Flow<Recipe?> = error("Graph must be validated before recursive hydration")
            override suspend fun recipeDependencies(id: NutritionalItemId.Recipe) = edges[id.id].orEmpty().map(NutritionalItemId::Recipe)
            override suspend fun insertRecipe(name: String, servings: Int, note: String?, isLiquid: Boolean, ingredients: List<RecipeIngredient>): NutritionalItemId.Recipe = error("No writes")
            override suspend fun updateRecipe(recipe: Recipe) = error("No writes")
            override suspend fun deleteRecipe(recipe: Recipe) = error("No writes")
        }
        val id = NutritionalItemId.Recipe(1)
        assertTrue(repository.hasRecipeCycle(id, listOf(id)))
        assertFalse(repository.hasRecipeCycle(id, listOf(NutritionalItemId.Product(1), NutritionalItemId.Recipe(2), NutritionalItemId.Recipe(4))))
        edges[3] = listOf(1)
        assertTrue(repository.hasRecipeCycle(id, listOf(NutritionalItemId.Recipe(2))))
        edges[3] = listOf(2)
        assertTrue(repository.hasRecipeCycle(null, listOf(NutritionalItemId.Recipe(2))))
    }
}
