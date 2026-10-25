package com.maksimowiczm.foodyou.food.domain.event

import com.maksimowiczm.foodyou.common.domain.event.IntegrationEventHandler
import com.maksimowiczm.foodyou.food.domain.repository.NutritionalItemMeasurementSuggestionRepository
import com.maksimowiczm.foodyou.fooddiary.domain.event.NutritionalItemDiaryEntryCreatedEvent

internal class NutritionalItemDiaryEntryCreatedEventHandler(
    private val repository: NutritionalItemMeasurementSuggestionRepository
) : IntegrationEventHandler<NutritionalItemDiaryEntryCreatedEvent> {
    override suspend fun handle(event: NutritionalItemDiaryEntryCreatedEvent) {
        repository.insert(foodId = event.foodId, measurement = event.measurement)
    }
}
