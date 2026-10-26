package com.maksimowiczm.foodyou.food.domain.usecase

import com.maksimowiczm.foodyou.common.domain.database.TransactionProvider
import com.maksimowiczm.foodyou.common.log.Logger
import com.maksimowiczm.foodyou.common.log.logAndReturnFailure
import com.maksimowiczm.foodyou.common.result.Ok
import com.maksimowiczm.foodyou.common.result.Result
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import com.maksimowiczm.foodyou.food.domain.entity.Product
import com.maksimowiczm.foodyou.food.domain.entity.Recipe
import com.maksimowiczm.foodyou.food.domain.repository.ProductRepository
import com.maksimowiczm.foodyou.food.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.first

sealed interface DeleteNutritionalItemError {
    data object FoodNotFound : DeleteNutritionalItemError
}

class DeleteNutritionalItemUseCase(
    private val productRepository: ProductRepository,
    private val recipeRepository: RecipeRepository,
    private val transactionProvider: TransactionProvider,
    private val logger: Logger,
) {
    suspend fun delete(foodId: NutritionalItemId): Result<Unit, DeleteNutritionalItemError> =
        transactionProvider.withTransaction {
            val food =
                when (foodId) {
                    is NutritionalItemId.Product -> productRepository.observeProduct(foodId)
                    is NutritionalItemId.Recipe -> recipeRepository.observeRecipe(foodId)
                }.first()

            if (food == null) {
                return@withTransaction logger.logAndReturnFailure(
                    tag = TAG,
                    error = DeleteNutritionalItemError.FoodNotFound,
                    message = { "Food with ID $foodId not found." },
                )
            }

            when (food) {
                is Product -> productRepository.deleteProduct(food)
                is Recipe -> recipeRepository.deleteRecipe(food)
            }

            Ok(Unit)
        }

    private companion object {
        const val TAG = "DeleteFoodUseCase"
    }
}
