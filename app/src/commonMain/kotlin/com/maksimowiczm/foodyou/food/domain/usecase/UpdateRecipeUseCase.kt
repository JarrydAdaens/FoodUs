package com.maksimowiczm.foodyou.food.domain.usecase

import com.maksimowiczm.foodyou.common.domain.database.TransactionProvider
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.log.Logger
import com.maksimowiczm.foodyou.common.log.logAndReturnFailure
import com.maksimowiczm.foodyou.common.result.Ok
import com.maksimowiczm.foodyou.common.result.Result
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import com.maksimowiczm.foodyou.food.domain.entity.RecipeIngredient
import com.maksimowiczm.foodyou.food.domain.repository.ProductRepository
import com.maksimowiczm.foodyou.food.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.CancellationException
import com.maksimowiczm.foodyou.common.result.Err
import com.maksimowiczm.foodyou.common.domain.food.hasValidValues

sealed interface UpdateRecipeError {
    data object EmptyName : UpdateRecipeError

    data object NonPositiveServings : UpdateRecipeError

    data object EmptyIngredients : UpdateRecipeError

    data class RecipeNotFound(val id: NutritionalItemId.Recipe) : UpdateRecipeError

    data class IngredientNotFound(val foodId: NutritionalItemId) : UpdateRecipeError

    data object InvalidIngredient : UpdateRecipeError

    data object SaveFailed : UpdateRecipeError

    data object CircularIngredient : UpdateRecipeError
}

class UpdateRecipeUseCase(
    private val recipeRepository: RecipeRepository,
    private val productRepository: ProductRepository,
    private val transactionProvider: TransactionProvider,
    private val logger: Logger,
) {
    suspend fun update(
        id: NutritionalItemId.Recipe,
        name: String,
        servings: Int,
        note: String?,
        isLiquid: Boolean,
        ingredients: List<Pair<NutritionalItemId, Measurement>>,
    ): Result<Unit, UpdateRecipeError> = try {
        updateValidated(id, name, servings, note, isLiquid, ingredients)
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        Err(UpdateRecipeError.SaveFailed)
    }

    private suspend fun updateValidated(
        id: NutritionalItemId.Recipe,
        name: String,
        servings: Int,
        note: String?,
        isLiquid: Boolean,
        ingredients: List<Pair<NutritionalItemId, Measurement>>,
    ): Result<Unit, UpdateRecipeError> {
        if (name.isBlank()) {
            return logger.logAndReturnFailure(
                tag = TAG,
                throwable = null,
                error = UpdateRecipeError.EmptyName,
                message = { "Recipe name cannot be empty." },
            )
        }

        if (servings <= 0) {
            return logger.logAndReturnFailure(
                tag = TAG,
                throwable = null,
                error = UpdateRecipeError.NonPositiveServings,
                message = { "Recipe servings must be a positive integer." },
            )
        }

        if (ingredients.isEmpty()) {
            return logger.logAndReturnFailure(
                tag = TAG,
                throwable = null,
                error = UpdateRecipeError.EmptyIngredients,
                message = { "Recipe must have at least one ingredient." },
            )
        }

        return transactionProvider.withTransaction {
            val recipe = recipeRepository.observeRecipe(id).firstOrNull()

            if (recipe == null) {
                return@withTransaction logger.logAndReturnFailure(
                        tag = TAG,
                        throwable = null,
                        error = UpdateRecipeError.RecipeNotFound(id),
                        message = { "Recipe with ID $id not found." },
                )
            }

            // The linked editor cannot change a reported definition's mode or discard its rows.
            if (recipe.isReported) return@withTransaction Err(UpdateRecipeError.SaveFailed)

            if (recipeRepository.hasRecipeCycle(id, ingredients.map { it.first })) {
                return@withTransaction Err(UpdateRecipeError.CircularIngredient)
            }
            val ingredients =
                ingredients.map { (foodId, measurement) ->
                    val food =
                        when (foodId) {
                            is NutritionalItemId.Product ->
                                productRepository.observeProduct(foodId).firstOrNull()

                            is NutritionalItemId.Recipe -> recipeRepository.observeRecipe(foodId).firstOrNull()
                        }

                    if (food == null) {
                        return@withTransaction logger.logAndReturnFailure(
                            tag = TAG,
                            throwable = null,
                            error = UpdateRecipeError.IngredientNotFound(foodId),
                            message = { "Ingredient with ID $foodId not found." },
                        )
                    }

                    if (food.nutritionFor(measurement) == null) {
                        return@withTransaction Err(UpdateRecipeError.InvalidIngredient)
                    }
                    RecipeIngredient(food, measurement)
                }

            val updatedRecipe =
                recipe.copy(
                    name = name,
                    servings = servings,
                    note = note,
                    isLiquid = isLiquid,
                    ingredients = ingredients,
                )

            if (!updatedRecipe.isResolvable || !updatedRecipe.totalNutrition.hasValidValues()) {
                return@withTransaction Err(UpdateRecipeError.InvalidIngredient)
            }

            recipeRepository.updateRecipe(updatedRecipe)
            Ok(Unit)
        }
    }

    private companion object {
        const val TAG = "UpdateRecipeUseCase"
    }
}
