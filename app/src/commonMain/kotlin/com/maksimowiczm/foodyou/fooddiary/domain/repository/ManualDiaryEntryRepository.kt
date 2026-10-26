package com.maksimowiczm.foodyou.fooddiary.domain.repository

import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import com.maksimowiczm.foodyou.fooddiary.domain.entity.ManualDiaryEntry
import com.maksimowiczm.foodyou.fooddiary.domain.entity.ManualDiaryEntryId
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

interface ManualDiaryEntryRepository {
    fun observe(id: ManualDiaryEntryId): Flow<ManualDiaryEntry?>

    fun observeAll(diarySlotId: Long, date: LocalDate): Flow<List<ManualDiaryEntry>>

    suspend fun insert(
        name: String,
        diarySlotId: Long,
        date: LocalDate,
        nutritionFacts: NutritionFacts,
        createdAt: LocalDateTime,
        description: String? = null,
        isPlaceholder: Boolean = false,
        servingCount: Double? = null,
        weightGrams: Double? = null,
        capturedPortion: com.maksimowiczm.foodyou.common.domain.measurement.Measurement? = null,
    ): ManualDiaryEntryId

    suspend fun update(entry: ManualDiaryEntry)

    /** Captures optional import provenance without losing it at the persistence boundary. */
    suspend fun insert(entry: ManualDiaryEntry): ManualDiaryEntryId {
        check(entry.portionDescription == null && entry.nutritionMetadata == null) { "Captured provenance insertion unavailable" }
        return insert(entry.name, entry.diarySlotId, entry.date, entry.nutritionFacts, entry.createdAt,
            entry.description, entry.isPlaceholder, entry.servingCount, entry.weightGrams, entry.capturedPortion)
    }

    suspend fun delete(id: ManualDiaryEntryId)
}
