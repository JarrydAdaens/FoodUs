package com.maksimowiczm.foodyou.food

import android.os.Build
import android.os.Process
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.maksimowiczm.foodyou.app.infrastructure.room.FoodYouDatabase
import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.result.isSuccess
import com.maksimowiczm.foodyou.food.domain.entity.*
import com.maksimowiczm.foodyou.food.infrastructure.repository.*
import com.maksimowiczm.foodyou.fooddiary.domain.entity.MealItem
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.CreateMealDefinitionUseCase
import com.maksimowiczm.foodyou.fooddiary.infrastructure.repository.RoomMealRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test

/** Run seed and verify as separate instrumentation invocations with target process stop between. */
class PersonalLibraryRestartTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val databaseName = "foodus-story38-restart.db"
    private val receipt get() = java.io.File(context.filesDir, "foodus-story38-seed-pid.txt")
    private fun requireEmulator() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("foodus.libraryRestart") == "true")
        assumeTrue(Build.FINGERPRINT.contains("generic") || Build.MODEL.contains("sdk_gphone"))
    }

    @Test fun seedLibraryDefinitions() = runBlocking {
        requireEmulator()
        context.deleteDatabase(databaseName)
        val db = Room.databaseBuilder(context, FoodYouDatabase::class.java, databaseName).build()
        try {
            val products = RoomProductRepository(db.productDao)
            val recipes = RoomRecipeRepository(db.recipeDao, products)
            val facts = NutritionFacts(energy = NutrientValue.Complete(321.0), proteins = NutrientValue.Incomplete(null))
            products.insertProduct(Product(NutritionalItemId.Product(0), "Synthetic persisted reference Product", null, null, null,
                false, null, null, ProductSource(ProductSource.Type.User), facts, ProductFactBasis.REFERENCE_SERVING,
                PortionDescription(2.0, "pieces", "Synthetic captured quantity")))
            recipes.insertRecipe(Recipe(NutritionalItemId.Recipe(0), "Synthetic persisted unknown-yield Recipe", null,
                emptyList(), null, false, ReportedNutrition(facts, ReportedBasis.WHOLE_ITEM)))
            val meals = RoomMealRepository(db, db.mealDao)
            assertTrue(CreateMealDefinitionUseCase(meals).create("Synthetic persisted Meal", listOf(
                MealItem("Synthetic captured item", facts, capturedPortion = Measurement.Serving(2.0)))).isSuccess())
            receipt.writeText(Process.myPid().toString())
        } finally { db.close() }
    }

    @Test fun verifyLibraryDefinitionsAfterProcessRestart() = runBlocking {
        requireEmulator()
        assertNotEquals("A separate application process must verify restart persistence", receipt.readText().trim().toInt(), Process.myPid())
        val db = Room.databaseBuilder(context, FoodYouDatabase::class.java, databaseName).build()
        try {
            val products = RoomProductRepository(db.productDao)
            val recipes = RoomRecipeRepository(db.recipeDao, products)
            val product = products.observeProduct(products.observeLibraryNames().first().single().id as NutritionalItemId.Product).first()!!
            assertEquals(ProductFactBasis.REFERENCE_SERVING, product.factBasis)
            assertEquals("Synthetic captured quantity", product.referencePortion!!.text)
            assertNull(product.weight(Measurement.Serving(1.0)))
            val recipe = recipes.observeRecipe(recipes.observeLibraryNames().first().single().id as NutritionalItemId.Recipe).first()!!
            assertNull(recipe.servings)
            assertEquals(321.0, recipe.reportedNutrition!!.facts.energy.value)
            assertNull(recipe.nutritionFor(Measurement.Serving(1.0)))
            val meal = RoomMealRepository(db, db.mealDao).observeAll().first().single()
            assertEquals(Measurement.Serving(2.0), meal.items.single().capturedPortion)
            assertEquals(321.0, meal.items.single().nutritionFacts.energy.value)
            assertNull(meal.items.single().nutritionFacts.proteins.value)
        } finally { db.close() }
    }
}
