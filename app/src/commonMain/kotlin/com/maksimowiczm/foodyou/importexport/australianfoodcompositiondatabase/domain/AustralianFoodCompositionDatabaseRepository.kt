package com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase.domain

/**
 * Downloads the Australian Food Composition Database nutrient workbook. Platform-specific: the
 * network fetch, temporary storage, and unzip are provided per target. Common code only parses the
 * returned [AfcdWorkbookFiles].
 */
interface AustralianFoodCompositionDatabaseRepository {
    suspend fun downloadWorkbook(): AfcdWorkbookFiles

    /**
     * Fetches the workbook's remote version signals via an HTTP `HEAD` request, without downloading
     * the file. Used by the update check to compare against the installed dataset.
     */
    suspend fun fetchRemoteSignature(): AfcdRemoteSignature
}
