package com.maksimowiczm.foodyou.importexport.providermetadata.infrastructure.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.maksimowiczm.foodyou.common.infrastructure.room.FoodSourceType
import kotlinx.coroutines.flow.Flow

@Dao
interface ProviderMetadataDao {
    @Query(
        """
        SELECT *
        FROM ProviderMetadata
        WHERE sourceType = :source
        """
    )
    fun observe(source: FoodSourceType): Flow<ProviderMetadataEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ProviderMetadataEntity)
}
