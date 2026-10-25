package com.maksimowiczm.foodyou.food.domain.repository

import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import kotlinx.coroutines.flow.Flow

interface NutritionalItemMeasurementSuggestionRepository {
    suspend fun insert(foodId: NutritionalItemId, measurement: Measurement)

    fun observeByFoodId(foodId: NutritionalItemId, limit: Int): Flow<List<Measurement>>
}
