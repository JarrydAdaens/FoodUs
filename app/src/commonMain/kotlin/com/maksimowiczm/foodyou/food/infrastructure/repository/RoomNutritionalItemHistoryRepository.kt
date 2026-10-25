package com.maksimowiczm.foodyou.food.infrastructure.repository

import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemHistory
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import com.maksimowiczm.foodyou.food.domain.repository.NutritionalItemHistoryRepository
import com.maksimowiczm.foodyou.food.infrastructure.room.FoodEventDao
import com.maksimowiczm.foodyou.food.infrastructure.room.FoodEventEntity
import com.maksimowiczm.foodyou.food.infrastructure.room.FoodEventType
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RoomNutritionalItemHistoryRepository(private val foodEventDao: FoodEventDao) :
    NutritionalItemHistoryRepository {
    override suspend fun insert(foodId: NutritionalItemId, history: NutritionalItemHistory) {
        foodEventDao.insert(history.toEntity(foodId))
    }

    override fun observeFoodHistory(foodId: NutritionalItemId): Flow<List<NutritionalItemHistory>> =
        when (foodId) {
            is NutritionalItemId.Product -> foodEventDao.observeProductEvents(foodId.id)
            is NutritionalItemId.Recipe -> foodEventDao.observeRecipeEvents(foodId.id)
        }.map { list -> list.map { it.toModel() } }
}

private fun FoodEventEntity.toModel(): NutritionalItemHistory {
    val timestamp = Instant.fromEpochSeconds(epochSeconds)

    return when (type) {
        FoodEventType.Created -> NutritionalItemHistory.Created(timestamp)
        FoodEventType.Downloaded -> NutritionalItemHistory.Downloaded(timestamp, extra)
        FoodEventType.Imported -> NutritionalItemHistory.Imported(timestamp)
        FoodEventType.Edited -> NutritionalItemHistory.Edited(timestamp)
        FoodEventType.ImportedFromFoodYou2 -> NutritionalItemHistory.ImportedFromFoodYou2(timestamp)
    }
}

private fun NutritionalItemHistory.toEntity(foodId: NutritionalItemId): FoodEventEntity {
    val extra =
        when (this) {
            is NutritionalItemHistory.Created -> null
            is NutritionalItemHistory.Downloaded -> url
            is NutritionalItemHistory.Imported -> null
            is NutritionalItemHistory.Edited -> null
            is NutritionalItemHistory.ImportedFromFoodYou2 -> null
        }

    val type =
        when (this) {
            is NutritionalItemHistory.Created -> FoodEventType.Created
            is NutritionalItemHistory.Downloaded -> FoodEventType.Downloaded
            is NutritionalItemHistory.Imported -> FoodEventType.Imported
            is NutritionalItemHistory.Edited -> FoodEventType.Edited
            is NutritionalItemHistory.ImportedFromFoodYou2 -> FoodEventType.ImportedFromFoodYou2
        }

    return FoodEventEntity(
        type = type,
        epochSeconds = timestamp.epochSeconds,
        extra = extra,
        productId = (foodId as? NutritionalItemId.Product)?.id,
        recipeId = (foodId as? NutritionalItemId.Recipe)?.id,
    )
}
