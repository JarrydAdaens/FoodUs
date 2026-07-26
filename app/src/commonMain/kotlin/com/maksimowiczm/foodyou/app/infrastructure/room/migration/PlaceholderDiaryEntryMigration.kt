package com.maksimowiczm.foodyou.app.infrastructure.room.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * Adds fast-text placeholder support (Milestone 2, Story 8) to [ManualDiaryEntry][
 * com.maksimowiczm.foodyou.fooddiary.infrastructure.room.ManualDiaryEntryEntity].
 *
 * Two additive, backward-compatible columns:
 * - `description`: nullable free text holding optional placeholder context.
 * - `isPlaceholder`: flags zero-nutrition placeholder entries so Story 9 can route them to their
 *   dedicated resolution editor. Defaults to `0` so every existing manual / Quick Add entry keeps
 *   its current (non-placeholder) meaning and its logged nutrition is left untouched.
 */
object PlaceholderDiaryEntryMigration : Migration(32, 33) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE `ManualDiaryEntry` ADD COLUMN `description` TEXT")
        connection.execSQL(
            "ALTER TABLE `ManualDiaryEntry` ADD COLUMN `isPlaceholder` INTEGER NOT NULL DEFAULT 0"
        )
    }
}
