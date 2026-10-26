package com.maksimowiczm.foodyou.app.ui.food.diary.fasttext

import kotlinx.datetime.LocalDate
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel

fun Module.foodDiaryFastText() {
    viewModel { (date: LocalDate, diarySlotId: Long) ->
        FastTextViewModel(
            diarySlotId = diarySlotId,
            date = date,
            manualDiaryEntryRepository = get(),
            dateProvider = get(),
        )
    }
}
