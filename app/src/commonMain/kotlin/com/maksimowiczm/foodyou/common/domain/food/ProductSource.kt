package com.maksimowiczm.foodyou.common.domain.food

data class ProductSource(val type: Type, val url: String? = null) {
    enum class Type {
        User,
        OpenFoodFacts,
        USDA,
        SwissFoodCompositionDatabase,
        AustralianFoodCompositionDatabase,
    }
}
