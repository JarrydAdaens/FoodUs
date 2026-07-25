package com.maksimowiczm.foodyou.importexport.providermetadata

import com.maksimowiczm.foodyou.importexport.providermetadata.domain.ProviderMetadataRepository
import com.maksimowiczm.foodyou.importexport.providermetadata.infrastructure.RoomProviderMetadataRepository
import com.maksimowiczm.foodyou.importexport.providermetadata.infrastructure.room.ProviderMetadataDatabase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val providerMetadataModule = module {
    factory { get<ProviderMetadataDatabase>().providerMetadataDao }
    factoryOf(::RoomProviderMetadataRepository).bind<ProviderMetadataRepository>()
}
