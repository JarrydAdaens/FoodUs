package com.maksimowiczm.foodyou.food

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.platform.app.InstrumentationRegistry
import com.maksimowiczm.foodyou.app.infrastructure.room.FoodYouDatabase
import com.maksimowiczm.foodyou.app.infrastructure.room.migration.StructuredLibraryMigration
import com.maksimowiczm.foodyou.app.ui.food.diary.add.toDiaryFood
import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.food.domain.entity.*
import com.maksimowiczm.foodyou.food.infrastructure.repository.*
import com.maksimowiczm.foodyou.fooddiary.domain.entity.*
import com.maksimowiczm.foodyou.fooddiary.infrastructure.repository.*
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.DiarySlotEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.*
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Disposable synthetic fixtures only; accepted exported schemas are never rewritten. */
class StructuredLibraryPersistenceTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val migrationName = "story28-migration-" + java.util.UUID.randomUUID() + ".db"
    @get:Rule val helper = MigrationTestHelper(instrumentation = instrumentation, file = context.getDatabasePath(migrationName), driver = AndroidSQLiteDriver(), databaseClass = FoodYouDatabase::class)

    @Test fun schema41MigrationPreservesEveryOldColumnAndCascadingRowThenReopens() = runBlocking {
        val schema = JSONObject(instrumentation.context.assets.open("com.maksimowiczm.foodyou.app.infrastructure.room.FoodYouDatabase/41.json").bufferedReader().use { it.readText() }).getJSONObject("database")
        val tables = schema.getJSONArray("entities")
        val columns = linkedMapOf<String, List<String>>()
        for (i in 0 until tables.length()) {
            val table = tables.getJSONObject(i)
            if (!table.getString("createSql").startsWith("CREATE TABLE")) continue
            val fields = table.getJSONArray("fields")
            columns[table.getString("tableName")] = (0 until fields.length()).map { fields.getJSONObject(it).getString("columnName") }
        }
        lateinit var before: Map<String, List<List<String>>>
        helper.createDatabase(41).use { old ->
            old.execSQL("INSERT INTO Product(id,name,isLiquid,sourceType,energy,proteins,note) VALUES(2801,'Synthetic existing Food',0,0,100,0,NULL)")
            old.execSQL("INSERT INTO Recipe(id,name,servings,isLiquid,note) VALUES(2801,'Synthetic existing Recipe',3,0,'original note')")
            old.execSQL("INSERT INTO RecipeIngredient(id,recipeId,ingredientProductId,measurement,quantity) VALUES(2801,2801,2801,0,100)")
            old.execSQL("INSERT INTO FoodEvent(id,type,epochSeconds,productId,recipeId,extra) VALUES(2801,0,17,NULL,2801,NULL)")
            old.execSQL("INSERT INTO MeasurementSuggestion(id,recipeId,type,value,epochSeconds) VALUES(2801,2801,1,2,17)")
            old.execSQL("INSERT INTO Meal(id,name,fromHour,fromMinute,toHour,toMinute,rank) VALUES(2801,'Synthetic existing slot',0,0,23,59,0)")
            old.execSQL("INSERT INTO DiaryProduct(id,name,isLiquid,sourceType,energy,proteins) VALUES(2801,'Synthetic legacy snapshot',0,0,100,NULL)")
            for (contract in 0..1) {
                val id = 2801 + contract
                old.execSQL("INSERT INTO DiaryRecipe(id,name,servings,isLiquid,note,calculationVersion) VALUES($id,'Synthetic existing Recipe snapshot',2,0,NULL,$contract)")
                old.execSQL("INSERT INTO DiaryRecipeIngredient(id,recipeId,ingredientProductId,measurement,quantity) VALUES($id,$id,2801,0,100)")
                old.execSQL("INSERT INTO Measurement(id,mealId,epochDay,recipeId,measurement,quantity,createdAt,updatedAt) VALUES($id,2801,0,$id,1,1,17,18)")
            }
            old.execSQL("INSERT INTO MealTemplate(id,name,createdEpochSeconds) VALUES(2801,'Synthetic existing Meal',17)")
            old.execSQL("INSERT INTO MealTemplateItem(id,templateId,name,position,energy,proteins) VALUES(2801,2801,'Synthetic captured item',0,123,0)")
            before = projection(old, columns)
        }
        // Android's pooled framework driver did not retain a SQL-only FK setting through
        // helper configuration (checkpoint02 records the actual migration setting as 0).
        // Prove FK-on SQL separately, then validate the helper's normal registered path.
        verifyForeignKeyEnabledTransaction(before, columns)
        verifyProductionRegisteredMigration(before, columns)
        helper.runMigrationsAndValidate(42, listOf(StructuredLibraryMigration)).use { upgraded ->
            assertEquals(before, projection(upgraded, columns))
            upgraded.prepare("PRAGMA foreign_key_check").use { assertFalse(it.step()) }
            upgraded.prepare("SELECT factBasis,referencePortion,nutritionMetadata FROM Product WHERE id=2801").use {
                assertTrue(it.step()); assertEquals("METRIC_100", it.getText(0)); assertTrue(it.isNull(1)); assertTrue(it.isNull(2))
            }
            upgraded.prepare("SELECT servings,reportedNutrition FROM Recipe WHERE id=2801").use { assertTrue(it.step()); assertEquals(3L,it.getLong(0)); assertTrue(it.isNull(1)) }
        }
        val db = Room.databaseBuilder(context, FoodYouDatabase::class.java, migrationName).build()
        try {
            val entries = RoomNutritionalItemDiaryEntryRepository(db,db.measurementDao)
            for (contract in 0..1) {
                val snapshot = entries.observe(NutritionalItemDiaryEntryId(2801L+contract)).first()!!.food as RecipeSnapshot
                assertEquals(contract,snapshot.calculationVersion)
                assertEquals(2,snapshot.servings)
                assertEquals(50.0,snapshot.nutritionFor(Measurement.Serving(1.0))!!.energy.value)
            }
            val recipes = RoomRecipeRepository(db.recipeDao, RoomProductRepository(db.productDao))
            assertNotNull(recipes.observeRecipe(NutritionalItemId.Recipe(2801)).first())
            assertEquals(listOf(NutritionalItemId.Recipe(2801)), db.recipeDao.getRecipeIngredients(2801).map { NutritionalItemId.Recipe(it.recipeId!!) })
        } finally { db.close(); context.deleteDatabase(migrationName) }
    }

    private suspend fun verifyProductionRegisteredMigration(before: Map<String,List<List<String>>>, columns: Map<String,List<String>>) {
        val productionName = "story28-registered-room-" + java.util.UUID.randomUUID() + ".db"
        val productionFile = context.getDatabasePath(productionName)
        context.getDatabasePath(migrationName).copyTo(productionFile)
        val db = Room.databaseBuilder(context,FoodYouDatabase::class.java,productionName)
            .addMigrations(StructuredLibraryMigration).build()
        try {
            // Force the generated Room open/migrate/validate path on a real schema41 file.
            assertNotNull(db.recipeDao.observeRecipe(2801).first())
            AndroidSQLiteDriver().open(productionFile.path).use { connection ->
                connection.prepare("PRAGMA user_version").use { assertTrue(it.step()); assertEquals(42L,it.getLong(0)) }
                assertEquals("Registered production Room migration preserves old columns",before,projection(connection,columns))
                connection.prepare("PRAGMA foreign_key_check").use { assertFalse(it.step()) }
            }
        } finally { db.close(); context.deleteDatabase(productionName) }
    }

    private fun verifyForeignKeyEnabledTransaction(before: Map<String,List<List<String>>>, columns: Map<String,List<String>>) {
        val directName = "story28-direct-fk-" + java.util.UUID.randomUUID() + ".db"
        val directFile = context.getDatabasePath(directName)
        // The helper creation connection is closed; verify the copied old rows before migrating.
        context.getDatabasePath(migrationName).copyTo(directFile)
        try {
            BundledSQLiteDriver().open(directFile.path).use { connection ->
                assertEquals("Direct fixture starts with the complete schema41 rows",before,projection(connection,columns))
                connection.execSQL("PRAGMA foreign_keys = ON")
                connection.execSQL("BEGIN EXCLUSIVE TRANSACTION")
                try {
                    connection.prepare("PRAGMA foreign_keys").use {
                        assertTrue("Direct migration connection reports its FK setting",it.step())
                        assertEquals("FK enforcement is active inside the actual migration transaction",1L,it.getLong(0))
                    }
                    StructuredLibraryMigration.migrate(connection)
                    assertEquals("FK-on migration preserves every original column and row",before,projection(connection,columns))
                    connection.prepare("PRAGMA foreign_key_check").use { assertFalse(it.step()) }
                    connection.execSQL("COMMIT TRANSACTION")
                } catch (failure: Throwable) {
                    connection.execSQL("ROLLBACK TRANSACTION")
                    throw failure
                }
            }
        } finally { context.deleteDatabase(directName) }
    }

    @Test fun referenceAndReportedDefinitionsCaptureAllFieldsAndOpaqueComponentsThroughRestartAndDeletion() = runBlocking {
        val name = "story28-capture-" + java.util.UUID.randomUUID() + ".db"
        var db = Room.databaseBuilder(context,FoodYouDatabase::class.java,name).build()
        try {
            val metadata = NutritionMetadata(notes="Synthetic note",provenance="Synthetic provenance",percentDailyValues=mapOf("protein" to 0.0),observations=listOf(NutritionObservation("energy-claim",777.0,"kcal")),textTruncated=true)
            val reference = PortionDescription(2.0,"pieces","2 pieces; metric amount unavailable")
            val products = RoomProductRepository(db.productDao)
            val productId = products.insertProduct(Product(NutritionalItemId.Product(0),"Synthetic reference Food",null,null,null,false,null,null,ProductSource(ProductSource.Type.User),allFacts(),ProductFactBasis.REFERENCE_SERVING,reference,metadata))
            val product = products.observeProduct(productId).first()!!
            for (field in NutritionFactsField.entries) assertEquals(allFacts()[field],product.nutritionFacts[field])
            db.diarySlotDao.insertWithLastRank(DiarySlotEntity(2801,"Synthetic target",0,0,23,59))
            val entries = RoomNutritionalItemDiaryEntryRepository(db,db.measurementDao)
            val productEntry = entries.insert(Measurement.Serving(2.0),2801,LocalDate(2026,10,25),product.toDiaryFood(),ProductTestClock.now())
            val recipes = RoomRecipeRepository(db.recipeDao,products)
            val components = listOf(ReportedRecipeComponent("Synthetic first",reference,reference=DefinitionProvenance(DefinitionKind.PRODUCT,"synthetic","1",productId.id)),ReportedRecipeComponent("Synthetic second",PortionDescription(text="unknown amount"),metadata=metadata))
            val recipeId = recipes.insertRecipe(Recipe(NutritionalItemId.Recipe(0),"Synthetic reported Recipe",null,emptyList(),null,false,ReportedNutrition(allFacts(),ReportedBasis.REFERENCE_SERVING,metadata=metadata),components))
            val recipe = recipes.observeRecipe(recipeId).first()!!
            val recipeEntry = entries.insert(Measurement.Serving(1.0),2801,LocalDate(2026,10,25),recipe.toDiaryFood(),ProductTestClock.now())
            val capturedProduct = entries.observe(productEntry).first()!!
            val capturedRecipe = entries.observe(recipeEntry).first()!!
            assertNull(capturedProduct.weight)
            assertEquals(listOf("Synthetic first","Synthetic second"),(capturedRecipe.food as RecipeSnapshot).reportedComponents.map { it.name })
            assertTrue(capturedRecipe.food.reportedComponents.all { it.reference?.localId == null })
            assertEquals(2,capturedRecipe.food.calculationVersion)
            recipes.deleteRecipe(recipe); products.deleteProduct(product)
            db.close(); db = Room.databaseBuilder(context,FoodYouDatabase::class.java,name).build()
            val reopened = RoomNutritionalItemDiaryEntryRepository(db,db.measurementDao)
            assertEquals(capturedProduct,reopened.observe(productEntry).first())
            assertEquals(capturedRecipe,reopened.observe(recipeEntry).first())
            val copy = reopened.insert(capturedRecipe.measurement,2801,LocalDate(2026,10,26),capturedRecipe.food,ProductTestClock.now())
            assertEquals(capturedRecipe.food,reopened.observe(copy).first()!!.food)
        } finally { db.close(); context.deleteDatabase(name) }
    }

    @Test fun encodedOverflowRejectsProductInsertAndRollsBackNestedSnapshotCapture() = runBlocking {
        val name = "story28-encoded-overflow-" + java.util.UUID.randomUUID() + ".db"
        val db = Room.databaseBuilder(context,FoodYouDatabase::class.java,name).build()
        try {
            val overflow = NutritionFacts(vitaminA = NutrientValue.Complete(1e303))
            assertTrue(overflow.hasValidValues())
            val products = RoomProductRepository(db.productDao)
            try {
                products.insertProduct(Product(NutritionalItemId.Product(0),"Synthetic unencodable Food",null,null,null,false,null,null,
                    ProductSource(ProductSource.Type.User),overflow,ProductFactBasis.REFERENCE_SERVING,PortionDescription(1.0,"piece")))
                fail("Encoded-unit overflow must reject the Product before insertion")
            } catch (_: IllegalArgumentException) { }
            assertTrue(products.observeProducts(10,0).first().isEmpty())
            db.diarySlotDao.insertWithLastRank(DiarySlotEntity(2801,"Synthetic rollback target",0,0,23,59))
            val valid = ProductSnapshot("Synthetic valid snapshot",NutritionFacts(energy = NutrientValue.Complete(5.0)),null,null,false,
                ProductSource(ProductSource.Type.User),null,ProductFactBasis.REFERENCE_SERVING,PortionDescription(1.0,"piece"))
            val invalid = valid.copy(name = "Synthetic unencodable snapshot",nutritionFacts = overflow)
            val recipe = RecipeSnapshot("Synthetic nested rollback",1,listOf(
                RecipeIngredientSnapshot(valid,Measurement.Serving(1.0)),
                RecipeIngredientSnapshot(invalid,Measurement.Serving(1.0))),false,null)
            val entries = RoomNutritionalItemDiaryEntryRepository(db,db.measurementDao)
            try {
                entries.insert(Measurement.Serving(1.0),2801,LocalDate(2026,10,25),recipe,ProductTestClock.now())
                fail("Encoded overflow must roll back the Recipe and previously captured constituent")
            } catch (_: IllegalArgumentException) { }
            assertTrue(entries.observeAll(2801,LocalDate(2026,10,25)).first().isEmpty())
            AndroidSQLiteDriver().open(context.getDatabasePath(name).path).use { connection ->
                for (table in listOf("Product","DiaryProduct","DiaryRecipe","DiaryRecipeIngredient","Measurement")) {
                    connection.prepare("SELECT count(*) FROM $table").use { assertTrue(it.step()); assertEquals("No partial rows in $table",0L,it.getLong(0)) }
                }
            }
        } finally { db.close(); context.deleteDatabase(name) }
    }

    private fun projection(connection: SQLiteConnection, tables: Map<String,List<String>>): Map<String,List<List<String>>> = tables.mapValues { (table, columns) ->
        val rows = mutableListOf<List<String>>()
        val expressions = columns.joinToString(",") { "quote(`$it`)" }
        connection.prepare("SELECT $expressions FROM `$table` ORDER BY rowid").use { statement ->
            while (statement.step()) rows.add(columns.indices.map { statement.getText(it) })
        }
        rows
    }

    private fun allFacts() = NutritionFacts(
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
}
