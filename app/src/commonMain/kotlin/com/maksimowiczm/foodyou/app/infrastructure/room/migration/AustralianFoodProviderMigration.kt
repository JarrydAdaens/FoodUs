package com.maksimowiczm.foodyou.app.infrastructure.room.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * Groundwork for downloadable food providers (Milestone 2, Story 15 — Australian Food Composition
 * Database).
 *
 * Two additive changes, so every existing row is untouched and the upgrade cannot affect logged
 * diary data or custom foods:
 * - A nullable `sourceRecordId` on `Product`, letting a provider row carry the source's own record
 *   id (e.g. the AFCD Public Food Key) for inspectable, deterministic dataset replacement.
 * - A new `ProviderMetadata` table recording each provider's installed version, publication date,
 *   checksum, import date, and last successful check for the provider UI.
 *
 * Column definitions mirror Room's exported schema 35 so the runtime validation check passes.
 */
object AustralianFoodProviderMigration : Migration(34, 35) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE `Product` ADD COLUMN `sourceRecordId` TEXT")

        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `ProviderMetadata` (
                `sourceType` INTEGER NOT NULL,
                `installedVersion` TEXT,
                `publicationDate` TEXT,
                `checksum` TEXT,
                `importedAtEpochSeconds` INTEGER,
                `lastSuccessfulCheckEpochSeconds` INTEGER,
                `lastError` TEXT,
                PRIMARY KEY(`sourceType`)
            )
            """
                .trimIndent()
        )
    }
}
