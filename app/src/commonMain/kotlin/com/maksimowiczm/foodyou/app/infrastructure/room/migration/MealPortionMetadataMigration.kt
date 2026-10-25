package com.maksimowiczm.foodyou.app.infrastructure.room.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/** Add quantity context without reinterpreting existing absolute facts or Recipe contracts. */
object MealPortionMetadataMigration : Migration(40, 41) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE MealTemplateItem ADD COLUMN capturedPortionType INTEGER DEFAULT NULL")
        connection.execSQL("ALTER TABLE MealTemplateItem ADD COLUMN capturedPortionValue REAL DEFAULT NULL")
        connection.execSQL("ALTER TABLE MealTemplateItem ADD COLUMN servingCount REAL DEFAULT NULL")
        connection.execSQL("ALTER TABLE MealTemplateItem ADD COLUMN weightGrams REAL DEFAULT NULL")
        connection.execSQL("ALTER TABLE ManualDiaryEntry ADD COLUMN capturedPortionType INTEGER DEFAULT NULL")
        connection.execSQL("ALTER TABLE ManualDiaryEntry ADD COLUMN capturedPortionValue REAL DEFAULT NULL")
    }
}
