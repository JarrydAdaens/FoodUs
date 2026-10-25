package com.maksimowiczm.foodyou.fooddiary.infrastructure

import com.maksimowiczm.foodyou.common.infrastructure.koin.userPreferencesRepositoryOf
import com.maksimowiczm.foodyou.fooddiary.domain.repository.DiarySlotRepository
import com.maksimowiczm.foodyou.fooddiary.domain.repository.NutritionalItemDiaryEntryRepository
import com.maksimowiczm.foodyou.fooddiary.domain.repository.ManualDiaryEntryRepository
import com.maksimowiczm.foodyou.fooddiary.domain.repository.MealRepository
import com.maksimowiczm.foodyou.fooddiary.domain.service.LocalizedDiarySlotsProvider
import com.maksimowiczm.foodyou.fooddiary.infrastructure.compose.ComposeLocalizedDiarySlotsProvider
import com.maksimowiczm.foodyou.fooddiary.infrastructure.repository.DataStoreDiarySlotsPreferencesRepository
import com.maksimowiczm.foodyou.fooddiary.infrastructure.repository.RoomDiarySlotRepository
import com.maksimowiczm.foodyou.fooddiary.infrastructure.repository.RoomNutritionalItemDiaryEntryRepository
import com.maksimowiczm.foodyou.fooddiary.infrastructure.repository.RoomManualDiaryEntryRepository
import com.maksimowiczm.foodyou.fooddiary.infrastructure.repository.RoomMealRepository
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.FoodDiaryDatabase
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.InitializeDiarySlotsCallback
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.scope.Scope
import org.koin.dsl.bind

internal fun Module.foodDiaryInfrastructureModule() {
    factoryOf(::ComposeLocalizedDiarySlotsProvider).bind<LocalizedDiarySlotsProvider>()

    userPreferencesRepositoryOf(::DataStoreDiarySlotsPreferencesRepository)
    factoryOf(::RoomNutritionalItemDiaryEntryRepository).bind<NutritionalItemDiaryEntryRepository>()
    factoryOf(::RoomManualDiaryEntryRepository).bind<ManualDiaryEntryRepository>()
    factoryOf(::RoomDiarySlotRepository).bind<DiarySlotRepository>()
    factoryOf(::RoomMealRepository).bind<MealRepository>()

    factoryOf(::InitializeDiarySlotsCallback)

    factory { database.diarySlotDao }
    factory { database.manualDiaryEntryDao }
    factory { database.measurementDao }
    factory { database.mealDao }
}

private val Scope.database: FoodDiaryDatabase
    get() = get()
