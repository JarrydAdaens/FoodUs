package com.maksimowiczm.foodyou.food.search.domain

import com.maksimowiczm.foodyou.common.domain.event.IntegrationEventHandler
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemSearchHistory
import com.maksimowiczm.foodyou.food.domain.repository.NutritionalItemSearchHistoryRepository

class NutritionalItemSearchEventHandler(private val repository: NutritionalItemSearchHistoryRepository) :
    IntegrationEventHandler<NutritionalItemSearchEvent> {
    override suspend fun handle(event: NutritionalItemSearchEvent) {
        repository.insert(event.toSearchHistory())
    }
}

private fun NutritionalItemSearchEvent.toSearchHistory(): NutritionalItemSearchHistory =
    NutritionalItemSearchHistory(timestamp, query)
