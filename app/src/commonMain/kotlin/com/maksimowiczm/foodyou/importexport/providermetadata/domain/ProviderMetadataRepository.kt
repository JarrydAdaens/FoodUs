package com.maksimowiczm.foodyou.importexport.providermetadata.domain

import com.maksimowiczm.foodyou.common.domain.food.ProductSource
import kotlinx.coroutines.flow.Flow

interface ProviderMetadataRepository {
    fun observe(source: ProductSource.Type): Flow<ProviderMetadata?>

    suspend fun put(metadata: ProviderMetadata)
}
