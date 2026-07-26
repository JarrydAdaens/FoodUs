package com.maksimowiczm.foodyou.app.ui.food.diary.quickadd

import com.maksimowiczm.foodyou.common.infrastructure.koin.applicationCoroutineScope
import kotlinx.datetime.LocalDate
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf

fun Module.foodDiaryQuickAdd() {
    viewModel { (date: LocalDate, mealId: Long) ->
        CreateQuickAddViewModel(
            mealId = mealId,
            date = date,
            manualDiaryEntryRepository = get(),
            dateProvider = get(),
        )
    }
    viewModelOf(::UpdateQuickAddViewModel)
    viewModel { (seed: QuickAddPromotionSeed) ->
        PromoteToRecipePlaceholderViewModel(
            seed = seed,
            createProductUseCase = get(),
            deleteFoodUseCase = get(),
            dateProvider = get(),
            applicationScope = applicationCoroutineScope(),
            savedStateHandle = get(),
        )
    }
}
