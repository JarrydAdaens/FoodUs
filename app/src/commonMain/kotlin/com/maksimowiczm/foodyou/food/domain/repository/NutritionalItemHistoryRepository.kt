package com.maksimowiczm.foodyou.food.domain.repository

import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemHistory
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import kotlinx.coroutines.flow.Flow

interface NutritionalItemHistoryRepository {
    suspend fun insert(foodId: NutritionalItemId, history: NutritionalItemHistory)

    fun observeFoodHistory(foodId: NutritionalItemId): Flow<List<NutritionalItemHistory>>
}
