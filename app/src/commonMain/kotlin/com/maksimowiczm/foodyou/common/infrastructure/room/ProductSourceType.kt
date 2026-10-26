package com.maksimowiczm.foodyou.common.infrastructure.room

import com.maksimowiczm.foodyou.common.domain.food.ProductSource

enum class ProductSourceType {
    User,
    OpenFoodFacts,
    USDA,
    SwissFoodCompositionDatabase,
    AustralianFoodCompositionDatabase,
}

fun ProductSourceType.toDomain(): ProductSource.Type =
    when (this) {
        ProductSourceType.User -> ProductSource.Type.User
        ProductSourceType.OpenFoodFacts -> ProductSource.Type.OpenFoodFacts
        ProductSourceType.USDA -> ProductSource.Type.USDA
        ProductSourceType.SwissFoodCompositionDatabase -> ProductSource.Type.SwissFoodCompositionDatabase
        ProductSourceType.AustralianFoodCompositionDatabase ->
            ProductSource.Type.AustralianFoodCompositionDatabase
    }

fun ProductSource.Type.toEntity(): ProductSourceType =
    when (this) {
        ProductSource.Type.User -> ProductSourceType.User
        ProductSource.Type.OpenFoodFacts -> ProductSourceType.OpenFoodFacts
        ProductSource.Type.USDA -> ProductSourceType.USDA
        ProductSource.Type.SwissFoodCompositionDatabase -> ProductSourceType.SwissFoodCompositionDatabase
        ProductSource.Type.AustralianFoodCompositionDatabase ->
            ProductSourceType.AustralianFoodCompositionDatabase
    }
