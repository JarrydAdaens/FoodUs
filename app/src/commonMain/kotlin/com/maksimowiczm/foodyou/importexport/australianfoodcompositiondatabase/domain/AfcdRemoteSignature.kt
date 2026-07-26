package com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase.domain

/**
 * Lightweight remote version signals for the AFCD workbook, obtained from an HTTP `HEAD` request so
 * an update check never downloads the multi-megabyte file. FSANZ does not expose the release number
 * over HTTP, so the strongest available signal is the `Last-Modified` publication date, with the
 * `ETag` and `Content-Length` as weaker fallbacks.
 *
 * @param lastModified The server `Last-Modified` header value, if reported.
 * @param entityTag The server `ETag` header value, if reported.
 * @param contentLength The server `Content-Length` header value, if reported.
 */
data class AfcdRemoteSignature(
    val lastModified: String?,
    val entityTag: String?,
    val contentLength: String?,
)
