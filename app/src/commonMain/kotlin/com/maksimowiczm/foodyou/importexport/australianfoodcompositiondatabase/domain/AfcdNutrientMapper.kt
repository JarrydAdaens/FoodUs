package com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase.domain

import com.maksimowiczm.foodyou.common.domain.food.NutrientValue.Companion.toNutrientValue
import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts

/** Column letters for the AFCD nutrient fields Food You imports, resolved from the header row. */
data class AfcdColumnIndex(
    val publicFoodKey: String,
    val name: String,
    val energyKj: String,
    val protein: String,
    val fat: String,
    val carbohydrate: String,
    val dietaryFibre: String?,
    val totalSugars: String?,
    val addedSugars: String?,
    val sodiumMg: String?,
    val calciumMg: String?,
    val ironMg: String?,
)

/** Raised when the workbook does not contain the columns required to import AFCD foods. */
class AfcdFormatException(message: String) : Exception(message)

/**
 * Maps AFCD "All solids & liquids per 100 g" rows onto Food You's [NutritionFacts].
 *
 * Columns are located by their human-readable header text (not fixed positions) so the import
 * survives column reordering between releases. Only values actually supplied by AFCD are mapped;
 * absent cells stay absent. Energy (kJ) and minerals (mg) are normalized via [AfcdUnits].
 */
object AfcdNutrientMapper {

    /** Builds a column index from the header row, or throws [AfcdFormatException] if incompatible. */
    fun buildColumnIndex(headerCells: Map<String, String>): AfcdColumnIndex {
        val byName: List<Pair<String, String>> =
            headerCells.map { (column, header) -> column to normalize(header) }

        fun find(predicate: (String) -> Boolean): String? =
            byName.firstOrNull { predicate(it.second) }?.first

        fun require(field: String, predicate: (String) -> Boolean): String =
            find(predicate)
                ?: throw AfcdFormatException("AFCD workbook is missing required column: $field")

        return AfcdColumnIndex(
            publicFoodKey = require("Public Food Key") { it == "public food key" },
            name = require("Food Name") { it == "food name" },
            energyKj = require("Energy (kJ)") { it.contains("energy with dietary fibre") },
            protein = require("Protein (g)") { it == "protein (g)" },
            fat = require("Fat, total (g)") { it == "fat, total (g)" },
            carbohydrate =
                require("Available carbohydrate") {
                    it.contains("available carbohydrate, with sugar alcohols")
                },
            dietaryFibre = find { it == "total dietary fibre (g)" },
            totalSugars = find { it == "total sugars (g)" },
            addedSugars = find { it == "added sugars (g)" },
            sodiumMg = find { it.startsWith("sodium (na)") },
            calciumMg = find { it.startsWith("calcium (ca)") },
            ironMg = find { it.startsWith("iron (fe)") },
        )
    }

    /**
     * Maps one data row to an [AfcdProduct], or null when the row has no food key or name (e.g. a
     * blank or heading row).
     */
    fun map(index: AfcdColumnIndex, rowCells: Map<String, String>): AfcdProduct? {
        val key = rowCells[index.publicFoodKey]?.trim().orEmpty()
        val name = rowCells[index.name]?.trim().orEmpty()
        if (key.isEmpty() || name.isEmpty()) return null

        fun grams(column: String?): Double? =
            column?.let { AfcdUnits.parseNumber(rowCells[it]) }

        fun mineralGrams(column: String?): Double? =
            AfcdUnits.milligramsToGrams(column?.let { AfcdUnits.parseNumber(rowCells[it]) })

        val energyKcal =
            AfcdUnits.kilojoulesToKilocalories(AfcdUnits.parseNumber(rowCells[index.energyKj]))

        val nutritionFacts =
            NutritionFacts(
                energy = energyKcal.toNutrientValue(),
                proteins = grams(index.protein).toNutrientValue(),
                fats = grams(index.fat).toNutrientValue(),
                carbohydrates = grams(index.carbohydrate).toNutrientValue(),
                dietaryFiber = grams(index.dietaryFibre).toNutrientValue(),
                sugars = grams(index.totalSugars).toNutrientValue(),
                addedSugars = grams(index.addedSugars).toNutrientValue(),
                sodium = mineralGrams(index.sodiumMg).toNutrientValue(),
                calcium = mineralGrams(index.calciumMg).toNutrientValue(),
                iron = mineralGrams(index.ironMg).toNutrientValue(),
            )

        return AfcdProduct(publicFoodKey = key, name = name, nutritionFacts = nutritionFacts)
    }

    /** Lower-cases, collapses internal whitespace (incl. newlines), and trims a header cell. */
    private fun normalize(header: String): String =
        header.replace('\n', ' ').replace('\r', ' ').trim().lowercase().replace(WHITESPACE, " ")

    private val WHITESPACE = Regex("\\s+")
}
