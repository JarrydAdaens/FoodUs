package com.maksimowiczm.foodyou.importexport.structuredlibrary.domain

data class LibraryImportPreview(
    val products:Int, val recipes:Int, val meals:Int, val skipped:Int, val conflicts:Int,
    val metricUnavailable:Int, val nutritionUnavailable:Int,
)
data class LibraryImportResult(val products:Int,val recipes:Int,val meals:Int,val skipped:Int)

interface LibraryImporter {
    suspend fun preview(library:ValidatedLibrary):LibraryImportPreview
    suspend fun apply(library:ValidatedLibrary):LibraryImportResult
}
