package com.maksimowiczm.foodyou.food.domain.repository

import com.maksimowiczm.foodyou.common.domain.food.ProductSource
import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import com.maksimowiczm.foodyou.food.domain.entity.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    /** Personal definitions only. Lightweight listing does not resolve nutritional graphs. */
    fun observeLibraryNames(): Flow<List<com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemName>> =
        kotlinx.coroutines.flow.flow { error("Library listing unavailable") }

    fun observeProduct(id: NutritionalItemId.Product): Flow<Product?>

    fun observeProducts(limit: Int, offset: Int): Flow<List<Product>>

    /**
     * @param name Name of the product.
     * @param brand Brand of the product, if available.
     * @param barcode Barcode of the product, if available.
     * @param note Additional note about the product, if available.
     * @param isLiquid Indicates whether the product is liquid (e.g., juice, milk).
     * @param packageWeight Weight of the product package, if available.
     * @param source Source of the product.
     * @param servingWeight Weight of a single serving of the product, if available.
     * @param nutritionFacts Nutrition facts of the product per 100g or 100ml, depending on whether
     *   the product is solid or liquid.
     * @param sourceRecordId The provider's own identifier for this record, if available.
     */
    suspend fun insertProduct(
        name: String,
        brand: String?,
        barcode: String?,
        note: String?,
        isLiquid: Boolean,
        packageWeight: Double?,
        servingWeight: Double?,
        source: ProductSource,
        nutritionFacts: NutritionFacts,
        sourceRecordId: String? = null,
    ): NutritionalItemId.Product

    /**
     * Creates a new product only if a product with the same name, brand, and barcode does not
     * already exist.
     *
     * @return The ID of the newly created product, or null if a duplicate product exists.
     */
    suspend fun insertUniqueProduct(
        name: String,
        brand: String?,
        barcode: String?,
        note: String?,
        isLiquid: Boolean,
        packageWeight: Double?,
        servingWeight: Double?,
        source: ProductSource,
        nutritionFacts: NutritionFacts,
        sourceRecordId: String? = null,
    ): NutritionalItemId.Product?

    suspend fun updateProduct(product: Product)

    /** Complete definitions retain an explicit nonmetric nutrition reference. */
    suspend fun insertProduct(product: Product): NutritionalItemId.Product = error("Complete Product insertion unavailable")

    suspend fun deleteProduct(product: Product)

    /**
     * Deletes every product belonging to a single provider [source]. Intended for fully replacing a
     * downloaded provider's dataset. Passing [FoodSource.Type.User] is rejected so custom foods can
     * never be purged by a provider refresh.
     */
    suspend fun deleteProductsBySource(source: ProductSource.Type)
}
