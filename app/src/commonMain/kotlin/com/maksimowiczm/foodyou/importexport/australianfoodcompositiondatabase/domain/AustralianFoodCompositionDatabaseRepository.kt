package com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase.domain

/**
 * Downloads the Australian Food Composition Database nutrient workbook. Platform-specific: the
 * network fetch, temporary storage, and unzip are provided per target. Common code only parses the
 * returned [AfcdWorkbookFiles].
 */
interface AustralianFoodCompositionDatabaseRepository {
    suspend fun downloadWorkbook(): AfcdWorkbookFiles
}
