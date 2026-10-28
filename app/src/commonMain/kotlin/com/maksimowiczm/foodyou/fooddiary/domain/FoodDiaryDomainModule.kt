package com.maksimowiczm.foodyou.fooddiary.domain

import com.maksimowiczm.foodyou.common.infrastructure.koin.userPreferencesRepository
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.CreateMealDefinitionUseCase
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.ApplyMealUseCase
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.CreateNutritionalItemDiaryEntryUseCase
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.ObserveDiarySlotEntriesUseCase
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.UpdateMealUseCase
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.SaveMealUseCase
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.UnpackNutritionalItemDiaryEntryUseCase
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.UpdateNutritionalItemDiaryEntryUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf

internal fun Module.foodDiaryDomainModule() {
    factoryOf(::CreateNutritionalItemDiaryEntryUseCase)
    factoryOf(::UnpackNutritionalItemDiaryEntryUseCase)
    factoryOf(::UpdateNutritionalItemDiaryEntryUseCase)
    factoryOf(::SaveMealUseCase)
    factoryOf(::CreateMealDefinitionUseCase)
    factoryOf(::UpdateMealUseCase)
    factoryOf(::ApplyMealUseCase)

    factory {
        ObserveDiarySlotEntriesUseCase(
            diarySlotRepository = get(),
            diarySlotsPreferencesRepository = userPreferencesRepository(),
            foodEntryRepository = get(),
            manualEntryRepository = get(),
            dateProvider = get(),
        )
    }
}
