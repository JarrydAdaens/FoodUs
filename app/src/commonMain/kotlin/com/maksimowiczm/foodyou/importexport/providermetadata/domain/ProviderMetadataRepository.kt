package com.maksimowiczm.foodyou.importexport.providermetadata.domain

import com.maksimowiczm.foodyou.common.domain.food.FoodSource
import kotlinx.coroutines.flow.Flow

interface ProviderMetadataRepository {
    fun observe(source: FoodSource.Type): Flow<ProviderMetadata?>

    suspend fun put(metadata: ProviderMetadata)
}
