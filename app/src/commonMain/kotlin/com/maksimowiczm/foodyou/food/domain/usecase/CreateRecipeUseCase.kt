package com.maksimowiczm.foodyou.food.domain.usecase

import com.maksimowiczm.foodyou.common.domain.database.TransactionProvider
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.log.Logger
import com.maksimowiczm.foodyou.common.log.logAndReturnFailure
import com.maksimowiczm.foodyou.common.result.Ok
import com.maksimowiczm.foodyou.common.result.Result
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemHistory
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import com.maksimowiczm.foodyou.food.domain.entity.Recipe
import com.maksimowiczm.foodyou.food.domain.entity.RecipeIngredient
import com.maksimowiczm.foodyou.food.domain.repository.NutritionalItemHistoryRepository
import com.maksimowiczm.foodyou.food.domain.repository.ProductRepository
import com.maksimowiczm.foodyou.food.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.CancellationException
import com.maksimowiczm.foodyou.common.result.Err
import com.maksimowiczm.foodyou.common.domain.food.hasValidValues

sealed interface CreateRecipeError {
    data object EmptyName : CreateRecipeError

    data object NonPositiveServings : CreateRecipeError

    data object EmptyIngredients : CreateRecipeError

    data class IngredientNotFound(val foodId: NutritionalItemId) : CreateRecipeError

    data object InvalidIngredient : CreateRecipeError

    data object SaveFailed : CreateRecipeError

    data object CircularIngredient : CreateRecipeError
}

class CreateRecipeUseCase(
    private val recipeRepository: RecipeRepository,
    private val productRepository: ProductRepository,
    private val historyRepository: NutritionalItemHistoryRepository,
    private val transactionProvider: TransactionProvider,
    private val logger: Logger,
) {
    suspend fun create(
        name: String,
        servings: Int,
        note: String?,
        isLiquid: Boolean,
        ingredients: List<Pair<NutritionalItemId, Measurement>>,
        history: NutritionalItemHistory.CreationHistory,
    ): Result<NutritionalItemId.Recipe, CreateRecipeError> = try {
        createValidated(name, servings, note, isLiquid, ingredients, history)
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        Err(CreateRecipeError.SaveFailed)
    }

    private suspend fun createValidated(
        name: String,
        servings: Int,
        note: String?,
        isLiquid: Boolean,
        ingredients: List<Pair<NutritionalItemId, Measurement>>,
        history: NutritionalItemHistory.CreationHistory,
    ): Result<NutritionalItemId.Recipe, CreateRecipeError> {
        if (name.isBlank()) {
            return logger.logAndReturnFailure(
                tag = TAG,
                error = CreateRecipeError.EmptyName,
                message = { "Recipe name cannot be empty." },
            )
        }

        if (servings <= 0) {
            return logger.logAndReturnFailure(
                tag = TAG,
                error = CreateRecipeError.NonPositiveServings,
                message = { "Recipe servings must be a positive integer." },
            )
        }

        if (ingredients.isEmpty()) {
            return logger.logAndReturnFailure(
                tag = TAG,
                error = CreateRecipeError.EmptyIngredients,
                message = { "Recipe must have at least one ingredient." },
            )
        }

        return transactionProvider.withTransaction {
            if (recipeRepository.hasRecipeCycle(null, ingredients.map { it.first })) {
                return@withTransaction Err(CreateRecipeError.CircularIngredient)
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
                            error = CreateRecipeError.IngredientNotFound(foodId),
                            message = { "Ingredient with ID $foodId not found." },
                        )
                    }

                    if (food.nutritionFor(measurement) == null) {
                        return@withTransaction Err(CreateRecipeError.InvalidIngredient)
                    }
                    RecipeIngredient(food, measurement)
                }

            val recipe =
                Recipe(
                    id = NutritionalItemId.Recipe(0L),
                    name = name,
                    servings = servings,
                    note = note?.takeIf { it.isNotBlank() },
                    isLiquid = isLiquid,
                    ingredients = ingredients,
                )

            if (!recipe.isResolvable || !recipe.totalNutrition.hasValidValues()) {
                return@withTransaction Err(CreateRecipeError.InvalidIngredient)
            }

            val recipeId =
                recipeRepository.insertRecipe(
                    name = name,
                    servings = servings,
                    note = note?.takeIf { it.isNotBlank() },
                    isLiquid = isLiquid,
                    ingredients = ingredients,
                )

            historyRepository.insert(recipeId, history)

            Ok(recipeId)
        }
    }

    private companion object {
        const val TAG = "CreateRecipeUseCase"
    }
}
