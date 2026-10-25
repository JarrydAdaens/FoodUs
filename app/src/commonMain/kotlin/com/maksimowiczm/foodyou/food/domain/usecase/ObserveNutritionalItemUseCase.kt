package com.maksimowiczm.foodyou.food.domain.usecase

import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItem
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import com.maksimowiczm.foodyou.food.domain.repository.ProductRepository
import com.maksimowiczm.foodyou.food.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow

class ObserveNutritionalItemUseCase(
    private val productRepository: ProductRepository,
    private val recipeRepository: RecipeRepository,
) {
    fun observe(foodId: NutritionalItemId): Flow<NutritionalItem?> =
        when (foodId) {
            is NutritionalItemId.Product -> productRepository.observeProduct(foodId)
            is NutritionalItemId.Recipe -> recipeRepository.observeRecipe(foodId)
        }
}
