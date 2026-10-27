package com.maksimowiczm.foodyou.food

import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.maksimowiczm.foodyou.app.infrastructure.room.FoodYouDatabase
import com.maksimowiczm.foodyou.app.ui.food.library.*
import com.maksimowiczm.foodyou.app.ui.food.meals.MealsViewModel
import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.food.domain.entity.*
import com.maksimowiczm.foodyou.food.domain.usecase.ObserveNutritionalItemUseCase
import com.maksimowiczm.foodyou.food.infrastructure.repository.*
import com.maksimowiczm.foodyou.fooddiary.domain.entity.*
import com.maksimowiczm.foodyou.fooddiary.domain.repository.MealRepository
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.*
import com.maksimowiczm.foodyou.fooddiary.infrastructure.repository.*
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.DiarySlotEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.LocalDate
import org.junit.Assert.*
import org.junit.Test

class PersonalLibraryPersistenceTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val facts = NutritionFacts(energy = NutrientValue.Complete(120.0))

    @Test fun personalNamesExcludeProvidersAndDoNotHydrateReportedOrMissingRecipeGraphs() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, FoodYouDatabase::class.java).build()
        try {
            val products = RoomProductRepository(db.productDao)
            val recipes = RoomRecipeRepository(db.recipeDao, products)
            repeat(130) { products.insertProduct("Synthetic $it", null, null, null, false, null, null,
                ProductSource(ProductSource.Type.User), facts) }
            products.insertProduct("Synthetic provider", null, null, null, false, null, null,
                ProductSource(ProductSource.Type.USDA), facts)
            val id = recipes.insertRecipe(Recipe(NutritionalItemId.Recipe(0), "Synthetic unknown yield", null,
                emptyList(), null, false, ReportedNutrition(facts, ReportedBasis.WHOLE_ITEM)))
            val names = products.observeLibraryNames().first()
            assertEquals(130, names.size)
            assertTrue(names.none { it.name == "Synthetic provider" })
            assertEquals(id, recipes.observeLibraryNames().first().single().id)
            assertNull(libraryNutrition(recipes.observeRecipe(id).first()!!).facts)
            assertNull(recipes.observeRecipe(NutritionalItemId.Recipe(99999)).first())
        } finally { db.close() }
    }

    @Test fun newMealCancelFailureAndRepeatedSaveLeaveHistoryUntouched() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, FoodYouDatabase::class.java).build()
        var vm: MealsViewModel? = null
        try {
            val products = RoomProductRepository(db.productDao)
            val recipes = RoomRecipeRepository(db.recipeDao, products)
            val meals = RoomMealRepository(db, db.mealDao)
            val manual = RoomManualDiaryEntryRepository(db.manualDiaryEntryDao)
            val slots = RoomDiarySlotRepository(db.diarySlotDao)
            val date = LocalDate(2026, 1, 1)
            db.diarySlotDao.insertWithLastRank(DiarySlotEntity(83801, "Synthetic destination", 0, 0, 23, 59))
            manual.insert("Synthetic historical entry", 83801, date, facts, ProductTestClock.now())
            val before = manual.observeAll(83801, date).first()
            val food = products.insertProduct("Synthetic ingredient", null, null, null, false, null, null,
                ProductSource(ProductSource.Type.User), facts)
            val entered = CompletableDeferred<Unit>()
            val release = CompletableDeferred<Unit>()
            var writes = 0
            var fail = true
            val delayed = object : MealRepository by meals {
                override suspend fun save(name: String, items: List<MealItem>): MealId {
                    if(fail) error("Synthetic failure before write")
                    writes++; entered.complete(Unit); release.await()
                    return meals.save(name, items)
                }
            }
            val model = withContext(Dispatchers.Main) {
                MealsViewModel(delayed, UpdateMealUseCase(delayed), ApplyMealUseCase(meals, manual, slots, db, ProductTestClock),
                    slots, ObserveNutritionalItemUseCase(products, recipes))
            }.also { vm = it }
            withContext(Dispatchers.Main) { model.create(); model.rename("Synthetic discarded"); model.cancel() }
            assertTrue(meals.observeAll().first().isEmpty())
            withContext(Dispatchers.Main) {
                model.create(); model.rename("Synthetic saved bundle")
                model.capture(food, Measurement.Gram(100.0)) {}
            }
            withTimeout(5000) { model.editor.first { it?.meal?.items?.size == 1 } }
            withContext(Dispatchers.Main) { model.save() }
            withTimeout(5000) { model.busy.first { !it } }
            assertNotNull(model.editor.value)
            assertTrue(meals.observeAll().first().isEmpty())
            fail = false
            withContext(Dispatchers.Main) { model.save(); model.save() }
            withTimeout(5000) { entered.await() }
            assertEquals(1, writes)
            release.complete(Unit)
            withTimeout(5000) { model.editor.first { it == null } }
            assertEquals(1, meals.observeAll().first().size)
            assertEquals(before, manual.observeAll(83801, date).first())
            val saved = meals.observeAll().first().single()
            assertEquals(Measurement.Gram(100.0), saved.items.single().capturedPortion)
        } finally {
            withContext(Dispatchers.Main) { vm?.viewModelScope?.cancel() }
            db.close()
        }
    }

    @Test fun librarySubscriptionRestartRetainsSuccessfulNamesInsteadOfEmittingLoading() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, FoodYouDatabase::class.java).build()
        var vm: LibraryViewModel? = null
        try {
            val products = RoomProductRepository(db.productDao)
            val recipes = RoomRecipeRepository(db.recipeDao, products)
            products.insertProduct("Synthetic retained", null, null, null, false, null, null,
                ProductSource(ProductSource.Type.User), facts)
            val model = withContext(Dispatchers.Main) { LibraryViewModel(products, recipes, ObserveNutritionalItemUseCase(products, recipes)) }.also { vm = it }
            val first = launch { model.products.collect() }
            withTimeout(5000) { model.products.first { it is LibraryRead.Ready } }
            first.cancelAndJoin()
            delay(5300)
            val seen = mutableListOf<LibraryRead<List<NutritionalItemName>>>()
            val second = launch { model.products.collect { seen += it } }
            delay(400)
            second.cancelAndJoin()
            assertTrue(seen.isNotEmpty())
            assertTrue(seen.all { it is LibraryRead.Ready })
        } finally {
            withContext(Dispatchers.Main) { vm?.viewModelScope?.cancel() }
            db.close()
        }
    }
}
