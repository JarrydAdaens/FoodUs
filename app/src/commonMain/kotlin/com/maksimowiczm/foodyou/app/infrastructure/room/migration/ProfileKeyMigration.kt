package com.maksimowiczm.foodyou.app.infrastructure.room.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * Adds the messaging public key to the local profile (Milestone 3, Story 3).
 *
 * Purely additive: two nullable columns on an existing table, so no row is rewritten. They are
 * nullable because a profile created under Story 2 predates the key pair; the reconcile path fills
 * them in from the key vault on next use, which is also the recovery path after a restore onto a
 * device whose vault holds a different key.
 *
 * Only public material lands here. The private key is barred from this database by
 * `context/laws.md` §2.
 *
 * Column definitions mirror Room's exported schema 38 so the runtime validation check passes.
 */
object ProfileKeyMigration : Migration(37, 38) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE `Profile` ADD COLUMN `publicKey` TEXT")
        connection.execSQL("ALTER TABLE `Profile` ADD COLUMN `keyAlgorithm` TEXT")
    }
}
