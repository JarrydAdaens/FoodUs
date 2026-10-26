package com.maksimowiczm.foodyou.app.ui.home

import com.maksimowiczm.foodyou.app.ui.home.goals.GoalsViewModel
import com.maksimowiczm.foodyou.app.ui.home.master.HomeViewModel
import com.maksimowiczm.foodyou.app.ui.home.meals.card.DiarySlotsCardsViewModel
import com.maksimowiczm.foodyou.app.ui.home.meals.settings.DiarySlotsCardsSettingsViewModel
import com.maksimowiczm.foodyou.app.ui.home.personalization.HomePersonalizationViewModel
import com.maksimowiczm.foodyou.app.ui.home.poll.PollsViewModel
import com.maksimowiczm.foodyou.common.infrastructure.koin.userPreferencesRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel

fun Module.home() {
    viewModel { HomeViewModel(settingsRepository = userPreferencesRepository()) }
    viewModel {
        DiarySlotsCardsViewModel(
            observeDiarySlotEntriesUseCase = get(),
            foodEntryRepository = get(),
            manualEntryRepository = get(),
            saveMealUseCase = get(),
            applyMealUseCase = get(),
            mealRepository = get(),
            bulkRepository = get(),
            diarySlotsPreferencesRepository = userPreferencesRepository(),
        )
    }
    viewModel {
        DiarySlotsCardsSettingsViewModel(diarySlotsPreferencesRepository = userPreferencesRepository())
    }
    viewModel {
        GoalsViewModel(
            settingsRepository = userPreferencesRepository(),
            observeDiarySlotEntriesUseCase = get(),
            goalsRepository = get(),
        )
    }
    viewModel { HomePersonalizationViewModel(settingsRepository = userPreferencesRepository()) }

    viewModel {
        PollsViewModel(
            observeActivePollUseCase = get(),
            pollPreferencesRepository = userPreferencesRepository(),
        )
    }
}
