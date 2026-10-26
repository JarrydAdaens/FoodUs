package com.maksimowiczm.foodyou.fooddiary.infrastructure.repository

import androidx.room.RoomDatabase
import com.maksimowiczm.foodyou.common.infrastructure.room.immediateTransaction
import com.maksimowiczm.foodyou.fooddiary.domain.repository.*
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext

/** Raw snapshot copies intentionally bypass live definitions and nutrient normalization. */
internal class RoomDiaryBulkRepository(
    private val database: RoomDatabase,
    private val measurementDao: MeasurementDao,
    private val manualDiaryEntryDao: ManualDiaryEntryDao,
    private val diarySlotDao: DiarySlotDao,
) : DiaryBulkRepository {
    private data class RecipeGraph(
        val row: DiaryRecipeEntity,
        val ingredients: List<IngredientGraph>,
        val components: List<DiaryReportedRecipeComponentEntity>,
    )
    private data class IngredientGraph(val row: DiaryRecipeIngredientEntity, val food: FoodGraph)
    private data class FoodGraph(val product: DiaryProductEntity? = null, val recipe: RecipeGraph? = null)
    private data class Entry(
        val manual: ManualDiaryEntryEntity? = null,
        val measurement: MeasurementEntity? = null,
        val food: FoodGraph? = null,
    ) {
        val order get() = manual?.sortOrder ?: measurement?.sortOrder
        val name get() = manual?.name ?: food?.product?.name ?: food!!.recipe!!.row.name
        val kind get() = if (manual != null) 0 else 1
        val id get() = manual?.id ?: measurement!!.id
    }
    private data class Version(val entries: List<Entry>) : DiaryBulkVersion

    override suspend fun prepare(source: DiaryEndpoint): DiaryBulkPreview = guarded {
        database.immediateTransaction {
            validateSlot(source)
            val entries = read(source)
            DiaryBulkPreview(source, entries.size, Version(entries))
        }
    }

    override suspend fun execute(action: DiaryBulkAction, preview: DiaryBulkPreview, destination: DiaryEndpoint?) = guarded {
        database.immediateTransaction {
            validateSlot(preview.source)
            if (action != DiaryBulkAction.Delete) {
                if (destination == null || preview.source == destination) fail(DiaryBulkError.SameEndpoint)
                validateSlot(destination)
            }
            val source = read(preview.source)
            if (preview.version != Version(source)) fail(DiaryBulkError.SourceChanged)
            if (source.isEmpty()) fail(DiaryBulkError.EmptySource)
            coroutineContext.ensureActive()
            if (action == DiaryBulkAction.Delete) {
                source.forEach { entry ->
                    entry.manual?.let { manualDiaryEntryDao.delete(it.id) }
                    entry.measurement?.let { measurementDao.deleteEntry(it.id) }
                }
                source.mapNotNull { it.food }.forEach { cleanup(it) }
            } else {
                val target = destination!!
                val existing = read(target)
                // Materialize the same deterministic order the diary showed before mutation.
                existing.forEachIndexed { index, entry -> place(entry, target, index.toLong()) }
                source.forEachIndexed { index, entry ->
                    coroutineContext.ensureActive()
                    val order = existing.size.toLong() + index
                    if (action == DiaryBulkAction.Move) place(entry, target, order)
                    else copy(entry, target, order)
                }
            }
            coroutineContext.ensureActive()
        }
    }

    private suspend fun validateSlot(endpoint: DiaryEndpoint) {
        if (!diarySlotDao.exists(endpoint.slotId)) fail(DiaryBulkError.SlotDeleted)
    }

    private suspend fun read(endpoint: DiaryEndpoint): List<Entry> {
        val day = endpoint.date.toEpochDays()
        val manual = manualDiaryEntryDao.entries(endpoint.slotId, day).map { Entry(manual = it) }
        val food = measurementDao.entries(endpoint.slotId, day).map {
            Entry(measurement = it, food = graph(it.productId, it.recipeId, emptySet()))
        }
        return (manual + food).sortedWith(compareBy<Entry> { it.order ?: Long.MAX_VALUE }
            .thenBy { it.name }.thenBy { it.kind }.thenBy { it.id })
    }

    private suspend fun graph(productId: Long?, recipeId: Long?, ancestors: Set<Long>): FoodGraph {
        check((productId == null) != (recipeId == null))
        if (productId != null) return FoodGraph(product = checkNotNull(measurementDao.product(productId)))
        val id = checkNotNull(recipeId)
        check(id !in ancestors && ancestors.size < 100) { "Invalid snapshot graph" }
        val row = checkNotNull(measurementDao.recipe(id))
        val ingredients = measurementDao.ingredients(id).map {
            IngredientGraph(it, graph(it.ingredientProductId, it.ingredientRecipeId, ancestors + id))
        }
        return FoodGraph(recipe = RecipeGraph(row, ingredients, measurementDao.components(id)))
    }

    private suspend fun place(entry: Entry, endpoint: DiaryEndpoint, order: Long) {
        entry.manual?.let { manualDiaryEntryDao.update(it.copy(diarySlotId = endpoint.slotId,
            dateEpochDay = endpoint.date.toEpochDays(), sortOrder = order)) }
        entry.measurement?.let { measurementDao.updateMeasurement(it.copy(diarySlotId = endpoint.slotId,
            epochDay = endpoint.date.toEpochDays(), sortOrder = order)) }
    }

    private suspend fun copy(entry: Entry, endpoint: DiaryEndpoint, order: Long) {
        entry.manual?.let { manualDiaryEntryDao.insert(it.copy(id = 0, diarySlotId = endpoint.slotId,
            dateEpochDay = endpoint.date.toEpochDays(), sortOrder = order)) }
        entry.measurement?.let {
            val (productId, recipeId) = clone(checkNotNull(entry.food))
            measurementDao.insertMeasurement(it.copy(id = 0, diarySlotId = endpoint.slotId,
                epochDay = endpoint.date.toEpochDays(), sortOrder = order, productId = productId, recipeId = recipeId))
        }
    }

    private suspend fun clone(food: FoodGraph): Pair<Long?, Long?> {
        food.product?.let { return measurementDao.insertDiaryProduct(it.copy(id = 0)) to null }
        val recipe = checkNotNull(food.recipe)
        val id = measurementDao.insertDiaryRecipe(recipe.row.copy(id = 0))
        recipe.ingredients.forEach {
            val (product, childRecipe) = clone(it.food)
            measurementDao.insertDiaryRecipeIngredient(it.row.copy(id = 0, recipeId = id,
                ingredientProductId = product, ingredientRecipeId = childRecipe))
        }
        measurementDao.insertReportedComponents(recipe.components.map { it.copy(recipeId = id) })
        return null to id
    }

    /** Clean only this operation's proven unreferenced roots and descendants. */
    private suspend fun cleanup(food: FoodGraph) {
        food.product?.let {
            if (measurementDao.productEntries(it.id) == 0 && measurementDao.productIngredients(it.id) == 0)
                measurementDao.deleteDiaryProduct(it.id)
        }
        food.recipe?.let {
            if (measurementDao.recipeEntries(it.row.id) == 0 && measurementDao.recipeIngredients(it.row.id) == 0) {
                measurementDao.deleteDiaryRecipe(it.row.id)
                it.ingredients.forEach { ingredient -> cleanup(ingredient.food) }
            }
        }
    }

    private fun fail(error: DiaryBulkError): Nothing = throw DiaryBulkException(error)
    private suspend fun <T> guarded(block: suspend () -> T): T = try { block() }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (known: DiaryBulkException) { throw known }
        catch (_: Exception) { fail(DiaryBulkError.StorageFailure) }
}
