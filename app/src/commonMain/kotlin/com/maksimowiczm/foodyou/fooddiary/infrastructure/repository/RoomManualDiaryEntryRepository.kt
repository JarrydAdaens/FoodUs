package com.maksimowiczm.foodyou.fooddiary.infrastructure.repository

import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import com.maksimowiczm.foodyou.common.infrastructure.room.toEntityNutrients
import com.maksimowiczm.foodyou.common.infrastructure.room.toNutritionFacts
import com.maksimowiczm.foodyou.fooddiary.domain.entity.ManualDiaryEntry
import com.maksimowiczm.foodyou.fooddiary.domain.entity.ManualDiaryEntryId
import com.maksimowiczm.foodyou.fooddiary.domain.repository.ManualDiaryEntryRepository
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.ManualDiaryEntryDao
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.ManualDiaryEntryEntity
import com.maksimowiczm.foodyou.common.domain.measurement.rawValue
import com.maksimowiczm.foodyou.common.domain.measurement.type
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.capturedPortion
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

internal class RoomManualDiaryEntryRepository(private val dao: ManualDiaryEntryDao) :
    ManualDiaryEntryRepository {
    override fun observe(id: ManualDiaryEntryId): Flow<ManualDiaryEntry?> =
        dao.observe(id.value).map { it?.toModel() }

    override fun observeAll(diarySlotId: Long, date: LocalDate): Flow<List<ManualDiaryEntry>> =
        dao.observeAll(diarySlotId, date.toEpochDays()).map { list ->
            list.map(ManualDiaryEntryEntity::toModel)
        }

    override suspend fun insert(
        name: String,
        diarySlotId: Long,
        date: LocalDate,
        nutritionFacts: NutritionFacts,
        createdAt: LocalDateTime,
        description: String?,
        isPlaceholder: Boolean,
        servingCount: Double?,
        weightGrams: Double?,
        capturedPortion: com.maksimowiczm.foodyou.common.domain.measurement.Measurement?,
    ): ManualDiaryEntryId {
        val entry =
            ManualDiaryEntry(
                id = ManualDiaryEntryId(0),
                diarySlotId = diarySlotId,
                date = date,
                name = name,
                nutritionFacts = nutritionFacts,
                createdAt = createdAt,
                updatedAt = createdAt,
                description = description,
                isPlaceholder = isPlaceholder,
                servingCount = servingCount,
                weightGrams = weightGrams,
                capturedPortion = capturedPortion,
            )
        val id = dao.insert(entry.toEntity())
        return ManualDiaryEntryId(id)
    }

    override suspend fun update(entry: ManualDiaryEntry) = dao.update(entry.toEntity())

    override suspend fun insert(entry: ManualDiaryEntry): ManualDiaryEntryId =
        ManualDiaryEntryId(dao.insert(entry.toEntity().copy(id = 0)))

    override suspend fun delete(id: ManualDiaryEntryId) = dao.delete(id.value)
}

private fun ManualDiaryEntryEntity.toModel(): ManualDiaryEntry =
    ManualDiaryEntry(
        id = ManualDiaryEntryId(id),
        diarySlotId = diarySlotId,
        date = LocalDate.fromEpochDays(dateEpochDay.toInt()),
        name = name,
        nutritionFacts =
            toNutritionFacts(nutrients = nutrients, vitamins = vitamins, minerals = minerals),
        createdAt =
            Instant.fromEpochSeconds(createdEpochSeconds)
                .toLocalDateTime(TimeZone.currentSystemDefault()),
        updatedAt =
            Instant.fromEpochSeconds(updatedEpochSeconds)
                .toLocalDateTime(TimeZone.currentSystemDefault()),
        description = description,
        isPlaceholder = isPlaceholder,
        servingCount = servingCount,
        weightGrams = weightGrams,
        capturedPortion = capturedPortion(capturedPortionType, capturedPortionValue),
        portionDescription = portionDescription,
        nutritionMetadata = nutritionMetadata,
    )

private fun ManualDiaryEntry.toEntity(): ManualDiaryEntryEntity {
    val (nutrients, vitamins, minerals) = toEntityNutrients(nutritionFacts)

    return ManualDiaryEntryEntity(
        id = id.value,
        diarySlotId = diarySlotId,
        dateEpochDay = date.toEpochDays(),
        name = name,
        nutrients = nutrients,
        vitamins = vitamins,
        minerals = minerals,
        createdEpochSeconds = createdAt.toInstant(TimeZone.currentSystemDefault()).epochSeconds,
        updatedEpochSeconds = updatedAt.toInstant(TimeZone.currentSystemDefault()).epochSeconds,
        description = description,
        isPlaceholder = isPlaceholder,
        servingCount = servingCount,
        weightGrams = weightGrams,
        capturedPortionType = capturedPortion?.type,
        capturedPortionValue = capturedPortion?.rawValue,
        portionDescription = portionDescription,
        nutritionMetadata = nutritionMetadata,
    )
}
