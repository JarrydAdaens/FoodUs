package com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase

import com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase.domain.AustralianFoodCompositionDatabaseRepository
import com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase.infrastructure.AndroidAustralianFoodCompositionDatabaseRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.bind

actual fun Module.australianFoodCompositionDatabasePlatformModule() {
    factory { AndroidAustralianFoodCompositionDatabaseRepository(androidContext()) }
        .bind<AustralianFoodCompositionDatabaseRepository>()
}
