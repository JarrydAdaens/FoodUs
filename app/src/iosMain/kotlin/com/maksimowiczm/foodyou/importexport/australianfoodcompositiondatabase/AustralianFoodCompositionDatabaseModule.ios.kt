package com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase

import com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase.domain.AfcdWorkbookFiles
import com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase.domain.AustralianFoodCompositionDatabaseRepository
import org.koin.core.module.Module
import org.koin.dsl.bind

/**
 * The Australian Food Composition Database provider ships on Android only; the fork's validated
 * target. iOS binds a stub so the shared module compiles without pulling in a platform xlsx reader.
 */
actual fun Module.australianFoodCompositionDatabasePlatformModule() {
    factory { UnsupportedAustralianFoodCompositionDatabaseRepository }
        .bind<AustralianFoodCompositionDatabaseRepository>()
}

private object UnsupportedAustralianFoodCompositionDatabaseRepository :
    AustralianFoodCompositionDatabaseRepository {
    override suspend fun downloadWorkbook(): AfcdWorkbookFiles =
        throw UnsupportedOperationException(
            "Australian Food Composition Database import is not available on this platform"
        )
}
