package com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase.domain

import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts

/**
 * A single AFCD food after parsing and unit normalization, ready to persist as a product.
 *
 * @param publicFoodKey The AFCD "Public Food Key" — the source's own stable record id.
 * @param name The AFCD "Food Name".
 * @param nutritionFacts Per-100 g nutrition in Food You's canonical units.
 */
data class AfcdProduct(
    val publicFoodKey: String,
    val name: String,
    val nutritionFacts: NutritionFacts,
)
