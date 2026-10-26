package com.maksimowiczm.foodyou.fooddiary

import android.database.sqlite.SQLiteDatabase
import android.os.Build
import com.maksimowiczm.foodyou.app.infrastructure.room.FoodYouDatabase
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.security.MessageDigest
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

/** Explicitly staged synthetic main-database upgrade check; historical tests remain intact. */
class StructuredLibraryCanonicalUpgradeTest {
    @Test fun schema42PreservesOriginalSyntheticProjectionAndLegacyDefaults() {
        check(Build.HARDWARE == "ranchu" || Build.HARDWARE == "goldfish") {
            "Isolated emulator required"
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val beforeFile = File(context.filesDir, "story25-before.json")
        val original = beforeFile.readText()
        val originalHash = MessageDigest.getInstance("SHA-256")
            .digest(original.toByteArray()).joinToString("") { "%02x".format(it) }
        assertEquals("Known immutable synthetic fixture", ORIGINAL_SHA256, originalHash)
        val before = JSONObject(original)
        assertEquals(39, before.getInt("version"))
        val projection = JSONObject().put("version", 39)
        val after = JSONObject().put("version", FoodYouDatabase.VERSION)
        SQLiteDatabase.openDatabase(
            context.getDatabasePath("open_source_database.db").path,
            null,
            SQLiteDatabase.OPEN_READONLY,
        ).use { db ->
            assertEquals("Production open performed the registered upgrade", FoodYouDatabase.VERSION, db.version)
            db.rawQuery("PRAGMA foreign_key_check", null).use { assertEquals(0, it.count) }
            for (table in TABLES) {
                val expected = before.getJSONArray(table)
                val ids = (0 until expected.length()).map { index ->
                    expected.getJSONObject(index).getString("id").toLong().also {
                        check(it in 2501L..2599L) { "Synthetic fixture IDs required" }
                    }
                }
                val predicate = if (ids.isEmpty()) "0" else "id IN (${ids.joinToString(",")})"
                val rows = JSONArray()
                val projectedRows = JSONArray()
                // Select the original IDs, allowing later legitimate UI additions in this DB.
                db.rawQuery("SELECT * FROM $table WHERE $predicate ORDER BY id", null).use { cursor ->
                    assertEquals("Original row count in $table", expected.length(), cursor.count)
                    while (cursor.moveToNext()) {
                        val expectedRow = expected.getJSONObject(cursor.position)
                        val row = JSONObject()
                        val projected = JSONObject()
                        for (column in 0 until cursor.columnCount) {
                            val name = cursor.getColumnName(column)
                            val value = if (cursor.isNull(column)) JSONObject.NULL else cursor.getString(column)
                            row.put(name, value)
                            if (expectedRow.has(name)) {
                                projected.put(name, value)
                            } else {
                                when (name) {
                                    "calculationVersion" -> assertEquals("Legacy calculation stays 0", "0", value)
                                    "factBasis" -> assertEquals("Legacy metric basis", "METRIC_100", value)
                                    else -> assertTrue("Legacy added field stays unknown: $table.$name", cursor.isNull(column))
                                }
                            }
                        }
                        rows.put(row)
                        projectedRows.put(projected)
                    }
                }
                after.put(table, rows)
                projection.put(table, projectedRows)
            }
        }
        File(context.filesDir, "story28-after.json").writeText(after.toString())
        val projected = projection.toString()
        File(context.filesDir, "story28-legacy-projection.json").writeText(projected)
        assertEquals("Original rows, IDs, values and nulls remain byte-exact", original, projected)
        assertEquals("Original evidence remains immutable", original, beforeFile.readText())
    }

    private companion object {
        const val ORIGINAL_SHA256 = "496dac9591af7ac6569d014dab94facd7d1b6aa8f6bfef9abd8ace57b9dc0659"
        val TABLES = listOf(
            "Meal", "DiaryProduct", "DiaryRecipe", "DiaryRecipeIngredient",
            "Measurement", "ManualDiaryEntry", "MealTemplate", "MealTemplateItem",
        )
    }
}
