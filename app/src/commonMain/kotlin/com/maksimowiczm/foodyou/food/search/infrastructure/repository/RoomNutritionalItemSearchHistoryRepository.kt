package com.maksimowiczm.foodyou.food.search.infrastructure.repository

import com.maksimowiczm.foodyou.common.domain.search.SearchQuery
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemSearchHistory
import com.maksimowiczm.foodyou.food.domain.repository.NutritionalItemSearchHistoryRepository
import com.maksimowiczm.foodyou.food.search.infrastructure.room.FoodSearchDao
import com.maksimowiczm.foodyou.food.search.infrastructure.room.SearchEntry
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RoomNutritionalItemSearchHistoryRepository(private val foodSearchDao: FoodSearchDao) :
    NutritionalItemSearchHistoryRepository {
    override fun observeHistory(limit: Int): Flow<List<NutritionalItemSearchHistory>> =
        foodSearchDao.observeRecentSearches(limit).map { list -> list.map { it.toModel() } }

    override suspend fun insert(entry: NutritionalItemSearchHistory) {
        foodSearchDao.insertSearchEntry(entry.toEntity())
    }
}

private fun SearchEntry.toModel(): NutritionalItemSearchHistory =
    NutritionalItemSearchHistory(
        timestamp = epochSeconds.let(Instant::fromEpochSeconds),
        query = SearchQuery.Text(query),
    )

private fun NutritionalItemSearchHistory.toEntity(): SearchEntry =
    SearchEntry(epochSeconds = timestamp.epochSeconds, query = query.query)
