package com.maksimowiczm.foodyou.app.ui.notifications

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel

internal fun Module.notificationsUi() {
    viewModel { NotificationsViewModel(repository = get()) }
}
