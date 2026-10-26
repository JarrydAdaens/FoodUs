package com.maksimowiczm.foodyou.fooddiary

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.platform.app.InstrumentationRegistry
import com.maksimowiczm.foodyou.app.infrastructure.room.FoodYouDatabase
import com.maksimowiczm.foodyou.app.infrastructure.room.migration.*
import com.maksimowiczm.foodyou.common.domain.food.NutrientValue
import com.maksimowiczm.foodyou.common.result.isSuccess
import com.maksimowiczm.foodyou.food.ProductTestClock
import com.maksimowiczm.foodyou.fooddiary.domain.entity.*
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.*
import com.maksimowiczm.foodyou.fooddiary.infrastructure.repository.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Disposable synthetic fixtures; original canonical schema39/40 files are never opened here. */
class MealMigrationTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val filename = "story24-migration.db"
    @get:Rule val helper = MigrationTestHelper(
        instrumentation = instrumentation, file = context.getDatabasePath(filename),
        driver = AndroidSQLiteDriver(), databaseClass = FoodYouDatabase::class,
    )

    @Test fun schema39Through40To41PreservesEveryLegacyColumnAndDefaults() = upgrade(39)
    @Test fun schema40To41PreservesLegacyCurrentAndNestedRecipeContracts() = upgrade(40)

    private fun upgrade(version: Int) = runBlocking {
        helper.createDatabase(version).use { old ->
            old.execSQL("INSERT INTO Meal(id,name,fromHour,fromMinute,toHour,toMinute,rank) VALUES(2401,'Synthetic Meal slot',0,0,23,59,0)")
            old.execSQL("INSERT INTO MealTemplate(id,name,createdEpochSeconds) VALUES(2401,'Synthetic legacy Meal',123)")
            old.execSQL("INSERT INTO MealTemplateItem(id,templateId,name,position,energy,proteins) VALUES(2401,2401,'Synthetic numeric legacy',0,150,10)")
            old.execSQL("INSERT INTO ManualDiaryEntry(id,mealId,dateEpochDay,name,createdEpochSeconds,updatedEpochSeconds,isPlaceholder,energy,servingCount,weightGrams) VALUES(2401,2401,0,'Synthetic prior manual',1,2,0,780,2.5,250)")
            old.execSQL("INSERT INTO DiaryProduct(id,name,isLiquid,sourceType,energy,proteins) VALUES(2401,'Synthetic solid',0,0,100,10)")
            old.execSQL("INSERT INTO DiaryProduct(id,name,isLiquid,sourceType,energy,proteins) VALUES(2402,'Synthetic liquid',1,0,200,NULL)")
            val columns = if (version == 40) ",calculationVersion" else ""
            val legacy = if (version == 40) ",0" else ""
            old.execSQL("INSERT INTO DiaryRecipe(id,name,servings,isLiquid" + columns + ") VALUES(2401,'Synthetic legacy mixed',1,0" + legacy + ")")
            old.execSQL("INSERT INTO DiaryRecipe(id,name,servings,isLiquid" + columns + ") VALUES(2402,'Synthetic legacy nested',1,0" + legacy + ")")
            old.execSQL("INSERT INTO DiaryRecipeIngredient(id,recipeId,ingredientProductId,measurement,quantity) VALUES(2401,2401,2401,0,100)")
            old.execSQL("INSERT INTO DiaryRecipeIngredient(id,recipeId,ingredientProductId,measurement,quantity) VALUES(2402,2401,2402,3,100)")
            old.execSQL("INSERT INTO DiaryRecipeIngredient(id,recipeId,ingredientRecipeId,measurement,quantity) VALUES(2403,2402,2401,2,1)")
            old.execSQL("INSERT INTO Measurement(id,mealId,epochDay,recipeId,measurement,quantity,createdAt,updatedAt) VALUES(2401,2401,0,2401,0,100,1,2)")
            old.execSQL("INSERT INTO Measurement(id,mealId,epochDay,recipeId,measurement,quantity,createdAt,updatedAt) VALUES(2402,2401,0,2402,2,1,1,2)")
            if (version == 40) {
                old.execSQL("INSERT INTO DiaryRecipe(id,name,servings,isLiquid,calculationVersion) VALUES(2403,'Synthetic current mixed',3,0,1)")
                old.execSQL("INSERT INTO DiaryRecipe(id,name,servings,isLiquid,calculationVersion) VALUES(2404,'Synthetic current nested',3,0,1)")
                old.execSQL("INSERT INTO DiaryRecipeIngredient(id,recipeId,ingredientProductId,measurement,quantity) VALUES(2404,2403,2401,0,100)")
                old.execSQL("INSERT INTO DiaryRecipeIngredient(id,recipeId,ingredientProductId,measurement,quantity) VALUES(2405,2403,2402,3,100)")
                old.execSQL("INSERT INTO DiaryRecipeIngredient(id,recipeId,ingredientRecipeId,measurement,quantity) VALUES(2406,2404,2403,2,3)")
                old.execSQL("INSERT INTO Measurement(id,mealId,epochDay,recipeId,measurement,quantity,createdAt,updatedAt) VALUES(2403,2401,0,2404,2,2,1,2)")
            }
        }
        val before = projection(version)
        helper.runMigrationsAndValidate(41, listOf(RecipeSnapshotCalculationMigration, MealPortionMetadataMigration)).close()
        assertEquals("Every original column including existing 0/1 contracts is preserved", before, projection(version))
        SQLiteDatabase.openDatabase(context.getDatabasePath(filename).path, null, SQLiteDatabase.OPEN_READWRITE).use { sql ->
            sql.rawQuery("SELECT COUNT(*) FROM MealTemplateItem WHERE capturedPortionType IS NOT NULL OR capturedPortionValue IS NOT NULL OR servingCount IS NOT NULL OR weightGrams IS NOT NULL", null).use {
                assertTrue(it.moveToFirst()); assertEquals(0, it.getInt(0))
            }
            sql.rawQuery("SELECT COUNT(*) FROM ManualDiaryEntry WHERE capturedPortionType IS NOT NULL OR capturedPortionValue IS NOT NULL", null).use {
                assertTrue(it.moveToFirst()); assertEquals(0, it.getInt(0))
            }
            if (version == 39) sql.rawQuery("SELECT COUNT(*) FROM DiaryRecipe WHERE calculationVersion != 0", null).use {
                assertTrue(it.moveToFirst()); assertEquals(0, it.getInt(0))
            }
        }
        var db = Room.databaseBuilder(context, FoodYouDatabase::class.java, filename).build()
        var capturedId: MealId? = null
        var sourceBefore = emptyList<NutritionalItemDiaryEntry>()
        try {
            val source = RoomNutritionalItemDiaryEntryRepository(db, db.measurementDao)
            val manual = RoomManualDiaryEntryRepository(db.manualDiaryEntryDao)
            val meals = RoomMealRepository(db, db.mealDao)
            val slots = RoomDiarySlotRepository(db.diarySlotDao)
            val date = LocalDate.fromEpochDays(0)
            sourceBefore = source.observeAll(2401, date).first().sortedBy { it.id.value }
            val mixed = sourceBefore.first { it.id.value == 2401L }
            val nested = sourceBefore.first { it.id.value == 2402L }
            assertEquals(150.0, mixed.nutritionFacts.energy.value)
            assertEquals(NutrientValue.Incomplete(10.0), nested.nutritionFacts.proteins)
            assertEquals(0, (mixed.food as RecipeSnapshot).calculationVersion)
            assertEquals(0, ((nested.food as RecipeSnapshot).ingredients.single().food as RecipeSnapshot).calculationVersion)
            if (version == 40) {
                val current = sourceBefore.first { it.id.value == 2403L }
                assertEquals(1, (current.food as RecipeSnapshot).calculationVersion)
                assertEquals(1, (current.food.ingredients.single().food as RecipeSnapshot).calculationVersion)
                assertEquals(200.0, current.nutritionFacts.energy.value!!, 0.000001)
                assertNull(current.nutritionFacts.proteins.value)
            }
            val save = SaveMealUseCase(source, manual, meals, db).save("Synthetic captured contract Meal", 2401, date)
            assertTrue(save.isSuccess())
            capturedId = meals.observeAll().first().single { it.name == "Synthetic captured contract Meal" }.id
            val captured = meals.getMeal(capturedId!!)!!
            assertNull(captured.items.single { it.name == nested.name }.nutritionFacts.proteins.value)
            assertEquals(150.0, captured.items.single { it.name == mixed.name }.nutritionFacts.energy.value)
            val apply = ApplyMealUseCase(meals, manual, slots, db, ProductTestClock)
            assertTrue(apply.apply(capturedId!!, 2401, LocalDate.fromEpochDays(1)).isSuccess())
            val loaded = manual.observeAll(2401, LocalDate.fromEpochDays(1)).first()
            assertEquals(sourceBefore.size + 1, loaded.size)
            assertNull(loaded.single { it.name == nested.name }.nutritionFacts.proteins.value)
            assertEquals(captured.items.single { it.name == nested.name }.capturedPortion, loaded.single { it.name == nested.name }.capturedPortion)
            val legacy = meals.getMeal(MealId(2401))!!
            assertEquals(10.0, legacy.items.single().nutritionFacts.proteins.value)
            assertTrue(UpdateMealUseCase(meals).update(legacy.id, "Synthetic renamed legacy", legacy.items).isSuccess())
            assertTrue(apply.apply(legacy.id, 2401, LocalDate.fromEpochDays(2)).isSuccess())
            assertEquals(10.0, manual.observeAll(2401, LocalDate.fromEpochDays(2)).first().single().nutritionFacts.proteins.value)
            assertEquals(sourceBefore, source.observeAll(2401, date).first().sortedBy { it.id.value })
        } finally { db.close() }
        db = Room.databaseBuilder(context, FoodYouDatabase::class.java, filename).build()
        try {
            assertNotNull(RoomMealRepository(db, db.mealDao).getMeal(capturedId!!))
            assertEquals(sourceBefore, RoomNutritionalItemDiaryEntryRepository(db, db.measurementDao)
                .observeAll(2401, LocalDate.fromEpochDays(0)).first().sortedBy { it.id.value })
        } finally { db.close(); context.deleteDatabase(filename) }
    }

    /** Select exactly the original exported columns; new columns are checked independently above. */
    private fun projection(version: Int): Map<String, List<List<String?>>> {
        val schema = JSONObject(instrumentation.context.assets.open(
            "com.maksimowiczm.foodyou.app.infrastructure.room.FoodYouDatabase/" + version + ".json"
        ).bufferedReader().use { it.readText() }).getJSONObject("database").getJSONArray("entities")
        return SQLiteDatabase.openDatabase(context.getDatabasePath(filename).path, null, SQLiteDatabase.OPEN_READWRITE).use { db ->
            (0 until schema.length()).associate { index ->
                val entity = schema.getJSONObject(index)
                val table = entity.getString("tableName")
                val fields = entity.getJSONArray("fields")
                val columns = (0 until fields.length()).map { fields.getJSONObject(it).getString("columnName") }
                val select = columns.joinToString(",") { "`" + it + "`" }
                val rows = mutableListOf<List<String?>>()
                db.rawQuery("SELECT " + select + " FROM `" + table + "` ORDER BY rowid", null).use { cursor ->
                    while (cursor.moveToNext()) rows += columns.indices.map { if (cursor.isNull(it)) null else cursor.getString(it) }
                }
                table to rows
            }
        }
    }
}
