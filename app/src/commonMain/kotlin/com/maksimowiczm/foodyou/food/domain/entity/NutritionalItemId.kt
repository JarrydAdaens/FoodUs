package com.maksimowiczm.foodyou.food.domain.entity

sealed interface NutritionalItemId {
    data class Product(val id: Long) : NutritionalItemId

    data class Recipe(val id: Long) : NutritionalItemId

    companion object {
        /** Maps the established separate Product/Recipe storage and navigation references. */
        fun fromReferences(productId: Long?, recipeId: Long?): NutritionalItemId {
            require((productId != null) xor (recipeId != null)) {
                "Exactly one Product or Recipe reference is required"
            }
            return if (productId != null) Product(productId) else Recipe(requireNotNull(recipeId))
        }
    }
}
