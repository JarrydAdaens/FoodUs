package com.maksimowiczm.foodyou.food.infrastructure.repository

import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.domain.measurement.from
import com.maksimowiczm.foodyou.common.domain.measurement.rawValue
import com.maksimowiczm.foodyou.common.domain.measurement.type
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import com.maksimowiczm.foodyou.food.domain.repository.NutritionalItemMeasurementSuggestionRepository
import com.maksimowiczm.foodyou.food.infrastructure.room.MeasurementSuggestionDao
import com.maksimowiczm.foodyou.food.infrastructure.room.MeasurementSuggestionEntity
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RoomNutritionalItemMeasurementSuggestionRepository(
    private val measurementSuggestionDao: MeasurementSuggestionDao
) : NutritionalItemMeasurementSuggestionRepository {
    override suspend fun insert(foodId: NutritionalItemId, measurement: Measurement) {
        measurementSuggestionDao.insert(measurement.toEntity(foodId))
    }

    override fun observeByFoodId(foodId: NutritionalItemId, limit: Int): Flow<List<Measurement>> =
        measurementSuggestionDao.observeByFoodId(foodId, limit).map { list ->
            list.map(MeasurementSuggestionEntity::toMeasurement)
        }
}

private fun MeasurementSuggestionDao.observeByFoodId(
    foodId: NutritionalItemId,
    limit: Int,
): Flow<List<MeasurementSuggestionEntity>> =
    this.observeByFoodId(
        productId = (foodId as? NutritionalItemId.Product)?.id,
        recipeId = (foodId as? NutritionalItemId.Recipe)?.id,
        limit = limit,
    )

private fun Measurement.toEntity(foodId: NutritionalItemId, now: Instant = Clock.System.now()) =
    MeasurementSuggestionEntity(
        productId = (foodId as? NutritionalItemId.Product)?.id,
        recipeId = (foodId as? NutritionalItemId.Recipe)?.id,
        epochSeconds = now.epochSeconds,
        type = type,
        value = rawValue,
    )

private fun MeasurementSuggestionEntity.toMeasurement(): Measurement =
    Measurement.from(type = type, rawValue = value)
