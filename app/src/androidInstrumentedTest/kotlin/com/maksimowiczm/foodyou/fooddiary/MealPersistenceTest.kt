package com.maksimowiczm.foodyou.fooddiary

import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.maksimowiczm.foodyou.app.infrastructure.room.FoodYouDatabase
import com.maksimowiczm.foodyou.app.ui.food.meals.MealsViewModel
import com.maksimowiczm.foodyou.app.ui.home.meals.card.DiarySlotsCardsViewModel
import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.common.domain.measurement.*
import com.maksimowiczm.foodyou.common.domain.userpreferences.*
import com.maksimowiczm.foodyou.common.result.*
import com.maksimowiczm.foodyou.food.ProductTestClock
import com.maksimowiczm.foodyou.food.domain.usecase.ObserveNutritionalItemUseCase
import com.maksimowiczm.foodyou.food.infrastructure.repository.*
import com.maksimowiczm.foodyou.fooddiary.domain.entity.*
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.*
import com.maksimowiczm.foodyou.fooddiary.infrastructure.repository.*
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.LocalDate
import java.util.UUID
import org.junit.Assert.*
import org.junit.Test

class MealPersistenceTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun updateRetainsIdsAllNutritionAndQuantityAcrossReopenAndRejectsStaleOwnership() = runBlocking {
        val name = "story24-update-" + UUID.randomUUID() + ".db"
        var db = Room.databaseBuilder(context, FoodYouDatabase::class.java, name).build()
        var expected: Meal? = null
        try {
            val meals = RoomMealRepository(db, db.mealDao)
            val facts = NutritionFacts().mapMealNutrition { field, _ ->
                if (field.ordinal % 5 == 0) NutrientValue.Incomplete(null) else NutrientValue.Complete(field.ordinal + 0.125)
            }
            val portions = listOf(Measurement.Gram(80.0), Measurement.Ounce(2.5), Measurement.Milliliter(125.25),
                Measurement.FluidOunce(2.5), Measurement.Serving(1.234567), Measurement.Package(0.75))
            val id = meals.save("Synthetic portions", portions.mapIndexed { index, portion ->
                MealItem("Synthetic item " + index, facts, capturedPortion = portion, servingCount = 1.234567, weightGrams = 123.456789)
            })
            val original = meals.getMeal(id)!!
            val oldRows = db.mealDao.getById(id.value)!!
            assertTrue(UpdateMealUseCase(meals).update(id, "Synthetic renamed", original.items).isSuccess())
            val renamed = meals.getMeal(id)!!
            assertEquals(original.createdAt, renamed.createdAt)
            assertEquals(oldRows.items, db.mealDao.getById(id.value)!!.items)
            val changedItems = listOf(original.items[4].copy(name = "Synthetic retained"), MealItem("Synthetic added", facts)) + original.items.drop(5)
            assertTrue(UpdateMealUseCase(meals).update(id, "Synthetic changed", changedItems).isSuccess())
            expected = meals.getMeal(id)!!
            assertEquals(original.items[4].id, expected!!.items[0].id)
            assertEquals(original.items[5].id, expected!!.items[2].id)
            assertFalse(original.items.mapNotNull { it.id }.contains(expected!!.items[1].id))
            val other = meals.save("Synthetic other", listOf(MealItem("Other", facts)))
            val foreign = meals.getMeal(other)!!.items.single()
            assertTrue(UpdateMealUseCase(meals).update(id, "Bad ownership", listOf(foreign)).isError())
            assertTrue(UpdateMealUseCase(meals).update(id, "Duplicate", listOf(expected!!.items[0], expected!!.items[0])).isError())
            assertEquals(expected, meals.getMeal(id))
            assertTrue(UpdateMealUseCase(meals).update(id, "Removed stale row", listOf(original.items[0])).isError())
            assertEquals(expected, meals.getMeal(id))
            meals.delete(other)
            assertTrue(UpdateMealUseCase(meals).update(other, "Deleted aggregate", listOf(foreign)).isError())
            assertNull(meals.getMeal(other))
            assertEquals(expected, meals.getMeal(id))
            val partial = facts.mapMealNutrition { _, _ -> NutrientValue.Incomplete(10.0) }
            val partialId = meals.save("Synthetic partial", listOf(MealItem("Partial", partial)))
            val normalized = meals.getMeal(partialId)!!.items.single().nutritionFacts
            NutritionFactsField.entries.forEach { assertNull(it.name, normalized[it].value) }
        } finally { db.close() }
        db = Room.databaseBuilder(context, FoodYouDatabase::class.java, name).build()
        try {
            val reopened = RoomMealRepository(db, db.mealDao).getMeal(expected!!.id)!!
            assertEquals(expected, reopened)
            assertEquals(Measurement.Serving(1.234567), reopened.items[0].capturedPortion)
            assertEquals(1.234567, reopened.items[0].servingCount)
            assertEquals(123.456789, reopened.items[0].weightGrams)
        } finally { db.close(); context.deleteDatabase(name) }
    }

    @Test fun convertedNutritionOverflowRejectsSaveAndUpdateBeforeAnyRoomWrite() = runBlocking {
        val name = "story24-conversion-" + UUID.randomUUID() + ".db"
        val db = Room.databaseBuilder(context, FoodYouDatabase::class.java, name).build()
        try {
            val meals = RoomMealRepository(db, db.mealDao)
            val id = meals.save("Synthetic finite prior", listOf(MealItem("Keep", NutritionFacts(energy = NutrientValue.Complete(100.0)))))
            val prior = db.mealDao.getById(id.value)!!
            var headerWrites = 0
            val guarded = RoomMealRepository(db, object : MealDao by db.mealDao {
                override suspend fun insertMeal(meal: MealEntity): Long {
                    headerWrites++
                    return db.mealDao.insertMeal(meal)
                }
                override suspend fun updateMeal(meal: MealEntity) {
                    headerWrites++
                    db.mealDao.updateMeal(meal)
                }
            })
            for (field in listOf(NutritionFactsField.VitaminA, NutritionFactsField.Cholesterol)) {
                val initial = if (field == NutritionFactsField.VitaminA) 0.001 else 1.0
                val facts = NutritionFacts(energy = NutrientValue.Complete(100.0)).withMealNutrient(field, initial)
                val overflow = MealItem("Synthetic overflow", facts).scaleCapturedPortion(1e306)!!
                assertTrue(overflow.nutritionFacts.validMealNutrition())
                val stored = com.maksimowiczm.foodyou.common.infrastructure.room.toEntityNutrients(overflow.nutritionFacts)
                val roundTrip = com.maksimowiczm.foodyou.common.infrastructure.room.toNutritionFacts(stored.first, stored.second, stored.third)
                assertFalse(roundTrip.validMealNutrition())
                var rejected = false
                try { guarded.save("Must not create header", listOf(overflow)) } catch (_: IllegalStateException) { rejected = true }
                assertTrue(field.name, rejected)
                val retained = overflow.copy(id = MealItemId(prior.items.single().id))
                assertTrue(UpdateMealUseCase(guarded).update(id, "Must not change header", listOf(retained)).isError())
                assertEquals(0, headerWrites)
                assertEquals(prior, db.mealDao.getById(id.value))
                assertEquals(listOf(id), meals.observeAll().first().map { it.id })
            }
        } finally { db.close() }
        val reopened = Room.databaseBuilder(context, FoodYouDatabase::class.java, name).build()
        try {
            val preserved = RoomMealRepository(reopened, reopened.mealDao).observeAll().first().single()
            assertEquals("Synthetic finite prior", preserved.name)
            assertEquals("Keep", preserved.items.single().name)
            assertEquals(100.0, preserved.items.single().nutritionFacts.energy.value)
            assertNull(preserved.items.single().nutritionFacts.vitaminA.value)
            assertNull(preserved.items.single().nutritionFacts.cholesterol.value)
        } finally { reopened.close(); context.deleteDatabase(name) }
    }

    @Test fun intermediateUpdateFailureAndCancellationRollBackWholeAggregate() = runBlocking {
        val name = "story24-update-failure-" + UUID.randomUUID() + ".db"
        val db = Room.databaseBuilder(context, FoodYouDatabase::class.java, name).build()
        try {
            val meals = RoomMealRepository(db, db.mealDao)
            val facts = NutritionFacts(energy = NutrientValue.Complete(150.0))
            val id = meals.save("Synthetic prior", listOf(MealItem("Keep", facts), MealItem("Remove", facts)))
            val prior = meals.getMeal(id)!!
            val raw = db.mealDao.getById(id.value)
            val candidate = listOf(prior.items.first().copy(name = "Changed"), MealItem("New", facts))
            for (phase in listOf("header", "insert", "delete", "cancel")) {
                val dao = object : MealDao by db.mealDao {
                    override suspend fun updateMeal(meal: MealEntity) {
                        db.mealDao.updateMeal(meal)
                        if (phase == "header") error("Synthetic failure after header")
                    }
                    override suspend fun insertItems(items: List<MealItemEntity>) {
                        db.mealDao.insertItems(items)
                        if (phase == "insert") error("Synthetic failure after insert")
                    }
                    override suspend fun deleteItems(ids: List<Long>) {
                        db.mealDao.deleteItems(ids)
                        if (phase == "delete") error("Synthetic failure after delete")
                        if (phase == "cancel") throw CancellationException("Synthetic cancellation after writes")
                    }
                }
                val operation = UpdateMealUseCase(RoomMealRepository(db, dao))
                if (phase == "cancel") {
                    var cancelled = false
                    try { operation.update(id, "Changed", candidate) } catch (_: CancellationException) { cancelled = true }
                    assertTrue(cancelled)
                } else assertTrue(operation.update(id, "Changed", candidate).isError())
                assertEquals(phase, raw, db.mealDao.getById(id.value))
                assertEquals(prior, meals.getMeal(id))
            }
        } finally { db.close() }
        val reopened = Room.databaseBuilder(context, FoodYouDatabase::class.java, name).build()
        try {
            val meal = RoomMealRepository(reopened, reopened.mealDao).observeAll().first().single()
            assertEquals("Synthetic prior", meal.name)
            assertEquals(listOf("Keep", "Remove"), meal.items.map { it.name })
        } finally { reopened.close(); context.deleteDatabase(name) }
    }

    @Test fun atomicLoadFailureCancellationAndIntentionalRepeatedLoadsPreserveExistingDiary() = runBlocking {
        val name = "story24-load-" + UUID.randomUUID() + ".db"
        val db = Room.databaseBuilder(context, FoodYouDatabase::class.java, name).build()
        try {
            db.diarySlotDao.insertWithLastRank(DiarySlotEntity(2401, "Synthetic target", 0, 0, 23, 59))
            val meals = RoomMealRepository(db, db.mealDao)
            val manual = RoomManualDiaryEntryRepository(db.manualDiaryEntryDao)
            val slots = RoomDiarySlotRepository(db.diarySlotDao)
            val date = LocalDate(2026, 10, 25)
            val facts = NutritionFacts(energy = NutrientValue.Complete(400.0))
            manual.insert("Synthetic existing", 2401, date, facts, ProductTestClock.now())
            val id = meals.save("Synthetic load", listOf(
                MealItem("Recipe portion", facts, capturedPortion = Measurement.Serving(2.0)),
                MealItem("Quick Add", facts, servingCount = 2.5, weightGrams = 250.0),
            ))
            val prior = manual.observeAll(2401, date).first()
            val saved = meals.getMeal(id)
            for (cancel in listOf(false, true)) {
                var count = 0
                val failing = RoomManualDiaryEntryRepository(object : ManualDiaryEntryDao by db.manualDiaryEntryDao {
                    override suspend fun insert(entry: ManualDiaryEntryEntity): Long {
                        val inserted = db.manualDiaryEntryDao.insert(entry)
                        if (++count == 2) {
                            if (cancel) throw CancellationException("Synthetic load cancellation")
                            error("Synthetic second insert failure")
                        }
                        return inserted
                    }
                })
                val apply = ApplyMealUseCase(meals, failing, slots, db, ProductTestClock)
                if (cancel) {
                    var cancelled = false
                    try { apply.apply(id, 2401, date) } catch (_: CancellationException) { cancelled = true }
                    assertTrue(cancelled)
                } else assertTrue(apply.apply(id, 2401, date).isError())
                assertEquals(prior, manual.observeAll(2401, date).first())
                assertEquals(saved, meals.getMeal(id))
            }
            val apply = ApplyMealUseCase(meals, manual, slots, db, ProductTestClock)
            assertTrue(apply.apply(id, 9999, date).isError())
            assertEquals(prior, manual.observeAll(2401, date).first())
            assertTrue(apply.apply(id, 2401, date).isSuccess())
            assertTrue(apply.apply(id, 2401, date).isSuccess())
            val loaded = manual.observeAll(2401, date).first()
            assertEquals(5, loaded.size)
            assertEquals(2, loaded.count { it.name == "Recipe portion" && it.capturedPortion == Measurement.Serving(2.0) })
            assertTrue(loaded.filter { it.name == "Quick Add" }.all { it.nutritionFacts.energy.value == 400.0 && it.servingCount == 2.5 && it.weightGrams == 250.0 })
            meals.delete(id)
            assertTrue(apply.apply(id, 2401, date).isError())
            assertEquals(loaded, manual.observeAll(2401, date).first())
        } finally { db.close(); context.deleteDatabase(name) }
    }

    @Test fun suspendedSubmissionsCommitOnceAndNewDraftRowsKeepTheirKeys() = runBlocking {
        val name = "story24-guards-" + UUID.randomUUID() + ".db"
        val db = Room.databaseBuilder(context, FoodYouDatabase::class.java, name).build()
        val viewModels = mutableListOf<MealsViewModel>()
        try {
            db.diarySlotDao.insertWithLastRank(DiarySlotEntity(2401, "Synthetic guard slot", 0, 0, 23, 59))
            val meals = RoomMealRepository(db, db.mealDao)
            val manual = RoomManualDiaryEntryRepository(db.manualDiaryEntryDao)
            val slots = RoomDiarySlotRepository(db.diarySlotDao)
            val products = RoomProductRepository(db.productDao)
            val observe = ObserveNutritionalItemUseCase(products, RoomRecipeRepository(db.recipeDao, products))
            val facts = NutritionFacts(energy = NutrientValue.Complete(100.0))
            val id = meals.save("Synthetic guard prior", listOf(MealItem("First", facts), MealItem("Second", facts)))
            val prior = meals.getMeal(id)!!
            val saveEntered = CompletableDeferred<Unit>()
            val saveGate = CompletableDeferred<Unit>()
            val saveCalls = java.util.concurrent.atomic.AtomicInteger(0)
            val delayed = object : com.maksimowiczm.foodyou.fooddiary.domain.repository.MealRepository by meals {
                override suspend fun update(id: MealId, name: String, items: List<MealItem>): Boolean {
                    saveCalls.incrementAndGet()
                    saveEntered.complete(Unit)
                    saveGate.await()
                    return meals.update(id, name, items)
                }
            }
            val vm = MealsViewModel(meals, UpdateMealUseCase(delayed),
                ApplyMealUseCase(meals, manual, slots, db, ProductTestClock), slots, observe)
            viewModels += vm
            vm.edit(prior)
            vm.rename("Synthetic committed once")
            vm.save()
            withTimeout(10_000) { saveEntered.await() }
            vm.save()
            withContext(Dispatchers.Main) { Unit }
            assertEquals(1, saveCalls.get())
            assertEquals(prior, meals.getMeal(id))
            saveGate.complete(Unit)
            withTimeout(10_000) { vm.busy.first { !it } }
            assertNull(vm.editor.value)
            assertEquals("Synthetic committed once", meals.getMeal(id)!!.name)
            assertEquals(1, saveCalls.get())

            vm.edit(meals.getMeal(id)!!)
            val product = products.insertProduct("Synthetic draft source", null, null, null, false,
                null, null, ProductSource(ProductSource.Type.User), facts)
            suspend fun captureDraft() {
                val complete = CompletableDeferred<Unit>()
                vm.capture(product, Measurement.Gram(20.0)) { complete.complete(Unit) }
                withTimeout(10_000) { complete.await(); vm.busy.first { !it } }
            }
            captureDraft(); captureDraft()
            val beforeKeys = vm.editor.value!!.itemKeys
            assertEquals(4, beforeKeys.distinct().size)
            val lastKey = beforeKeys.last()
            vm.remove(2)
            assertEquals(lastKey, vm.editor.value!!.itemKeys.last())
            val lastItem = vm.editor.value!!.meal.items.last()
            vm.setItem(2, lastItem.copy(name = "Synthetic changed draft"))
            assertEquals(lastKey, vm.editor.value!!.itemKeys.last())
            vm.cancel()
            assertEquals("Draft additions never persist before Save", 2, meals.getMeal(id)!!.itemCount)

            val loadEntered = CompletableDeferred<Unit>()
            val loadGate = CompletableDeferred<Unit>()
            val insertCalls = java.util.concurrent.atomic.AtomicInteger(0)
            val completions = java.util.concurrent.atomic.AtomicInteger(0)
            val delayedManual = RoomManualDiaryEntryRepository(object : ManualDiaryEntryDao by db.manualDiaryEntryDao {
                override suspend fun insert(entry: ManualDiaryEntryEntity): Long {
                    if (insertCalls.incrementAndGet() == 1) {
                        loadEntered.complete(Unit)
                        loadGate.await()
                    }
                    return db.manualDiaryEntryDao.insert(entry)
                }
            })
            val loadTransactions = java.util.concurrent.atomic.AtomicInteger(0)
            val trackedTransactions = object : com.maksimowiczm.foodyou.common.domain.database.TransactionProvider {
                override suspend fun <T> withTransaction(block: suspend com.maksimowiczm.foodyou.common.domain.database.TransactionScope<T>.() -> T): T {
                    loadTransactions.incrementAndGet()
                    return db.withTransaction(block)
                }
            }
            val loadVm = MealsViewModel(meals, UpdateMealUseCase(meals),
                ApplyMealUseCase(meals, delayedManual, slots, trackedTransactions, ProductTestClock), slots, observe)
            viewModels += loadVm
            val date = LocalDate(2026, 10, 25)
            loadVm.load(id, 2401, date) { completions.incrementAndGet() }
            withTimeout(10_000) { loadEntered.await() }
            loadVm.load(id, 2401, date) { completions.incrementAndGet() }
            withContext(Dispatchers.Main) { Unit }
            assertEquals(1, loadTransactions.get())
            assertEquals(1, insertCalls.get())
            assertEquals(0, completions.get())
            loadGate.complete(Unit)
            withTimeout(10_000) { loadVm.busy.first { !it } }
            assertEquals(2, insertCalls.get())
            assertEquals(1, completions.get())
            assertEquals(2, manual.observeAll(2401, date).first().size)
        } finally {
            viewModels.forEach { it.viewModelScope.cancel() }
            db.close(); context.deleteDatabase(name)
        }
    }

    @Test fun malformedMealAndManualMetadataBecomeExplicitUnavailableAndRetryRecovers() = runBlocking {
        val name = "story24-read-" + UUID.randomUUID() + ".db"
        val db = Room.databaseBuilder(context, FoodYouDatabase::class.java, name).build()
        try {
            db.diarySlotDao.insertWithLastRank(DiarySlotEntity(2401, "Synthetic slot", 0, 0, 23, 59))
            val meals = RoomMealRepository(db, db.mealDao)
            val manual = RoomManualDiaryEntryRepository(db.manualDiaryEntryDao)
            val entries = RoomNutritionalItemDiaryEntryRepository(db, db.measurementDao)
            val slots = RoomDiarySlotRepository(db.diarySlotDao)
            val facts = NutritionFacts(energy = NutrientValue.Complete(100.0))
            val id = meals.save("Synthetic malformed", listOf(MealItem("Captured", facts, capturedPortion = Measurement.Serving(1.0))))
            val good = db.mealDao.getById(id.value)!!.items.single()
            db.mealDao.updateItems(listOf(good.copy(capturedPortionValue = null)))
            val products = RoomProductRepository(db.productDao)
            val observe = ObserveNutritionalItemUseCase(products, RoomRecipeRepository(db.recipeDao, products))
            val library = MealsViewModel(meals, UpdateMealUseCase(meals), ApplyMealUseCase(meals, manual, slots, db, ProductTestClock), slots, observe)
            val collector = launch { library.meals.collect() }
            withTimeout(10_000) { library.readFailed.first { it } }
            assertNull(library.meals.value)
            assertEquals(good.copy(capturedPortionValue = null), db.mealDao.getById(id.value)!!.items.single())
            db.mealDao.updateItems(listOf(good))
            library.retryRead()
            withTimeout(10_000) { library.meals.filterNotNull().first() }
            assertFalse(library.readFailed.value)
            collector.cancelAndJoin()
            library.viewModelScope.cancel()

            val prefs = object : UserPreferencesRepository<DiarySlotsPreferences> {
                private val state = MutableStateFlow(DiarySlotsPreferences(DiarySlotsCardsLayout.Vertical, false, false))
                override fun observe() = state
                override suspend fun update(transform: DiarySlotsPreferences.() -> DiarySlotsPreferences) { state.value = transform(state.value) }
            }
            val manualId = manual.insert("Synthetic malformed manual", 2401, LocalDate(2026, 10, 25), facts, ProductTestClock.now(), capturedPortion = Measurement.Milliliter(123.0))
            val goodManual = db.manualDiaryEntryDao.observe(manualId.value).first()!!
            db.manualDiaryEntryDao.update(goodManual.copy(capturedPortionValue = Double.NaN))
            val home = DiarySlotsCardsViewModel(ObserveDiarySlotEntriesUseCase(slots, prefs, entries, manual, ProductTestClock), entries, manual,
                SaveMealUseCase(entries, manual, meals, db), ApplyMealUseCase(meals, manual, slots, db, ProductTestClock), meals, prefs)
            home.setDate(LocalDate(2026, 10, 25))
            val homeCollector = launch { home.diarySlotEntries.collect() }
            withTimeout(10_000) { home.readFailed.first { it } }
            assertNull(home.diarySlotEntries.value)
            db.manualDiaryEntryDao.update(goodManual)
            home.retryRead()
            withTimeout(10_000) { home.diarySlotEntries.filterNotNull().first() }
            assertFalse(home.readFailed.value)
            homeCollector.cancelAndJoin()
            home.viewModelScope.cancel()
        } finally { db.close(); context.deleteDatabase(name) }
    }
}
