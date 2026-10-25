package com.maksimowiczm.foodyou.food.domain

import com.maksimowiczm.foodyou.common.infrastructure.koin.eventHandlerOf
import com.maksimowiczm.foodyou.food.domain.event.NutritionalItemDiaryEntryCreatedEventHandler
import com.maksimowiczm.foodyou.food.domain.usecase.CreateProductUseCase
import com.maksimowiczm.foodyou.food.domain.usecase.CreateRecipeUseCase
import com.maksimowiczm.foodyou.food.domain.usecase.DeleteNutritionalItemUseCase
import com.maksimowiczm.foodyou.food.domain.usecase.DownloadProductUseCase
import com.maksimowiczm.foodyou.food.domain.usecase.ObserveNutritionalItemUseCase
import com.maksimowiczm.foodyou.food.domain.usecase.ObserveMeasurementSuggestionsUseCase
import com.maksimowiczm.foodyou.food.domain.usecase.UpdateProductUseCase
import com.maksimowiczm.foodyou.food.domain.usecase.UpdateRecipeUseCase
import com.maksimowiczm.foodyou.food.domain.usecase.UpdateCapturedDefinitionUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf

fun Module.foodDomainModule() {
    factoryOf(::CreateProductUseCase)
    factoryOf(::CreateRecipeUseCase)
    factoryOf(::DeleteNutritionalItemUseCase)
    factoryOf(::DownloadProductUseCase)
    factoryOf(::ObserveNutritionalItemUseCase)
    factoryOf(::ObserveMeasurementSuggestionsUseCase)
    factoryOf(::UpdateProductUseCase)
    factoryOf(::UpdateRecipeUseCase)
    factoryOf(::UpdateCapturedDefinitionUseCase)

    eventHandlerOf(::NutritionalItemDiaryEntryCreatedEventHandler)
}
