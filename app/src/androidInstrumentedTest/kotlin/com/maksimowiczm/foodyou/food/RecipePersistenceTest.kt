package com.maksimowiczm.foodyou.food

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.platform.app.InstrumentationRegistry
import com.maksimowiczm.foodyou.app.infrastructure.room.FoodYouDatabase
import com.maksimowiczm.foodyou.app.infrastructure.room.migration.RecipeSnapshotCalculationMigration
import com.maksimowiczm.foodyou.app.infrastructure.room.migration.MealPortionMetadataMigration
import com.maksimowiczm.foodyou.app.ui.food.diary.add.toDiaryFood
import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.common.domain.measurement.*
import com.maksimowiczm.foodyou.common.result.*
import com.maksimowiczm.foodyou.food.domain.entity.*
import com.maksimowiczm.foodyou.food.domain.repository.RecipeRepository
import com.maksimowiczm.foodyou.food.domain.usecase.*
import com.maksimowiczm.foodyou.food.infrastructure.repository.*
import com.maksimowiczm.foodyou.fooddiary.domain.entity.*
import com.maksimowiczm.foodyou.fooddiary.domain.repository.NutritionalItemDiaryEntryRepository
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.*
import com.maksimowiczm.foodyou.fooddiary.infrastructure.repository.*
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.DiarySlotEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.*
import java.util.UUID
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Private synthetic databases only; never changes the canonical compatibility fixture. */
class RecipePersistenceTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val migrationName = "story27-migration.db"
    @get:Rule val helper = MigrationTestHelper(instrumentation = instrumentation, file = context.getDatabasePath(migrationName), driver = AndroidSQLiteDriver(), databaseClass = FoodYouDatabase::class)

    @Test fun schema39MigrationPreservesLegacyCalculationsAndCopyWhileNewCapturesUseCurrentContract() = runBlocking {
        helper.createDatabase(39).use { old ->
            old.execSQL("INSERT INTO Meal(id,name,fromHour,fromMinute,toHour,toMinute,rank) VALUES(2701,'Synthetic legacy slot',0,0,23,59,0)")
            old.execSQL("INSERT INTO DiaryProduct(id,name,isLiquid,sourceType,energy,proteins,note) VALUES(2701,'Synthetic solid',0,0,100,10,'solid note')")
            old.execSQL("INSERT INTO DiaryProduct(id,name,isLiquid,sourceType,energy,proteins,note) VALUES(2702,'Synthetic liquid',1,0,200,NULL,'liquid note')")
            old.execSQL("INSERT INTO DiaryRecipe(id,name,servings,isLiquid,note) VALUES(2701,'Synthetic legacy mixed',1,0,'recipe note')")
            old.execSQL("INSERT INTO DiaryRecipe(id,name,servings,isLiquid,note) VALUES(2702,'Synthetic legacy nested',1,0,'nested note')")
            old.execSQL("INSERT INTO DiaryRecipeIngredient(id,recipeId,ingredientProductId,measurement,quantity) VALUES(2701,2701,2701,0,100)")
            old.execSQL("INSERT INTO DiaryRecipeIngredient(id,recipeId,ingredientProductId,measurement,quantity) VALUES(2702,2701,2702,3,100)")
            old.execSQL("INSERT INTO DiaryRecipeIngredient(id,recipeId,ingredientRecipeId,measurement,quantity) VALUES(2703,2702,2701,2,1)")
            old.execSQL("INSERT INTO Measurement(id,mealId,epochDay,recipeId,measurement,quantity,createdAt,updatedAt) VALUES(2701,2701,0,2701,0,100,1,2)")
            old.execSQL("INSERT INTO Measurement(id,mealId,epochDay,recipeId,measurement,quantity,createdAt,updatedAt) VALUES(2702,2701,0,2702,2,1,1,2)")
        }
        helper.runMigrationsAndValidate(40, listOf(RecipeSnapshotCalculationMigration)).use { migrated ->
            migrated.prepare("SELECT sql FROM sqlite_master WHERE name = 'RecipeAllIngredientsView'").use { view ->
                assertTrue(view.step())
                val exported = org.json.JSONObject(instrumentation.context.assets.open(
                    "com.maksimowiczm.foodyou.app.infrastructure.room.FoodYouDatabase/40.json"
                ).bufferedReader().use { it.readText() }).getJSONObject("database").getJSONArray("views")
                val expectedView = (0 until exported.length()).map { exported.getJSONObject(it) }
                    .single { it.getString("viewName") == "RecipeAllIngredientsView" }
                    .getString("createSql").replace("$" + "{VIEW_NAME}", "RecipeAllIngredientsView")
                assertEquals("Migrated SQL exactly matches the independently exported Room40 schema", expectedView, view.getText(0))
            }
            migrated.prepare("SELECT id,name,servings,isLiquid,note,calculationVersion FROM DiaryRecipe ORDER BY id").use { rows ->
                assertTrue(rows.step()); assertEquals(2701L, rows.getLong(0)); assertEquals("Synthetic legacy mixed", rows.getText(1))
                assertEquals(1L, rows.getLong(2)); assertEquals(0L, rows.getLong(3)); assertEquals("recipe note", rows.getText(4)); assertEquals(0L, rows.getLong(5))
                assertTrue(rows.step()); assertEquals(2702L, rows.getLong(0)); assertEquals(0L, rows.getLong(5)); assertFalse(rows.step())
            }
        }
        var db = Room.databaseBuilder(context, FoodYouDatabase::class.java, migrationName).addMigrations(RecipeSnapshotCalculationMigration, MealPortionMetadataMigration).build()
        try {
            var entries = RoomNutritionalItemDiaryEntryRepository(db, db.measurementDao)
            val legacy = entries.observe(NutritionalItemDiaryEntryId(2701)).first()!!
            assertEquals(150.0, legacy.nutritionFacts.energy.value)
            assertEquals(5.0, legacy.nutritionFacts.proteins.value)
            val nested = entries.observe(NutritionalItemDiaryEntryId(2702)).first()!!
            assertEquals(10.0, nested.nutritionFacts.proteins.value)
            assertEquals(0, (legacy.food as RecipeSnapshot).calculationVersion)
            val copy = entries.insert(legacy.measurement, 2701, LocalDate.fromEpochDays(1), legacy.food, legacy.createdAt)
            assertEquals(legacy.food, entries.observe(copy).first()!!.food)
            val current = legacy.food.copy(calculationVersion = 1, servings = 3)
            assertNull(current.totalWeight)
            assertNull(current.nutritionFor(Measurement.Gram(100.0)))
            assertNull(current.nutritionFor(Measurement.Serving(2.0))!!.proteins.value)
            val slots = RoomDiarySlotRepository(db.diarySlotDao)
            val log = CreateNutritionalItemDiaryEntryUseCase(slots, entries, db, ProductTestClock, ProductTestLogger)
            val fresh = log.createDiaryEntry(Measurement.Serving(2.0), 2701, LocalDate.fromEpochDays(2), current).fold({ it }, { error("Current mixed serving failed") })
            assertEquals(200.0, entries.observe(fresh).first()!!.nutritionFacts.energy.value!!, 0.000001)
            db.close()
            db = Room.databaseBuilder(context, FoodYouDatabase::class.java, migrationName).build()
            entries = RoomNutritionalItemDiaryEntryRepository(db, db.measurementDao)
            assertEquals(legacy, entries.observe(legacy.id).first())
            assertEquals(nested, entries.observe(nested.id).first())
            assertEquals(0, (entries.observe(copy).first()!!.food as RecipeSnapshot).calculationVersion)
            assertEquals(1, (entries.observe(fresh).first()!!.food as RecipeSnapshot).calculationVersion)
            assertEquals(200.0, entries.observe(fresh).first()!!.nutritionFacts.energy.value!!, 0.000001)
        } finally { db.close(); context.deleteDatabase(migrationName) }
    }

    @Test fun exclusionDaoTerminatesLegacyCyclesAndDoesNotConfuseProductAndRecipeIds() = runBlocking {
        val name = "story27-graph-" + UUID.randomUUID() + ".db"
        val db = Room.databaseBuilder(context,FoodYouDatabase::class.java,name).build()
        try {
            val products = RoomProductRepository(db.productDao)
            val recipes = RoomRecipeRepository(db.recipeDao,products)
            val productId = products.insertProduct("Synthetic overlap",null,null,null,false,null,null,ProductSource(ProductSource.Type.User),NutritionFacts())
            val product = products.observeProduct(productId).first()!!
            suspend fun insert(label: String) = recipes.insertRecipe(label,1,null,false,listOf(RecipeIngredient(product,Measurement.Gram(100.0))))
            val edited = insert("Synthetic edited")
            val second = insert("Synthetic cycle second")
            val third = insert("Synthetic cycle third")
            val valid = insert("Synthetic unrelated valid")
            assertEquals(productId.id,edited.id)
            val secondItem = recipes.observeRecipe(second).first()!!
            val thirdItem = recipes.observeRecipe(third).first()!!
            // Bypass validated use cases deliberately to reproduce malformed legacy graph rows.
            recipes.updateRecipe(secondItem.copy(ingredients = listOf(RecipeIngredient(thirdItem,Measurement.Serving(1.0)))))
            recipes.updateRecipe(thirdItem.copy(ingredients = listOf(RecipeIngredient(secondItem,Measurement.Serving(1.0)))))
            val source = com.maksimowiczm.foodyou.common.infrastructure.room.ProductSourceType.User
            kotlinx.coroutines.withTimeout(5000) {
                assertEquals(4,db.foodSearchDao.observeFoodCount(source,edited.id).first())
                val page = db.foodSearchDao.observeFood(source,edited.id).load(androidx.paging.PagingSource.LoadParams.Refresh(null,20,false))
                when (page) {
                    is androidx.paging.PagingSource.LoadResult.Page -> {
                        assertTrue(page.data.any { it.recipeId == valid.id })
                        assertFalse(page.data.any { it.recipeId == edited.id })
                    }
                    else -> fail("Expected successful isolated exclusion DAO page")
                }
                assertNull(recipes.observeRecipe(second).first())
            }
        } finally { db.close(); context.deleteDatabase(name) }
    }

    @Test fun definitionsFailuresCyclesSnapshotsMealsAndAtomicUnpackUseRealRoomTransactions() = runBlocking {
        val name = "story27-" + UUID.randomUUID() + ".db"
        var db = Room.databaseBuilder(context, FoodYouDatabase::class.java, name).build()
        try {
            val products = RoomProductRepository(db.productDao)
            val recipes = RoomRecipeRepository(db.recipeDao, products)
            val history = RoomNutritionalItemHistoryRepository(db.foodEventDao)
            val entries = RoomNutritionalItemDiaryEntryRepository(db, db.measurementDao)
            val slots = RoomDiarySlotRepository(db.diarySlotDao)
            val date = LocalDate(2026, 10, 25)
            db.diarySlotDao.insertWithLastRank(DiarySlotEntity(2701, "Synthetic Recipe slot",0,0,23,59))
            val facts = NutritionFacts(energy = NutrientValue.Complete(200.0), proteins = NutrientValue.Complete(10.0))
            val a = products.insertProduct("Synthetic A", null,null,"A note",false,null,null,ProductSource(ProductSource.Type.User),facts)
            val b = products.insertProduct("Synthetic B", null,null,"B note",false,null,null,ProductSource(ProductSource.Type.User),facts)
            val ingredients = listOf(a to Measurement.Gram(100.0), b to Measurement.Gram(200.0))
            val create = CreateRecipeUseCase(recipes, products, history, db, ProductTestLogger)
            suspend fun createWith(yield: Int = 3, amounts: List<Pair<NutritionalItemId,Measurement>> = ingredients) = create.create("Synthetic Recipe",yield,"Recipe note",false,amounts,NutritionalItemHistory.Created(ProductTestClock.nowInstant()))
            assertTrue(createWith(0).isError())
            for (bad in listOf(0.0,-1.0,Double.NaN,Double.POSITIVE_INFINITY,Double.MAX_VALUE)) assertTrue(createWith(amounts = listOf(a to Measurement.Gram(bad))).isError())
            var failedId: NutritionalItemId? = null
            val persistedHistory = history
            val failingHistory = object : com.maksimowiczm.foodyou.food.domain.repository.NutritionalItemHistoryRepository by persistedHistory {
                override suspend fun insert(foodId: NutritionalItemId, history: NutritionalItemHistory) {
                    failedId = foodId
                    persistedHistory.insert(foodId,history)
                    error("Synthetic creation-history failure after write")
                }
            }
            val failedCreate = CreateRecipeUseCase(recipes,products,failingHistory,db,ProductTestLogger)
            assertTrue(failedCreate.create("Synthetic failed creation",3,"note",false,ingredients,NutritionalItemHistory.Created(ProductTestClock.nowInstant())).isError())
            assertNull(recipes.observeRecipe(failedId as NutritionalItemId.Recipe).first())
            assertTrue(history.observeFoodHistory(requireNotNull(failedId)).first().isEmpty())
            val id = createWith().fold({ it }, { error("Synthetic Recipe failed") })
            val original = recipes.observeRecipe(id).first()!!
            val update = UpdateRecipeUseCase(recipes, products, db, ProductTestLogger)
            assertTrue(update.update(id,"Bad self",3,null,false,listOf(id to Measurement.Serving(1.0))).isError())
            val parent = recipes.insertRecipe("Synthetic parent",1,null,false,listOf(RecipeIngredient(original, Measurement.Serving(1.0))))
            assertTrue(update.update(id,"Bad cycle",3,null,false,listOf(parent to Measurement.Serving(1.0))).isError())
            assertEquals(original, recipes.observeRecipe(id).first())
            val failedRepository = object : RecipeRepository by recipes {
                override suspend fun updateRecipe(recipe: Recipe) { recipes.updateRecipe(recipe); error("Synthetic persistence failure") }
            }
            assertTrue(UpdateRecipeUseCase(failedRepository, products, db, ProductTestLogger).update(id,"Failed",4,"Changed",true,ingredients).isError())
            assertEquals(original, recipes.observeRecipe(id).first())
            val cancelledRepository = object : RecipeRepository by recipes {
                override suspend fun updateRecipe(recipe: Recipe) { recipes.updateRecipe(recipe); throw CancellationException("Synthetic cancellation") }
            }
            try { UpdateRecipeUseCase(cancelledRepository, products, db, ProductTestLogger).update(id,"Cancelled",4,null,false,ingredients); fail("Cancellation must propagate") } catch (_: CancellationException) { }
            assertEquals(original, recipes.observeRecipe(id).first())
            val snapshot = original.toDiaryFood() as RecipeSnapshot
            val log = CreateNutritionalItemDiaryEntryUseCase(slots, entries, db, ProductTestClock, ProductTestLogger)
            val logged = log.createDiaryEntry(Measurement.Serving(2.0),2701,date,snapshot).fold({ it }, { error("Recipe log failed") })
            val captured = entries.observe(logged).first()!!
            assertEquals(400.0,captured.nutritionFacts.energy.value!!,0.000001)
            assertEquals(1,entries.observeAll(2701,date).first().size)
            val manual = RoomManualDiaryEntryRepository(db.manualDiaryEntryDao)
            val meals = RoomMealRepository(db, db.mealDao)
            manual.insert("Synthetic extra",2701,date,facts,ProductTestClock.now())
            val meal = SaveMealUseCase(entries,manual,meals,db).save("Synthetic bundle",2701,date).fold({ it }, { error("Meal save failed") })
            assertEquals(2,meals.getMeal(meal)!!.items.size)
            assertTrue(ApplyMealUseCase(meals,manual,slots,db,ProductTestClock).apply(meal,2701,LocalDate(2026,10,26)).isSuccess())
            assertEquals(listOf(200.0,400.0),manual.observeAll(2701,LocalDate(2026,10,26)).first().map { it.nutritionFacts.energy.value }.sortedBy { it })
            val failureDate = LocalDate(2026,10,27)
            suspend fun failUnpack(replace: Boolean, cancel: Boolean = false) {
                var inserts = 0
                val failing = object : NutritionalItemDiaryEntryRepository by entries {
                    override suspend fun insert(measurement: Measurement,diarySlotId: Long,date: LocalDate,food: NutritionalItemSnapshot,createdAt: LocalDateTime): NutritionalItemDiaryEntryId {
                        val result = entries.insert(measurement,diarySlotId,date,food,createdAt)
                        if (++inserts == 2) { if (cancel) throw CancellationException("Synthetic cancel") else error("Synthetic second insert failure") }
                        return result
                    }
                }
                val unpack = UnpackNutritionalItemDiaryEntryUseCase(failing,slots,db,ProductTestClock,ProductTestLogger)
                if (cancel) {
                    try { unpack.unpack(logged,Measurement.Serving(2.0),2701,date); fail("Cancellation must propagate") } catch (_: CancellationException) { }
                } else assertTrue((if(replace) unpack.unpack(logged,Measurement.Serving(2.0),2701,date) else unpack.append(snapshot,Measurement.Serving(2.0),2701,failureDate)).isError())
                assertEquals(captured,entries.observe(logged).first())
                assertTrue(entries.observeAll(2701,failureDate).first().isEmpty())
                assertEquals(1,entries.observeAll(2701,date).first().size)
            }
            failUnpack(false); failUnpack(true); failUnpack(true,true)
            val unpack = UnpackNutritionalItemDiaryEntryUseCase(entries,slots,db,ProductTestClock,ProductTestLogger)
            assertTrue(unpack.append(snapshot,Measurement.Serving(2.0),2701,failureDate).isSuccess())
            val constituents = entries.observeAll(2701,failureDate).first()
            assertEquals(2,constituents.size)
            assertEquals(400.0,constituents.sumOf { it.nutritionFacts.energy.value!! },0.000001)
            val amounts = constituents.map { (it.measurement as Measurement.Gram).value }.sorted()
            assertEquals(200.0/3,amounts[0],0.000001); assertEquals(400.0/3,amounts[1],0.000001)
            assertTrue(update.update(id,"Synthetic future",6,"Future note",false,listOf(a to Measurement.Gram(50.0))).isSuccess())
            assertEquals(captured,entries.observe(logged).first())
            recipes.deleteRecipe(recipes.observeRecipe(id).first()!!)
            products.deleteProduct(products.observeProduct(a).first()!!)
            assertEquals(captured,entries.observe(logged).first())
            db.close(); db = Room.databaseBuilder(context,FoodYouDatabase::class.java,name).build()
            val reopened = RoomNutritionalItemDiaryEntryRepository(db,db.measurementDao)
            assertEquals(captured,reopened.observe(logged).first())
            val replacement = UnpackNutritionalItemDiaryEntryUseCase(reopened,RoomDiarySlotRepository(db.diarySlotDao),db,ProductTestClock,ProductTestLogger)
            assertTrue(replacement.unpack(logged,Measurement.Serving(2.0),2701,date).isSuccess())
            assertNull(reopened.observe(logged).first())
            val replaced = reopened.observeAll(2701,date).first()
            assertEquals(2,replaced.size)
            assertEquals(400.0,replaced.sumOf { it.nutritionFacts.energy.value!! },0.000001)
        } finally { db.close(); context.deleteDatabase(name) }
    }
}
