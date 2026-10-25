package com.maksimowiczm.foodyou.food.infrastructure

import com.maksimowiczm.foodyou.food.domain.repository.NutritionalItemHistoryRepository
import com.maksimowiczm.foodyou.food.domain.repository.NutritionalItemMeasurementSuggestionRepository
import com.maksimowiczm.foodyou.food.domain.repository.ProductRepository
import com.maksimowiczm.foodyou.food.domain.repository.RecipeRepository
import com.maksimowiczm.foodyou.food.domain.repository.RemoteProductRequestFactory
import com.maksimowiczm.foodyou.food.infrastructure.network.RemoteProductMapper
import com.maksimowiczm.foodyou.food.infrastructure.network.RemoteProductRequestFactoryImpl
import com.maksimowiczm.foodyou.food.infrastructure.openfoodfacts.openFoodFactsModule
import com.maksimowiczm.foodyou.food.infrastructure.repository.RoomNutritionalItemHistoryRepository
import com.maksimowiczm.foodyou.food.infrastructure.repository.RoomNutritionalItemMeasurementSuggestionRepository
import com.maksimowiczm.foodyou.food.infrastructure.repository.RoomProductRepository
import com.maksimowiczm.foodyou.food.infrastructure.repository.RoomRecipeRepository
import com.maksimowiczm.foodyou.food.infrastructure.room.FoodDatabase
import com.maksimowiczm.foodyou.food.infrastructure.usda.USDAModule
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.scope.Scope
import org.koin.dsl.bind

fun Module.foodInfrastructureModule() {
    factory { database.foodEventDao }
    factory { database.measurementSuggestionDao }
    factory { database.productDao }
    factory { database.recipeDao }

    factoryOf(::RoomNutritionalItemHistoryRepository).bind<NutritionalItemHistoryRepository>()
    factoryOf(::RoomNutritionalItemMeasurementSuggestionRepository).bind<NutritionalItemMeasurementSuggestionRepository>()
    factoryOf(::RoomProductRepository).bind<ProductRepository>()
    factoryOf(::RoomRecipeRepository).bind<RecipeRepository>()

    factoryOf(::RemoteProductRequestFactoryImpl).bind<RemoteProductRequestFactory>()
    factoryOf(::RemoteProductMapper)

    USDAModule()
    openFoodFactsModule()
}

private val Scope.database: FoodDatabase
    get() = get()
