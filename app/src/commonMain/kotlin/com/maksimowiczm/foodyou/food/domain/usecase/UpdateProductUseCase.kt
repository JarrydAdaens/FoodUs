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
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import com.maksimowiczm.foodyou.food.domain.repository.ProductRepository
import kotlinx.coroutines.flow.first

sealed interface UpdateProductError {
    data object InvalidValues : UpdateProductError

    data object NameEmpty : UpdateProductError

    data class ProductNotFound(val id: NutritionalItemId.Product) : UpdateProductError
}

class UpdateProductUseCase(
    private val productRepository: ProductRepository,
    private val transactionProvider: TransactionProvider,
    private val logger: Logger,
) {
    suspend fun update(
        id: NutritionalItemId.Product,
        name: String,
        brand: String?,
        nutritionFacts: NutritionFacts,
        barcode: String?,
        packageWeight: Double?,
        servingWeight: Double?,
        note: String?,
        source: ProductSource,
        isLiquid: Boolean,
    ): Result<Unit, UpdateProductError> {
        if (name.isBlank()) {
            return logger.logAndReturnFailure(
                tag = TAG,
                throwable = null,
                error = UpdateProductError.NameEmpty,
                message = { "Product name cannot be empty." },
            )
        }

        if ((packageWeight != null && !MeasurementResolver.isKnownBasis(packageWeight)) ||
            (servingWeight != null && !MeasurementResolver.isKnownBasis(servingWeight)) ||
            !nutritionFacts.hasValidValues()) {
            return logger.logAndReturnFailure(
                tag = TAG,
                error = UpdateProductError.InvalidValues,
                message = { "Product nutrition and reference quantities must be valid finite values." },
            )
        }

        return transactionProvider.withTransaction {
            val product = productRepository.observeProduct(id).first()
            if (product == null) {
                return@withTransaction logger.logAndReturnFailure(
                    tag = TAG,
                    throwable = null,
                    error = UpdateProductError.ProductNotFound(id),
                    message = { "Product with ID $id not found." },
                )
            }

            val updatedProduct =
                product.copy(
                    name = name,
                    brand = brand?.ifBlank { null },
                    barcode = barcode?.ifBlank { null },
                    nutritionFacts = nutritionFacts,
                    packageWeight = packageWeight,
                    servingWeight = servingWeight,
                    note = note?.ifBlank { null },
                    source = source,
                    isLiquid = isLiquid,
                )

            productRepository.updateProduct(updatedProduct)

            Ok(Unit)
        }
    }

    private companion object {
        const val TAG = "UpdateProductUseCase"
    }
}
