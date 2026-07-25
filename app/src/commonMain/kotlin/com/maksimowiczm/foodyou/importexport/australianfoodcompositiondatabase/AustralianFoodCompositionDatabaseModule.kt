package com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase

import com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase.domain.ImportAustralianFoodCompositionDatabaseUseCase
import com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase.domain.ImportAustralianFoodCompositionDatabaseUseCaseImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

/** Binds the platform-specific [AustralianFoodCompositionDatabaseRepository] download source. */
expect fun Module.australianFoodCompositionDatabasePlatformModule()

val importExportAustralianFoodCompositionDatabaseModule = module {
    australianFoodCompositionDatabasePlatformModule()

    factoryOf(::ImportAustralianFoodCompositionDatabaseUseCaseImpl)
        .bind<ImportAustralianFoodCompositionDatabaseUseCase>()
}
