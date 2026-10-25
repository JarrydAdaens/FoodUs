package com.maksimowiczm.foodyou.fooddiary.infrastructure.repository

import androidx.room.RoomDatabase
import com.maksimowiczm.foodyou.common.infrastructure.room.immediateTransaction
import com.maksimowiczm.foodyou.common.infrastructure.room.toEntityNutrients
import com.maksimowiczm.foodyou.common.infrastructure.room.toNutritionFacts
import com.maksimowiczm.foodyou.common.domain.measurement.rawValue
import com.maksimowiczm.foodyou.common.domain.measurement.type
import com.maksimowiczm.foodyou.fooddiary.domain.entity.*
import com.maksimowiczm.foodyou.fooddiary.domain.repository.MealRepository
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.*
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal class RoomMealRepository(
    private val database: RoomDatabase,
    private val dao: MealDao,
) : MealRepository {
    override fun observeAll(): Flow<List<Meal>> =
        dao.observeAll().map { list -> list.map(MealWithItems::toModel) }

    override suspend fun getMeal(id: MealId): Meal? = dao.getById(id.value)?.toModel()

    override suspend fun save(name: String, items: List<MealItem>): MealId =
        database.immediateTransaction {
            check(validateMeal(name, items) == null) { "Invalid Meal" }
            val rows = items.mapIndexed { index, item -> item.copy(id = null).toEntity(0, index) }
            val id = dao.insertMeal(MealEntity(name = name.trim(), createdEpochSeconds = Clock.System.now().epochSeconds))
            dao.insertItems(rows.map { it.copy(mealId = id) })
            MealId(id)
        }

    override suspend fun update(id: MealId, name: String, items: List<MealItem>): Boolean =
        database.immediateTransaction {
            if (validateMeal(name, items) != null) return@immediateTransaction false
            val current = dao.getById(id.value) ?: return@immediateTransaction false
            val retained = items.mapNotNull { it.id?.value }
            val owned = current.items.map { it.id }.toSet()
            if (retained.size != retained.toSet().size || retained.any { it !in owned }) {
                return@immediateTransaction false
            }
            val rows = items.mapIndexed { position, item -> item.toEntity(id.value, position) }
            fun composition(items: List<MealItem>) = items.groupingBy { it.copy(name = "", id = null, metadata = null) }.eachCount()
            val changedComposition = composition(current.toModel().items) != composition(items)
            dao.updateMeal(current.meal.copy(name = name.trim(), reportedNutrition = current.meal.reportedNutrition?.let {
                if (changedComposition) it.copy(needsReview = true) else it
            }))
            dao.updateItems(rows.filter { it.id != 0L })
            dao.insertItems(rows.filter { it.id == 0L })
            val removed = owned - retained.toSet()
            if (removed.isNotEmpty()) dao.deleteItems(removed.toList())
            true
        }

    override suspend fun save(meal: Meal): MealId = database.immediateTransaction {
        check(validateMeal(meal.name, meal.items) == null) { "Invalid Meal" }
        val id = dao.insertMeal(MealEntity(name = meal.name.trim(), createdEpochSeconds = Clock.System.now().epochSeconds,
            reportedNutrition = meal.reportedNutrition, metadata = meal.metadata))
        dao.insertItems(meal.items.mapIndexed { position, item -> item.copy(id = null).toEntity(id, position) })
        MealId(id)
    }

    override suspend fun delete(id: MealId) = dao.delete(id.value)
}

private fun MealWithItems.toModel(): Meal = Meal(
    id = MealId(meal.id),
    name = meal.name,
    createdAt = Instant.fromEpochSeconds(meal.createdEpochSeconds).toLocalDateTime(TimeZone.currentSystemDefault()),
    items = items.sortedBy { it.position }.map { entity ->
        MealItem(
            id = MealItemId(entity.id),
            name = entity.name,
            nutritionFacts = toNutritionFacts(entity.nutrients, entity.vitamins, entity.minerals),
            capturedPortion = capturedPortion(entity.capturedPortionType, entity.capturedPortionValue),
            servingCount = entity.servingCount,
            weightGrams = entity.weightGrams,
            portionDescription = entity.portionDescription,
            metadata = entity.metadata,
        )
    },
    reportedNutrition = meal.reportedNutrition,
    metadata = meal.metadata,
)

private fun MealItem.toEntity(mealId: Long, position: Int): MealItemEntity {
    val (nutrients, vitamins, minerals) = toEntityNutrients(nutritionFacts.captureMealNutrition())
    check(toNutritionFacts(nutrients, vitamins, minerals).validMealNutrition()) { "Meal nutrition cannot be stored finitely" }
    return MealItemEntity(
        id = id?.value ?: 0,
        mealId = mealId,
        name = name.trim(),
        position = position,
        nutrients = nutrients,
        vitamins = vitamins,
        minerals = minerals,
        capturedPortionType = capturedPortion?.type,
        capturedPortionValue = capturedPortion?.rawValue,
        servingCount = servingCount,
        weightGrams = weightGrams,
        portionDescription = portionDescription,
        metadata = metadata,
    )
}
