package com.maksimowiczm.foodyou.fooddiary.domain.repository

import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.fooddiary.domain.entity.NutritionalItemSnapshot
import com.maksimowiczm.foodyou.fooddiary.domain.entity.NutritionalItemDiaryEntry
import com.maksimowiczm.foodyou.fooddiary.domain.entity.NutritionalItemDiaryEntryId
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

interface NutritionalItemDiaryEntryRepository {
    fun observe(id: NutritionalItemDiaryEntryId): Flow<NutritionalItemDiaryEntry?>

    fun observeAll(diarySlotId: Long, date: LocalDate): Flow<List<NutritionalItemDiaryEntry>>

    suspend fun insert(
        measurement: Measurement,
        diarySlotId: Long,
        date: LocalDate,
        food: NutritionalItemSnapshot,
        createdAt: LocalDateTime,
    ): NutritionalItemDiaryEntryId

    suspend fun update(entry: NutritionalItemDiaryEntry)

    suspend fun delete(id: NutritionalItemDiaryEntryId)
}
