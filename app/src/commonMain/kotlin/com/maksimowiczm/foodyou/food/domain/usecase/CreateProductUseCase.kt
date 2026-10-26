package com.maksimowiczm.foodyou.food.domain.usecase

import com.maksimowiczm.foodyou.common.domain.database.TransactionProvider
import com.maksimowiczm.foodyou.common.domain.food.ProductSource
import com.maksimowiczm.foodyou.common.domain.food.hasValidValues
import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementResolver
import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import com.maksimowiczm.foodyou.common.log.Logger
import com.maksimowiczm.foodyou.common.log.logAndReturnFailure
import com.maksimowiczm.foodyou.common.result.Ok
import com.maksimowiczm.foodyou.common.result.Result
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemHistory
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import com.maksimowiczm.foodyou.food.domain.repository.NutritionalItemHistoryRepository
import com.maksimowiczm.foodyou.food.domain.repository.ProductRepository

sealed interface CreateProductError {
    data object InvalidValues : CreateProductError

    object NameEmpty : CreateProductError
}

class CreateProductUseCase(
    private val productRepository: ProductRepository,
    private val historyRepository: NutritionalItemHistoryRepository,
    private val transactionProvider: TransactionProvider,
    private val logger: Logger,
) {
    suspend fun create(
        name: String,
        brand: String?,
        barcode: String?,
        note: String?,
        isLiquid: Boolean,
        packageWeight: Double?,
        servingWeight: Double?,
        source: ProductSource,
        nutritionFacts: NutritionFacts,
        history: NutritionalItemHistory.CreationHistory,
    ): Result<NutritionalItemId.Product, CreateProductError> {
        if (name.isBlank()) {
            return logger.logAndReturnFailure(
                tag = TAG,
                throwable = null,
                error = CreateProductError.NameEmpty,
                message = { "Product name cannot be empty." },
            )
        }

        if ((packageWeight != null && !MeasurementResolver.isKnownBasis(packageWeight)) ||
            (servingWeight != null && !MeasurementResolver.isKnownBasis(servingWeight)) ||
            !nutritionFacts.hasValidValues()) {
            return logger.logAndReturnFailure(
                tag = TAG,
                error = CreateProductError.InvalidValues,
                message = { "Product nutrition and reference quantities must be valid finite values." },
            )
        }

        return transactionProvider.withTransaction {
            val productId =
                productRepository.insertProduct(
                    name = name,
                    brand = brand?.ifBlank { null },
                    barcode = barcode?.ifBlank { null },
                    note = note?.ifBlank { null },
                    isLiquid = isLiquid,
                    packageWeight = packageWeight,
                    servingWeight = servingWeight,
                    source = source,
                    nutritionFacts = nutritionFacts,
                )

            historyRepository.insert(productId, history)

            Ok(productId)
        }
    }

    private companion object {
        const val TAG = "CreateProductUseCase"
    }
}
