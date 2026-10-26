package com.maksimowiczm.foodyou.fooddiary

import android.database.sqlite.SQLiteDatabase
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

/** Version-aware schema41 verification; does not replace or edit historical39/40 fixtures. */
class MealCanonicalUpgradeTest {
    @Test fun schema41PreservesOriginalSyntheticProjectionAndNewDefaults() {
        check(Build.HARDWARE == "ranchu" || Build.HARDWARE == "goldfish") { "Isolated emulator required" }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val beforeFile = File(context.filesDir, "story25-before.json")
        val original = beforeFile.readText()
        val before = JSONObject(original)
        assertEquals(39, before.getInt("version"))
        SQLiteDatabase.openDatabase(context.getDatabasePath("open_source_database.db").path, null, SQLiteDatabase.OPEN_READWRITE).use { db ->
            assertEquals(41, db.version)
            db.rawQuery("PRAGMA foreign_key_check", null).use { assertEquals(0, it.count) }
            val after = JSONObject().put("version", db.version)
            for (table in listOf("Meal", "DiaryProduct", "DiaryRecipe", "DiaryRecipeIngredient",
                "Measurement", "ManualDiaryEntry", "MealTemplate", "MealTemplateItem")) {
                val rows = JSONArray()
                db.rawQuery("SELECT * FROM " + table + " WHERE id BETWEEN 2501 AND 2599 ORDER BY id", null).use { cursor ->
                    while (cursor.moveToNext()) {
                        val row = JSONObject()
                        for (column in 0 until cursor.columnCount) row.put(cursor.getColumnName(column),
                            if (cursor.isNull(column)) JSONObject.NULL else cursor.getString(column))
                        rows.put(row)
                    }
                }
                after.put(table, rows)
            }
            File(context.filesDir, "story24-after.json").writeText(after.toString())
            for (table in listOf("MealTemplateItem", "ManualDiaryEntry")) {
                val columns = if (table == "MealTemplateItem") listOf("capturedPortionType", "capturedPortionValue", "servingCount", "weightGrams")
                    else listOf("capturedPortionType", "capturedPortionValue")
                val rows = after.getJSONArray(table)
                for (index in 0 until rows.length()) for (column in columns) {
                    val row = rows.getJSONObject(index)
                    assertTrue("Legacy quantity stays unknown: " + table + "." + column, row.has(column) && row.isNull(column))
                    row.remove(column)
                }
            }
            val recipes = after.getJSONArray("DiaryRecipe")
            for (index in 0 until recipes.length()) {
                val row = recipes.getJSONObject(index)
                assertEquals("Legacy Recipe calculation is not reinterpreted", "0", row.getString("calculationVersion"))
                row.remove("calculationVersion")
            }
            after.put("version", 39)
            val projection = after.toString()
            File(context.filesDir, "story24-legacy-projection.json").writeText(projection)
            assertEquals("Original columns/rows/IDs/nutrition remain exact after both registered paths", original, projection)
            assertEquals("Original before evidence remains immutable", original, beforeFile.readText())
        }
    }
}
