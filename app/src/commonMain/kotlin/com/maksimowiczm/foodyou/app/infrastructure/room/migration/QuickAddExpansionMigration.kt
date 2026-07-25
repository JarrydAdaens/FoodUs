package com.maksimowiczm.foodyou.app.infrastructure.room.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * Adds expanded Quick Add quantity context (Milestone 2, Story 18) to [ManualDiaryEntry][
 * com.maksimowiczm.foodyou.fooddiary.infrastructure.room.ManualDiaryEntryEntity].
 *
 * Two additive, nullable columns:
 * - `servingCount`: optional number of servings the entry represents.
 * - `weightGrams`: optional total weight of the entry in grams.
 *
 * Both are quantity context only. The entry's embedded nutrient columns remain its absolute total
 * and are deliberately left untouched, so this upgrade never rewrites logged nutrition. Existing
 * rows read back with `NULL` for both, matching the "serving count treated as 1, weight absent"
 * defaults. Description and fibre need no migration here: `description` arrived with Story 8 (32→33)
 * and fibre already persists in the embedded `dietaryFiber` column.
 */
object QuickAddExpansionMigration : Migration(35, 36) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE `ManualDiaryEntry` ADD COLUMN `servingCount` REAL")
        connection.execSQL("ALTER TABLE `ManualDiaryEntry` ADD COLUMN `weightGrams` REAL")
    }
}
