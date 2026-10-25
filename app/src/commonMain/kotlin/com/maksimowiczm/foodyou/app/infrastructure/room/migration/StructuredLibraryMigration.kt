package com.maksimowiczm.foodyou.app.infrastructure.room.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/** Preserve the schema-41 graph, including cascading children, while making Recipe yield nullable. */
object StructuredLibraryMigration : Migration(41, 42) {
    override fun migrate(connection: SQLiteConnection) {
        // Foreign keys may already be enabled inside Room's migration transaction. Back up every
        // referencing table before dropping parents: deferred checks alone do not prevent CASCADE.
        val objects = mutableListOf<String>()
        connection.prepare("SELECT sql FROM sqlite_master WHERE type IN ('index', 'trigger') AND sql IS NOT NULL AND tbl_name IN ('Recipe','DiaryRecipe','RecipeIngredient','DiaryRecipeIngredient','FoodEvent','MeasurementSuggestion')").use { statement ->
            while (statement.step()) objects.add(statement.getText(0))
        }
        val sequences = mutableMapOf<String, Long>()
        connection.prepare("SELECT name, seq FROM sqlite_sequence").use { statement ->
            while (statement.step()) sequences[statement.getText(0)] = statement.getLong(1)
        }
        connection.execSQL("DROP VIEW IF EXISTS RecipeAllIngredientsView")
        connection.execSQL("DROP VIEW IF EXISTS LatestMeasurementSuggestion")
        for ((table, columns) in copiedColumns) {
            connection.execSQL("CREATE TEMP TABLE _library_copy_$table AS SELECT $columns FROM $table")
        }
        // Children first; all values are copied explicitly, including IDs and nulls.
        for (table in listOf("RecipeIngredient", "DiaryRecipeIngredient", "FoodEvent", "MeasurementSuggestion", "Recipe", "DiaryRecipe")) {
            connection.execSQL("DROP TABLE $table")
        }
        for (sql in createTables.values) connection.execSQL(sql)
        for ((table, columns) in copiedColumns) {
            connection.execSQL("INSERT INTO $table ($columns) SELECT $columns FROM _library_copy_$table")
            connection.execSQL("DROP TABLE _library_copy_$table")
            sequences[table]?.let { sequence ->
                connection.execSQL("UPDATE sqlite_sequence SET seq = max(seq, $sequence) WHERE name = '$table'")
                connection.execSQL("INSERT INTO sqlite_sequence(name, seq) SELECT '$table', $sequence WHERE NOT EXISTS (SELECT 1 FROM sqlite_sequence WHERE name = '$table')")
            }
        }
        for (sql in objects) connection.execSQL(sql)
        for (sql in createViews) connection.execSQL(sql)
        for (table in listOf("Product", "DiaryProduct")) {
            connection.execSQL("ALTER TABLE $table ADD COLUMN factBasis TEXT NOT NULL DEFAULT 'METRIC_100'")
            connection.execSQL("ALTER TABLE $table ADD COLUMN referencePortion TEXT DEFAULT NULL")
            connection.execSQL("ALTER TABLE $table ADD COLUMN nutritionMetadata TEXT DEFAULT NULL")
        }
        for (table in listOf("Recipe", "DiaryRecipe", "MealTemplate")) {
            connection.execSQL("ALTER TABLE $table ADD COLUMN reportedNutrition TEXT DEFAULT NULL")
        }
        connection.execSQL("ALTER TABLE MealTemplate ADD COLUMN metadata TEXT DEFAULT NULL")
        for (table in listOf("MealTemplateItem", "ManualDiaryEntry")) {
            connection.execSQL("ALTER TABLE $table ADD COLUMN portionDescription TEXT DEFAULT NULL")
            val metadataColumn = if (table == "ManualDiaryEntry") "nutritionMetadata" else "metadata"
            connection.execSQL("ALTER TABLE $table ADD COLUMN $metadataColumn TEXT DEFAULT NULL")
        }
        connection.execSQL("CREATE TABLE IF NOT EXISTS ReportedRecipeComponent (recipeId INTEGER NOT NULL, position INTEGER NOT NULL, component TEXT NOT NULL, PRIMARY KEY(recipeId, position), FOREIGN KEY(recipeId) REFERENCES Recipe(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS DiaryReportedRecipeComponent (recipeId INTEGER NOT NULL, position INTEGER NOT NULL, component TEXT NOT NULL, PRIMARY KEY(recipeId, position), FOREIGN KEY(recipeId) REFERENCES DiaryRecipe(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS LibraryImportIdentity (namespace TEXT NOT NULL, kind TEXT NOT NULL, externalId TEXT NOT NULL, sourceDigest TEXT NOT NULL, localId INTEGER NOT NULL, PRIMARY KEY(namespace, kind, externalId))")
        connection.prepare("PRAGMA foreign_key_check").use { check(!it.step()) { "Library migration foreign key check failed" } }
    }

    // Schema-41 table definitions; only the two servings columns change nullability.
    private val createTables = linkedMapOf(
        "Recipe" to """CREATE TABLE IF NOT EXISTS `Recipe` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `servings` INTEGER, `note` TEXT, `isLiquid` INTEGER NOT NULL)""",
        "RecipeIngredient" to """CREATE TABLE IF NOT EXISTS `RecipeIngredient` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `recipeId` INTEGER NOT NULL, `ingredientProductId` INTEGER, `ingredientRecipeId` INTEGER, `measurement` INTEGER NOT NULL, `quantity` REAL NOT NULL, FOREIGN KEY(`recipeId`) REFERENCES `Recipe`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`ingredientProductId`) REFERENCES `Product`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`ingredientRecipeId`) REFERENCES `Recipe`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )""",
        "FoodEvent" to """CREATE TABLE IF NOT EXISTS `FoodEvent` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `type` INTEGER NOT NULL, `epochSeconds` INTEGER NOT NULL, `extra` TEXT, `productId` INTEGER, `recipeId` INTEGER, FOREIGN KEY(`productId`) REFERENCES `Product`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`recipeId`) REFERENCES `Recipe`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )""",
        "DiaryRecipe" to """CREATE TABLE IF NOT EXISTS `DiaryRecipe` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `servings` INTEGER, `isLiquid` INTEGER NOT NULL, `note` TEXT, `calculationVersion` INTEGER NOT NULL DEFAULT 0)""",
        "DiaryRecipeIngredient" to """CREATE TABLE IF NOT EXISTS `DiaryRecipeIngredient` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `recipeId` INTEGER NOT NULL, `ingredientProductId` INTEGER, `ingredientRecipeId` INTEGER, `measurement` INTEGER NOT NULL, `quantity` REAL NOT NULL, FOREIGN KEY(`recipeId`) REFERENCES `DiaryRecipe`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`ingredientProductId`) REFERENCES `DiaryProduct`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`ingredientRecipeId`) REFERENCES `DiaryRecipe`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )""",
        "MeasurementSuggestion" to """CREATE TABLE IF NOT EXISTS `MeasurementSuggestion` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `productId` INTEGER, `recipeId` INTEGER, `type` INTEGER NOT NULL, `value` REAL NOT NULL, `epochSeconds` INTEGER NOT NULL, FOREIGN KEY(`productId`) REFERENCES `Product`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`recipeId`) REFERENCES `Recipe`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"""
    )
    private val copiedColumns = linkedMapOf(
        "Recipe" to "`id`, `name`, `servings`, `note`, `isLiquid`",
        "RecipeIngredient" to "`id`, `recipeId`, `ingredientProductId`, `ingredientRecipeId`, `measurement`, `quantity`",
        "FoodEvent" to "`id`, `type`, `epochSeconds`, `extra`, `productId`, `recipeId`",
        "DiaryRecipe" to "`id`, `name`, `servings`, `isLiquid`, `note`, `calculationVersion`",
        "DiaryRecipeIngredient" to "`id`, `recipeId`, `ingredientProductId`, `ingredientRecipeId`, `measurement`, `quantity`",
        "MeasurementSuggestion" to "`id`, `productId`, `recipeId`, `type`, `value`, `epochSeconds`"
    )
    private val createViews = listOf(
        """CREATE VIEW `RecipeAllIngredientsView` AS WITH RECURSIVE recipeIngredients(targetRecipeId, ingredientId) AS (
        SELECT recipeId, ingredientRecipeId
        FROM RecipeIngredient
        WHERE ingredientRecipeId IS NOT NULL
        UNION
        SELECT previous.targetRecipeId, child.ingredientRecipeId
        FROM RecipeIngredient child
        INNER JOIN recipeIngredients previous ON child.recipeId = previous.ingredientId
        WHERE child.ingredientRecipeId IS NOT NULL
    )
    SELECT targetRecipeId, ingredientId FROM recipeIngredients""",
        """CREATE VIEW `LatestMeasurementSuggestion` AS SELECT id, productId, recipeId, type, value, epochSeconds
        FROM (
            SELECT 
                ms.*,
                ROW_NUMBER() OVER (
                    PARTITION BY productId, recipeId
                    ORDER BY epochSeconds DESC
                ) AS rn
            FROM MeasurementSuggestion AS ms
        )
        WHERE rn = 1"""
    )
}
