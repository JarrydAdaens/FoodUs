package com.maksimowiczm.foodyou.fooddiary

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.platform.app.InstrumentationRegistry
import com.maksimowiczm.foodyou.app.infrastructure.room.FoodYouDatabase
import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementType
import com.maksimowiczm.foodyou.common.infrastructure.room.*
import com.maksimowiczm.foodyou.common.result.isSuccess
import com.maksimowiczm.foodyou.fooddiary.domain.entity.*
import com.maksimowiczm.foodyou.fooddiary.domain.repository.*
import com.maksimowiczm.foodyou.fooddiary.infrastructure.repository.*
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.util.UUID

/** Raw-row assertions protect historical unknowns, timestamps and independent snapshot ownership. */
class DiaryBulkPersistenceTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val source = DiaryEndpoint(LocalDate(2020, 2, 3), 3901)
    private val target = DiaryEndpoint(LocalDate(2030, 4, 5), 3902)
    private val migrationName = "story39-migration.db"
    @get:Rule val migration = MigrationTestHelper(instrumentation, context.getDatabasePath(migrationName), AndroidSQLiteDriver(), FoodYouDatabase::class)
    private val facts = NutritionFacts().mapMealNutrition { field, _ ->
        if (field.ordinal % 4 == 0) NutrientValue.Incomplete(null) else if (field.ordinal % 4 == 1) NutrientValue.Incomplete(field.ordinal + 0.125) else NutrientValue.Complete(field.ordinal + 0.125)
    }
    private val portion = PortionDescription(1.25, "Synthetic bowl", "Synthetic context", 231.125, MetricUnit.GRAM)
    private val metadata = NutritionMetadata("Synthetic note", "Synthetic provenance", mapOf("Unknown" to 12.75),
        listOf(NutritionObservation("Synthetic observation", 4.25, "Synthetic unit", "Synthetic text")), true,
        DefinitionProvenance(DefinitionKind.PRODUCT, "Synthetic namespace", "Synthetic external"))

    @Test fun copiesEveryRawKindAndGraphIndependentlyAndPreservesAppendOrderAcrossRestart() = runBlocking<Unit> {
        fixture { db, repo, name ->
            val expected = normalized(db, source)
            val prior = normalized(db, target)
            val ids = rows(db, source).map { it.first }
            val preview = repo.prepare(source)
            assertEquals(7, preview.count)
            repo.execute(DiaryBulkAction.Copy, preview, target)
            assertEquals(expected, normalized(db, source))
            assertEquals(prior + expected, normalized(db, target))
            assertEquals(8, rows(db, target).size)
            val cloned = db.measurementDao.entries(target.slotId, target.date.toEpochDays())
            val original = db.measurementDao.entries(source.slotId, source.date.toEpochDays())
            assertTrue(cloned.none { copy -> original.any { it.id == copy.id || it.productId != null && it.productId == copy.productId || it.recipeId != null && it.recipeId == copy.recipeId } })
            db.close()
            val reopened = Room.databaseBuilder(context, FoodYouDatabase::class.java, name).build()
            try {
                assertEquals(prior + expected, normalized(reopened, target))
                val destination = rows(reopened, target).map { it.first }
                assertEquals(destination.size, destination.distinct().size)
                val manual = reopened.manualDiaryEntryDao.entries(target.slotId, target.date.toEpochDays()).last()
                reopened.manualDiaryEntryDao.update(manual.copy(name = "A renamed after append"))
                assertEquals(destination, rows(reopened, target).map { it.first })
                val newEntry = manual.copy(id = 0, name = "A newest", sortOrder = null)
                val appended = reopened.manualDiaryEntryDao.insert(newEntry)
                assertEquals("manual:$appended", rows(reopened, target).last().first)
                val movedRepo = repository(reopened)
                movedRepo.execute(DiaryBulkAction.Delete, movedRepo.prepare(target))
                assertEquals(expected, normalized(reopened, source))
                assertEquals(ids, rows(reopened, source).map { it.first })
                assertTrue(rows(reopened, target).isEmpty())
                assertTrue(reopened.diarySlotDao.exists(3901)); assertTrue(reopened.diarySlotDao.exists(3902))
            } finally { reopened.close() }
        }
    }

    @Test fun moveAndCopySupportSlotDayAndCombinedEndpointsAndRetainMoveIdentities() = runBlocking<Unit> {
        for (action in listOf(DiaryBulkAction.Move, DiaryBulkAction.Copy)) {
            for (destination in listOf(source.copy(slotId = 3902), source.copy(date = target.date), target)) {
                fixture { db, repo, _ ->
                    val before = normalized(db, source)
                    val prior = normalized(db, destination)
                    val ids = rows(db, source).map { it.first }
                    repo.execute(action, repo.prepare(source), destination)
                    assertEquals(prior + before, normalized(db, destination))
                    if (action == DiaryBulkAction.Move) {
                        assertTrue(rows(db, source).isEmpty())
                        assertEquals(ids, rows(db, destination).takeLast(ids.size).map { it.first })
                    } else assertEquals(before, normalized(db, source))
                }
            }
        }
    }

    @Test fun staleAddDeleteEditSnapshotAndReassignmentRejectBeforeAnyMutation() = runBlocking<Unit> {
        for (change in 0..4) fixture { db, repo, _ ->
            val preview = repo.prepare(source)
            val manual = db.manualDiaryEntryDao.entries(source.slotId, source.date.toEpochDays()).first()
            when (change) {
                0 -> db.manualDiaryEntryDao.insert(manual.copy(id = 0))
                1 -> db.manualDiaryEntryDao.delete(manual.id)
                2 -> db.manualDiaryEntryDao.update(manual.copy(description = "Changed in same second"))
                3 -> {
                    val row = db.measurementDao.entries(source.slotId, source.date.toEpochDays()).first { it.productId != null }
                    val product = db.measurementDao.product(row.productId!!)!!
                    sql(db) { execSQL("UPDATE DiaryProduct SET note='Changed raw snapshot' WHERE id=" + product.id) }
                }
                4 -> db.manualDiaryEntryDao.update(manual.copy(diarySlotId = 3902))
            }
            val before = normalized(db, source); val prior = normalized(db, target)
            rejected(DiaryBulkError.SourceChanged) { repo.execute(DiaryBulkAction.Copy, preview, target) }
            assertEquals(before, normalized(db, source)); assertEquals(prior, normalized(db, target))
        }
    }

    @Test fun sameEmptyDeletedAndCorruptSourcesAreRejectedWithoutWrites() = runBlocking<Unit> {
        fixture { db, repo, _ ->
            rejected(DiaryBulkError.SameEndpoint) { repo.execute(DiaryBulkAction.Move, repo.prepare(source), source) }
            val empty = source.copy(date = LocalDate(1999, 1, 1))
            rejected(DiaryBulkError.EmptySource) { repo.execute(DiaryBulkAction.Copy, repo.prepare(empty), target) }
            val preview = repo.prepare(source)
            db.diarySlotDao.delete(target.slotId)
            rejected(DiaryBulkError.SlotDeleted) { repo.execute(DiaryBulkAction.Copy, preview, target) }
            val row = db.measurementDao.entries(source.slotId, source.date.toEpochDays()).first()
            db.measurementDao.updateMeasurement(row.copy(productId = 999999, recipeId = null))
            rejected(DiaryBulkError.StorageFailure) { repo.prepare(source) }
        }
    }

    @Test fun eachActionRollsBackRealSqlFailureAfterEarlierWritesIncludingOrderingAndClones() = runBlocking<Unit> {
        for (action in DiaryBulkAction.entries) fixture { db, repo, _ ->
            val preview = repo.prepare(source)
            val before = rawDatabase(db)
            val day = target.date.toEpochDays()
            // A source ordered as A manual, B product, C recipe... guarantees a prior write.
            val trigger = when (action) {
                DiaryBulkAction.Copy -> "CREATE TRIGGER story39_abort BEFORE INSERT ON Measurement WHEN NEW.mealId=3902 AND NEW.epochDay=$day BEGIN SELECT RAISE(ABORT,'Synthetic later write'); END"
                DiaryBulkAction.Move -> "CREATE TRIGGER story39_abort BEFORE UPDATE ON Measurement WHEN NEW.mealId=3902 AND NEW.epochDay=$day BEGIN SELECT RAISE(ABORT,'Synthetic later write'); END"
                DiaryBulkAction.Delete -> "CREATE TRIGGER story39_abort BEFORE DELETE ON Measurement BEGIN SELECT RAISE(ABORT,'Synthetic later write'); END"
            }
            sql(db) { execSQL(trigger) }
            rejected(DiaryBulkError.StorageFailure) { repo.execute(action, preview, target) }
            assertEquals(action.name, before, rawDatabase(db))
            sql(db) { execSQL("DROP TRIGGER story39_abort") }
            repo.execute(action, repo.prepare(source), target)
        }
    }

    @Test fun coroutineCancellationAfterARealWriteRollsBackAndPropagates() = runBlocking<Unit> {
        fixture { db, _, _ ->
            val dao = object : ManualDiaryEntryDao by db.manualDiaryEntryDao {
                override suspend fun update(entry: ManualDiaryEntryEntity) {
                    db.manualDiaryEntryDao.update(entry)
                    throw CancellationException("Synthetic cancellation after write")
                }
            }
            val repo = RoomDiaryBulkRepository(db, db.measurementDao, dao, db.diarySlotDao)
            val before = rawDatabase(db)
            var cancelled = false
            try { repo.execute(DiaryBulkAction.Move, repo.prepare(source), target) }
            catch (_: CancellationException) { cancelled = true }
            assertTrue(cancelled); assertEquals(before, rawDatabase(db))
        }
    }

    @Test fun deleteOnlyTouchesSelectedDateSlotAndKeepsReferencedSnapshotRoots() = runBlocking<Unit> {
        fixture { db, repo, _ ->
            val root = db.measurementDao.entries(source.slotId, source.date.toEpochDays()).first { it.productId != null }
            db.measurementDao.insertMeasurement(root.copy(id = 0, diarySlotId = target.slotId, epochDay = target.date.toEpochDays()))
            val prior = normalized(db, target)
            val product = db.measurementDao.product(root.productId!!)!!
            repo.execute(DiaryBulkAction.Delete, repo.prepare(source))
            assertEquals(prior, normalized(db, target)); assertEquals(product, db.measurementDao.product(root.productId))
            assertTrue(rows(db, source).isEmpty())
            assertEquals(2, db.diarySlotDao.observeDiarySlots().first().size)
        }
    }

    @Test fun registered42To43MigrationPreservesEveryOriginalColumnAndAddsNullOrder() = runBlocking<Unit> {
        migration.createDatabase(42).use {
            it.execSQL("INSERT INTO meal(id,name,fromHour,fromMinute,toHour,toMinute,rank) VALUES(3901,'Synthetic historical',0,0,23,59,0)")
            it.execSQL("INSERT INTO ManualDiaryEntry(id,mealId,dateEpochDay,name,createdEpochSeconds,updatedEpochSeconds,isPlaceholder,description,energy) VALUES(3901,3901,0,'Synthetic placeholder',1,2,1,'Synthetic note',NULL)")
            it.execSQL("INSERT INTO DiaryProduct(id,name,isLiquid,sourceType,energy) VALUES(3901,'Synthetic captured',0,0,123.125)")
            it.execSQL("INSERT INTO Measurement(id,mealId,epochDay,productId,measurement,quantity,createdAt,updatedAt) VALUES(3901,3901,0,3901,0,125.5,1,2)")
        }
        val originalRows = migrationProjection()
        val db = Room.databaseBuilder(context, FoodYouDatabase::class.java, migrationName).build()
        try {
            val manual = db.manualDiaryEntryDao.entries(3901, 0).single()
            val food = db.measurementDao.entries(3901, 0).single()
            assertNull(manual.sortOrder); assertNull(food.sortOrder)
            assertEquals(3901L, manual.id); assertEquals("Synthetic note", manual.description)
            assertTrue(manual.isPlaceholder); assertNull(manual.nutrients.energy)
            assertEquals(125.5, food.quantity, 0.0); assertEquals(1L, food.createdAt); assertEquals(2L, food.updatedAt)
            assertEquals(123.125, db.measurementDao.product(3901)!!.nutrients.energy)
            sql(db) { rawQuery("PRAGMA foreign_key_check", null).use { assertEquals(0, it.count) } }
            assertEquals("Every original row, column, value and null survives registered migration", originalRows, migrationProjection())
        } finally { db.close() }
        migration.runMigrationsAndValidate(43, emptyList()).close()
        context.deleteDatabase(migrationName)
    }

    @Test fun ordinaryProductManualMealAndSoleRecipeUnpackAppendAfterBulkOrdering() = runBlocking<Unit> {
        fixture { db, repo, _ ->
            val entries = RoomNutritionalItemDiaryEntryRepository(db, db.measurementDao)
            val manual = RoomManualDiaryEntryRepository(db.manualDiaryEntryDao)
            val slots = RoomDiarySlotRepository(db.diarySlotDao)
            val meals = RoomMealRepository(db, db.mealDao)
            val clock = com.maksimowiczm.foodyou.food.ProductTestClock
            val f = NutritionFacts(energy = NutrientValue.Complete(100.0))
            val product = ProductSnapshot("A new Product", f, 100.0, 200.0, false, ProductSource(ProductSource.Type.User), null)
            repo.execute(DiaryBulkAction.Copy, repo.prepare(source), target)
            val before = rows(db, target).map { it.first }
            val productId = entries.insert(com.maksimowiczm.foodyou.common.domain.measurement.Measurement.Gram(100.0), target.slotId, target.date, product, clock.now())
            val quick = manual.insert("A Quick Add", target.slotId, target.date, f, clock.now())
            val placeholder = manual.insert("A placeholder", target.slotId, target.date, NutritionFacts(), clock.now(), isPlaceholder = true)
            val meal = meals.save("Synthetic append Meal", listOf(MealItem("A loaded Meal", f)))
            assertTrue(com.maksimowiczm.foodyou.fooddiary.domain.usecase.ApplyMealUseCase(meals, manual, slots, db, clock).apply(meal, target.slotId, target.date).isSuccess())
            val after = rows(db, target).map { it.first }
            assertEquals(before, after.take(before.size))
            assertEquals(listOf("food:${productId.value}", "manual:${quick.value}", "manual:${placeholder.value}"), after.drop(before.size).take(3))
            val dated = source.copy(date = LocalDate(2001, 1, 1))
            val recipe = RecipeSnapshot("Synthetic sole ordered Recipe", 1,
                listOf(RecipeIngredientSnapshot(product.copy(name = "Z first constituent"), com.maksimowiczm.foodyou.common.domain.measurement.Measurement.Gram(100.0)),
                    RecipeIngredientSnapshot(product.copy(name = "A second constituent"), com.maksimowiczm.foodyou.common.domain.measurement.Measurement.Gram(100.0))), false, null)
            val id = entries.insert(com.maksimowiczm.foodyou.common.domain.measurement.Measurement.Serving(1.0), dated.slotId, dated.date, recipe, clock.now())
            val raw = db.measurementDao.observeMeasurementById(id.value).first()!!
            db.measurementDao.updateMeasurement(raw.copy(sortOrder = 0))
            val result = com.maksimowiczm.foodyou.fooddiary.domain.usecase.UnpackNutritionalItemDiaryEntryUseCase(entries, slots, db, clock, com.maksimowiczm.foodyou.food.ProductTestLogger)
                .unpack(id, com.maksimowiczm.foodyou.common.domain.measurement.Measurement.Serving(1.0), dated.slotId, dated.date)
            assertTrue(result.isSuccess())
            assertEquals(listOf("Z first constituent", "A second constituent"), rows(db, dated).map { it.second.first })
            val renamed = manual.observe(quick).first()!!
            manual.update(renamed.copy(name = "Z rename retains position"))
            assertEquals(after, rows(db, target).map { it.first })
        }
    }

    /** Run alone, then explicitly force-stop the test app before verifyProcessRestartFixture. */
    @Test fun seedProcessRestartFixture() = runBlocking<Unit> {
        org.junit.Assume.assumeTrue(InstrumentationRegistry.getArguments().getString("story39RestartPhase") == "seed")
        val destination = context.getDatabasePath("story39-process-restart.db")
        context.deleteDatabase("story39-process-restart.db")
        fixture { db, repo, name ->
            repo.execute(DiaryBulkAction.Copy, repo.prepare(source), target)
            val digest = java.security.MessageDigest.getInstance("SHA-256").digest(rawDatabase(db).toString().toByteArray()).joinToString("") { "%02x".format(it) }
            context.openFileOutput("story39-process-restart.sha256", 0).use { it.write(digest.toByteArray()) }
            db.close()
            context.getDatabasePath(name).copyTo(destination, overwrite = true)
        }
    }

    @Test fun verifyProcessRestartFixture() = runBlocking<Unit> {
        org.junit.Assume.assumeTrue(InstrumentationRegistry.getArguments().getString("story39RestartPhase") == "verify")
        val db = Room.databaseBuilder(context, FoodYouDatabase::class.java, "story39-process-restart.db").build()
        try {
            // Force Room to reopen the persisted file in this new application process.
            assertEquals(7, repository(db).prepare(source).count)
            assertEquals(8, repository(db).prepare(target).count)
            val digest = java.security.MessageDigest.getInstance("SHA-256").digest(rawDatabase(db).toString().toByteArray()).joinToString("") { "%02x".format(it) }
            val expected = context.openFileInput("story39-process-restart.sha256").bufferedReader().use { it.readText() }
            assertEquals("Every persisted fact, context, identity and order survives process restart", expected, digest)
            assertEquals(normalized(db, source), normalized(db, target).drop(1))
        } finally { db.close() }
    }

    private suspend fun fixture(block: suspend (FoodYouDatabase, RoomDiaryBulkRepository, String) -> Unit) {
        val name = "story39-" + UUID.randomUUID() + ".db"
        val db = Room.databaseBuilder(context, FoodYouDatabase::class.java, name).build()
        try {
            db.diarySlotDao.insertWithLastRank(DiarySlotEntity(3901, "Synthetic source", 0, 0, 23, 59, 0))
            db.diarySlotDao.insertWithLastRank(DiarySlotEntity(3902, "Synthetic destination", 0, 0, 23, 59, 1))
            val (n, v, m) = toEntityNutrients(facts)
            val manual = ManualDiaryEntryEntity(diarySlotId = source.slotId, dateEpochDay = source.date.toEpochDays(), name = "A manual", nutrients = n, vitamins = v, minerals = m,
                createdEpochSeconds = 11, updatedEpochSeconds = 12, description = "Synthetic description", servingCount = 1.234567, weightGrams = 123.456789,
                capturedPortionType = MeasurementType.Serving, capturedPortionValue = 1.234567, portionDescription = portion, nutritionMetadata = metadata)
            db.manualDiaryEntryDao.insert(manual)
            db.manualDiaryEntryDao.insert(manual.copy(name = "G placeholder", isPlaceholder = true))
            db.manualDiaryEntryDao.insert(manual.copy(diarySlotId = target.slotId, dateEpochDay = target.date.toEpochDays(), name = "Z existing"))
            val product = DiaryProductEntity(name = "B product", nutrients = n, vitamins = v, minerals = m, packageWeight = 250.5, servingWeight = 125.25,
                isLiquid = false, sourceType = ProductSourceType.User, sourceUrl = "https://example.invalid/synthetic", note = "Synthetic product note", nutritionMetadata = metadata)
            val productId = db.measurementDao.insertDiaryProduct(product)
            val reference = db.measurementDao.insertDiaryProduct(product.copy(name = "E reference", factBasis = ProductFactBasis.REFERENCE_SERVING, referencePortion = portion))
            suspend fun entry(productId: Long? = null, recipeId: Long? = null) { db.measurementDao.insertMeasurement(MeasurementEntity(diarySlotId = source.slotId,
                epochDay = source.date.toEpochDays(), productId = productId, recipeId = recipeId, measurement = MeasurementType.Serving, quantity = 1.234567, createdAt = 21, updatedAt = 22)) }
            entry(productId = productId); entry(productId = reference)
            var nested: Long? = null
            for (version in 0..1) {
                val recipe = db.measurementDao.insertDiaryRecipe(DiaryRecipeEntity(name = if (version == 0) "C legacy" else "D nested", servings = 3, isLiquid = false, note = "Synthetic recipe note", calculationVersion = version))
                db.measurementDao.insertDiaryRecipeIngredient(DiaryRecipeIngredientEntity(recipeId = recipe, ingredientProductId = productId, ingredientRecipeId = null, measurement = MeasurementType.Gram, quantity = 125.5))
                nested?.let { db.measurementDao.insertDiaryRecipeIngredient(DiaryRecipeIngredientEntity(recipeId = recipe, ingredientProductId = null, ingredientRecipeId = it, measurement = MeasurementType.Serving, quantity = 2.25)) }
                entry(recipeId = recipe); nested = recipe
            }
            val reported = db.measurementDao.insertDiaryRecipe(DiaryRecipeEntity(name = "F reported", servings = null, isLiquid = true, note = "Synthetic reported note", calculationVersion = 2,
                reportedNutrition = ReportedNutrition(facts, ReportedBasis.UNSPECIFIED, portion, metadata, true, metadata)))
            db.measurementDao.insertReportedComponents(listOf(DiaryReportedRecipeComponentEntity(reported, 0, ReportedRecipeComponent("Synthetic component", portion, facts, metadata.reference, metadata))))
            entry(recipeId = reported)
            block(db, repository(db), name)
        } finally { db.close(); context.deleteDatabase(name) }
    }

    private fun repository(db: FoodYouDatabase) = RoomDiaryBulkRepository(db, db.measurementDao, db.manualDiaryEntryDao, db.diarySlotDao)
    private suspend fun rejected(reason: DiaryBulkError, block: suspend () -> Unit) {
        try { block(); fail("Expected " + reason) } catch (failure: DiaryBulkException) { assertEquals(reason, failure.reason) }
    }
    private suspend fun rows(db: FoodYouDatabase, endpoint: DiaryEndpoint): List<Pair<String, Pair<String, Long?>>> =
        (db.manualDiaryEntryDao.entries(endpoint.slotId, endpoint.date.toEpochDays()).map { "manual:${it.id}" to (it.name to it.sortOrder) } +
            db.measurementDao.entries(endpoint.slotId, endpoint.date.toEpochDays()).map { "food:${it.id}" to ((it.productId?.let { id -> db.measurementDao.product(id)!!.name } ?: db.measurementDao.recipe(it.recipeId!!)!!.name) to it.sortOrder) })
            .sortedWith(compareBy<Pair<String, Pair<String, Long?>>> { it.second.second ?: Long.MAX_VALUE }.thenBy { it.second.first }.thenBy { if (it.first.startsWith("manual")) 0 else 1 }.thenBy { it.first.substringAfter(':').toLong() })

    private suspend fun normalized(db: FoodYouDatabase, endpoint: DiaryEndpoint): List<Any> = rows(db, endpoint).map { (key, _) ->
        val id = key.substringAfter(':').toLong()
        if (key.startsWith("manual")) db.manualDiaryEntryDao.observe(id).first()!!.copy(id = 0, diarySlotId = 0, dateEpochDay = 0, sortOrder = null)
        else {
            val entry = db.measurementDao.observeMeasurementById(id).first()!!
            listOf(entry.copy(id = 0, diarySlotId = 0, epochDay = 0, productId = null, recipeId = null, sortOrder = null), graph(db, entry.productId, entry.recipeId))
        }
    }
    private suspend fun graph(db: FoodYouDatabase, product: Long?, recipe: Long?): Any {
        if (product != null) return db.measurementDao.product(product)!!.copy(id = 0)
        val id = recipe!!
        return listOf(db.measurementDao.recipe(id)!!.copy(id = 0), db.measurementDao.ingredients(id).map {
            listOf(it.copy(id = 0, recipeId = 0, ingredientProductId = null, ingredientRecipeId = null), graph(db, it.ingredientProductId, it.ingredientRecipeId))
        }, db.measurementDao.components(id).map { it.copy(recipeId = 0) })
    }
    private fun sql(db: FoodYouDatabase, block: SQLiteDatabase.() -> Unit) {
        SQLiteDatabase.openDatabase(context.getDatabasePath(checkNotNull(db.openHelper.databaseName)).path, null, SQLiteDatabase.OPEN_READWRITE).use(block)
    }
    private fun migrationProjection(): List<List<List<String?>>> =
        SQLiteDatabase.openDatabase(context.getDatabasePath(migrationName).path, null, SQLiteDatabase.OPEN_READONLY).use { sql ->
            listOf("Measurement", "ManualDiaryEntry", "DiaryProduct", "DiaryRecipe", "DiaryRecipeIngredient", "DiaryReportedRecipeComponent", "Meal", "MealTemplate", "MealTemplateItem", "Product", "Recipe").map { table ->
                sql.rawQuery("SELECT * FROM $table ORDER BY rowid", null).use { cursor ->
                    val rows = mutableListOf<List<String?>>()
                    val columns = (0 until cursor.columnCount).filter { cursor.getColumnName(it) != "sortOrder" }
                    while (cursor.moveToNext()) rows.add(columns.map { if (cursor.isNull(it)) null else cursor.getString(it) })
                    rows
                }
            }
        }

    private fun rawDatabase(db: FoodYouDatabase): List<List<List<String?>>> {
        val result = mutableListOf<List<List<String?>>>()
        sql(db) { for (table in listOf("Measurement", "ManualDiaryEntry", "DiaryProduct", "DiaryRecipe", "DiaryRecipeIngredient", "DiaryReportedRecipeComponent", "meal")) {
            rawQuery("SELECT * FROM $table ORDER BY rowid", null).use { cursor ->
                val rows = mutableListOf<List<String?>>()
                while (cursor.moveToNext()) rows.add((0 until cursor.columnCount).map { if (cursor.isNull(it)) null else cursor.getString(it) })
                result.add(rows)
            }
        } }
        return result
    }
}
