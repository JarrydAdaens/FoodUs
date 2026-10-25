package com.maksimowiczm.foodyou.food.domain.repository

import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemSearchHistory
import kotlinx.coroutines.flow.Flow

interface NutritionalItemSearchHistoryRepository {
    fun observeHistory(limit: Int): Flow<List<NutritionalItemSearchHistory>>

    suspend fun insert(entry: NutritionalItemSearchHistory)
}
