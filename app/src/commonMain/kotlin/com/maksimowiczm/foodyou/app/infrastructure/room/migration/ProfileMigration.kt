package com.maksimowiczm.foodyou.app.infrastructure.room.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * Adds the local profile (Milestone 3, Story 2).
 *
 * One new additive table, so every existing row is untouched and the upgrade cannot affect logged
 * diary data. `Profile` holds at most one row: the device's GUID identity plus its cosmetic
 * username and created / last-edited stamps.
 *
 * Column definitions mirror Room's exported schema 37 so the runtime validation check passes.
 */
object ProfileMigration : Migration(36, 37) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `Profile` (
                `id` TEXT NOT NULL,
                `username` TEXT NOT NULL,
                `createdEpochSeconds` INTEGER NOT NULL,
                `lastEditedEpochSeconds` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """
                .trimIndent()
        )
    }
}
