package com.maksimowiczm.foodyou.food

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.maksimowiczm.foodyou.app.infrastructure.room.FoodYouDatabase
import com.maksimowiczm.foodyou.common.domain.date.DateProvider
import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.common.domain.measurement.*
import com.maksimowiczm.foodyou.common.log.Logger
import com.maksimowiczm.foodyou.common.result.*
import com.maksimowiczm.foodyou.food.domain.entity.*
import com.maksimowiczm.foodyou.food.domain.usecase.*
import com.maksimowiczm.foodyou.food.infrastructure.repository.*
import com.maksimowiczm.foodyou.fooddiary.domain.entity.*
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.*
import com.maksimowiczm.foodyou.fooddiary.infrastructure.repository.*
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.DiarySlotEntity
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Instant
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.*
import org.junit.Assert.*
import org.junit.Test

/** Owns only a uniquely named synthetic database, never the canonical diary. */
class ProductPortionPersistenceTest {
    @Test fun typedDefinitionsPortionsFailuresAndHistoricalSnapshotsSurviveReopen() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "story26-product-" + UUID.randomUUID() + ".db"
        var db = Room.databaseBuilder(context, FoodYouDatabase::class.java, name).build()
        try {
            val products = RoomProductRepository(db.productDao)
            val recipes = RoomRecipeRepository(db.recipeDao, products)
            val history = RoomNutritionalItemHistoryRepository(db.foodEventDao)
            val suggestions = RoomNutritionalItemMeasurementSuggestionRepository(db.measurementSuggestionDao)
            val entries = RoomNutritionalItemDiaryEntryRepository(db, db.measurementDao)
            val slots = RoomDiarySlotRepository(db.diarySlotDao)
            val manual = RoomManualDiaryEntryRepository(db.manualDiaryEntryDao)
            db.diarySlotDao.insertWithLastRank(DiarySlotEntity(2601, "Synthetic Product slot", 0, 0, 23, 59))
            val facts = NutritionFacts(energy = NutrientValue.Complete(250.0), proteins = NutrientValue.Complete(10.0))
            val source = ProductSource(ProductSource.Type.User, "https://example.invalid/synthetic")
            val create = CreateProductUseCase(products, history, db, ProductTestLogger)
            suspend fun createWith(serving: Double?, nutrition: NutritionFacts = facts) = create.create(
                "Synthetic oats", "Synthetic brand", "2600001", "Captured note", false,
                200.0, serving, source, nutrition, NutritionalItemHistory.Created(ProductTestClock.nowInstant()))
            for (invalid in listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY)) assertTrue(createWith(invalid).isError())
            assertTrue(createWith(40.0, facts.copy(sodium = NutrientValue.Complete(Double.NaN))).isError())
            assertTrue(products.observeProducts(20, 0).first().isEmpty())
            val id = createWith(40.0).fold({ it }, { error("Synthetic Product creation failed") })
            val original = products.observeProduct(id).first()!!
            assertEquals("Synthetic brand", original.brand)
            assertEquals("2600001", original.barcode)
            assertEquals(source, original.source)
            assertEquals(200.0, original.packageWeight)
            assertEquals(40.0, original.servingWeight)
            val recipeId = recipes.insertRecipe("Synthetic recipe", 2, "Recipe note", false,
                listOf(RecipeIngredient(original, Measurement.Gram(100.0))))
            assertEquals(id.id, recipeId.id) // independent tables deliberately use the same number
            val observe = ObserveNutritionalItemUseCase(products, recipes)
            assertTrue(observe.observe(id).first() is Product)
            assertTrue(observe.observe(recipeId).first() is Recipe)
            suggestions.insert(id, Measurement.Serving(2.0))
            suggestions.insert(recipeId, Measurement.Gram(75.0))
            assertEquals(listOf(Measurement.Serving(2.0)), suggestions.observeByFoodId(id, 20).first())
            assertEquals(listOf(Measurement.Gram(75.0)), suggestions.observeByFoodId(recipeId, 20).first())
            history.insert(recipeId, NutritionalItemHistory.Edited(ProductTestClock.nowInstant()))
            assertTrue(history.observeFoodHistory(id).first().single() is NutritionalItemHistory.Created)
            assertTrue(history.observeFoodHistory(recipeId).first().single() is NutritionalItemHistory.Edited)

            val snapshot = ProductSnapshot(original.headline, facts, 40.0, 200.0, false, source, "Captured note")
            val log = CreateNutritionalItemDiaryEntryUseCase(slots, entries, db, ProductTestClock, ProductTestLogger)
            val date = LocalDate(2026, 10, 25)
            for (invalid in listOf(Measurement.Serving(0.0), Measurement.Serving(-1.0), Measurement.Gram(Double.NaN),
                Measurement.Milliliter(80.0), Measurement.Serving(Double.MAX_VALUE), Measurement.Gram(Double.MIN_VALUE))) {
                assertTrue(log.createDiaryEntry(invalid, 2601, date, snapshot).isError())
            }
            assertTrue(log.createDiaryEntry(Measurement.Serving(1.0), 2601, date, snapshot.copy(servingWeight = null)).isError())
            assertTrue(entries.observeAll(2601, date).first().isEmpty())
            val entryId = log.createDiaryEntry(Measurement.Serving(2.0), 2601, date, snapshot)
                .fold({ it }, { error("Synthetic portion logging failed") })
            val captured = entries.observe(entryId).first()!!
            assertEquals(80.0, captured.weight!!, 0.000001)
            assertEquals(200.0, captured.nutritionFacts.energy.value)
            val editEntry = UpdateNutritionalItemDiaryEntryUseCase(slots, entries, ProductTestClock, db, ProductTestLogger)
            assertTrue(editEntry.update(entryId, Measurement.Package(Double.POSITIVE_INFINITY), 2601, date).isError())
            assertEquals(captured, entries.observe(entryId).first())
            assertTrue(editEntry.update(entryId, Measurement.Gram(80.0), 2601, date).isSuccess())
            assertEquals(captured.nutritionFacts, entries.observe(entryId).first()!!.nutritionFacts)

            val update = UpdateProductUseCase(products, db, ProductTestLogger)
            suspend fun edit(serving: Double?) = update.update(id, "Synthetic changed", "New brand",
                facts.copy(energy = NutrientValue.Complete(500.0)), "2600002", 300.0, serving,
                "Future note", source, false)
            assertTrue(edit(-1.0).isError())
            assertEquals(original, products.observeProduct(id).first())
            assertTrue(edit(50.0).isSuccess())
            val changed = products.observeProduct(id).first()!!
            assertEquals(captured.nutritionFacts, entries.observe(entryId).first()!!.nutritionFacts)
            assertEquals(captured.food, entries.observe(entryId).first()!!.food)
            val future = ProductSnapshot(changed.headline, changed.nutritionFacts, changed.servingWeight,
                changed.totalWeight, false, changed.source, changed.note)
            assertTrue(log.createDiaryEntry(Measurement.Serving(2.0), 2601, date, future).isSuccess())
            assertTrue(log.createDiaryEntry(Measurement.Serving(0.5), 2601, date,
                snapshot.copy(isLiquid = true)).isSuccess())
            val quickId = manual.insert("Synthetic absolute Quick Add", 2601, date, facts, ProductTestClock.now(),
                servingCount = 3.0, weightGrams = 600.0)
            assertEquals(250.0, manual.observe(quickId).first()!!.nutritionFacts.energy.value)
            assertNull(manual.observe(quickId).first()!!.nutritionFacts.fats.value)
            db.close()
            db = Room.databaseBuilder(context, FoodYouDatabase::class.java, name).build()
            val reopened = RoomNutritionalItemDiaryEntryRepository(db, db.measurementDao)
            assertEquals(listOf(50.0, 200.0, 500.0), reopened.observeAll(2601, date).first().map { it.nutritionFacts.energy.value!! }.sorted())
            val restored = RoomProductRepository(db.productDao).observeProduct(id).first()!!
            assertEquals(changed, restored)
            assertEquals(captured.food, reopened.observe(entryId).first()!!.food)
            assertEquals(250.0, RoomManualDiaryEntryRepository(db.manualDiaryEntryDao).observe(quickId).first()!!.nutritionFacts.energy.value)
        } finally {
            db.close()
            context.deleteDatabase(name)
        }
    }
}

internal object ProductTestLogger : Logger {
    override fun d(tag: String, throwable: Throwable?, message: () -> String) = Unit
    override fun w(tag: String, throwable: Throwable?, message: () -> String) = Unit
    override fun e(tag: String, throwable: Throwable?, message: () -> String) = Unit
    override fun i(tag: String, throwable: Throwable?, message: () -> String) = Unit
}

internal object ProductTestClock : DateProvider {
    override fun nowInstant(): Instant = Instant.fromEpochSeconds(1792886400)
    override fun observeInstant(interval: Duration): Flow<Instant> = flowOf(nowInstant())
    override fun observeDate(timeZone: TimeZone): Flow<LocalDate> = flowOf(LocalDate(2026, 10, 25))
}
