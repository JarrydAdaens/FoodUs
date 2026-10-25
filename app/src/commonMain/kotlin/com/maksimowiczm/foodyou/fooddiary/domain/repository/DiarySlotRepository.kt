package com.maksimowiczm.foodyou.fooddiary.domain.repository

import com.maksimowiczm.foodyou.fooddiary.domain.entity.DiarySlot
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalTime

interface DiarySlotRepository {
    fun observeDiarySlot(diarySlotId: Long): Flow<DiarySlot?>

    fun observeDiarySlots(): Flow<List<DiarySlot>>

    suspend fun insertDiarySlotWithLastRank(name: String, from: LocalTime, to: LocalTime)

    suspend fun deleteDiarySlot(diarySlotId: Long)

    suspend fun updateDiarySlot(id: Long, name: String, from: LocalTime, to: LocalTime)

    suspend fun reorderDiarySlots(order: List<Long>)
}
