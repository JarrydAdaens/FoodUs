package com.maksimowiczm.foodyou.app.ui.settings.relay

import com.maksimowiczm.foodyou.common.infrastructure.koin.userPreferencesRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel

internal fun Module.relaySettingsModule() {
    viewModel {
        RelaySettingsViewModel(repository = userPreferencesRepository(), connectionChecker = get())
    }
}
