package com.maksimowiczm.foodyou.importexport.providermetadata.infrastructure.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.maksimowiczm.foodyou.common.infrastructure.room.FoodSourceType

/**
 * Persisted state for a downloadable food provider dataset. One row per provider [sourceType].
 *
 * Records what is installed and the outcome of the most recent import / update check so the
 * provider UI can show install state, update state, and the last successful check without
 * re-downloading. Enablement itself lives in [com.maksimowiczm.foodyou.food.search.domain
 * .FoodSearchPreferences]; this table only describes the installed dataset.
 */
@Entity(tableName = "ProviderMetadata")
data class ProviderMetadataEntity(
    @PrimaryKey val sourceType: FoodSourceType,
    val installedVersion: String?,
    val publicationDate: String?,
    val checksum: String?,
    val importedAtEpochSeconds: Long?,
    val lastSuccessfulCheckEpochSeconds: Long?,
    val lastError: String?,
)
