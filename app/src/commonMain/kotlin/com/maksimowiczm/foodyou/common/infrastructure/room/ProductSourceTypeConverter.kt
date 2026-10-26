package com.maksimowiczm.foodyou.common.infrastructure.room

import androidx.room.TypeConverter

internal class ProductSourceTypeConverter {

    @TypeConverter
    fun fromFoodSourceType(value: ProductSourceType): Int =
        when (value) {
            ProductSourceType.User -> FoodSourceTypeSQLConstants.USER
            ProductSourceType.OpenFoodFacts -> FoodSourceTypeSQLConstants.OPEN_FOOD_FACTS
            ProductSourceType.USDA -> FoodSourceTypeSQLConstants.USDA
            ProductSourceType.SwissFoodCompositionDatabase ->
                FoodSourceTypeSQLConstants.SWISS_FOOD_COMPOSITION_DATABASE
            ProductSourceType.AustralianFoodCompositionDatabase ->
                FoodSourceTypeSQLConstants.AUSTRALIAN_FOOD_COMPOSITION_DATABASE
        }

    @TypeConverter
    fun toFoodSourceType(value: Int): ProductSourceType =
        when (value) {
            FoodSourceTypeSQLConstants.USER -> ProductSourceType.User
            FoodSourceTypeSQLConstants.OPEN_FOOD_FACTS -> ProductSourceType.OpenFoodFacts
            FoodSourceTypeSQLConstants.USDA -> ProductSourceType.USDA
            FoodSourceTypeSQLConstants.SWISS_FOOD_COMPOSITION_DATABASE ->
                ProductSourceType.SwissFoodCompositionDatabase
            FoodSourceTypeSQLConstants.AUSTRALIAN_FOOD_COMPOSITION_DATABASE ->
                ProductSourceType.AustralianFoodCompositionDatabase
            else -> error("Unknown food source type value: $value")
        }
}

internal object FoodSourceTypeSQLConstants {
    const val USER = 0
    const val OPEN_FOOD_FACTS = 1
    const val USDA = 2
    const val SWISS_FOOD_COMPOSITION_DATABASE = 3
    const val AUSTRALIAN_FOOD_COMPOSITION_DATABASE = 4
}
