package com.maksimowiczm.foodyou.fooddiary.domain.event

import com.maksimowiczm.foodyou.common.domain.event.IntegrationEvent
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import kotlin.time.Instant

data class NutritionalItemDiaryEntryCreatedEvent(
    val foodId: NutritionalItemId,
    val timestamp: Instant,
    val measurement: Measurement,
) : IntegrationEvent
