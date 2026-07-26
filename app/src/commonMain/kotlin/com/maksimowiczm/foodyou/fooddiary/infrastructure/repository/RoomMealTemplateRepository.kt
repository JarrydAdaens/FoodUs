package com.maksimowiczm.foodyou.fooddiary.infrastructure.repository

import androidx.room.RoomDatabase
import com.maksimowiczm.foodyou.common.infrastructure.room.immediateTransaction
import com.maksimowiczm.foodyou.common.infrastructure.room.toEntityNutrients
import com.maksimowiczm.foodyou.common.infrastructure.room.toNutritionFacts
import com.maksimowiczm.foodyou.fooddiary.domain.entity.MealTemplate
import com.maksimowiczm.foodyou.fooddiary.domain.entity.MealTemplateId
import com.maksimowiczm.foodyou.fooddiary.domain.entity.MealTemplateItem
import com.maksimowiczm.foodyou.fooddiary.domain.repository.MealTemplateRepository
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.MealTemplateDao
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.MealTemplateEntity
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.MealTemplateItemEntity
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.MealTemplateWithItems
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal class RoomMealTemplateRepository(
    private val database: RoomDatabase,
    private val dao: MealTemplateDao,
) : MealTemplateRepository {

    override fun observeAll(): Flow<List<MealTemplate>> =
        dao.observeAll().map { list -> list.map(MealTemplateWithItems::toModel) }

    override suspend fun getTemplate(id: MealTemplateId): MealTemplate? =
        dao.getById(id.value)?.toModel()

    override suspend fun save(name: String, items: List<MealTemplateItem>): MealTemplateId =
        database.immediateTransaction {
            val templateId =
                dao.insertTemplate(
                    MealTemplateEntity(
                        name = name,
                        createdEpochSeconds = Clock.System.now().epochSeconds,
                    )
                )

            dao.insertItems(
                items.mapIndexed { index, item -> item.toEntity(templateId, index) }
            )

            MealTemplateId(templateId)
        }

    override suspend fun delete(id: MealTemplateId) = dao.delete(id.value)
}

private fun MealTemplateWithItems.toModel(): MealTemplate =
    MealTemplate(
        id = MealTemplateId(template.id),
        name = template.name,
        createdAt =
            Instant.fromEpochSeconds(template.createdEpochSeconds)
                .toLocalDateTime(TimeZone.currentSystemDefault()),
        items =
            items.sortedBy { it.position }.map { entity ->
                MealTemplateItem(
                    name = entity.name,
                    nutritionFacts =
                        toNutritionFacts(
                            nutrients = entity.nutrients,
                            vitamins = entity.vitamins,
                            minerals = entity.minerals,
                        ),
                )
            },
    )

private fun MealTemplateItem.toEntity(templateId: Long, position: Int): MealTemplateItemEntity {
    val (nutrients, vitamins, minerals) = toEntityNutrients(nutritionFacts)
    return MealTemplateItemEntity(
        templateId = templateId,
        name = name,
        position = position,
        nutrients = nutrients,
        vitamins = vitamins,
        minerals = minerals,
    )
}
