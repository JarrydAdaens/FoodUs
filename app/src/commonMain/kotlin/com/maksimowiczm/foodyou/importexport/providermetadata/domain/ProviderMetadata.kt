package com.maksimowiczm.foodyou.importexport.providermetadata.domain

import com.maksimowiczm.foodyou.common.domain.food.FoodSource

/**
 * Domain view of a downloadable provider's installed dataset and last check outcome.
 *
 * @param installed Whether a dataset has ever been imported for this provider.
 */
data class ProviderMetadata(
    val source: FoodSource.Type,
    val installedVersion: String?,
    val publicationDate: String?,
    val checksum: String?,
    val importedAtEpochSeconds: Long?,
    val lastSuccessfulCheckEpochSeconds: Long?,
    val lastError: String?,
) {
    val installed: Boolean
        get() = importedAtEpochSeconds != null
}
