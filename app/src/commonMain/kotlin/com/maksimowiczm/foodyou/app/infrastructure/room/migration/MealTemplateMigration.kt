package com.maksimowiczm.foodyou.app.infrastructure.room.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * Adds reusable meal templates (Milestone 2, Story 13).
 *
 * Two new additive tables, so every existing row is untouched and the upgrade cannot affect logged
 * diary data:
 * - `MealTemplate`: a named template header.
 * - `MealTemplateItem`: one snapshotted item per template, cascade-deleted with its parent, storing
 *   the item name plus the same embedded nutrient / vitamin / mineral columns used by manual diary
 *   entries so applying a template recreates each item's total nutrition exactly.
 *
 * Column definitions mirror Room's exported schema 34 so the runtime validation check passes.
 */
object MealTemplateMigration : Migration(33, 34) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `MealTemplate` (
                `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `name` TEXT NOT NULL,
                `createdEpochSeconds` INTEGER NOT NULL
            )
            """
                .trimIndent()
        )

        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `MealTemplateItem` (
                `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `templateId` INTEGER NOT NULL,
                `name` TEXT NOT NULL,
                `position` INTEGER NOT NULL,
                `energy` REAL,
                `proteins` REAL,
                `fats` REAL,
                `saturatedFats` REAL,
                `transFats` REAL,
                `monounsaturatedFats` REAL,
                `polyunsaturatedFats` REAL,
                `omega3` REAL,
                `omega6` REAL,
                `carbohydrates` REAL,
                `sugars` REAL,
                `addedSugars` REAL,
                `dietaryFiber` REAL,
                `solubleFiber` REAL,
                `insolubleFiber` REAL,
                `salt` REAL,
                `cholesterolMilli` REAL,
                `caffeineMilli` REAL,
                `vitaminAMicro` REAL,
                `vitaminB1Milli` REAL,
                `vitaminB2Milli` REAL,
                `vitaminB3Milli` REAL,
                `vitaminB5Milli` REAL,
                `vitaminB6Milli` REAL,
                `vitaminB7Micro` REAL,
                `vitaminB9Micro` REAL,
                `vitaminB12Micro` REAL,
                `vitaminCMilli` REAL,
                `vitaminDMicro` REAL,
                `vitaminEMilli` REAL,
                `vitaminKMicro` REAL,
                `manganeseMilli` REAL,
                `magnesiumMilli` REAL,
                `potassiumMilli` REAL,
                `calciumMilli` REAL,
                `copperMilli` REAL,
                `zincMilli` REAL,
                `sodiumMilli` REAL,
                `ironMilli` REAL,
                `phosphorusMilli` REAL,
                `seleniumMicro` REAL,
                `iodineMicro` REAL,
                `chromiumMicro` REAL,
                FOREIGN KEY(`templateId`) REFERENCES `MealTemplate`(`id`)
                    ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """
                .trimIndent()
        )

        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_MealTemplateItem_templateId` " +
                "ON `MealTemplateItem` (`templateId`)"
        )
    }
}
