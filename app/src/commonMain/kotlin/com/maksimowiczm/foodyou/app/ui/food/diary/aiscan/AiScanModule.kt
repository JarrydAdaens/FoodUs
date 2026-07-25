package com.maksimowiczm.foodyou.app.ui.food.diary.aiscan

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf

fun Module.foodDiaryAiScan() {
    viewModelOf(::AiScanViewModel)
}
