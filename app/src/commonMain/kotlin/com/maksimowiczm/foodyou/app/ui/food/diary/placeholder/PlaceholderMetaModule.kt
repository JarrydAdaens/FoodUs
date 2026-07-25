package com.maksimowiczm.foodyou.app.ui.food.diary.placeholder

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel

fun Module.foodDiaryPlaceholder() {
    viewModel { (manualEntryId: Long) ->
        PlaceholderMetaViewModel(
            manualEntryId = manualEntryId,
            manualDiaryEntryRepository = get(),
            mealRepository = get(),
            aiSearchQueryGenerator = get(),
            appConfig = get(),
        )
    }
}
