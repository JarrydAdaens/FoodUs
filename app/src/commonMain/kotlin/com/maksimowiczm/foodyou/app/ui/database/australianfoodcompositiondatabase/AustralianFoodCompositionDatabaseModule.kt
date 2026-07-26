package com.maksimowiczm.foodyou.app.ui.database.australianfoodcompositiondatabase

import com.maksimowiczm.foodyou.common.infrastructure.koin.userPreferencesRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel

internal fun Module.australianFoodCompositionDatabaseModule() {
    viewModel {
        AustralianFoodCompositionDatabaseViewModel(
            importUseCase = get(),
            checkUpdateUseCase = get(),
            preferencesRepository = userPreferencesRepository(),
            metadataRepository = get(),
        )
    }
}
