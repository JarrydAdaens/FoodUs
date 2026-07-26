package com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase.domain

import com.maksimowiczm.foodyou.common.domain.database.TransactionProvider
import com.maksimowiczm.foodyou.common.domain.date.DateProvider
import com.maksimowiczm.foodyou.common.domain.food.FoodSource
import com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase.AustralianFoodCompositionDatabaseConfig
import com.maksimowiczm.foodyou.importexport.providermetadata.domain.ProviderMetadata
import com.maksimowiczm.foodyou.importexport.providermetadata.domain.ProviderMetadataRepository
import com.maksimowiczm.foodyou.food.domain.entity.FoodHistory
import com.maksimowiczm.foodyou.food.domain.repository.FoodHistoryRepository
import com.maksimowiczm.foodyou.food.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first

/** Progress of an AFCD import, surfaced to the UI. */
sealed interface AfcdImportProgress {
    data object Downloading : AfcdImportProgress

    /** Transactional replace in flight; [fraction] is 0f..1f of rows written. */
    data class Importing(val fraction: Float) : AfcdImportProgress

    data object Finished : AfcdImportProgress
}

fun interface ImportAustralianFoodCompositionDatabaseUseCase {
    fun import(): Flow<AfcdImportProgress>
}

/**
 * Imports the AFCD dataset with the import-safety ordering the story requires: download to
 * temporary storage → validate/parse fully in memory → a single transactional replace
 * (delete-by-source, guarded to never touch user foods → insert) → persist provider metadata.
 *
 * The download and parse happen before any database mutation, so a network or format failure leaves
 * the previously imported dataset completely untouched. If the transaction itself fails it rolls
 * back, again preserving the last-good dataset. On any failure the error is recorded in provider
 * metadata (without disturbing the installed-version fields) and rethrown for the UI.
 */
internal class ImportAustralianFoodCompositionDatabaseUseCaseImpl(
    private val repository: AustralianFoodCompositionDatabaseRepository,
    private val transactionProvider: TransactionProvider,
    private val productRepository: ProductRepository,
    private val historyRepository: FoodHistoryRepository,
    private val metadataRepository: ProviderMetadataRepository,
    private val dateProvider: DateProvider,
) : ImportAustralianFoodCompositionDatabaseUseCase {

    override fun import(): Flow<AfcdImportProgress> = channelFlow {
        val source = FoodSource.Type.AustralianFoodCompositionDatabase
        try {
            send(AfcdImportProgress.Downloading)

            // Download + validate + parse BEFORE touching the database.
            val files = repository.downloadWorkbook()
            val products = AfcdWorkbookParser.parse(files)

            send(AfcdImportProgress.Importing(0f))

            transactionProvider.withTransaction {
                productRepository.deleteProductsBySource(source)

                val total = products.size
                products.forEachIndexed { position, product ->
                    val id =
                        productRepository.insertProduct(
                            name = product.name,
                            brand = null,
                            barcode = null,
                            note = null,
                            isLiquid = false,
                            packageWeight = null,
                            servingWeight = null,
                            source =
                                FoodSource(
                                    type = source,
                                    url = AustralianFoodCompositionDatabaseConfig.SOURCE_URL,
                                ),
                            nutritionFacts = product.nutritionFacts,
                            sourceRecordId = product.publicFoodKey,
                        )
                    historyRepository.insert(
                        id,
                        FoodHistory.Imported(timestamp = dateProvider.nowInstant()),
                    )

                    if (position % PROGRESS_STRIDE == 0 || position == total - 1) {
                        send(AfcdImportProgress.Importing((position + 1).toFloat() / total))
                    }
                }
            }

            val now = dateProvider.nowInstant().epochSeconds
            metadataRepository.put(
                ProviderMetadata(
                    source = source,
                    installedVersion = AustralianFoodCompositionDatabaseConfig.RELEASE_LABEL,
                    publicationDate = files.publicationDate,
                    checksum = files.checksum,
                    importedAtEpochSeconds = now,
                    lastSuccessfulCheckEpochSeconds = now,
                    lastError = null,
                )
            )

            send(AfcdImportProgress.Finished)
        } catch (error: Exception) {
            recordFailure(source, error)
            throw error
        }
    }

    /** Records the failure while preserving any previously installed dataset metadata. */
    private suspend fun recordFailure(source: FoodSource.Type, error: Exception) {
        val previous = metadataRepository.observe(source).first()
        metadataRepository.put(
            ProviderMetadata(
                source = source,
                installedVersion = previous?.installedVersion,
                publicationDate = previous?.publicationDate,
                checksum = previous?.checksum,
                importedAtEpochSeconds = previous?.importedAtEpochSeconds,
                lastSuccessfulCheckEpochSeconds = previous?.lastSuccessfulCheckEpochSeconds,
                lastError = error.message ?: error::class.simpleName,
            )
        )
    }

    private companion object {
        const val PROGRESS_STRIDE = 50
    }
}
