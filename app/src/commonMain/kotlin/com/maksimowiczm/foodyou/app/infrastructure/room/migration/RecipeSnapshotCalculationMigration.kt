package com.maksimowiczm.foodyou.app.infrastructure.room.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.maksimowiczm.foodyou.food.search.infrastructure.room.RECIPE_ANCESTRY_SQL

/** Preserve captured calculations and make recipe-only exclusion traversal safe for legacy graphs. */
object RecipeSnapshotCalculationMigration : Migration(39, 40) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE DiaryRecipe ADD COLUMN calculationVersion INTEGER NOT NULL DEFAULT 0")
        connection.execSQL("DROP VIEW IF EXISTS RecipeAllIngredientsView")
        connection.execSQL("CREATE VIEW `RecipeAllIngredientsView` AS ${RECIPE_ANCESTRY_SQL.trim()}")
    }
}
