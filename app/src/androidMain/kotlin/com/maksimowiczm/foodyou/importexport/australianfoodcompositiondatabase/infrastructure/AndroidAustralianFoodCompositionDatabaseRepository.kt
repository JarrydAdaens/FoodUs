package com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase.infrastructure

import android.content.Context
import com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase.AustralianFoodCompositionDatabaseConfig
import com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase.domain.AfcdRemoteSignature
import com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase.domain.AfcdWorkbookFiles
import com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase.domain.AustralianFoodCompositionDatabaseRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.statement.readRawBytes
import io.ktor.http.HttpHeaders
import java.io.File
import java.security.MessageDigest
import java.util.zip.ZipFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android download source for the AFCD nutrient workbook.
 *
 * Streams the `.xlsx` to a private cache file (never active storage), unzips only the OOXML parts
 * common code needs, then deletes the temp file. The unzip uses the JVM's built-in
 * [java.util.zip.ZipFile] so no third-party spreadsheet dependency is introduced.
 */
internal class AndroidAustralianFoodCompositionDatabaseRepository(private val context: Context) :
    AustralianFoodCompositionDatabaseRepository {

    override suspend fun downloadWorkbook(): AfcdWorkbookFiles =
        withContext(Dispatchers.IO) {
            val tempFile = File.createTempFile("afcd", ".xlsx", context.cacheDir)
            try {
                val (bytes, lastModified) = download()
                tempFile.writeBytes(bytes)
                readWorkbook(tempFile, publicationDate = lastModified, checksum = md5(bytes))
            } finally {
                tempFile.delete()
            }
        }

    private suspend fun download(): Pair<ByteArray, String?> {
        val client = HttpClient { install(HttpTimeout) { requestTimeoutMillis = 120_000 } }
        return client.use {
            val response =
                it.get(AustralianFoodCompositionDatabaseConfig.NUTRIENT_WORKBOOK_URL)
            response.readRawBytes() to response.headers[HttpHeaders.LastModified]
        }
    }

    override suspend fun fetchRemoteSignature(): AfcdRemoteSignature =
        withContext(Dispatchers.IO) {
            val client = HttpClient { install(HttpTimeout) { requestTimeoutMillis = 30_000 } }
            client.use {
                val response = it.head(AustralianFoodCompositionDatabaseConfig.NUTRIENT_WORKBOOK_URL)
                AfcdRemoteSignature(
                    lastModified = response.headers[HttpHeaders.LastModified],
                    entityTag = response.headers[HttpHeaders.ETag],
                    contentLength = response.headers[HttpHeaders.ContentLength],
                )
            }
        }

    private fun readWorkbook(
        file: File,
        publicationDate: String?,
        checksum: String?,
    ): AfcdWorkbookFiles =
        ZipFile(file).use { zip ->
            fun read(name: String): String =
                zip.getEntry(name)?.let { entry ->
                    zip.getInputStream(entry).bufferedReader(Charsets.UTF_8).use { it.readText() }
                } ?: error("Workbook entry '$name' missing")

            val worksheets =
                zip.entries()
                    .asSequence()
                    .map { it.name }
                    .filter { it.startsWith("xl/worksheets/") && it.endsWith(".xml") }
                    .associateWith { read(it) }

            AfcdWorkbookFiles(
                workbookXml = read("xl/workbook.xml"),
                workbookRelsXml = read("xl/_rels/workbook.xml.rels"),
                sharedStringsXml = read("xl/sharedStrings.xml"),
                worksheets = worksheets,
                publicationDate = publicationDate,
                checksum = checksum,
            )
        }

    private fun md5(bytes: ByteArray): String =
        MessageDigest.getInstance("MD5").digest(bytes).joinToString("") {
            (it.toInt() and 0xFF).toString(16).padStart(2, '0')
        }
}
