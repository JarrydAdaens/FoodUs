package com.maksimowiczm.foodyou.app.ui.groups.profile

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel

internal fun Module.profileUi() {
    viewModel {
        ProfileViewModel(
            repository = get(),
            createProfile = get(),
            renameProfile = get(),
            reconcileProfileKey = get(),
        )
    }
}
