package com.maksimowiczm.foodyou.food.domain.entity

import kotlin.time.Instant

sealed interface NutritionalItemHistory {
    val timestamp: Instant

    sealed interface CreationHistory : NutritionalItemHistory

    class Created(override val timestamp: Instant) : CreationHistory

    class Downloaded(override val timestamp: Instant, val url: String?) : CreationHistory

    class Imported(override val timestamp: Instant) : CreationHistory

    class Edited(override val timestamp: Instant) : NutritionalItemHistory

    /**
     * Represents an event when a food item is imported from FoodYou2. This event is used to track
     * the migration of data from the old app.
     */
    class ImportedFromFoodYou2(override val timestamp: Instant) : NutritionalItemHistory
}
