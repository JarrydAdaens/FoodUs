package com.maksimowiczm.foodyou.food.infrastructure.repository

import com.maksimowiczm.foodyou.common.domain.food.ProductSource
import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import com.maksimowiczm.foodyou.common.infrastructure.room.toDomain
import com.maksimowiczm.foodyou.common.infrastructure.room.toEntity
import com.maksimowiczm.foodyou.common.infrastructure.room.toEntityNutrients
import com.maksimowiczm.foodyou.common.infrastructure.room.toValidatedEntityNutrients
import com.maksimowiczm.foodyou.common.infrastructure.room.toNutritionFacts
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import com.maksimowiczm.foodyou.food.domain.entity.Product
import com.maksimowiczm.foodyou.food.domain.repository.ProductRepository
import com.maksimowiczm.foodyou.food.infrastructure.room.ProductDao
import com.maksimowiczm.foodyou.food.infrastructure.room.ProductEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RoomProductRepository(private val productDao: ProductDao) : ProductRepository {
    override fun observeProducts(limit: Int, offset: Int): Flow<List<Product>> =
        productDao.observeProducts(limit, offset).map { list -> list.map { it.toModel() } }

    override fun observeProduct(id: NutritionalItemId.Product): Flow<Product?> =
        productDao.observeProduct(id.id).map { it?.toModel() }

    override suspend fun deleteProduct(product: Product) {
        val entity = product.toEntity()
        productDao.deleteProduct(entity)
    }

    override suspend fun insertProduct(
        name: String,
        brand: String?,
        barcode: String?,
        note: String?,
        isLiquid: Boolean,
        packageWeight: Double?,
        servingWeight: Double?,
        source: ProductSource,
        nutritionFacts: NutritionFacts,
        sourceRecordId: String?,
    ): NutritionalItemId.Product {
        val product =
            Product(
                id = NutritionalItemId.Product(0), // Temporary ID, will be replaced upon insertion
                name = name,
                brand = brand,
                barcode = barcode,
                note = note,
                isLiquid = isLiquid,
                packageWeight = packageWeight,
                servingWeight = servingWeight,
                source = source,
                nutritionFacts = nutritionFacts,
            )
        val entity = product.toEntity().copy(sourceRecordId = sourceRecordId)
        val id = productDao.insertProduct(entity)
        return NutritionalItemId.Product(id)
    }

    override suspend fun insertUniqueProduct(
        name: String,
        brand: String?,
        barcode: String?,
        note: String?,
        isLiquid: Boolean,
        packageWeight: Double?,
        servingWeight: Double?,
        source: ProductSource,
        nutritionFacts: NutritionFacts,
        sourceRecordId: String?,
    ): NutritionalItemId.Product? {
        val product =
            Product(
                id = NutritionalItemId.Product(0), // Temporary ID, will be replaced upon insertion
                name = name,
                brand = brand,
                barcode = barcode,
                note = note,
                isLiquid = isLiquid,
                packageWeight = packageWeight,
                servingWeight = servingWeight,
                source = source,
                nutritionFacts = nutritionFacts,
            )
        return productDao
            .insertUniqueProduct(product.toEntity().copy(sourceRecordId = sourceRecordId))
            ?.let(NutritionalItemId::Product)
    }

    override suspend fun updateProduct(product: Product) {
        productDao.updateProduct(product.toEntity(validateNutrition=product.factBasis==com.maksimowiczm.foodyou.common.domain.food.ProductFactBasis.REFERENCE_SERVING || product.nutritionMetadata?.reference!=null))
    }

    override suspend fun insertProduct(product: Product): NutritionalItemId.Product =
        NutritionalItemId.Product(productDao.insertProduct(product.toEntity(validateNutrition = true).copy(id = 0)))

    override suspend fun deleteProductsBySource(source: ProductSource.Type) {
        require(source != ProductSource.Type.User) {
            "Refusing to bulk-delete user products; delete-by-source is only for downloaded providers"
        }
        productDao.deleteProductsBySource(source.toEntity())
    }
}

private fun ProductEntity.toModel(): Product =
    Product(
        id = NutritionalItemId.Product(this.id),
        name = this.name,
        brand = this.brand,
        barcode = this.barcode,
        note = this.note,
        isLiquid = this.isLiquid,
        packageWeight = this.packageWeight,
        servingWeight = this.servingWeight,
        source = ProductSource(type = this.sourceType.toDomain(), url = this.sourceUrl),
        nutritionFacts = this.toNutritionFacts(),
        factBasis = this.factBasis,
        referencePortion = this.referencePortion,
        nutritionMetadata = this.nutritionMetadata,
    )

private fun ProductEntity.toNutritionFacts(): NutritionFacts =
    toNutritionFacts(nutrients, vitamins, minerals)

private fun Product.toEntity(validateNutrition: Boolean = false): ProductEntity {
    val (nutrients, vitamins, minerals) = if (validateNutrition) toValidatedEntityNutrients(nutritionFacts) else toEntityNutrients(nutritionFacts)

    return ProductEntity(
        id = id.id,
        name = name,
        brand = brand,
        barcode = barcode,
        nutrients = nutrients,
        vitamins = vitamins,
        minerals = minerals,
        packageWeight = packageWeight,
        servingWeight = servingWeight,
        note = note,
        sourceType = source.type.toEntity(),
        sourceUrl = source.url,
        isLiquid = isLiquid,
        factBasis = factBasis,
        referencePortion = referencePortion,
        nutritionMetadata = nutritionMetadata,
    )
}
