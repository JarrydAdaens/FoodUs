package com.maksimowiczm.foodyou.fooddiary.infrastructure.repository

import androidx.room.RoomDatabase
import com.maksimowiczm.foodyou.common.domain.food.ProductSource
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.domain.measurement.from
import com.maksimowiczm.foodyou.common.domain.measurement.rawValue
import com.maksimowiczm.foodyou.common.domain.measurement.type
import com.maksimowiczm.foodyou.common.extension.combine
import com.maksimowiczm.foodyou.common.infrastructure.room.immediateTransaction
import com.maksimowiczm.foodyou.common.infrastructure.room.toDomain
import com.maksimowiczm.foodyou.common.infrastructure.room.toEntity
import com.maksimowiczm.foodyou.common.infrastructure.room.toEntityNutrients
import com.maksimowiczm.foodyou.common.infrastructure.room.toValidatedEntityNutrients
import com.maksimowiczm.foodyou.common.infrastructure.room.toNutritionFacts
import com.maksimowiczm.foodyou.fooddiary.domain.entity.NutritionalItemSnapshot
import com.maksimowiczm.foodyou.fooddiary.domain.entity.ProductSnapshot
import com.maksimowiczm.foodyou.fooddiary.domain.entity.RecipeSnapshot
import com.maksimowiczm.foodyou.fooddiary.domain.entity.RecipeIngredientSnapshot
import com.maksimowiczm.foodyou.fooddiary.domain.entity.NutritionalItemDiaryEntry
import com.maksimowiczm.foodyou.fooddiary.domain.entity.NutritionalItemDiaryEntryId
import com.maksimowiczm.foodyou.fooddiary.domain.repository.NutritionalItemDiaryEntryRepository
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.DiaryProductEntity
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.DiaryRecipeEntity
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.DiaryRecipeIngredientEntity
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.DiaryReportedRecipeComponentEntity
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.MeasurementDao
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.MeasurementEntity
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

internal class RoomNutritionalItemDiaryEntryRepository(
    private val database: RoomDatabase,
    private val dao: MeasurementDao,
) : NutritionalItemDiaryEntryRepository {

    override fun observe(id: NutritionalItemDiaryEntryId): Flow<NutritionalItemDiaryEntry?> {
        return dao.observeMeasurementById(id.value).flatMapLatest { entity ->
            if (entity == null) return@flatMapLatest flowOf(null)
            observeFood(entity).map { food ->
                val createdAt =
                    Instant.fromEpochSeconds(entity.createdAt)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                val updatedAt =
                    Instant.fromEpochSeconds(entity.updatedAt)
                        .toLocalDateTime(TimeZone.currentSystemDefault())

                NutritionalItemDiaryEntry(
                    id = entity.id.toFoodDiaryEntryId(),
                    diarySlotId = entity.diarySlotId,
                    date = LocalDate.fromEpochDays(entity.epochDay),
                    measurement = Measurement.from(entity.measurement, entity.quantity),
                    food = food,
                    createdAt = createdAt,
                    updatedAt = updatedAt,
                )
            }
        }
    }

    override fun observeAll(diarySlotId: Long, date: LocalDate): Flow<List<NutritionalItemDiaryEntry>> {
        return dao.observeMeasurements(diarySlotId = diarySlotId, epochDay = date.toEpochDays())
            .flatMapLatest { entities ->
                if (entities.isEmpty()) {
                    return@flatMapLatest flowOf(emptyList())
                }

                entities
                    .map { entity ->
                        val foodFlow = observeFood(entity)
                        val createdAt =
                            Instant.fromEpochSeconds(entity.createdAt)
                                .toLocalDateTime(TimeZone.currentSystemDefault())
                        val updatedAt =
                            Instant.fromEpochSeconds(entity.updatedAt)
                                .toLocalDateTime(TimeZone.currentSystemDefault())

                        foodFlow.map {
                            NutritionalItemDiaryEntry(
                                id = entity.id.toFoodDiaryEntryId(),
                                diarySlotId = entity.diarySlotId,
                                date = LocalDate.fromEpochDays(entity.epochDay),
                                measurement = Measurement.from(entity.measurement, entity.quantity),
                                food = it,
                                createdAt = createdAt,
                                updatedAt = updatedAt,
                            )
                        }
                    }
                    .combine()
            }
    }

    override suspend fun insert(
        measurement: Measurement,
        diarySlotId: Long,
        date: LocalDate,
        food: NutritionalItemSnapshot,
        createdAt: LocalDateTime,
    ): NutritionalItemDiaryEntryId =
        database.immediateTransaction {
            val recipeId = run {
                if (food is RecipeSnapshot) {
                    insertRecipe(food)
                } else {
                    null
                }
            }

            val productId = run {
                if (food is ProductSnapshot) {
                    insertProduct(food)
                } else {
                    null
                }
            }

            if (recipeId == null && productId == null) {
                error("Diary entry must have either a product or a recipe")
            }

            val createdAt = createdAt.toInstant(TimeZone.currentSystemDefault()).epochSeconds

            val entity =
                MeasurementEntity(
                    diarySlotId = diarySlotId,
                    epochDay = date.toEpochDays(),
                    productId = productId,
                    recipeId = recipeId,
                    measurement = measurement.type,
                    quantity = measurement.rawValue,
                    createdAt = createdAt,
                    updatedAt = createdAt,
                )

            dao.insertMeasurement(entity).toFoodDiaryEntryId()
        }

    override suspend fun update(entry: NutritionalItemDiaryEntry) =
        database.immediateTransaction {
            val entity = dao.observeMeasurementById(entry.id.value).firstOrNull()

            if (entity == null) {
                error("Measurement with id ${entry.id} not found")
            }

            // Delete the existing product or recipe if it exists
            val productId = entity.productId
            val recipeId = entity.recipeId
            if (productId != null) {
                deleteProduct(productId)
            } else if (recipeId != null) {
                deleteRecipe(recipeId)
            }

            // Insert the new product or recipe
            val newProductId = run {
                when (val food = entry.food) {
                    is ProductSnapshot -> insertProduct(food)
                    else -> null
                }
            }
            val newRecipeId = run {
                when (val food = entry.food) {
                    is RecipeSnapshot -> insertRecipe(food)
                    else -> null
                }
            }

            if (newProductId == null && newRecipeId == null) {
                error("Diary entry must have either a product or a recipe")
            }

            val updatedEntity =
                entity.copy(
                    diarySlotId = entry.diarySlotId,
                    epochDay = entry.date.toEpochDays(),
                    productId = newProductId,
                    recipeId = newRecipeId,
                    measurement = entry.measurement.type,
                    quantity = entry.measurement.rawValue,
                    updatedAt =
                        entry.updatedAt.toInstant(TimeZone.currentSystemDefault()).epochSeconds,
                )

            dao.updateMeasurement(updatedEntity)
        }

    override suspend fun delete(id: NutritionalItemDiaryEntryId) = dao.deleteMeasurement(id.value)

    private fun observeFood(measurementEntity: MeasurementEntity): Flow<NutritionalItemSnapshot> =
        measurementEntity.productId?.let { productId -> observeProduct(productId) }
            ?: measurementEntity.recipeId?.let { recipeId -> observeRecipe(recipeId) }
            ?: error("Measurement entity must have either a product or a recipe")

    private fun observeProduct(productId: Long): Flow<ProductSnapshot> =
        dao.observeDiaryProduct(productId).filterNotNull().map { entity -> entity.toModel() }

    private fun observeRecipe(recipeId: Long): Flow<RecipeSnapshot> =
        dao.observeDiaryRecipe(recipeId)
            .filterNotNull()
            .map { entity ->
                if (entity.calculationVersion == 2) return@map dao.observeReportedComponents(entity.id).map { components ->
                    RecipeSnapshot(name = entity.name, servings = entity.servings, ingredients = emptyList(),
                        isLiquid = entity.isLiquid, note = entity.note, calculationVersion = 2,
                        reportedNutrition = entity.reportedNutrition, reportedComponents = components.map { it.component.captured() })
                }
                val ingredients =
                    dao.observeDiaryRecipeIngredients(entity.id)
                        .map { ingredients ->
                            ingredients
                                .map { ingredient ->
                                    ingredient.ingredientProductId?.let {
                                        observeProduct(it).filterNotNull().map {
                                            Measurement.from(
                                                ingredient.measurement,
                                                ingredient.quantity,
                                            ) to it
                                        }
                                    }
                                        ?: ingredient.ingredientRecipeId?.let { recipeId ->
                                            observeRecipe(recipeId).filterNotNull().map {
                                                Measurement.from(
                                                    ingredient.measurement,
                                                    ingredient.quantity,
                                                ) to it
                                            }
                                        }
                                        ?: error(
                                            "Recipe ingredient must have either a product or a recipe"
                                        )
                                }
                                .combine()
                        }
                        .flatMapLatest { it }
                        .map { list ->
                            list.map { (measurement, food) ->
                                RecipeIngredientSnapshot(food = food, measurement = measurement)
                            }
                        }

                ingredients.map { ingredients ->
                    RecipeSnapshot(
                        name = entity.name,
                        servings = entity.servings,
                        calculationVersion = entity.calculationVersion,
                        ingredients = ingredients,
                        isLiquid = entity.isLiquid,
                        note = entity.note,
                    )
                }
            }
            .flatMapLatest { it }

    private suspend fun insertProduct(diaryFoodProduct: ProductSnapshot): Long {
        val entity = diaryFoodProduct.toEntity()
        return dao.insertDiaryProduct(entity)
    }

    private suspend fun insertRecipe(diaryRecipe: RecipeSnapshot): Long {

        val recipe =
            DiaryRecipeEntity(
                name = diaryRecipe.name,
                servings = diaryRecipe.servings,
                calculationVersion = diaryRecipe.calculationVersion,
                isLiquid = diaryRecipe.isLiquid,
                note = diaryRecipe.note,
                reportedNutrition = diaryRecipe.reportedNutrition,
            )

        val recipeId = dao.insertDiaryRecipe(recipe)

        if (diaryRecipe.isReported) {
            check(diaryRecipe.ingredients.isEmpty()) { "Reported snapshots cannot contain derived ingredients" }
            dao.insertReportedComponents(diaryRecipe.reportedComponents.mapIndexed { position, component ->
                DiaryReportedRecipeComponentEntity(recipeId, position, component.captured())
            })
            return recipeId
        }

        diaryRecipe.ingredients.forEach { ingredient ->
            val ingredientRecipeId = run {
                when (val food = ingredient.food) {
                    is RecipeSnapshot -> insertRecipe(food)
                    else -> null
                }
            }

            val ingredientProductId = run {
                when (val food = ingredient.food) {
                    is ProductSnapshot -> insertProduct(food)
                    else -> null
                }
            }

            val ingredientEntity =
                DiaryRecipeIngredientEntity(
                    recipeId = recipeId,
                    ingredientProductId = ingredientProductId,
                    ingredientRecipeId = ingredientRecipeId,
                    measurement = ingredient.measurement.type,
                    quantity = ingredient.measurement.rawValue,
                )

            dao.insertDiaryRecipeIngredient(ingredientEntity)
        }

        return recipeId
    }

    private suspend fun deleteProduct(productId: Long) = dao.deleteDiaryProduct(productId)

    private suspend fun deleteRecipe(recipeId: Long) = dao.deleteDiaryRecipe(recipeId)
}

private fun Long.toFoodDiaryEntryId(): NutritionalItemDiaryEntryId = NutritionalItemDiaryEntryId(this)

private fun ProductSnapshot.toEntity(): DiaryProductEntity {
    val (nutrients, vitamins, minerals) = toValidatedEntityNutrients(nutritionFacts)

    return DiaryProductEntity(
        name = name,
        nutrients = nutrients,
        vitamins = vitamins,
        minerals = minerals,
        servingWeight = servingWeight,
        packageWeight = totalWeight,
        isLiquid = isLiquid,
        sourceType = source.type.toEntity(),
        sourceUrl = source.url,
        note = note,
        factBasis = factBasis,
        referencePortion = referencePortion,
        nutritionMetadata = nutritionMetadata,
    )
}

private fun DiaryProductEntity.toModel(): ProductSnapshot =
    ProductSnapshot(
        name = name,
        nutritionFacts = toNutritionFacts(nutrients, vitamins, minerals),
        servingWeight = servingWeight,
        totalWeight = packageWeight,
        isLiquid = isLiquid,
        source = ProductSource(type = sourceType.toDomain(), url = sourceUrl),
        note = note,
        factBasis = factBasis,
        referencePortion = referencePortion,
        nutritionMetadata = nutritionMetadata,
    )
