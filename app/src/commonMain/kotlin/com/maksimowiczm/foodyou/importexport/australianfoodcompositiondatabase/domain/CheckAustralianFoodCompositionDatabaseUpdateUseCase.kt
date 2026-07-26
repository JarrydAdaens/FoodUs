package com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase.domain

import com.maksimowiczm.foodyou.common.domain.date.DateProvider
import com.maksimowiczm.foodyou.common.domain.food.FoodSource
import com.maksimowiczm.foodyou.importexport.providermetadata.domain.DatasetComparison
import com.maksimowiczm.foodyou.importexport.providermetadata.domain.DatasetVersion
import com.maksimowiczm.foodyou.importexport.providermetadata.domain.ProviderMetadata
import com.maksimowiczm.foodyou.importexport.providermetadata.domain.ProviderMetadataRepository
import com.maksimowiczm.foodyou.importexport.providermetadata.domain.compareDatasetVersion
import kotlinx.coroutines.flow.first

/** Result of checking the AFCD source for a newer dataset than the one installed locally. */
sealed interface AfcdUpdateCheckOutcome {
    /** The installed dataset matches the remote one. */
    data object UpToDate : AfcdUpdateCheckOutcome

    /** A newer dataset is available; the user must explicitly download and replace. */
    data object UpdateAvailable : AfcdUpdateCheckOutcome

    /** The check could not complete; the local dataset is untouched. */
    data class Failure(val message: String?) : AfcdUpdateCheckOutcome
}

fun interface CheckAustralianFoodCompositionDatabaseUpdateUseCase {
    suspend fun check(): AfcdUpdateCheckOutcome
}

/**
 * Checks whether FSANZ publishes a newer AFCD workbook than the installed one. It performs a single
 * `HEAD` request (no download, no dataset mutation) and compares the remote `Last-Modified` signal
 * against the stored publication date via the shared version-comparison cascade.
 *
 * A successful check — one where the network request completes — records `lastSuccessfulCheck` and
 * clears any prior error, regardless of whether an update was found. A failed check records the
 * error, leaves `lastSuccessfulCheck` untouched (so a failure is never mistaken for a healthy
 * check), and keeps the installed dataset intact. When the comparison cannot decide (no shared
 * identifier, e.g. nothing installed yet) the safe side is taken and an update is offered rather than
 * falsely reporting the dataset current.
 */
internal class CheckAustralianFoodCompositionDatabaseUpdateUseCaseImpl(
    private val repository: AustralianFoodCompositionDatabaseRepository,
    private val metadataRepository: ProviderMetadataRepository,
    private val dateProvider: DateProvider,
) : CheckAustralianFoodCompositionDatabaseUpdateUseCase {

    private val source = FoodSource.Type.AustralianFoodCompositionDatabase

    override suspend fun check(): AfcdUpdateCheckOutcome {
        val previous = metadataRepository.observe(source).first()

        val signature =
            try {
                repository.fetchRemoteSignature()
            } catch (error: Exception) {
                recordFailure(previous, error)
                return AfcdUpdateCheckOutcome.Failure(error.message ?: error::class.simpleName)
            }

        val comparison =
            compareDatasetVersion(local = previous.toLocalVersion(), remote = signature.toVersion())

        recordSuccessfulCheck(previous)

        return when (comparison) {
            DatasetComparison.UpToDate -> AfcdUpdateCheckOutcome.UpToDate
            // Indeterminate errs toward offering an update rather than a false "up to date".
            DatasetComparison.UpdateAvailable,
            DatasetComparison.Indeterminate -> AfcdUpdateCheckOutcome.UpdateAvailable
        }
    }

    private suspend fun recordSuccessfulCheck(previous: ProviderMetadata?) {
        val now = dateProvider.nowInstant().epochSeconds
        metadataRepository.put(
            ProviderMetadata(
                source = source,
                installedVersion = previous?.installedVersion,
                publicationDate = previous?.publicationDate,
                checksum = previous?.checksum,
                importedAtEpochSeconds = previous?.importedAtEpochSeconds,
                lastSuccessfulCheckEpochSeconds = now,
                lastError = null,
            )
        )
    }

    private suspend fun recordFailure(previous: ProviderMetadata?, error: Exception) {
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
}

private fun ProviderMetadata?.toLocalVersion(): DatasetVersion =
    DatasetVersion(
        explicitVersion = this?.installedVersion,
        publicationDate = this?.publicationDate,
        checksum = this?.checksum,
        modifiedTime = this?.publicationDate,
    )

private fun AfcdRemoteSignature.toVersion(): DatasetVersion =
    DatasetVersion(
        // FSANZ does not surface the release label over HTTP, so leave the explicit version unset
        // and let the publication-date tier decide.
        explicitVersion = null,
        publicationDate = lastModified,
        revisionId = entityTag ?: contentLength,
        modifiedTime = lastModified,
    )
