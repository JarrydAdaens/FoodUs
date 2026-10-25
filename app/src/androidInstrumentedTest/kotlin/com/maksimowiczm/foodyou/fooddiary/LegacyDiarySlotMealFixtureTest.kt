package com.maksimowiczm.foodyou.fooddiary

import android.database.sqlite.SQLiteDatabase
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

/**
 * Raw Android-only fixture entry points: this class also runs against the pre-rename APK.
 * Invoke individual methods explicitly on an isolated emulator after launching the baseline app.
 * No FoodUs type references are allowed here, so old and renamed classes cannot affect seeding.
 */
class LegacyDiarySlotMealFixtureTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    @Test
    fun seedCanonicalDatabase() = withDatabase { db ->
        requireEmulator()
        assertEquals(39, db.version)
        assertEquals(0L, count(db, "Meal", "id BETWEEN 2501 AND 2599"))
        assertEquals(0L, count(db, "MealTemplate", "id BETWEEN 2501 AND 2599"))
        db.beginTransaction()
        try {
            db.execSQL("INSERT INTO Meal VALUES (2501, 'Synthetic Breakfast', 6, 15, 10, 45, 40)")
            db.execSQL("INSERT INTO Meal VALUES (2502, 'Synthetic Evening', 18, 30, 22, 0, 41)")
            db.execSQL("""INSERT INTO DiaryProduct
                (id, name, isLiquid, sourceType, servingWeight, energy, proteins)
                VALUES (2501, 'Synthetic Toast', 0, 0, 50, 200, 10)""")
            db.execSQL("""INSERT INTO DiaryRecipe (id, name, servings, isLiquid)
                VALUES (2501, 'Synthetic Omelette', 2, 0)""")
            // Measurement type 0 is grams in the existing persisted contract.
            db.execSQL("""INSERT INTO DiaryRecipeIngredient
                (id, recipeId, ingredientProductId, measurement, quantity)
                VALUES (2501, 2501, 2501, 0, 200)""")
            db.execSQL("""INSERT INTO Measurement
                (id, mealId, epochDay, productId, measurement, quantity, createdAt, updatedAt)
                VALUES (2501, 2501, 20751, 2501, 0, 75, 1792886400, 1792886400)""")
            db.execSQL("""INSERT INTO Measurement
                (id, mealId, epochDay, recipeId, measurement, quantity, createdAt, updatedAt)
                VALUES (2502, 2501, 20751, 2501, 0, 100, 1792886400, 1792886400)""")
            db.execSQL("""INSERT INTO ManualDiaryEntry
                (id, mealId, dateEpochDay, name, createdEpochSeconds, updatedEpochSeconds,
                 isPlaceholder, servingCount, weightGrams, energy, proteins, sodiumMilli)
                VALUES (2501, 2501, 20751, 'Synthetic Quick Add', 1792886400, 1792886400,
                    0, 2.5, 250, 780, 35, 1.2)""")
            db.execSQL("""INSERT INTO ManualDiaryEntry
                (id, mealId, dateEpochDay, name, createdEpochSeconds, updatedEpochSeconds,
                 description, isPlaceholder, energy)
                VALUES (2502, 2501, 20751, 'Synthetic unresolved', 1792886400, 1792886400,
                    'Synthetic note', 1, 0)""")
            db.execSQL("""INSERT INTO MealTemplate (id, name, createdEpochSeconds)
                VALUES (2501, 'Synthetic usual breakfast', 1792886400)""")
            // Deliberately insert out of display order; position, not row ID, controls loading.
            db.execSQL("""INSERT INTO MealTemplateItem
                (id, templateId, name, position, energy, proteins)
                VALUES (2501, 2501, 'Synthetic saved second', 1, 420, 21)""")
            db.execSQL("""INSERT INTO MealTemplateItem
                (id, templateId, name, position, energy)
                VALUES (2502, 2501, 'Synthetic saved first', 0, 150)""")
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        assertFixture(db)
        snapshot(db, "story25-before.json")
    }

    @Test
    fun verifyCanonicalDatabaseAfterUpgrade() = withDatabase { db ->
        requireEmulator()
        assertFixture(db)
        val before = File(context.filesDir, "story25-before.json").readText()
        assertEquals("All legacy fixture rows and nullable nutrients survive in-place upgrade",
            before, snapshot(db, "story25-after.json"))
    }

    /** Separate schema40 projection; the original schema39 seed, verifier and before JSON are immutable. */
    @Test
    fun verifyCanonicalRecipeCalculationUpgrade() = withDatabase { db ->
        requireEmulator()
        assertEquals(40, db.version)
        db.rawQuery("PRAGMA foreign_key_check", null).use { assertEquals(0, it.count) }
        val original = File(context.filesDir, "story25-before.json").readText()
        val before = JSONObject(original)
        assertEquals(39, before.getInt("version"))
        val after = JSONObject(snapshot(db, "story27-after.json"))
        val recipes = after.getJSONArray("DiaryRecipe")
        assertEquals(before.getJSONArray("DiaryRecipe").length(), recipes.length())
        for (index in 0 until recipes.length()) {
            val row = recipes.getJSONObject(index)
            assertEquals("Every retained legacy Recipe uses contract0", "0", row.getString("calculationVersion"))
            row.remove("calculationVersion")
        }
        after.put("version", 39)
        val projection = after.toString()
        File(context.filesDir, "story27-legacy-projection.json").writeText(projection)
        assertEquals("Every original row, ID, nullable nutrient and column survives39→40", original, projection)
        assertEquals("The original seed evidence is never overwritten", original, File(context.filesDir, "story25-before.json").readText())
    }

    private fun assertFixture(db: SQLiteDatabase) {
        assertEquals(39, db.version)
        assertEquals(2L, count(db, "Meal", "id IN (2501, 2502)"))
        assertEquals(2L, count(db, "Measurement", "id IN (2501, 2502) AND mealId = 2501"))
        assertEquals(2L, count(db, "ManualDiaryEntry", "id IN (2501, 2502) AND mealId = 2501"))
        assertEquals(1L, count(db, "MealTemplate", "id = 2501"))
        assertEquals(2L, count(db, "MealTemplateItem", "templateId = 2501"))
        db.rawQuery("PRAGMA foreign_key_check", null).use { assertEquals(0, it.count) }
    }

    private fun snapshot(db: SQLiteDatabase, filename: String): String {
        val result = JSONObject().put("version", db.version)
        for (table in listOf("Meal", "DiaryProduct", "DiaryRecipe", "DiaryRecipeIngredient",
                "Measurement", "ManualDiaryEntry", "MealTemplate", "MealTemplateItem")) {
            val rows = JSONArray()
            db.rawQuery("SELECT * FROM $table WHERE id BETWEEN 2501 AND 2599 ORDER BY id", null).use { cursor ->
                while (cursor.moveToNext()) {
                    val row = JSONObject()
                    for (column in 0 until cursor.columnCount) {
                        row.put(cursor.getColumnName(column),
                            if (cursor.isNull(column)) JSONObject.NULL else cursor.getString(column))
                    }
                    rows.put(row)
                }
            }
            result.put(table, rows)
        }
        return result.toString().also { File(context.filesDir, filename).writeText(it) }
    }

    private fun count(db: SQLiteDatabase, table: String, where: String): Long =
        db.rawQuery("SELECT COUNT(*) FROM $table WHERE $where", null).use {
            assertTrue(it.moveToFirst())
            it.getLong(0)
        }

    private fun requireEmulator() {
        check(Build.HARDWARE == "ranchu" || Build.HARDWARE == "goldfish") {
            "Synthetic canonical-database fixture requires an isolated emulator"
        }
    }

    private fun withDatabase(block: (SQLiteDatabase) -> Unit) {
        requireEmulator()
        val file = context.getDatabasePath("open_source_database.db")
        check(file.exists()) { "Launch the baseline app first to create its database" }
        SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READWRITE).use(block)
    }
}
