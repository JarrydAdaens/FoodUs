package com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase.domain

/**
 * Raw OOXML parts extracted from a downloaded AFCD nutrient workbook, plus provenance metadata.
 *
 * The platform layer is responsible only for downloading and unzipping; all interpretation happens
 * in common code via [AfcdWorkbookParser].
 *
 * @param worksheets Worksheet entry path (e.g. `"xl/worksheets/sheet2.xml"`) to its XML content.
 * @param publicationDate The server `Last-Modified` value, if reported.
 * @param checksum A content checksum of the downloaded file, if computed.
 */
data class AfcdWorkbookFiles(
    val workbookXml: String,
    val workbookRelsXml: String,
    val sharedStringsXml: String,
    val worksheets: Map<String, String>,
    val publicationDate: String?,
    val checksum: String?,
)
