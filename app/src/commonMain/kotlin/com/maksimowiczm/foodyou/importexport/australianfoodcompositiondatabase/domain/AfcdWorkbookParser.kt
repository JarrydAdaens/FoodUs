package com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase.domain

import com.maksimowiczm.foodyou.importexport.xlsx.XlsxSheetReader
import com.maksimowiczm.foodyou.importexport.xlsx.XlsxWorkbook

/**
 * Turns a downloaded AFCD nutrient workbook into a list of [AfcdProduct]s.
 *
 * Locates the per-100 g worksheet by name, resolves the header row (the row declaring the "Public
 * Food Key" column), then maps every subsequent data row. Pure and deterministic so it can be unit
 * tested end to end from raw XML fixtures.
 */
object AfcdWorkbookParser {

    /** Visible name of the primary nutrient worksheet in the AFCD nutrient workbook. */
    const val PRIMARY_SHEET_NAME: String = "All solids & liquids per 100 g"

    fun parse(files: AfcdWorkbookFiles): List<AfcdProduct> {
        val sheetPath =
            XlsxWorkbook.resolveSheetPath(
                workbookXml = files.workbookXml,
                workbookRelsXml = files.workbookRelsXml,
                sheetName = PRIMARY_SHEET_NAME,
            ) ?: throw AfcdFormatException("Worksheet '$PRIMARY_SHEET_NAME' not found in workbook")

        val sheetXml =
            files.worksheets[sheetPath]
                ?: throw AfcdFormatException("Worksheet entry '$sheetPath' missing from workbook")

        val sharedStrings = XlsxSheetReader.parseSharedStrings(files.sharedStringsXml)

        val products = mutableListOf<AfcdProduct>()
        var index: AfcdColumnIndex? = null

        XlsxSheetReader.forEachRow(sharedStrings, sheetXml) { row ->
            val currentIndex = index
            if (currentIndex == null) {
                if (row.cells.values.any { it.trim().equals("Public Food Key", ignoreCase = true) }
                ) {
                    index = AfcdNutrientMapper.buildColumnIndex(row.cells)
                }
            } else {
                AfcdNutrientMapper.map(currentIndex, row.cells)?.let(products::add)
            }
        }

        if (index == null) {
            throw AfcdFormatException("AFCD header row (with 'Public Food Key') not found")
        }
        if (products.isEmpty()) {
            throw AfcdFormatException("AFCD workbook contained no food rows")
        }
        return products
    }
}
