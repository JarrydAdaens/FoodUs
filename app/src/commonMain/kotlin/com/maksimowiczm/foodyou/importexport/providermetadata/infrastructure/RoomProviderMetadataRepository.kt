package com.maksimowiczm.foodyou.importexport.providermetadata.infrastructure

import com.maksimowiczm.foodyou.common.domain.food.FoodSource
import com.maksimowiczm.foodyou.common.infrastructure.room.toDomain
import com.maksimowiczm.foodyou.common.infrastructure.room.toEntity
import com.maksimowiczm.foodyou.importexport.providermetadata.domain.ProviderMetadata
import com.maksimowiczm.foodyou.importexport.providermetadata.domain.ProviderMetadataRepository
import com.maksimowiczm.foodyou.importexport.providermetadata.infrastructure.room.ProviderMetadataDao
import com.maksimowiczm.foodyou.importexport.providermetadata.infrastructure.room.ProviderMetadataEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RoomProviderMetadataRepository(private val dao: ProviderMetadataDao) :
    ProviderMetadataRepository {
    override fun observe(source: FoodSource.Type): Flow<ProviderMetadata?> =
        dao.observe(source.toEntity()).map { it?.toDomain() }

    override suspend fun put(metadata: ProviderMetadata) {
        dao.upsert(metadata.toEntity())
    }
}

private fun ProviderMetadataEntity.toDomain(): ProviderMetadata =
    ProviderMetadata(
        source = sourceType.toDomain(),
        installedVersion = installedVersion,
        publicationDate = publicationDate,
        checksum = checksum,
        importedAtEpochSeconds = importedAtEpochSeconds,
        lastSuccessfulCheckEpochSeconds = lastSuccessfulCheckEpochSeconds,
        lastError = lastError,
    )

private fun ProviderMetadata.toEntity(): ProviderMetadataEntity =
    ProviderMetadataEntity(
        sourceType = source.toEntity(),
        installedVersion = installedVersion,
        publicationDate = publicationDate,
        checksum = checksum,
        importedAtEpochSeconds = importedAtEpochSeconds,
        lastSuccessfulCheckEpochSeconds = lastSuccessfulCheckEpochSeconds,
        lastError = lastError,
    )
